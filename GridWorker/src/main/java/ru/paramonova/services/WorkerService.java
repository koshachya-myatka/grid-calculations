package ru.paramonova.services;

import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.paramonova.annotations.Calculator;
import ru.paramonova.annotations.Main;
import ru.paramonova.annotations.Param;
import ru.paramonova.dto.ResultRequest;
import ru.paramonova.dto.SolveRequest;
import tools.jackson.databind.ObjectMapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Service
public class WorkerService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Setter
    private int workerId;
    private final AtomicBoolean busy = new AtomicBoolean(false);
    //TODO можно замутить список задач, что он уже решал когда-то
    private Integer taskId;
    private String taskData;
    private Class<?> calculatorClass;
    private Object calculatorInstance;
    private Method mainMethod;

    public boolean tryLock() {
        return busy.compareAndSet(false, true);
    }

    public void unlock() {
        busy.set(false);
    }

    public void solveSubtask(SolveRequest request) {
        try {
            if (request.getJsonTaskData() != null) {
                setTaskData(request.getTaskId(), request.getJsonTaskData(), request.getJarCalculator());
            }
            invokeMain(request);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось выполнить подзадачу");
        }
    }

    public void setTaskData(int taskId, String taskData, byte[] jarCalculator) throws Exception {
        this.taskId = taskId;
        this.taskData = taskData;
        this.calculatorClass = findCalculatorClass(jarCalculator);
        this.calculatorInstance = calculatorClass.getDeclaredConstructor().newInstance();
        this.mainMethod = findMainMethod();
        System.out.println("Воркер " + workerId + " получил данные задачи " + taskId);
    }

    private Class<?> findCalculatorClass(byte[] jarBytes) {
        try {
            Path tempJar = Files.createTempFile("worker-" + workerId + "-", ".jar");
            Files.write(tempJar, jarBytes);
            tempJar.toFile().deleteOnExit();
            URLClassLoader classLoader = new URLClassLoader(
                    new URL[]{tempJar.toUri().toURL()},
                    Thread.currentThread().getContextClassLoader()
            );
            JarFile jarFile = new JarFile(tempJar.toFile());
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.getName().endsWith(".class")) {
                    continue;
                }
                String name = entry.getName();
                String className = name
                        .replace('/', '.')
                        .replace(".class", "");
                Class<?> cl = classLoader.loadClass(className);
                if (cl.isAnnotationPresent(Calculator.class)) {
                    return cl;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Произошла какая-то проблема при поиске класса");
        }
        throw new RuntimeException("Класс с аннотацией @Calculator не найден в jar");
    }

    private Method findMainMethod() {
        Method[] methods = calculatorClass.getDeclaredMethods();
        for (Method method : methods) {
            for (Annotation annotation : method.getAnnotations()) {
                if (annotation.annotationType() == Main.class) {
                    method.setAccessible(true);
                    return method;
                }
            }
        }
        throw new RuntimeException("Метод с аннотацией @Main не найден в классе " + calculatorClass.getName());
    }

    public void invokeMain(SolveRequest request) {
        //TODO вот тут сделать адекватный парсинг для передачи параметров в мейн-метод
        Parameter[] parameters = mainMethod.getParameters();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Param annotation = parameter.getAnnotation(Param.class);
            String name = annotation.value();
            Class<?> type = parameter.getType();
            String json;
            if (name.equals("task")) {
                json = taskData;
            } else {
                json = request.getJsonSubtaskData();
            }
            try {
                Message.Builder builder = (Message.Builder) type.getMethod("newBuilder").invoke(null);
                JsonFormat.parser().merge(json, builder);
                args[i] = builder.build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Object result = null;
        try {
            result = mainMethod.invoke(calculatorInstance, args);
            sendResult(request, result);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось запустить метод Main" + e);
        }
    }

    private void sendResult(SolveRequest request, Object result) {
        ResultRequest resultRequest = ResultRequest.builder()
                .workerId(workerId)
                .taskId(taskId)
                .subtaskId(request.getSubtaskId())
                .jsonResult(objectMapper.writeValueAsString(result))
                .build();
        RestTemplate rest = new RestTemplate();
        rest.postForEntity(
                request.getDistributorAddress() + "/results",
                resultRequest,
                Void.class
        );
    }
}