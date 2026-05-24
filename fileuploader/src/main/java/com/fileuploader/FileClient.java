package com.fileuploader;
import com.example.grpc.FileServiceGrpc;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class FileClient {
    private final String host;
    private final int port;

    public FileClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void uploadFile(String path) throws Exception {
        var channel = io.grpc.ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        try {
            ClientUpload uploader = new ClientUpload();
            uploader.uploadFile(FileServiceGrpc.newStub(channel), path);
        } finally {
            channel.shutdown();
            if (!channel.awaitTermination(5, TimeUnit.SECONDS)) {
                channel.shutdownNow();
            }
        }
    }

    public void downloadFile(String remoteName) throws Exception {
        var channel = io.grpc.ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        try {
            ClientDownload downloader = new ClientDownload();
            String localFile = Paths.get(remoteName).getFileName().toString();
            downloader.downloadFile(FileServiceGrpc.newBlockingStub(channel), remoteName, localFile);
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

}
