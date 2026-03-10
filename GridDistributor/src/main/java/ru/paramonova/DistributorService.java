package ru.paramonova;

import ru.paramonova.grpc.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DistributorService {
    private final Map<Integer, Task> tasks = new ConcurrentHashMap<>();
    private final Map<Integer, byte[]> jars = new ConcurrentHashMap<>();
    private final Map<Integer, List<Boolean>> results = new ConcurrentHashMap<>();

    public void registerTask(Task task, byte[] jarBytes) {
        int taskId = task.getTaskId();
        tasks.put(taskId, task);
        jars.put(taskId, jarBytes);
        results.put(taskId, new ArrayList<>());
    }

    public Task getTask(int taskId) {
        return tasks.get(taskId);
    }

    public byte[] getJar(int taskId) {
        return jars.get(taskId);
    }

}
