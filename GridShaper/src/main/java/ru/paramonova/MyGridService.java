package ru.paramonova;

import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import ru.paramonova.grpc.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

public class MyGridService
        extends GridServiceGrpc.GridServiceImplBase {
    private final ShaperService shaperService;
    private final ConcurrentHashMap<Integer, StreamObserver<BatchResponse>> activeStreams = new ConcurrentHashMap<>();

    public MyGridService(ShaperService shaperService) {
        this.shaperService = shaperService;
    }

    @Override
    public void addTask(FileRequest request, StreamObserver<TaskIdResponse> responseObserver) {
        try {
            System.out.println("Получен запрос на добавление задачи с файлом: " + request.getFileName());
            Task task = shaperService.addTask(
                    request.getFileName(),
                    request.getFileData().toByteArray()
            );
            TaskIdResponse response = TaskIdResponse.newBuilder()
                    .setTaskId(task.getTaskId())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            System.out.printf("Задача %d создана%n", task.getTaskId());
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Ошибка при создании задачи: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void registerTask(TaskRequest request, StreamObserver<RegisterResponse> responseObserver) {
        int taskId = request.getTaskId();
        Task task = shaperService.getTask(taskId);
        if (task == null) {
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("Задача " + taskId + " не найдена")
                    .asRuntimeException());
            return;
        }
        try {
            String pathToParent = new File(System.getProperty("user.dir")).getParent();
            Path jarPath = Paths.get(pathToParent + "\\GridShaper\\libs\\GridShaper-1.0.jar");
            byte[] jarBytes = Files.readAllBytes(jarPath);
            RegisterResponse response = RegisterResponse.newBuilder()
                    .setTask(task)
                    .setTaskJar(ByteString.copyFrom(jarBytes))
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            System.out.println("Задача " + request.getTaskId() + " зарегистрирована в распределителе");
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Ошибка при генерации jar и регистрации задачи: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getTaskInfo(TaskRequest request, StreamObserver<TaskResponse> responseObserver) {
        try {
            int taskId = request.getTaskId();
            Task task = shaperService.getTask(taskId);
            if (task == null) {
                responseObserver.onError(io.grpc.Status.NOT_FOUND
                        .withDescription("Задача " + taskId + " не найдена")
                        .asRuntimeException());
                return;
            }
            TaskResponse.Builder responseBuilder = TaskResponse.newBuilder()
                    .setTask(task);
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Ошибка при получении информации: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public StreamObserver<TaskRequest> getNextBatch(StreamObserver<BatchResponse> responseObserver) {
        return new StreamObserver<TaskRequest>() {
            private int currentTaskId = -1;

            @Override
            public void onNext(TaskRequest request) {
                int taskId = request.getTaskId();
                currentTaskId = taskId;
                try {
                    Task task = shaperService.getTask(taskId);
                    if (task == null) {
                        responseObserver.onError(io.grpc.Status.NOT_FOUND
                                .withDescription("Задача " + taskId + " не найдена")
                                .asRuntimeException());
                        return;
                    }
                    Batch batch = shaperService.getNextBatch(taskId);
                    if (batch == null) {
                        responseObserver.onCompleted();
                        return;
                    }
                    BatchResponse.Builder responseBuilder = BatchResponse.newBuilder()
                            .setBatch(batch);
                    responseObserver.onNext(responseBuilder.build());
                } catch (Exception e) {
                    responseObserver.onError(io.grpc.Status.INTERNAL
                            .withDescription("Ошибка при получении батча: " + e.getMessage())
                            .asRuntimeException());
                }
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("Ошибка в стриме для задачи " + currentTaskId + ": " + t.getMessage());
                if (currentTaskId != -1) {
                    activeStreams.remove(currentTaskId);
                }
            }

            @Override
            public void onCompleted() {
                System.out.println("Стрим для задачи " + currentTaskId + " завершен");
                if (currentTaskId != -1) {
                    activeStreams.remove(currentTaskId);
                }
                System.out.println("Результат для задачи " + currentTaskId + ":\n");
                shaperService.getResult(currentTaskId);
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void addResults(ResultsRequest request, StreamObserver<ResultsResponse> responseObserver) {
        try {
            int taskId = request.getTaskId();
            shaperService.addResults(taskId, request.getResultsList());
            ResultsResponse response = ResultsResponse.newBuilder()
                    .setIsAccepted(true)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Ошибка при добавлении результатов: " + e.getMessage())
                    .asRuntimeException());
        }
    }
}

