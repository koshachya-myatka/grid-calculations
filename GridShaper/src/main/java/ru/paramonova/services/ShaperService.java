package ru.paramonova.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.paramonova.dto.BatchInfo;
import ru.paramonova.dto.BatchStatus;
import ru.paramonova.grpc.*;

import java.util.*;

public class ShaperService {
    private final long BATCH_TIMEOUT_MS = 30_000;
    // id таски - сама таска
    private final Map<Integer, Task> tasks = new HashMap<>();
    // id таски - список ее батчей
    private final Map<Integer, List<BatchInfo>> batches = new HashMap<>();
    // id таски - ее общее кол-во батчей
    private final Map<Integer, Long> taskTotalBatches = new HashMap<>();
    // id таски - текущий номер ее батча
    private final Map<Integer, Long> taskCurrentBatchNum = new HashMap<>();
    // id таски - текущий стартовый номер комбинации белых кругов
    private final Map<Integer, Long> batchStartsWhite = new HashMap<>();
    // id таски - текущий стартовый номер комбинации черных кругов
    private final Map<Integer, Long> batchStartsBlack = new HashMap<>();
    // id таски - список успешных результатов решений для нее
    private final Map<Integer, List<Result>> results = new HashMap<>();
    private int nextTaskId = 0;
    private long nextBatchId = 0L;

    public long getCountTotalBatches(int taskId) {
        if (taskTotalBatches.get(taskId) == null) {
            return 0;
        }
        return taskTotalBatches.get(taskId);
    }

