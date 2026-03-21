package ru.paramonova.services;

import ru.paramonova.grpc.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ShaperService {
    // id таски - сама таска
    private final Map<Integer, Task> tasks = new ConcurrentHashMap<>();
    // id таски - список ее батчей
    private final Map<Integer, List<Batch>> batches = new ConcurrentHashMap<>();
    // id таски - текущий стартовый номер комбинации белых кругов
    private final Map<Integer, Integer> batchStartsWhite = new ConcurrentHashMap<>();
    // id таски - текущий стартовый номер комбинации черных кругов
    private final Map<Integer, Integer> batchStartsBlack = new ConcurrentHashMap<>();
    // id таски - список результатов решений для нее
    private final Map<Integer, List<Result>> results = new ConcurrentHashMap<>();
    private final AtomicInteger nextTaskId = new AtomicInteger(0);
    private final AtomicInteger nextBatchId = new AtomicInteger(0);

    public Task addTask(String fileName, byte[] fileData) {
        int taskId = nextTaskId.getAndIncrement();
        Task task = createTask(taskId, fileName, fileData);
        tasks.put(taskId, task);
        batches.put(taskId, new ArrayList<>());
        results.put(taskId, new ArrayList<>());
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
                    throw new IllegalArgumentException("Неверный формат строки с размерами поля\n");
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
                    throw new IllegalArgumentException("Неверный формат строки с описанием круга " + line + "\n");
                }
                int x = Integer.parseInt(values[0].trim());
                int y = Integer.parseInt(values[1].trim());
                boolean color = "1".equals(values[2].trim());
                if (x < 0 || x >= fieldLength || y < 0 || y >= fieldWidth) {
                    throw new IllegalArgumentException(
                            String.format("Круг с координатами (%d, %d) выходит за пределы поля\n", x, y)
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
            throw new RuntimeException("Ошибка при чтении файла: " + fileName + "\n");
        }
        return Task.newBuilder()
                .setTaskId(taskId)
                .setFieldWidth(fieldWidth)
                .setFieldLength(fieldLength)
                .setTotalWhiteCombinations((int) Math.pow(12, whiteCircles.size()))
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
        int totalW = task.getTotalWhiteCombinations();
        int totalB = task.getTotalBlackCombinations();
        int startWhiteCombination = batchStartsWhite.get(taskId);
        int startBlackCombination = batchStartsBlack.get(taskId);
        if (startWhiteCombination >= totalW || startBlackCombination >= totalB) {
            //TODO добавить более адекватное закольцовывание, когда мы сгенерили все возможные батчи,
            // но у нас появился новый свободный воркер ?
//            if (batches.get(taskId) != null && !batches.get(taskId).isEmpty()) {
//                return batches.get(taskId).getFirst();
//            }
            return null;
        }
        int batchId = nextBatchId.getAndIncrement();
        int numberWhiteCombinations = Math.min((int) Math.pow(12, 3), totalW - startWhiteCombination);
        int numberBlackCombinations = Math.min((int) Math.pow(4, 5), totalB - startBlackCombination);
        Batch batch = Batch.newBuilder()
                .setBatchId(batchId)
                .setTaskId(taskId)
                .setStartWhiteCombination(startWhiteCombination)
                .setNumberWhiteCombinations(numberWhiteCombinations)
                .setStartBlackCombination(startBlackCombination)
                .setNumberBlackCombinations(numberBlackCombinations)
                .build();
        List<Batch> currentTaskBatches = batches.get(taskId);
        currentTaskBatches.add(batch);
        batchStartsBlack.merge(taskId, numberBlackCombinations, Integer::sum);
        if (startBlackCombination + numberBlackCombinations >= task.getTotalBlackCombinations()) {
            batchStartsBlack.replace(taskId, 0);
            batchStartsWhite.merge(taskId, numberWhiteCombinations, Integer::sum);
        }
        return batch;
    }

    public void addResults(int taskId, int batchId, List<Result> newResults) {
        if (results.get(taskId) == null) {
            throw new RuntimeException("Отсутствует список для результатов задачи " + taskId + "\n");
        }
        List<Result> currentResults = results.get(taskId);
        currentResults.addAll(newResults);
        System.out.println("Добавлено " + newResults.size() + " результатов для задачи " + taskId + "\n");
        //todo придумать варик получше
        if (isLastResult(taskId)) {
            getResult(taskId);
        }
    }

    private boolean isLastResult(int taskId) {
        if (batches.get(taskId) == null || results.get(taskId) == null) {
            throw new RuntimeException("Не были найдены батчи или результаты для задачи " + taskId);
        }
        List<Integer> idsInResults = results.get(taskId)
                .stream().map(Result::getBatchId).toList();
        List<Integer> idsInBatches = batches.get(taskId)
                .stream().map(Batch::getBatchId).toList();
        return idsInResults.containsAll(idsInBatches);
    }

    public Result getResult(int taskId) {
        if (results.get(taskId) == null) {
            throw new RuntimeException("Отсутствует список для результатов задачи " + taskId + "\n");
        }
        List<Result> allResults = results.get(taskId);
        Result finalResult = allResults.getFirst();
        for (Result result : allResults) {
            if (result.getConnected()) {
                System.out.println("Успешное соединение:");
                visualizeTaskSolution(taskId, result);
                finalResult = result;
            }
        }
        System.out.println("Всего результатов: " + results.get(taskId).size());
        return finalResult;
    }

    private void visualizeTaskSolution(int taskId, Result result) {
        Task task = tasks.get(taskId);
        if (task == null) {
            throw new RuntimeException("Не найдена задача " + taskId + "\n");
        }
        List<String> lineSymbols = List.of(" ", "-", "|", "┌", "└", "┘", "┐");
        List<String> circleSymbols = List.of("○", "●");
        int width = task.getFieldWidth();
        int length = task.getFieldLength();
        String[][] matrix = new String[length][width];
        for (Line line : result.getLinesList()) {
            matrix[line.getX()][line.getY()] = lineSymbols.get(line.getPosition());
        }
        for (Pipe pipe : result.getPipesList()) {
            matrix[pipe.getX()][pipe.getY()] = pipe.getColor() ? circleSymbols.get(1) : circleSymbols.get(0);
        }
        for (int x = 0; x < length; x++) {
            for (int y = 0; y < width; y++) {
                System.out.print(matrix[x][y] + " ");
            }
            System.out.print("\n");
        }
    }
}
