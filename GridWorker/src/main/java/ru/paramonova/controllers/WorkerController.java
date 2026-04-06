package ru.paramonova.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.paramonova.dto.SolveRequest;
import ru.paramonova.services.WorkerService;

@RestController
@RequiredArgsConstructor
public class WorkerController {
    private final WorkerService workerService;

    @PostMapping("/register")
    public ResponseEntity<String> registerWorkerInDistributor(@RequestBody String registerUrl) {
        String distributorAddress = workerService.getDistributorAddressFromUrlString(registerUrl);
        Integer workerId = workerService.getWorkerIdForDistributor(registerUrl);
        if (workerId == null) {
            return ResponseEntity.badRequest().body("Не удалось зарегистрировать воркер");
        }
        workerService.setWorkerIdForDistributor(distributorAddress, workerId);
        workerService.setDistributorRegisterUrl(distributorAddress, registerUrl);
        return ResponseEntity.ok("Воркер зарегистрирован с id " + workerId
                + " для распределителя " + distributorAddress);
    }

    @PostMapping("/solveSubtask")
    public ResponseEntity<Void> solveSubtask(@RequestBody SolveRequest request) {
        if (!workerService.tryLock()) {
            workerService.addDistributorInInformWhenNotBusyList(request.getResultUrl());
            return ResponseEntity.status(403).build();
        }
        workerService.solveSubtask(request);
        return ResponseEntity.ok().build();
    }
}

