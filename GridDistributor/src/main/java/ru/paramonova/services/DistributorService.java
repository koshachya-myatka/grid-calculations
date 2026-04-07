package ru.paramonova.services;

import com.google.protobuf.util.JsonFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import ru.paramonova.dto.SolveRequest;
import ru.paramonova.dto.WorkerInfo;
import ru.paramonova.grpc.*;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DistributorService {
    @Value("${server.port}")
    private String serverPort;
    private final WorkerService workerService;
    // ключи - id таски
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, byte[]> jars = new HashMap<>();
    private final Map<Integer, List<Result>> results = new HashMap<>();

    public void registerTask(Task task, byte[] jarBytes) {
        int taskId = task.getTaskId();
        tasks.put(taskId, task);
        jars.put(taskId, jarBytes);
        results.put(taskId, new ArrayList<>());
    }

    public Task getTask(int taskId) {
        return tasks.get(taskId);
    }

    public boolean trySendSubtask(Batch batch) {
        if (batch == null) {
            return false;
        }
        Optional<WorkerInfo> workerOptional = workerService.findFreeWorker();
        if (workerOptional.isEmpty()) {
            return false;
        }
        WorkerInfo worker = workerOptional.get();
        try {
            Task task = tasks.get(batch.getTaskId());
            boolean needTaskData = worker.getTasksIds().isEmpty() || !worker.getTasksIds().contains(task.getTaskId());
            JsonFormat.Printer printer = JsonFormat.printer().includingDefaultValueFields();
            String subtaskJson = printer.print(batch);
            String resultSubtaskJson = String.format("{\"batch\":%s}", subtaskJson);
            SolveRequest request = SolveRequest.builder()
                    .taskId(task.getTaskId())
                    .subtaskId(batch.getBatchId())
                    .jsonSubtaskData(resultSubtaskJson)
                    .resultUrl("http://localhost:" + serverPort + "/results")
                    .build();
            if (needTaskData) {
                String taskJson = printer.print(task);
                String resultTaskJson = String.format("{\"task\":%s}", taskJson);
                request.setJarCalculator(jars.get(task.getTaskId()));
                request.setJsonTaskData(resultTaskJson);
                worker.addTaskId(task.getTaskId());
            }
            if (!sendSubtask(worker, request)) {
                return false;
            }
        } catch (Exception e) {
            workerService.releaseWorker(worker.getWorkerId());
            throw new RuntimeException("Ошибка при отправке батча на воркер", e);
        }
        return true;
    }

    private boolean sendSubtask(WorkerInfo worker, SolveRequest request) {
        RestTemplate restTemplate = new RestTemplate();
        String url = worker.getAddress() + "/solveSubtask";
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                workerService.removeWorker(worker.getWorkerId());
                return false;
            }
        } catch (ResourceAccessException e) {
            workerService.removeWorker(worker.getWorkerId());
            return false;
        }
        System.out.println("Отправил подзадачу на воркер " + worker.getWorkerId() + "\n");
        return true;
    }

    public void addResults(int taskId, List<Result> newResults) {
        List<Result> current = results.get(taskId);
        if (current == null) {
            throw new RuntimeException("Результаты для задачи " + taskId + " не были найдены\n");
        }
        current.addAll(newResults);
        System.out.println("Распределитель сохранил " + newResults.size() + " результатов задачи " + taskId + "\n");
    }
}