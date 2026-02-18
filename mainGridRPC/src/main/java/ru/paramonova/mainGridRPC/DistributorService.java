package ru.paramonova.mainGridRPC;

import ru.paramonova.grpc.Circle;
import ru.paramonova.grpc.Pipe;
import ru.paramonova.grpc.PipeList;
import ru.paramonova.grpc.Task;
import ru.paramonova.mainGridRPC.models.GridTask;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class DistributorService {
    private final ConcurrentHashMap<Integer, Task> tasks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Integer> subtaskIndexes = new ConcurrentHashMap<>();
    private final AtomicInteger nextTaskId = new AtomicInteger(0);

    public Task addTask(String fileName, byte[] fileData) {
        int taskId = nextTaskId.getAndIncrement();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(fileData)))) {
            GridTask gridTask = new GridTask(reader, taskId);
            List<Circle> grpcCircles = gridTask.getCircles().stream()
                    .map(circle -> Circle.newBuilder()
                            .setX(circle.getX())
                            .setY(circle.getY())
                            .setColor(circle.getColor())
                            .build())
                    .toList();
            List<PipeList> grpcCombinations = gridTask.getCombinations().stream()
                    .map(pipeList -> {
                        PipeList.Builder pipeListBuilder = PipeList.newBuilder();
                        pipeList.stream()
                                .map(pipe -> Pipe.newBuilder()
                                        .setX(pipe.getX())
                                        .setY(pipe.getY())
                                        .setColor(pipe.getColor())
                                        .setPosition(pipe.getPosition())
                                        .build())
                                .forEach(pipeListBuilder::addPipes);
                        return pipeListBuilder.build();
                    })
                    .collect(Collectors.toList());
            Task task = Task.newBuilder()
                    .setId(gridTask.getId())
                    .setFieldWidth(gridTask.getFieldWidth())
                    .setFieldLength(gridTask.getFieldLength())
                    .addAllCircles(grpcCircles)
                    .addAllCombinations(grpcCombinations)
                    .setTotalCombinations(gridTask.getCombinations().size())
                    .build();
            tasks.put(taskId, task);
            subtaskIndexes.put(taskId, 0);
            return task;
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при чтении файла: " + fileName);
        }
    }

    public Task getTask(int taskId) {
        return tasks.get(taskId);
    }

    public int getNextSubtaskIndex(int taskId) {
        return subtaskIndexes.merge(taskId, 1, Integer::sum) - 1;
    }

    public int getCurrentSubtaskIndex(int taskId) {
        return subtaskIndexes.getOrDefault(taskId, 0);
    }
}
