package com.fileuploader;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: java -jar fileuploader.jar <upload/download> <file> <remoteHost(optional)>");
            return;
        }

        String mode = args[0];
        String path = args[1];
        String remoteHost = args.length > 2 ? args[2] : "localhost";
        FileClient client = new FileClient(remoteHost, 50051);

        if (mode.equalsIgnoreCase("upload")) {
            client.uploadFile(path);
        } else if (mode.equalsIgnoreCase("download")) {
            client.downloadFile(path);
        } else {
            System.out.println("Unknown mode: " + mode);
            return;
        }

    }
}
