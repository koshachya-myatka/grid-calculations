package ru.paramonova.mainGridRPC;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class MainGridRpcApplication {
    public static void main(String[] args) throws Exception {
        Server server = ServerBuilder
                .forPort(8080)
//                .addService(new MyGridService(new DistributorService()))
                .build();
        server.start();
        server.awaitTermination();
    }
}