    public Task addTask(String jsonString) {
        int taskId = nextTaskId++;
        Task task = createTask(taskId, jsonString);
        tasks.put(taskId, task);
        long batchWhiteSize = 1L << (4 * 3); // 16^3 = 2^(4*3)
        long batchBlackSize = 1L << (2 * 8); // 4^8 = 2^(2*8)
        long whiteChunks = (task.getTotalWhiteCombinations() + batchWhiteSize - 1) / batchWhiteSize;
        long blackChunks = (task.getTotalBlackCombinations() + batchBlackSize - 1) / batchBlackSize;
        long totalBatches = whiteChunks * blackChunks;
        taskTotalBatches.put(taskId, totalBatches);
        taskCurrentBatchNum.put(taskId, 0L);
        batchStartsWhite.put(taskId, 0L);
        batchStartsBlack.put(taskId, 0L);
        batches.put(taskId, new ArrayList<>());
        results.put(taskId, new ArrayList<>());
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
            throw new RuntimeException("Задача с id " + taskId + " не была найдена\n");
        }
        List<BatchInfo> lst = batches.get(taskId);
        if (lst == null) {
            throw new RuntimeException("Список батчей задачи " + taskId + " не был найден\n");
        }
        // пытаемся выдать новый батч
        if (!isLastBatchGenerated(taskId)) {
            BatchInfo batchInfo = createNextBatchInfo(task);
            if (batchInfo != null) {
                batchInfo.setStatus(BatchStatus.IN_PROGRESS);
                batchInfo.setLastSendTime(System.currentTimeMillis());
                lst.add(batchInfo);
                return batchInfo.getBatch();
            }
        }
        // переотправка батчей, если все батчи уже сгенерены
        long now = System.currentTimeMillis();
        for (BatchInfo batchInfo : lst) {
            if (batchInfo.getStatus() == BatchStatus.IN_PROGRESS &&
                    now - batchInfo.getLastSendTime() > BATCH_TIMEOUT_MS) {
                batchInfo.setLastSendTime(now);
                return batchInfo.getBatch();
            }
        }
        return null;
    }

    public BatchInfo createNextBatchInfo(Task task) {
        int taskId = task.getTaskId();
        if (isLastBatchGenerated(taskId)) {
            return null;
        }
        long totalW = task.getTotalWhiteCombinations();
        long totalB = task.getTotalBlackCombinations();
        long startWhiteCombination = batchStartsWhite.get(taskId);
        long startBlackCombination = batchStartsBlack.get(taskId);
        if (startWhiteCombination >= totalW || startBlackCombination >= totalB) {
            return null;
        }
        long batchId = nextBatchId++;
        // определение размера батча по степеням в комбинациях
        long numberWhiteCombinations = Math.min((long) Math.pow(16, 3), totalW - startWhiteCombination);
        long numberBlackCombinations = Math.min((long) Math.pow(4, 8), totalB - startBlackCombination);
        Batch batch = Batch.newBuilder()
                .setBatchId(batchId)
                .setTaskId(taskId)
                .setStartWhiteCombination(startWhiteCombination)
                .setNumberWhiteCombinations(numberWhiteCombinations)
                .setStartBlackCombination(startBlackCombination)
                .setNumberBlackCombinations(numberBlackCombinations)
                .build();
        // подсчет новых стартовых значений для следующего батча
        batchStartsBlack.merge(taskId, numberBlackCombinations, Long::sum);
        if (startBlackCombination + numberBlackCombinations >= task.getTotalBlackCombinations()) {
            batchStartsBlack.replace(taskId, 0L);
            batchStartsWhite.merge(taskId, numberWhiteCombinations, Long::sum);
        }
        // увеличиваем кол-во уже прошедших батчей задачи
        taskCurrentBatchNum.merge(taskId, 1L, Long::sum);
        return new BatchInfo(batch);
    }

    public long getTimeoutForTask(int taskId) {
        List<BatchInfo> lst = batches.get(taskId);
        if (lst == null) {
            throw new RuntimeException("Список батчей задачи " + taskId + " не был найден\n");
        }
        long now = System.currentTimeMillis();
        long timeout = BATCH_TIMEOUT_MS;
        for (BatchInfo batchInfo : lst) {
            if (batchInfo.getStatus() == BatchStatus.IN_PROGRESS &&
                    BATCH_TIMEOUT_MS - (now - batchInfo.getLastSendTime()) < timeout) {
                timeout = BATCH_TIMEOUT_MS - (now - batchInfo.getLastSendTime());
            }
        }
        return timeout;
    }

    private boolean isLastBatchGenerated(int taskId) {
        return Objects.equals(taskCurrentBatchNum.get(taskId), taskTotalBatches.get(taskId));
    }

    public boolean isTaskFinished(int taskId) {
        List<BatchInfo> lst = batches.get(taskId);
        if (lst == null) {
            throw new RuntimeException("Список батчей задачи " + taskId + " не был найден\n");
        }
        if (lst.isEmpty()) return false;
        for (BatchInfo batchInfo : lst) {
            if (batchInfo.getStatus() != BatchStatus.DONE) {
                return false;
            }
        }
        return true;
    }

    public boolean hasTaskSuccessfulResult(int taskId) {
        if (results.get(taskId) == null) {
            throw new RuntimeException("Отсутствует список для результатов задачи " + taskId + "\n");
        }
        List<Result> allResults = results.get(taskId);
        return !allResults.isEmpty();
    }

    public void addResults(int taskId, long batchId, List<Result> newResults) {
        if (results.get(taskId) == null) {
            throw new RuntimeException("Отсутствует список для результатов задачи " + taskId + "\n");
        }
        Optional<BatchInfo> optionalBatchInfo = batches.get(taskId).stream()
                .filter(bI -> bI.getBatch().getBatchId() == batchId)
                .findFirst();
        if (optionalBatchInfo.isEmpty()) {
            throw new RuntimeException("Батч " + batchId + " задачи " + taskId + " не был найден\n");
        }
        BatchInfo batchInfo = optionalBatchInfo.get();
        batchInfo.setStatus(BatchStatus.DONE);
        if (!newResults.isEmpty()) {
            List<Result> currentResults = results.get(taskId);
            currentResults.addAll(newResults);
            taskCurrentBatchNum.replace(taskId, taskTotalBatches.get(taskId));
        }
    }

    public void getResult(int taskId) {
        if (results.get(taskId) == null) {
            throw new RuntimeException("Отсутствует список для результатов задачи " + taskId + "\n");
        }
        List<Result> allResults = results.get(taskId);
        if (allResults.isEmpty()) {
            System.out.println("Для задачи " + taskId + " не было найдено успешное решение");
        } else {
            System.out.println("У задачи " + taskId + " всего успешных результатов: " + results.get(taskId).size());
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