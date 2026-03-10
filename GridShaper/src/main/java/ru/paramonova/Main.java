package ru.paramonova;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import ru.paramonova.services.MyGridService;
import ru.paramonova.services.ShaperService;

public class Main {
    public static void main(String[] args) throws Exception {
        Server server = ServerBuilder
                .forPort(8080)
                .addService(new MyGridService(new ShaperService()))
                .build();
        server.start();
        server.awaitTermination();
    }
}