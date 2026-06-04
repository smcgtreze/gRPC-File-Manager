package com.fileuploader;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.example.grpc.FileServiceGrpc;
import com.google.protobuf.ByteString;
import com.example.grpc.FileChunk;
import com.example.grpc.UploadStatus;
import io.grpc.stub.StreamObserver;

public class ClientUpload {

    private static final int BUFFER_SIZE = 64 * 1024;
    private static final int TIMEOUT = 10;
    private static final Path UPLOADS_DIR = Paths.get("uploads");

    public void uploadFile(FileServiceGrpc.FileServiceStub asyncStub, String path) throws Exception {
        CountDownLatch finishLatch = new CountDownLatch(1);
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        StreamObserver<UploadStatus> responseObserver = buildResponseObserver(finishLatch, errorRef);
        StreamObserver<FileChunk> requestObserver = asyncStub.upload(responseObserver);

        try{
            uploadFile(path, requestObserver);
        } catch (Exception exception) {
            requestObserver.onError(exception);
            throw exception;
        }
        requestObserver.onCompleted();

        if (!finishLatch.await(TIMEOUT, TimeUnit.MINUTES)) {
            throw new RuntimeException("Upload did not complete within " + TIMEOUT + " minutes. Aborting...");
        }
        if (errorRef.get() != null) {
            throw new RuntimeException("Upload failed", errorRef.get());
        }
    }

    private StreamObserver<UploadStatus> buildResponseObserver(CountDownLatch finishLatch,
            AtomicReference<Throwable> errorRef) {
        return new StreamObserver<>() {
            @Override public void onNext(UploadStatus status) {
                System.out.println("Server: " + status.getMessage());
            }
            @Override public void onError(Throwable t) {
                errorRef.set(t);
                finishLatch.countDown();
            }
            @Override public void onCompleted() {
                System.out.println("Upload finished.");
                finishLatch.countDown();
            }
        };
    }
    
    final String resolveFilePath(String path) {
        return UPLOADS_DIR.resolve(new File(path).getName()).toString();
    }

    void uploadFile( final String path, final StreamObserver<FileChunk> requestObserver) throws Exception {
        InputStream inputStream = new FileInputStream(path);
        
        byte[] buffer = new byte[BUFFER_SIZE];
        boolean first = true;
        int bytesRead;
        String outputPath = resolveFilePath(path);
            
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            FileChunk chunk = FileChunk.newBuilder()
                    .setData(ByteString.copyFrom(buffer, 0, bytesRead))
                    .setFilename(first ? outputPath : "")
                    .build();
            first = false;
            requestObserver.onNext(chunk);
        }
        inputStream.close();
    }
}
