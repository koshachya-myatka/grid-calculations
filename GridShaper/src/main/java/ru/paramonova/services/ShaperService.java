package ru.paramonova.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.paramonova.grpc.*;

import java.util.*;

public class ShaperService {
    // id таски - сама таска
    private final Map<Integer, Task> tasks = new HashMap<>();
    //todo поменять способ хранения батчей
    // id таски - список ее батчей
    private final Map<Integer, List<Batch>> batches = new HashMap<>();
    // id таски - закончились ли ее батчи
    private final Map<Integer, Boolean> isTaskLastBatch = new HashMap<>();
    private final Map<Integer, BatchCreator> batchCreators = new HashMap<>();
    // id таски - список успешных результатов решений для нее
    private final Map<Integer, List<Result>> results = new HashMap<>();
    private int nextTaskId = 0;
    private long nextBatchId = 0L;


    public Task addTask(String jsonString) {
        int taskId = nextTaskId++;
        Task task = createTask(taskId, jsonString);
        tasks.put(taskId, task);
        batchCreators.put(taskId, new BatchCreator(task));
        isTaskLastBatch.put(taskId, false);
        results.put(taskId, new ArrayList<>());
        //todo временно, убрать при смене хранения батчей
        batches.put(taskId, new ArrayList<>());
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
            throw new RuntimeException("Ошибка при чтении JSON: ", e);
        }
        return Task.newBuilder()
                .setTaskId(taskId)
                .setFieldWidth(fieldWidth)
                .setFieldLength(fieldLength)
                .setTotalWhiteCombinations((long) Math.pow(16, whiteCircles.size()))
                .setTotalBlackCombinations((long) Math.pow(4, blackCircles.size()))
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
        //todo добавить сюда закольцовывание при отвале воркеров и потере решений
        // (отправление батчей без результатов еще раз, мб ориентироваться на статус
        // и время отправки батча)
        long batchId = nextBatchId++;
        List<PipeList> batchPipes = new ArrayList<>();
        BatchCreator batchCreator = batchCreators.get(taskId);
        while (batchPipes.size() < 5) {
            PipeList pipeList = batchCreator.create();
            if (batchCreator.isFinished()) break;
            batchPipes.add(pipeList);
        }
        if (batchPipes.isEmpty()) {
            isTaskLastBatch.replace(taskId, true);
            return null;
        }
        Batch batch = Batch.newBuilder()
                .setBatchId(batchId)
                .setTaskId(taskId)
                .addAllCombinations(batchPipes)
                .build();
        //todo временно, убрать при смене хранения батчей
        List<Batch> currentTaskBatches = batches.get(taskId);
        currentTaskBatches.add(batch);
        return batch;
    }

    public void addResults(int taskId, long batchId, List<Result> newResults) {
        if (results.get(taskId) == null) {
            throw new RuntimeException("Отсутствует список для результатов задачи " + taskId + "\n");
        }
        //todo тут добавить смену статуса у батча, когда мы получили результаты для него
        if (!newResults.isEmpty()) {
            List<Result> currentResults = results.get(taskId);
            currentResults.addAll(newResults);
        }
        if (isTaskLastBatch.get(taskId) == true) {
            System.out.println("Зашел в последний результат");
            getResult(taskId);
        }
        getResult(taskId);
    }

    public void getResult(int taskId) {
        if (results.get(taskId) == null) {
            throw new RuntimeException("Отсутствует список для результатов задачи " + taskId + "\n");
        }
        List<Result> allResults = results.get(taskId);
        if (allResults.isEmpty()) {
            System.out.println("Для задачи " + taskId + " не было найдено успешное решение");
        } else {
            System.out.println("Всего результатов: " + results.get(taskId).size());
            for (Result result : allResults) {
                visualizeTaskSolution(taskId, result);
            }
        }
    }

    private void visualizeTaskSolution(int taskId, Result result) {
        System.out.println("Успешное соединение:");
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