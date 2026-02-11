package ru.paramonova.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.paramonova.models.Distributor;
import ru.paramonova.models.Pipe;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DistributorController {

    private final Distributor distributor;
    private int index = 0;

    @PostMapping("/add/{path:.+}")
    public ResponseEntity<Integer> addTask(@PathVariable("path") String path) {
        distributor.addTask(path);
        return ResponseEntity.ok(distributor.getLastTaskId());
    }

    @GetMapping("/task/{taskId}/info")
    public ResponseEntity<String> getFieldInfo(@PathVariable("taskId") int taskId) {
        return ResponseEntity.ok(String.format("Ширина: %.0f,\nВысота: %.0f,\nКоличество кругов: %d,\nКруги: %s",
                distributor.getTask(taskId).getFieldWidth(),
                distributor.getTask(taskId).getFieldLength(),
                distributor.getTask(taskId).getCircles().size(),
                distributor.getTask(taskId).getCircles()));
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<String> getCombination(@PathVariable("taskId") int taskId) {
        return ResponseEntity.ok(distributor.getTask(taskId)
                .getCombinations().get(index++).toString());
    }
}
