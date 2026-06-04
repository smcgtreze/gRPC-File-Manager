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
                         final String remoteName, final String localPath) throws Exception {

        final FileRequest fileRequest = FileRequest.newBuilder()
                .setFilename(remoteName)
                .setPath(DOWNLOAD_DIR.toString())
                .build();

        final Iterator<FileChunk> chunksIt = blockingStub.download(fileRequest);

        try {
            downloadFile(localPath, chunksIt);
        } catch (Exception e) {
            throw new RuntimeException("Download failed due to " + e.getMessage(), e);
        }
    }

    private Path resolveFilePath(final String localPath) {
        return DOWNLOAD_DIR.resolve(localPath);
    }
    
    private void downloadFile( final String localPath, final Iterator<FileChunk> chunksIt) throws Exception {
        Path outputPath = resolveFilePath(localPath);
        if (outputPath.getParent() != null) {
            Files.createDirectories(outputPath.getParent());
        }
        
        OutputStream out = Files.newOutputStream(outputPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        while (chunksIt.hasNext()) {
            FileChunk chunk = chunksIt.next();
            out.write(chunk.getData().toByteArray());
        }
        out.close();
    }

    
}
