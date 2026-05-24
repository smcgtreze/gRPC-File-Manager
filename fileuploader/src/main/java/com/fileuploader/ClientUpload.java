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

    private static final Path UPLOADS_DIR = Paths.get("uploads");

    public void uploadFile(FileServiceGrpc.FileServiceStub asyncStub, String path) throws Exception {
        CountDownLatch finishLatch = new CountDownLatch(1);
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        StreamObserver<UploadStatus> responseObserver = new StreamObserver<>() {
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

        StreamObserver<FileChunk> requestObserver = asyncStub.upload(responseObserver);

    try (InputStream in = new FileInputStream(path)) {
            byte[] buffer = new byte[64 * 1024];
            boolean first = true;
            int n;
            String outputPath = UPLOADS_DIR.resolve(new File(path).getName()).toString();
            while ((n = in.read(buffer)) != -1) {
                FileChunk chunk = FileChunk.newBuilder()
                        .setData(ByteString.copyFrom(buffer, 0, n))
                        .setFilename(first ? outputPath : "")
                        .build();
                first = false;
                requestObserver.onNext(chunk);
            }
        }
    requestObserver.onCompleted();

        if (!finishLatch.await(10, TimeUnit.MINUTES)) {
            throw new RuntimeException("Upload did not complete within 1 minute");
        }
        if (errorRef.get() != null) {
            throw new RuntimeException("Upload failed", errorRef.get());
        }
    }

    
}
