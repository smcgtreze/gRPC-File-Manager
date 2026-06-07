package com.fileuploader;
import com.example.grpc.FileServiceGrpc;

import io.grpc.ManagedChannel;

import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class FileClient {
    private static final int TIMEOUT = 5;
    private static final long MAX_FILE_SIZE = 8L * 1024 * 1024 * 1024; // 8 GB
    private final String host;
    private final int port;

    public FileClient(final String host, final int port) {
        this.host = host;
        this.port = port;
    }

    public void uploadFile(final String path) throws Exception {
        validateFileSize(path, "upload");
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

    private void validateFileSize(final String path, final String operation) throws IllegalArgumentException {
        File file = new File(path);
        if (!file.exists()) {
            throw new IllegalArgumentException("File not found: " + path);
        }
        long fileSize = file.length();
        if (fileSize > MAX_FILE_SIZE) {
            long fileSizeGB = fileSize / (1024 * 1024 * 1024);
            throw new IllegalArgumentException(
                String.format("Cannot %s file: %s is %d GB, exceeds maximum allowed size of 8 GB", 
                    operation, path, fileSizeGB)
            );
        }
    }

    private ManagedChannel buildChannel() {
        return io.grpc.ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
    }

}
