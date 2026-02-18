package ru.paramonova.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.paramonova.dto.PipeDto;
import ru.paramonova.models.Distributor;
import ru.paramonova.models.Pipe;
import ru.paramonova.models.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tasks")
public class DistributorController {

    // curl -X POST -F "file=@D:\Study\Grid Calculations\test2.txt" http://localhost:8080/tasks/add
    // curl -X POST -F "file=@C:\Study\grid-calculations\test2.txt" http://localhost:8080/tasks/add
    // curl -X GET http://localhost:8080/tasks/0/next-subtask
    // curl -X GET http://localhost:8080/tasks/0/info

    private final Distributor distributor;

    @PostMapping(value = "/add", consumes = "multipart/form-data")
    public ResponseEntity<?> addTask(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Файл не может быть пустым");
            }
            distributor.addTask(file);
            return ResponseEntity.ok(distributor.getLastTaskId());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Ошибка при обработке файла: " + e.getMessage());
        }
    }

    @GetMapping("/{taskId}/next-subtask")
    public ResponseEntity<?> getNextTaskCombination(@PathVariable("taskId") int taskId) {
        Optional<Task> optionalTask = distributor.getTask(taskId);
        if (optionalTask.isEmpty()) {
            return ResponseEntity.badRequest().body("Задача с таким id не найдена");
        }
        Task task = optionalTask.get();
        int subtaskId = distributor.getSubtasksIndexes().get(taskId);
        distributor.nextSubtaskId(taskId);
        List<Pipe> combination = task.getCombinations().get(subtaskId);
        List<PipeDto> pipeDtos = combination.stream()
                .map(pipe -> new PipeDto(pipe.getX(), pipe.getY(), pipe.isColor(), pipe.getPosition()))
                .toList();
        Map<String, Object> response = new HashMap<>();
        response.put("taskId", taskId);
        response.put("subtaskId", subtaskId);
        response.put("totalCombinations", task.getCombinations().size());
        response.put("combination", pipeDtos);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{taskId}/info")
    public ResponseEntity<String> getTaskInfo(@PathVariable("taskId") int taskId) {
        Optional<Task> optionalTask = distributor.getTask(taskId);
        if (optionalTask.isEmpty()) {
            return ResponseEntity.badRequest().body("Задача с таким id не найдена");
        }
        Task task = optionalTask.get();
        return ResponseEntity.ok(String.format("Ширина: %d,\nВысота: %d,\nКоличество кругов: %d,\nКруги: %s",
                task.getFieldWidth(),
                task.getFieldLength(),
                task.getCircles().size(),
                task.getCircles()));
    }
}
