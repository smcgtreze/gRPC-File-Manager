package com.fileuploader;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class FileServer {
    static final int DEFAULT_PORT = 50051;
    public static void main(String[] args) throws Exception {
        int port;
        if (args.length < 1) {
            System.out.println("Usage: java -jar fileServer.jar port. Defaulting to port " + DEFAULT_PORT);
            port = DEFAULT_PORT;
        } else {
            port = Integer.parseInt(args[0]);
        }

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
