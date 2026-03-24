package ru.paramonova.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.jsonpath.JsonPath;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.paramonova.annotations.Calculator;
import ru.paramonova.annotations.Main;
import ru.paramonova.annotations.Param;
import ru.paramonova.dto.ResultRequest;
import ru.paramonova.dto.SolveRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Service
public class WorkerService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Setter
    private int workerId;
    // все id задач, что он когда-либо решал
    private final List<Integer> tasksIds = new ArrayList<>();
    // id задачи - данные задачи
    private final Map<Integer, String> tasksData = new HashMap<>();
    private final Map<Integer, Object> calculatorInstances = new HashMap<>();
    private final Map<Integer, Method> mainMethods = new HashMap<>();

    public void solveSubtask(SolveRequest request) {
        try {
            if (request.getJsonTaskData() != null) {
                setTaskData(request.getTaskId(), request.getJsonTaskData(), request.getJarCalculator());
            }
            invokeMain(request);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось выполнить подзадачу", e);
        }
    }

    public void setTaskData(int taskId, String taskData, byte[] jarCalculator) throws Exception {
        tasksIds.add(taskId);
        tasksData.put(taskId, taskData);
        Class<?> calculatorClass = findCalculatorClass(jarCalculator);
        calculatorInstances.put(taskId, calculatorClass.getDeclaredConstructor().newInstance());
        mainMethods.put(taskId, findMainMethod(calculatorClass));
        System.out.println("Воркер " + workerId + " получил данные задачи " + taskId + "\n");
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
            throw new RuntimeException("Произошла какая-то проблема при поиске класса", e);
        }
        throw new RuntimeException("Класс с аннотацией @Calculator не найден в jar\n");
    }

    private Method findMainMethod(Class<?> calculatorClass) {
        Method[] methods = calculatorClass.getDeclaredMethods();
        for (Method method : methods) {
            for (Annotation annotation : method.getAnnotations()) {
                if (annotation.annotationType() == Main.class) {
                    method.setAccessible(true);
                    return method;
                }
            }
        }
        throw new RuntimeException("Метод с аннотацией @Main не найден в классе " + calculatorClass.getName() + "\n");
    }

    public void invokeMain(SolveRequest request) {
        new Thread(() -> {
            int taskId = request.getTaskId();
            String taskData = tasksData.get(taskId);
            Method mainMethod = mainMethods.get(taskId);
            Object calculatorInstance = calculatorInstances.get(taskId);
            if (taskData == null || mainMethod == null || calculatorInstance == null) {
                throw new RuntimeException("Не были найдены данные для задачи " + taskId + "\n");
            }
            System.out.println("Запускаем метод @Main\n");
            try {
                // соединяем json-нки в кучу
                ObjectNode commonJson = objectMapper.createObjectNode();
                JsonNode taskNode = objectMapper.readTree(taskData);
                JsonNode batchNode = objectMapper.readTree(request.getJsonSubtaskData());
                commonJson.setAll((ObjectNode) taskNode);
                commonJson.setAll((ObjectNode) batchNode);
                String fullJson = objectMapper.writeValueAsString(commonJson);
                // запускаем метод
                Parameter[] parameters = mainMethod.getParameters();
                Object[] args = new Object[parameters.length];
                for (int i = 0; i < parameters.length; i++) {
                    Parameter parameter = parameters[i];
                    Param annotation = parameter.getAnnotation(Param.class);
                    if (annotation == null) {
                        throw new RuntimeException("@Param отсутствует у параметра\n");
                    }
                    String name = annotation.value();
                    Class<?> type = parameter.getType();
                    List<Object> value = JsonPath.read(fullJson, "$.." + name);
                    if (value == null || value.isEmpty()) {
                        throw new RuntimeException("В JSON нет параметра " + name + "\n");
                    }
                    args[i] = objectMapper.convertValue(value.getFirst(), type);
                }
                Object result;
                result = mainMethod.invoke(calculatorInstance, args);
                sendResult(request, result);
            } catch (Exception e) {
                throw new RuntimeException("Не удалось запустить метод Main\n" + e);
            }
        }).start();
    }

    private void sendResult(SolveRequest request, Object result) {
        if (result == null) {
            throw new RuntimeException("@Calculator вернул null результат\n");
        }
        int taskId = request.getTaskId();
        System.out.println("Отправляем результат для подзадачи " + request.getSubtaskId() + " задачи " + taskId + "\n");
        ResultRequest resultRequest = null;
        try {
            resultRequest = ResultRequest.builder()
                    .workerId(workerId)
                    .taskId(taskId)
                    .subtaskId(request.getSubtaskId())
                    .jsonResult(objectMapper.writeValueAsString(result))
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Не удалось преобразовать Json в строку", e);
        }
        RestTemplate rest = new RestTemplate();
        rest.postForEntity(
                request.getDistributorAddress() + "/results",
                resultRequest,
                Void.class
        );
    }
}