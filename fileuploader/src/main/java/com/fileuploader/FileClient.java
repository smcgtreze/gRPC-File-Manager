package com.fileuploader;
import com.example.grpc.FileServiceGrpc;

import io.grpc.ManagedChannel;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class FileClient {
    private static final int TIMEOUT = 5;
    private final String host;
    private final int port;

    public FileClient(final String host, final int port) {
        this.host = host;
        this.port = port;
    }

    public void uploadFile(final String path) throws Exception {
        final ManagedChannel channel = buildChannel();
        try {
            ClientUpload uploader = new ClientUpload();
            uploader.uploadFile(FileServiceGrpc.newStub(channel), path);
        } finally {
            channel.shutdownNow().awaitTermination(TIMEOUT, TimeUnit.SECONDS);
        }
    }

    public void downloadFile(final String remoteName) throws Exception {
        final ManagedChannel channel = buildChannel();
        try {
            ClientDownload downloader = new ClientDownload();
            String localFile = "downloaded_" + Paths.get(remoteName).getFileName().toString();
            downloader.downloadFile( FileServiceGrpc.newBlockingStub(channel), remoteName, localFile );
        } finally {
            channel.shutdownNow().awaitTermination(TIMEOUT, TimeUnit.SECONDS);
        }
    }

    private ManagedChannel buildChannel() {
        return io.grpc.ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
    }

}
