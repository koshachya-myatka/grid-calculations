package ru.paramonova.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.paramonova.clients.MyGridClient;
import ru.paramonova.dto.ResultRequest;
import ru.paramonova.grpc.Result;
import ru.paramonova.services.DistributorService;
import ru.paramonova.services.WorkerService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DistributorController {
    private final WorkerService workerService;
    private final DistributorService distributorService;
    private final MyGridClient gridClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/workers")
    public ResponseEntity<Integer> registerWorker(@RequestBody String workerAddress) {
        int workerId = workerService.registerWorker(workerAddress);
//        distributorService.trySendSubtask();
        gridClient.workerAvailable.release();
        return ResponseEntity.ok(workerId);
    }

    @PostMapping("/results")
    public void receiveResults(@RequestBody ResultRequest request) {
        try {
            System.out.println("Получены результаты подзадачи " + request.getSubtaskId() + " задачи " + request.getTaskId());
            List<Result> results = objectMapper.readValue(request.getJsonResult(),
                    new TypeReference<List<Result>>() {
                    });
            distributorService.addResults(request.getTaskId(), results);
            gridClient.addResults(request.getTaskId(), results);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка обработки результатов для подзадачи " + request.getSubtaskId() + " задачи " + request.getTaskId());
        }
        workerService.releaseWorker(request.getWorkerId());
        gridClient.workerAvailable.release();
//        distributorService.trySendSubtask();
    }
}
