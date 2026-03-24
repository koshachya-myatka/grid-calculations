package ru.paramonova.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.paramonova.grpc.MyGrpcClient;
import ru.paramonova.dto.ResultRequest;
import ru.paramonova.services.WorkerService;

@RestController
@RequiredArgsConstructor
public class DistributorController {
    private final MyGrpcClient gridClient;
    private final WorkerService workerService;

    @PostMapping("/tasks")
    public ResponseEntity<String> addTask(@RequestBody String jsonString) {
        new Thread(() -> {
            try {
                int taskId = gridClient.addTask(jsonString);
                gridClient.registerTask(taskId);
                gridClient.getTaskInfo(taskId);
                gridClient.streamBatches(taskId);
            } catch (Exception e) {
                throw new RuntimeException("Не удалось добавить задачу\n", e);
            }
        }).start();
        return ResponseEntity.ok("Задача принята");
    }

    @PostMapping("/workers")
    public ResponseEntity<Integer> registerWorker(@RequestBody String workerAddress) {
        int workerId = workerService.registerWorker(workerAddress);
        gridClient.addFreeWorker();
        return ResponseEntity.ok(workerId);
    }

    @PostMapping("/results")
    public void addResults(@RequestBody ResultRequest request) {
        try {
            System.out.println("Получены результаты подзадачи " + request.getSubtaskId() + " задачи " + request.getTaskId() + "\n");
            gridClient.addResults(request.getTaskId(), request.getSubtaskId(), request.getJsonResult());
        } catch (Exception e) {
            throw new RuntimeException("Ошибка обработки результатов для подзадачи " + request.getSubtaskId() + " задачи " + request.getTaskId(), e);
        }
        workerService.releaseWorker(request.getWorkerId());
        gridClient.addFreeWorker();
    }
}
