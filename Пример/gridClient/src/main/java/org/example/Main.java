package org.example;

import io.grpc.*;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class Main {
    public static void main(String[] args) throws Exception{
        Channel channel= ManagedChannelBuilder
                .forAddress("localhost",8080)
                .usePlaintext()
                .build();
       // GridServiceGrpc.GridServiceBlockingStub gridService=GridServiceGrpc.newBlockingStub(channel);
        TaskRequest request=TaskRequest.newBuilder().setId(5).build();
        GridServiceGrpc.GridServiceStub gridService=GridServiceGrpc.newStub(channel);
        CountDownLatch latch= new CountDownLatch(5);

        StreamObserver<TaskRequest> result=gridService.getSubTask(new StreamObserver<SubTaskRequest>() {
            @Override
            public void onNext(SubTaskRequest subTaskRequest) {
                System.out.println(subTaskRequest);
                latch.countDown();
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println(throwable);
            }

            @Override
            public void onCompleted() {
                System.out.println("the end");
            }
        });

        for (int i = 0; i < 5; i++) {
            result.onNext(request);
        }
        result.onCompleted();

        latch.await();
        System.out.println("the end");
    }
}