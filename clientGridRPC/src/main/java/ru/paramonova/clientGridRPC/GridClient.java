package ru.paramonova.clientGridRPC;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import ru.paramonova.grpc.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GridClient {
    private final ManagedChannel channel;
    private final GridServiceGrpc.GridServiceBlockingStub blockingStub;
    private final GridServiceGrpc.GridServiceStub asyncStub;

    public GridClient(String host, int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        this.blockingStub = GridServiceGrpc.newBlockingStub(channel);
        this.asyncStub = GridServiceGrpc.newStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public int addTask(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        byte[] fileData = Files.readAllBytes(path);
        String fileName = path.getFileName().toString();

        FileRequest request = FileRequest.newBuilder()
                .setFileName(fileName)
                .setFileData(ByteString.copyFrom(fileData))
                .build();

        TaskResponse response = blockingStub.addTask(request);
        System.out.println("Задача " + response.getId() + " создана. ");
        return response.getId();
    }

    public void streamSubtasks(int taskId, int count) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<TaskRequest> requestObserver = asyncStub.getNextTaskCombination(new StreamObserver<SubTaskResponse>() {
            private int receivedCount = 0;

            @Override
            public void onNext(SubTaskResponse response) {
                receivedCount++;
                StringBuilder sb = new StringBuilder();
                sb.append("Задача: ").append(response.getTaskId()).append("\n")
                        .append("Подзадача: ").append(response.getSubtaskId()).append("\n")
                        .append("Ширина поля: ").append(response.getFieldWidth()).append("\n")
                        .append("Длина поля: ").append(response.getFieldLength()).append("\n")
                        .append("Текущая комбинация:\n");
                for (Pipe pipe : response.getCombinationList()) {
                    sb.append(String.format("(%d, %d), цвет=%s, позиция=%d\n",
                            pipe.getX(), pipe.getY(),
                            pipe.getColor() ? "белый" : "чёрный",
                            pipe.getPosition()));
                }
                System.out.println(sb);
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("Ошибка в стриме: " + t.getMessage());
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Стрим завершен. Получено комбинаций: " + receivedCount);
                latch.countDown();
            }
        });
        try {
            for (int i = 0; i < count; i++) {
                TaskRequest request = TaskRequest.newBuilder()
                        .setId(taskId)
                        .build();
                requestObserver.onNext(request);
            }
        } catch (Exception e) {
            requestObserver.onError(e);
        }
        requestObserver.onCompleted();
        latch.await();
    }

    public void getTaskInfo(int taskId) {
        TaskRequest request = TaskRequest.newBuilder()
                .setId(taskId)
                .build();
        TaskInfoResponse response = blockingStub.getTaskInfo(request);
        StringBuilder sb = new StringBuilder();
        sb.append("Задача: ").append(response.getTaskId()).append("\n")
                .append("Ширина поля: ").append(response.getFieldWidth()).append("\n")
                .append("Длина поля: ").append(response.getFieldLength()).append("\n");
        for (Circle circle : response.getCirclesList()) {
            sb.append(String.format("(%d, %d), цвет=%s\n",
                    circle.getX(), circle.getY(),
                    circle.getColor() ? "белый" : "чёрный"));
        }
        System.out.println(sb);
    }
}
