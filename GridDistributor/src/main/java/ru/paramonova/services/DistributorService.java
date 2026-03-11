package ru.paramonova.services;

import com.google.protobuf.util.JsonFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.paramonova.dto.SolveRequest;
import ru.paramonova.clients.WorkerClient;
import ru.paramonova.models.WorkerInfo;
import ru.paramonova.grpc.*;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class DistributorService {
    private final WorkerClient workerClient;
    private final WorkerService workerService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<Integer, Task> tasks = new ConcurrentHashMap<>();
    private final Map<Integer, byte[]> jars = new ConcurrentHashMap<>();
    private final Map<Integer, List<Result>> results = new ConcurrentHashMap<>();

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

    public void trySendSubtask(Batch batch) {
        System.out.println("ПЫТАЮСЬ");
//        while (true) {
            Optional<WorkerInfo> workerOptional = workerService.findFreeWorker();
            if (workerOptional.isEmpty()) {
                System.out.println("ПУСТО");
                return;
            }
//            Batch batch = batchQueue.poll();
            if (batch == null) {
                System.out.println("ПУСТОЙ БАТЧ");
                workerService.releaseWorker(workerOptional.get().getWorkerId());
                return;
            }

            WorkerInfo worker = workerOptional.get();
            try {
                Task task = tasks.get(batch.getTaskId());
                boolean needTaskData = worker.getTaskId() == null || !worker.getTaskId().equals(batch.getTaskId());
                SolveRequest request = SolveRequest.builder()
                        .taskId(task.getTaskId())
                        .subtaskId(batch.getBatchId())
                        .jsonSubtaskData(JsonFormat.printer().print(batch))
                        .distributorAddress("http://localhost:8081")
                        .build();
                if (needTaskData) {
                    request.setJarCalculator(jars.get(task.getTaskId()));
                    request.setJsonTaskData(JsonFormat.printer().print(task));
                    worker.setTaskId(task.getTaskId());
                }
                System.out.println("Я ТУТ ПОЧТИ КИНУЛ ЗАПРОС");
                workerClient.sendSubtask(worker, request);
            } catch (Exception e) {
                System.out.println("ОШИБКА КАКАЯ-ТО УПАЛА");
                e.printStackTrace();
                workerService.releaseWorker(worker.getWorkerId());
            }
//        }
    }

    public void addResults(int taskId, List<Result> newResults) {
        List<Result> current = results.get(taskId);
        if (current == null) {
            throw new RuntimeException("Результаты для задачи " + taskId + " не были найдены");
        }
        current.addAll(newResults);
        System.out.println("Распределитель получил " + newResults.size() + " результатов задачи " + taskId);
    }
}
