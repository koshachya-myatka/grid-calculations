package ru.paramonova.grpc;

import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import ru.paramonova.services.ShaperService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

public class MyGrpcService extends GridServiceGrpc.GridServiceImplBase {
    private final ShaperService shaperService;
    private final ConcurrentHashMap<Integer, StreamObserver<BatchResponse>> activeStreams = new ConcurrentHashMap<>();

    public MyGrpcService(ShaperService shaperService) {
        this.shaperService = shaperService;
    }

    @Override
    public void addTask(FileRequest request, StreamObserver<TaskIdResponse> responseObserver) {
        try {
            System.out.println("Получен запрос на добавление задачи из файла: " + request.getFileName());
            Task task = shaperService.addTask(
                    request.getFileName(),
                    request.getFileData().toByteArray()
            );
            TaskIdResponse response = TaskIdResponse.newBuilder()
                    .setTaskId(task.getTaskId())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            System.out.printf("Задача %d создана%n%n", task.getTaskId());
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
                    .withDescription("Задача " + taskId + " не найдена\n")
                    .asRuntimeException());
            return;
        }
        try {
            String pathToParent = new File(System.getProperty("user.dir")).getParent();
            Path jarPath = Paths.get(pathToParent + "\\GridCalculator\\libs\\GridCalculator-1.0.jar");
            byte[] jarBytes = Files.readAllBytes(jarPath);
            RegisterResponse response = RegisterResponse.newBuilder()
                    .setTask(task)
                    .setTaskJar(ByteString.copyFrom(jarBytes))
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            System.out.println("Задача " + request.getTaskId() + " зарегистрирована в распределителе\n");
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Ошибка при нахождении jar и регистрации задачи " + taskId + ":\n" + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getTaskInfo(TaskRequest request, StreamObserver<TaskResponse> responseObserver) {
        int taskId = request.getTaskId();
        Task task = shaperService.getTask(taskId);
        if (task == null) {
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("Задача " + taskId + " не найдена\n")
                    .asRuntimeException());
            return;
        }
        try {
            TaskResponse.Builder responseBuilder = TaskResponse.newBuilder()
                    .setTask(task);
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Ошибка при получении информации задачи " + taskId + ":\n" + e.getMessage())
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
                                .withDescription("Задача " + taskId + " не найдена\n")
                                .asRuntimeException());
                        return;
                    }
                    Batch batch = shaperService.getNextBatch(taskId);
                    if (batch == null) {
                        responseObserver.onCompleted();
                        System.out.println("Результат для задачи " + currentTaskId + ":");
                        //todo тут оформить возврат именно нужного значения и определить когда это стоит вызывать
                        shaperService.getResult(currentTaskId);
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
                //todo эту штуку запустить тут вручную, потому что на клиенте она уже
                // не запустится, т.к. сервер тормозит стрим
                if (currentTaskId != -1) {
                    activeStreams.remove(currentTaskId);
                }
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void addResults(ResultsRequest request, StreamObserver<ResultsResponse> responseObserver) {
        int taskId = request.getTaskId();
        Task task = shaperService.getTask(taskId);
        if (task == null) {
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("Задача " + taskId + " не найдена\n")
                    .asRuntimeException());
            return;
        }
        try {
            shaperService.addResults(taskId, request.getBatchId(), request.getResultsList());
            ResultsResponse response = ResultsResponse.newBuilder()
                    .setAccepted(true)
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