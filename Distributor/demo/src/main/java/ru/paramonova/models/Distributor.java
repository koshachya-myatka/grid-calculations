package ru.paramonova.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@NoArgsConstructor
@Getter
@ToString
public class Distributor {
    private List<Task> tasks = new ArrayList<>();
    private Map<Integer, Integer> subtasksIndexes = new HashMap<>();

    public void addTask(MultipartFile file) {
        int id = getLastTaskId() + 1;
        Task task = new Task(file, id);
        tasks.add(task);
        subtasksIndexes.put(id, 0);
    }

    public int getLastTaskId() {
        return tasks.isEmpty() ? -1 : tasks.size() - 1;
    }

    public Optional<Task> getTask(int taskId) {
        return tasks.stream()
                .filter(task -> task.getId() == taskId)
                .findFirst();
    }

    public void nextSubtaskIndex(int taskId) {
        Optional<Task> optionalTask = tasks.stream()
                .filter(task -> task.getId() == taskId)
                .findFirst();
        if (optionalTask.isPresent()) {
            int value = subtasksIndexes.get(taskId) + 1;
            subtasksIndexes.put(taskId, value);
            if (value == optionalTask.get().getCombinations().size()) {
                subtasksIndexes.put(taskId, 0);
            }
        }
    }
}
