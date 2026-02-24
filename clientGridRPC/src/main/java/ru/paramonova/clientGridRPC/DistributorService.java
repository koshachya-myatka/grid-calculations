package ru.paramonova.clientGridRPC;

import ru.paramonova.grpc.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DistributorService {
    private final Map<Integer, Task> tasks = new ConcurrentHashMap<>();
    private final Map<Integer, byte[]> jars = new ConcurrentHashMap<>();
    private final Map<Integer, String> calcClassNames = new ConcurrentHashMap<>();
    private final Map<Integer, List<Boolean>> results = new ConcurrentHashMap<>();

    public void registerTask(Task task, byte[] jarBytes, String calculatorClassName) {
        int taskId = task.getTaskId();
        tasks.put(taskId, task);
        jars.put(taskId, jarBytes);
        calcClassNames.put(taskId, calculatorClassName);
        results.put(taskId, new ArrayList<>());
    }

    public boolean isTaskRegistered(int taskId) {
        return tasks.containsKey(taskId);
    }

    public void executeBatch(Batch batch) {

    }
}
