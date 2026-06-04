package com.fileuploader;

public class Main {
    private static final int PORT = 50051;
    private static final String UPLOAD = "upload";
    private static final String DOWNLOAD = "download";

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: java -jar fileuploader.jar <upload/download> <file> <remoteHost(optional)>");
            return;
        }

        final String mode = args[0];
        final String path = args[1];
        final String remoteHost = args.length > 2 ? args[2] : "localhost";
        final FileClient client = new FileClient(remoteHost, PORT);

        if (mode.equalsIgnoreCase(UPLOAD)) {
            client.uploadFile(path);
        } else if (mode.equalsIgnoreCase(DOWNLOAD)) {
            client.downloadFile(path);
        } else {
            System.out.println("Unknown mode: " + mode);
            return;
        }

    }
}
