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

    @PostMapping("/workers")
    public ResponseEntity<Integer> registerWorker(@RequestBody String workerAddress) {
        int workerId = workerService.registerWorker(workerAddress);
        gridClient.addFreeWorker();
        return ResponseEntity.ok(workerId);
    }

    @PostMapping("/results")
    public void receiveResults(@RequestBody ResultRequest request) {
        try {
            System.out.println("Получены результаты подзадачи " + request.getSubtaskId() + " задачи " + request.getTaskId());
            gridClient.addResults(request.getTaskId(), request.getSubtaskId(), request.getJsonResult());
        } catch (Exception e) {
            throw new RuntimeException("Ошибка обработки результатов для подзадачи " + request.getSubtaskId() + " задачи " + request.getTaskId(), e);
        }
        workerService.releaseWorker(request.getWorkerId());
        gridClient.addFreeWorker();
    }
}
