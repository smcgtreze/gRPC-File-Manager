package com.fileuploader;

import java.io.IOException;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import com.google.protobuf.ByteString;
import com.example.grpc.FileServiceGrpc;
import com.example.grpc.FileChunk;
import com.example.grpc.FileRequest;
import com.example.grpc.UploadStatus;

public class FileServiceImpl extends FileServiceGrpc.FileServiceImplBase {

    @Override
    public StreamObserver<FileChunk> upload(StreamObserver<UploadStatus> responseObserver) {

        return new StreamObserver<>() {
            OutputStream out;
            String filename;

            @Override
            public void onNext(FileChunk chunk) {
                try {
                    if (out == null) {
                        filename = chunk.getFilename();
                        var file = new java.io.File(filename);
                        var parent = file.getParentFile();
                        if (parent != null && !parent.exists()) {
                            parent.mkdirs();
                        }
                        out = new BufferedOutputStream(new FileOutputStream(file));
                    }
                    out.write(chunk.getData().toByteArray());
                } catch (IOException e) {
                    responseObserver.onError(Status.INTERNAL
                            .withDescription("Failed to write uploaded file: " + e.getMessage())
                            .withCause(e)
                            .asRuntimeException());
                }
            }

            @Override
            public void onError(Throwable t) {
                closeQuietly();
            }

            @Override
            public void onCompleted() {
                closeQuietly();
                UploadStatus status = UploadStatus.newBuilder()
                        .setSuccess(true)
                        .setMessage("Upload complete")
                        .build();
                responseObserver.onNext(status);
                responseObserver.onCompleted();
            }

            private void closeQuietly() {
                try { if (out != null) out.close(); } catch (IOException ignored) {}
            }
        };
    }

    @Override
    public void download(FileRequest request, StreamObserver<FileChunk> responseObserver) {
        try (InputStream in = new FileInputStream(request.getFilename())) {
            byte[] buffer = new byte[64 * 1024];
            int n;
            while ((n = in.read(buffer)) != -1) {
                FileChunk chunk = FileChunk.newBuilder()
                        .setData(ByteString.copyFrom(buffer, 0, n))
                        .build();
                responseObserver.onNext(chunk);
            }
            responseObserver.onCompleted();
        } catch (IOException e) {
            responseObserver.onError(e);
        }
    }
}
