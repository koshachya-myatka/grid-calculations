package ru.paramonova.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<?> solveSubtask(@RequestBody SolveRequest request) {
        if (!workerService.tryLock()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        workerService.solveSubtask(request);
        return ResponseEntity.ok().build();
    }
}

