//@author Julian Powell
package flatNetworkFileSystem;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class Server {
    private static final int TIMEOUT_MS = 10000; // 10 seconds timeout
    public static void main(String[] args) throws IOException, ClassNotFoundException, SocketTimeoutException, InterruptedException {
        // Create the server socket to accept connections 50702 : 50900
        ServerSocket serverSocket = new ServerSocket(50900);

        while (true) {
        System.out.println("Allowing connections");

        Socket aSocket = serverSocket.accept();
        Thread.sleep(11000);
        handleTimer(aSocket);

        ObjectOutputStream out = new ObjectOutputStream(aSocket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(aSocket.getInputStream());

        try {

            while (true) {
                Object request = in.readObject();
                Request aRequest = (Request) request;
                Response aResponse = new Response("","",0);
                switch (aRequest.getMethod()) {
                    case "add" -> {
                        handleAdd(aRequest, aResponse, in);
                    }
                    case "fetch" -> {
                        handleFetch(aRequest, aResponse, out);
                    }
                    case "append" -> {
                        handleAppend(aRequest, aResponse, in);
                    }
                    case "quit" -> {
                        out.close();
                        in.close();
                        aSocket.close();
                        return; // Exit the loop
                    }
                    default -> aResponse.setError("Invalid method");
                }

                out.writeObject(aResponse);
            }
        } catch (IOException e) {
            // Handle the IOException that occurs when the client disconnects
           e.printStackTrace();
        }
        }
        }
//    public static String STORAGE_PATH = "/home/lynchburg.edu/wisej797/"; // Directory to store files
    public static String STORAGE_PATH = "C:\\Users\\powellj387\\Downloads\\";

    private static void handleTimer(Socket socket) throws SocketException {
        socket.setSoTimeout(TIMEOUT_MS);
    }
    private static void handleAdd(Request aRequest, Response aResponse, ObjectInputStream in) throws IOException {
        // Read server file name and local file path from the client
        String serverFileName = aRequest.getFileName();

        // Check if the file already exists on the server
        File serverFile = new File(STORAGE_PATH+serverFileName);
        boolean fileExists = serverFile.exists();

        //Download the data to the server
        try (FileOutputStream fos = new FileOutputStream(serverFile)) {
            byte[] buffer = new byte[2048];
            long bytesRead = 0;
            long totalBytes = aRequest.getFileSize();

            while (bytesRead != totalBytes) {
                long bytesReadInThisIteration = in.read(buffer);
                fos.write(buffer, 0, (int)bytesReadInThisIteration);
                bytesRead += bytesReadInThisIteration;
            }

            if (fileExists) {
                // Send a response to the client indicating whether the file existed and was overwritten
                aResponse.setMessage("File " + aRequest.getFileName() + " was overwritten, File added successfully");
            } else {
                aResponse.setMessage("File added successfully");
            }
        } catch (IOException e) {
            aResponse.setError("Error occurred while handling 'add' command: " + e.getMessage());
        }
    }

    public static void handleAppend(Request aRequest, Response aResponse, ObjectInputStream in){
        // Read server file name and local file path from the client
        String serverFileName = aRequest.getFileName();

        // Check if the file already exists on the server
        File serverFile = new File(serverFileName);
        boolean fileExists = serverFile.exists();

        if (fileExists) {
            // Read the file content to append from the client and append it to the server file
            try (FileOutputStream fos = new FileOutputStream(STORAGE_PATH+serverFileName, true)) {

                byte[] buffer = new byte[2048];
                long bytesRead = 0;
                long totalBytes = aRequest.getFileSize();

                while (bytesRead != totalBytes) {
                    long bytesReadInThisIteration = in.read(buffer);
                    fos.write(buffer);
                    bytesRead += bytesReadInThisIteration;
                }

            }catch (IOException e) {
                aResponse.setError("Error occurred while handling 'append' command: " + e.getMessage());
            }
            aResponse.setMessage("File appended successfully. New length: " + serverFile.length());
        } else{
            aResponse.setError("File does not exist");
        }
    }
    public static void handleFetch(Request aRequest, Response aResponse, ObjectOutputStream out) {
        // Read server file name and local file path from the client
        String serverFileName = aRequest.getFileName();

        // Check if the file already exists on the server
        File serverFile = new File(STORAGE_PATH+serverFileName);
        long totalBytes = serverFile.length();
        boolean fileExists = serverFile.exists();

        try (FileInputStream fis = new FileInputStream(STORAGE_PATH+serverFileName)) {
            if (fileExists) {
                // Read the file content from the server and write it to the client
                // FileOutputStream fos = new FileOutputStream(serverFile);
                byte[] buffer = new byte[2048];
                long bytesRead = 0;
                out.writeLong(totalBytes);

                aResponse.setValue(totalBytes);

                while (bytesRead != totalBytes) {
                    long bytesReadInThisIteration = fis.read(buffer);
                    out.write(buffer, 0, (int)bytesReadInThisIteration);
                    bytesRead += bytesReadInThisIteration;
                }

                aResponse.setMessage("Fetch successful, file has been saved");
            } else {
                // Respond to the client that the file doesn't exist
                aResponse.setError("File '" + serverFileName + "' does not exist on the server.");
            }
        } catch (IOException e) {
            aResponse.setError("Error occurred while handling 'add' command: " + e.getMessage());
        }
    }
}
