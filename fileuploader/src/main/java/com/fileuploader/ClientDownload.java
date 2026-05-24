package com.fileuploader;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

import com.example.grpc.FileServiceGrpc;
import com.example.grpc.FileRequest;
import com.example.grpc.FileChunk;

public class ClientDownload {
    private static final Path DOWNLOAD_DIR = Paths.get("downloads");

    public void downloadFile(FileServiceGrpc.FileServiceBlockingStub blockingStub,
                         String remoteName, String localPath) throws Exception {

        FileRequest req = FileRequest.newBuilder()
                .setFilename(remoteName)
                .setPath(DOWNLOAD_DIR.toString())
                .build();

        Iterator<FileChunk> chunks = blockingStub.download(req);

        Path outputPath = DOWNLOAD_DIR.resolve(localPath);
        if (outputPath.getParent() != null) {
            Files.createDirectories(outputPath.getParent());
        }

        try (OutputStream out = Files.newOutputStream(outputPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            while (chunks.hasNext()) {
                FileChunk chunk = chunks.next();
                out.write(chunk.getData().toByteArray());
            }
        }
    }

    
}
