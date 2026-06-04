package com.fileuploader;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class FileServer {
    public static void main(String[] args) throws Exception {
        final int port = 50051;
        Server server = ServerBuilder.forPort(port)
                .addService(new FileServiceImpl())
                .build()
                .start();

        System.out.println("gRPC FileService server started on port " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down gRPC server...");
            if (server != null) {
                server.shutdown();
            }
        }));

        server.awaitTermination();
    }
}
