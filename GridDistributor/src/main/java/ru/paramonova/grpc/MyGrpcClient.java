package ru.paramonova.grpc;

import com.google.protobuf.util.JsonFormat;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.paramonova.services.DistributorService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class MyGrpcClient {
    private final ManagedChannel channel;
    private final GridServiceGrpc.GridServiceBlockingStub blockingStub;
    private final GridServiceGrpc.GridServiceStub asyncStub;

    @Autowired
    private DistributorService distributorService;
    private final Semaphore workerAvailable = new Semaphore(0);

    public void addFreeWorker() {
        workerAvailable.release();
    }

    public MyGrpcClient() {
        this.channel = ManagedChannelBuilder
                .forAddress("localhost", 8080)
                .usePlaintext()
                .build();
        this.blockingStub = GridServiceGrpc.newBlockingStub(channel);
        this.asyncStub = GridServiceGrpc.newStub(channel);
    }

    public int addTask(String jsonString) throws IOException {
        TaskJsonRequest request = TaskJsonRequest.newBuilder()
                .setJsonString(jsonString)
                .build();
        TaskIdResponse response = blockingStub.addTask(request);
        System.out.println("Задача " + response.getTaskId() + " создана\n");
        return response.getTaskId();
    }

    public void registerTask(int taskId) {
        TaskRequest taskRequest = TaskRequest.newBuilder()
                .setTaskId(taskId)
                .build();
        RegisterResponse registerResponse = blockingStub.registerTask(taskRequest);
        distributorService.registerTask(registerResponse.getTask(),
                registerResponse.getTaskJar().toByteArray());
    }

    public void getTaskInfo(int taskId) {
        TaskRequest request = TaskRequest.newBuilder()
                .setTaskId(taskId)
                .build();
        TaskResponse response = blockingStub.getTaskInfo(request);
        StringBuilder sb = new StringBuilder();
        Task task = response.getTask();
        sb.append("Задача: ").append(task.getTaskId()).append("\n")
                .append("Ширина поля: ").append(task.getFieldWidth()).append("\n")
                .append("Длина поля: ").append(task.getFieldLength()).append("\n")
                .append("Кол-во комбинаций белых кругов: ").append(task.getTotalWhiteCombinations()).append("\n")
                .append("Кол-во комбинаций черных кругов: ").append(task.getTotalBlackCombinations()).append("\n");
        List<Circle> allCircles = new ArrayList<>();
        allCircles.addAll(task.getBlackCirclesList());
        allCircles.addAll(task.getWhiteCirclesList());
        for (Circle circle : allCircles) {
            sb.append(String.format("(%d, %d), цвет=%s\n",
                    circle.getX(), circle.getY(),
                    circle.getColor() ? "белый" : "чёрный"));
        }
        System.out.println(sb);
    }

    public void streamBatches(int taskId) throws InterruptedException {
        AtomicBoolean streamFinished = new AtomicBoolean(false);

        StreamObserver<TaskRequest> requestObserver = asyncStub.getNextBatch(new StreamObserver<BatchResponse>() {
            private int receivedCount = 0;

            @Override
            public void onNext(BatchResponse response) {
                receivedCount++;
                Batch batch = response.getBatch();
                StringBuilder sb = new StringBuilder();
                sb.append("Задача: ").append(batch.getTaskId()).append("\n")
                        .append("Батч: ").append(batch.getBatchId()).append("\n")
                        .append("Текущая комбинация:\n")
                        .append("Стартовый номер комбинации для белых кругов: ").append(batch.getStartWhiteCombination()).append("\n")
                        .append("Кол-во комбинаций для белых кругов: ").append(batch.getNumberWhiteCombinations()).append("\n")
                        .append("Стартовый номер комбинации для черных кругов: ").append(batch.getStartBlackCombination()).append("\n")
                        .append("Кол-во комбинаций для черных кругов: ").append(batch.getNumberBlackCombinations()).append("\n");
                System.out.println(sb);
                if (!distributorService.trySendSubtask(batch)) {
                    addFreeWorker();
                }
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("Ошибка в стриме: " + t.getMessage());
                addFreeWorker();
            }

            @Override
            public void onCompleted() {
                System.out.println("Стрим задачи " + taskId + " завершен. Получено батчей: " + receivedCount + "\n");
                streamFinished.set(true);
                addFreeWorker();
            }
        });

        new Thread(() -> {
            try {
                while (!streamFinished.get()) {
                    if (streamFinished.get()) {
                        break;
                    }
                    workerAvailable.acquire();
                    TaskRequest request = TaskRequest.newBuilder()
                            .setTaskId(taskId)
                            .build();
                    requestObserver.onNext(request);
                }
                addFreeWorker();
            } catch (Exception e) {
                requestObserver.onError(e);
            }
        }).start();
    }

    public void addResults(int taskId, int subtaskId, String resultsJson) {
        List<Result> results = new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode array = mapper.readTree(resultsJson);
            for (JsonNode node : array) {
                Result.Builder builder = Result.newBuilder();
                JsonFormat.parser().merge(node.toString(), builder);
                results.add(builder.build());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        distributorService.addResults(taskId, results);
        ResultsRequest req = ResultsRequest.newBuilder()
                .setTaskId(taskId)
                .setBatchId(subtaskId)
                .addAllResults(results)
                .build();
        ResultsResponse resp = blockingStub.addResults(req);
        System.out.println("Формирователь ответил на addResult " + resp.getAccepted() + " для задачи " + taskId + "\n");
    }
}