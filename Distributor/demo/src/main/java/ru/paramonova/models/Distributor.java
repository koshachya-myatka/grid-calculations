package ru.paramonova.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@NoArgsConstructor
@Getter
@ToString
public class Distributor {
    private List<Task> tasks = new ArrayList<>();
    private int taskId = 0;

    public void addTask(String fileName) {
        Task task = new Task(fileName, taskId++);
        tasks.add(task);
    }

    public Task getTask(int taskId) {
        return tasks.stream()
                .filter(task -> task.getId() == taskId)
                .findFirst()
                .orElse(null);
    }

    public int getLastTaskId() {
        return tasks.getLast().getId();
    }
}
