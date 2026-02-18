package org.example;

import io.grpc.stub.StreamObserver;

public class MyGridService extends GridServiceGrpc.GridServiceImplBase {
    int x;

    @Override
    public StreamObserver<TaskRequest> getSubTask(StreamObserver<SubTaskRequest> responseObserver) {
        return new StreamObserver<TaskRequest>() {
            @Override
            public void onNext(TaskRequest taskRequest) {
                SubTaskRequest req = SubTaskRequest.newBuilder()
                        .setTaskId(taskRequest.getId())
                        .setId(x++)
                        .setText("AAAAA!!")
                        .build();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                responseObserver.onNext(req);
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println(throwable);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };

    }
    //    @Override
//    public void getSubTask(TaskRequest request, StreamObserver<SubTaskRequest> responseObserver) {
//        SubTaskRequest req=SubTaskRequest.newBuilder()
//                .setTaskId(request.getId())
//                .setId(x++)
//                .setText("AAAAA!!")
//                .build();
//        responseObserver.onNext(req);
//        responseObserver.onCompleted();
//    }

    @Override
    public void addTask(Task request, StreamObserver<TaskRequest> responseObserver) {

    }
}
