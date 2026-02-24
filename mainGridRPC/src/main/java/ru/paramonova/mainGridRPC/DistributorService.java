package ru.paramonova.mainGridRPC;

import ru.paramonova.grpc.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DistributorService {
    // id таски - сама таска
    private final ConcurrentHashMap<Integer, Task> tasks = new ConcurrentHashMap<>();
    // id таски - текущий стартовый номер комбинации белых кругов
    private final ConcurrentHashMap<Integer, Integer> batchStartsWhite = new ConcurrentHashMap<>();
    // id таски - текущий стартовый номер комбинации черных кругов
    private final ConcurrentHashMap<Integer, Integer> batchStartsBlack = new ConcurrentHashMap<>();
    // id таски - список результатов решений для нее
    private final ConcurrentHashMap<Integer, List<Boolean>> results = new ConcurrentHashMap<>();
    private final AtomicInteger nextTaskId = new AtomicInteger(0);
    private final AtomicInteger nextBatchId = new AtomicInteger(0);

    public Task addTask(String fileName, byte[] fileData) {
        int taskId = nextTaskId.getAndIncrement();
        Task task = createTask(taskId, fileName, fileData);
        tasks.put(taskId, task);
        batchStartsWhite.put(taskId, 0);
        batchStartsBlack.put(taskId, 0);
        return task;
    }

    private Task createTask(int taskId, String fileName, byte[] fileData) {
        int fieldWidth = 0;
        int fieldLength = 0;
        List<Circle> whiteCircles = new ArrayList<>();
        List<Circle> blackCircles = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(fileData)))) {
            String line;
            if ((line = reader.readLine()) != null) {
                String[] fieldSize = line.split(",");
                if (fieldSize.length != 2) {
                    throw new IllegalArgumentException("Неверный формат строки с размерами поля");
                }
                fieldWidth = Integer.parseInt(fieldSize[0].trim());
                fieldLength = Integer.parseInt(fieldSize[1].trim());
            } else {
                throw new IllegalArgumentException("Файл пустой");
            }
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] values = line.split(",");
                if (values.length != 3) {
                    throw new IllegalArgumentException("Неверный формат строки с описанием круга " + line);
                }
                int x = Integer.parseInt(values[0].trim());
                int y = Integer.parseInt(values[1].trim());
                boolean color = "1".equals(values[2].trim());
                if (x < 0 || x >= fieldWidth || y < 0 || y >= fieldLength) {
                    throw new IllegalArgumentException(
                            String.format("Круг с координатами (%d, %d) выходит за пределы поля", x, y)
                    );
                }
                Circle circle = Circle.newBuilder()
                        .setX(x)
                        .setY(y)
                        .setColor(color)
                        .build();
                if (circle.getColor()) {
                    whiteCircles.add(circle);
                } else {
                    blackCircles.add(circle);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при чтении файла: " + fileName);
        }
        return Task.newBuilder()
                .setTaskId(taskId)
                .setFieldWidth(fieldWidth)
                .setFieldLength(fieldLength)
                .setTotalWhiteCombinations((int) Math.pow(8, whiteCircles.size()))
                .setTotalBlackCombinations((int) Math.pow(4, blackCircles.size()))
                .addAllWhiteCircles(whiteCircles)
                .addAllBlackCircles(blackCircles)
                .build();
    }

    public Task getTask(int taskId) {
        return tasks.get(taskId);
    }

    public Batch getNextBatch(int taskId) {
        Task task = tasks.get(taskId);
        if (task == null) {
            return null;
        }
        int batchId = nextBatchId.getAndIncrement();
        int startWhiteCombination = batchStartsWhite.get(taskId);
        int startBlackCombination = batchStartsBlack.get(taskId);
        if (startBlackCombination + 4 < task.getTotalBlackCombinations()) {
            batchStartsBlack.merge(taskId, 4, Integer::sum);
        } else if (startWhiteCombination + 4 < task.getTotalWhiteCombinations()) {
            batchStartsBlack.replace(taskId, 0);
            batchStartsWhite.merge(taskId, 8, Integer::sum);
        } else {
            return Batch.newBuilder()
                    .setBatchId(batchId)
                    .setTaskId(taskId)
                    .setStartWhiteCombination(task.getTotalWhiteCombinations())
                    .setNumberWhiteCombinations(8)
                    .setStartBlackCombination(task.getTotalBlackCombinations())
                    .setNumberBlackCombinations(4)
                    .setResult(false)
                    .build();
        }
        return Batch.newBuilder()
                .setBatchId(batchId)
                .setTaskId(taskId)
                .setStartWhiteCombination(startWhiteCombination)
                .setNumberWhiteCombinations(8)
                .setStartBlackCombination(startBlackCombination)
                .setNumberBlackCombinations(4)
                .setResult(false)
                .build();
    }
}
