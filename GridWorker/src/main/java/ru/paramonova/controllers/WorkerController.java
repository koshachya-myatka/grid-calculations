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

    @PostMapping("/solveSubtask")
    public ResponseEntity<Void> solveSubtask(@RequestBody SolveRequest request) {
        if (!workerService.tryLock()) {
            //todo добавить сообщение этому адресу, что освободился
            return ResponseEntity.status(403).build();
        }
        workerService.solveSubtask(request);
        return ResponseEntity.ok().build();
    }
}

