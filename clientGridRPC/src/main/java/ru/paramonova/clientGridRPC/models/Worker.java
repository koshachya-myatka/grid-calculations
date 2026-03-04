package ru.paramonova.clientGridRPC.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.paramonova.grpc.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Getter
@ToString
public class Worker {
    private final int workerId;
    @Setter
    private boolean isBusy = false;
    private Task task;
    private Class<?> calculatorClass;
    private Object calculatorInstance;
    private Method mainMethod;

    public Worker(int workerId) {
        this.workerId = workerId;
    }

    public void setTaskData(Task task, byte[] jarBytes, String className) throws Exception {
        this.task = task;
        Path tempJar = Files.createTempFile("worker-" + workerId + "-", ".jar");
        Files.write(tempJar, jarBytes);
        tempJar.toFile().deleteOnExit();
        URLClassLoader classLoader = new URLClassLoader(
                new URL[]{tempJar.toUri().toURL()},
                Thread.currentThread().getContextClassLoader()
        );
        this.calculatorClass = classLoader.loadClass(className);
        this.calculatorInstance = calculatorClass.getDeclaredConstructor().newInstance();
        this.mainMethod = findMainMethod();
        System.out.println("Воркер " + workerId + " получил данные задачи " + task.getTaskId());
    }

    private Method findMainMethod() {
        Method[] methods = calculatorClass.getDeclaredMethods();
        for (Method method : methods) {
            for (Annotation annotation : method.getAnnotations()) {
                if (annotation.annotationType().getName().equals("Main") ||
                        annotation.annotationType().getName().endsWith(".Main")) {
                    method.setAccessible(true);
                    return method;
                }
            }
        }
        throw new RuntimeException("Метод с аннотацией @Main не найден в классе " + calculatorClass.getName());
    }

    public List<Result> executeBatch(Batch batch) {
        try {
            if (batch.getTaskId() != task.getTaskId()) {
                throw new IllegalArgumentException("Переданный батч не относится к переданной задаче");
            }
            Object result = mainMethod.invoke(calculatorInstance, task, batch);
            return (List<Result>) result;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при выполнении батча " + batch.getBatchId() + " воркером " + workerId + ":\n" + e.getMessage());
        }
    }
}