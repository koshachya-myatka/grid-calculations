package ru.paramonova.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.paramonova.grpc.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShaperService {
    // id таски - сама таска
    private final Map<Integer, Task> tasks = new HashMap<>();
    //todo поменять список хранения батчей
    // id таски - список ее батчей
    private final Map<Integer, List<Batch>> batches = new HashMap<>();
    // id таски - текущий стартовый номер комбинации белых кругов
    private final Map<Integer, Long> batchStartsWhite = new HashMap<>();
    // id таски - текущий стартовый номер комбинации черных кругов
    private final Map<Integer, Long> batchStartsBlack = new HashMap<>();
    // id таски - список успешных результатов решений для нее
    private final Map<Integer, List<Result>> results = new HashMap<>();
    private int nextTaskId = 0;
    private long nextBatchId = 0L;

    public Task addTask(String jsonString) {
        int taskId = nextTaskId;
        nextTaskId++;
        Task task = createTask(taskId, jsonString);
        tasks.put(taskId, task);
        batches.put(taskId, new ArrayList<>());
        results.put(taskId, new ArrayList<>());
        batchStartsWhite.put(taskId, 0L);
        batchStartsBlack.put(taskId, 0L);
        return task;
    }

    private Task createTask(int taskId, String jsonString) {
        int fieldWidth = 0;
        int fieldLength = 0;
        List<Circle> whiteCircles = new ArrayList<>();
        List<Circle> blackCircles = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode rootNode = objectMapper.readTree(jsonString);
            fieldWidth = rootNode.get("fieldWidth").asInt();
            fieldLength = rootNode.get("fieldLength").asInt();
            if (fieldWidth == 0 || fieldLength == 0) {
                throw new IllegalArgumentException("Неверный формат данных с размерами поля\n");
            }
            JsonNode circlesNode = rootNode.get("circles");
            if (circlesNode == null || !circlesNode.isArray()) {
                throw new IllegalArgumentException("Данные о кругах не заданы или заданы некорректно\n");
            }
            for (JsonNode circleNode : circlesNode) {
                int x = circleNode.get("x").asInt();
                int y = circleNode.get("y").asInt();
                boolean color = circleNode.get("color").asBoolean();
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
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка при чтении JSON: ", e);
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
        long totalW = task.getTotalWhiteCombinations();
        long totalB = task.getTotalBlackCombinations();
        long startWhiteCombination = batchStartsWhite.get(taskId);
        long startBlackCombination = batchStartsBlack.get(taskId);
        if (startWhiteCombination >= totalW || startBlackCombination >= totalB) {
            //TODO добавить более адекватное закольцовывание, когда мы сгенерили все возможные батчи,
            // но у нас появился новый свободный воркер ?
//            if (batches.get(taskId) != null && !batches.get(taskId).isEmpty()) {
//                return batches.get(taskId).getFirst();
//            }
            return null;
        }
        long batchId = nextBatchId;
        nextBatchId++;
        //todo выбери размер батча
        long numberWhiteCombinations = Math.min((long) Math.pow(12, 3), totalW - startWhiteCombination);
        long numberBlackCombinations = Math.min((long) Math.pow(4, 8), totalB - startBlackCombination);
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
        batchStartsBlack.merge(taskId, numberBlackCombinations, Long::sum);
        if (startBlackCombination + numberBlackCombinations >= task.getTotalBlackCombinations()) {
            batchStartsBlack.replace(taskId, 0L);
            batchStartsWhite.merge(taskId, numberWhiteCombinations, Long::sum);
        }
        return batch;
    }

    public void addResults(int taskId, long batchId, List<Result> newResults) {
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
        List<Long> idsInResults = results.get(taskId)
                .stream().map(Result::getBatchId).toList();
        List<Long> idsInBatches = batches.get(taskId)
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
