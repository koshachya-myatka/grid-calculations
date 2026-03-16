package ru.paramonova;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import ru.paramonova.grpc.MyGrpcService;
import ru.paramonova.services.ShaperService;

public class Main {
    public static void main(String[] args) throws Exception {
        Server server = ServerBuilder
                .forPort(8080)
                .addService(new MyGrpcService(new ShaperService()))
                .build();
        server.start();
        server.awaitTermination();
    }
}