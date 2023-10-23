//@author Julian Powell
package flatNetworkFileSystem;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Server {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        final String STORAGE_PATH = "server_storage/"; // Directory to store files

        // Create the server socket to accept connections
        ServerSocket serverSocket = new ServerSocket(50702);

        while (true) {
        System.out.println("Allowing connections");
        Socket aSocket = serverSocket.accept();

        ObjectOutputStream out = new ObjectOutputStream(aSocket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(aSocket.getInputStream());

        try {
            while (true) {
                Object request = in.readObject();
                Request aRequest = (Request) request;
                Response aResponse = new Response("","","");
                switch (aRequest.getMethod()) {
                    case "add":
                        File serverFile = new File(STORAGE_PATH + aRequest.getFileName());

                        if (serverFile.exists()) {
                            serverFile.delete(); // If a file with the same name exists, overwrite it.
                        }

                        try (InputStream localFileStream = new FileInputStream(aRequest.getLocalPath());
                             OutputStream serverFileStream = new FileOutputStream(serverFile)) {
                            byte[] buffer = new byte[64 * 1024]; // 64KB buffer
                            int bytesRead;

                            while ((bytesRead = localFileStream.read(buffer)) != -1) {
                                serverFileStream.write(buffer, 0, bytesRead);
                            }
                        }
                        break;

                    case "fetch":
                       serverFile = new File(STORAGE_PATH + aRequest.getFileName());

                        if (!serverFile.exists()) {
                            // Server should return an error if the file does not exist
                            aResponse = new Response("fetch", "File not found", "");
                            out.writeObject(aResponse);
                            return;
                        }

                        try (InputStream serverFileStream = new FileInputStream(serverFile);
                             OutputStream localFileStream = new FileOutputStream(aRequest.getLocalPath())) {
                            byte[] buffer = new byte[64 * 1024]; // 64KB buffer for efficient file transfer
                            int bytesRead;

                            while ((bytesRead = serverFileStream.read(buffer)) != -1) {
                                localFileStream.write(buffer, 0, bytesRead);
                            }
                        }
                        break;

                    case "append":
                        serverFile = new File(STORAGE_PATH + aRequest.getFileName());

                        if (!serverFile.exists()) {
                            // Server should return an error if the file does not exist
                            aResponse = new Response("append", "File not found", "");
                            out.writeObject(aResponse);
                            return;
                        }

                        try (InputStream localFileStream = new FileInputStream(aRequest.getLocalPath());
                             OutputStream serverFileStream = new FileOutputStream(serverFile, true)) {
                            byte[] buffer = new byte[64 * 1024]; // 64KB buffer for efficient file transfer
                            int bytesRead;

                            while ((bytesRead = localFileStream.read(buffer)) != -1) {
                                serverFileStream.write(buffer, 0, bytesRead);
                            }
                        }
                        // Server response to a successful append including the new file size
                        aResponse = new Response("append", "File appended successfully", String.valueOf(serverFile.length()));
                        out.writeObject(aResponse);
                        break;

                    case "exit":
                        // Optionally, you can add an "exit" command to close the connection
                        out.close();
                        in.close();
                        aSocket.close();
                        return; // Exit the loop

                    default:
                        aResponse.setError("Invalid method");
                        break;
                }
                out.writeObject(aResponse);
            }
        } catch (IOException e) {
            // Handle the IOException that occurs when the client disconnects
            e.printStackTrace();
        }
        }
    }
}
