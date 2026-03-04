package ru.paramonova.clientGridRPC;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import ru.paramonova.clientGridRPC.models.Worker;
import ru.paramonova.grpc.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MyGridClient {
    private final ManagedChannel channel;
    private final GridServiceGrpc.GridServiceBlockingStub blockingStub;
    private final GridServiceGrpc.GridServiceStub asyncStub;
    private final DistributorService distributorService;

    private final ExecutorService executor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors()
    );
    private final Semaphore workerAvailable = new Semaphore(0);
    private final Map<Integer, Worker> workers = new ConcurrentHashMap<>();
    private final AtomicInteger nextWorkerId = new AtomicInteger(0);

    public MyGridClient(String host, int port, DistributorService distributorService) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        this.blockingStub = GridServiceGrpc.newBlockingStub(channel);
        this.asyncStub = GridServiceGrpc.newStub(channel);
        this.distributorService = distributorService;
    }

    public void await() throws InterruptedException {
        channel.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
    }

    public int addTask(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        byte[] fileData = Files.readAllBytes(path);
        String fileName = path.getFileName().toString();
        FileRequest request = FileRequest.newBuilder()
                .setFileName(fileName)
                .setFileData(ByteString.copyFrom(fileData))
                .build();
        TaskIdResponse response = blockingStub.addTask(request);
        System.out.println("Задача " + response.getTaskId() + " создана. ");
        return response.getTaskId();
    }

    public void registerTask(int taskId) {
        TaskRequest taskRequest = TaskRequest.newBuilder()
                .setTaskId(taskId)
                .build();
        RegisterResponse registerResponse = blockingStub.registerTask(taskRequest);
        distributorService.registerTask(registerResponse.getTask(),
                registerResponse.getTaskJar().toByteArray(), registerResponse.getCalcClassName());
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
        CountDownLatch latch = new CountDownLatch(1);
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
                executeBatch(batch);
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("Ошибка в стриме: " + t.getMessage());
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Стрим завершен. Получено батчей: " + receivedCount);
                latch.countDown();
            }
        });
        //TODO подумать будет ли тут статичное кол-во вызовов или бахнуть while(true)
        Task task = distributorService.getTask(taskId);
        int totalBatches = task.getTotalBlackCombinations() * task.getTotalBlackCombinations() / 32;
        try {
            for (int i = 0; i < totalBatches; i++) {
                workerAvailable.acquire();
                TaskRequest request = TaskRequest.newBuilder()
                        .setTaskId(taskId)
                        .build();
                requestObserver.onNext(request);
            }
        } catch (Exception e) {
            requestObserver.onError(e);
        }
        requestObserver.onCompleted();
        latch.await();
    }

    public void registerWorker() {
        int workerId = nextWorkerId.getAndIncrement();
        Worker worker = new Worker(workerId);
        workers.put(workerId, worker);
        workerAvailable.release();
    }

    public void executeBatch(Batch batch) {
        executor.submit(() -> {
            Worker worker = findFreeWorker();
            try {
                worker.setBusy(true);
                if (worker.getTask() == null || worker.getTask().getTaskId() != batch.getTaskId()) {
                    worker.setTaskData(distributorService.getTask(batch.getTaskId()),
                            distributorService.getJar(batch.getTaskId()),
                            distributorService.calcClassName(batch.getTaskId()));
                }
                List<Result> results = worker.executeBatch(batch);
                addResults(batch.getTaskId(), results);
            } catch (Exception e) {
                System.err.println("Ошибка при выполнении батча " + batch.getBatchId() + ": " + e.getMessage());
            } finally {
                worker.setBusy(false);
                workerAvailable.release();
            }
        });
    }

    private Worker findFreeWorker() {
        for (Worker w : workers.values()) {
            if (!w.isBusy()) return w;
        }
        return null;
    }

    public void addResults(int taskId, List<Result> results) {
        ResultsRequest req = ResultsRequest.newBuilder()
                .setTaskId(taskId)
                .addAllResults(results)
                .build();
        ResultsResponse resp = blockingStub.addResults(req);
        System.out.println("Сервер ответил на addResult " + resp.getIsAccepted() + " для задачи " + taskId);
    }
}
