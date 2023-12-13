//@author Julian Powell
package flatNetworkFileSystem;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Server {
    private static final int TIMEOUT_MS = 10000; // 10 seconds timeout
    public static void main(String[] args) throws IOException, ClassNotFoundException, SocketTimeoutException, InterruptedException {
        // Create the server socket to accept connections 50702 : 50900
        ServerSocket serverSocket = new ServerSocket(50900);

        while (true) {
        System.out.println("Allowing connections");

        Socket aSocket = serverSocket.accept();
        //Thread.sleep(11000);
        //handleTimer(aSocket);

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
        } catch (IOException | NoSuchAlgorithmException e) {
            // Handle the IOException that occurs when the client disconnects
           e.printStackTrace();
        }
        }
        }
    public static String STORAGE_PATH = "/home/lynchburg.edu/wisej797/"; // Directory to store files
    //public static String STORAGE_PATH = "C:\\Users\\jacks\\Downloads\\"; // Directory to store files
    //public static String STORAGE_PATH = "C:\\Users\\powellj387\\Downloads\\";

    private static void handleTimer(Socket socket) throws SocketException {
        socket.setSoTimeout(TIMEOUT_MS);
    }
    private static void handleAdd(Request aRequest, Response aResponse, ObjectInputStream in) throws IOException, NoSuchAlgorithmException {
        // Read server file name and local file path from the client
        String serverFileName = aRequest.getFileName();

        // Check if the file already exists on the server
        File serverFile = new File(STORAGE_PATH+serverFileName);
        boolean fileExists = serverFile.exists();

        MessageDigest checkSum = MessageDigest.getInstance("SHA-256");

        //Download the data to the server
        try (FileOutputStream fos = new FileOutputStream(STORAGE_PATH+serverFileName)) {
            byte[] buffer = new byte[2048];
            long bytesRead = 0;
            long totalBytes = aRequest.getFileSize();

            while (bytesRead != totalBytes) {
                long bytesReadInThisIteration = in.read(buffer);
                checkSum.update(buffer, 0, (int)bytesReadInThisIteration);
                fos.write(buffer, 0, (int)bytesReadInThisIteration);
                bytesRead += bytesReadInThisIteration;
            }

            byte[] checkSumCodeServer = checkSum.digest();

            long checkSumLength = in.readLong();

            byte[] checkSumCodeClient = new byte[(int)checkSumLength];
            in.read(checkSumCodeClient);

            if (Arrays.equals(checkSumCodeClient, checkSumCodeServer)) {
                if (fileExists) {
                    // Send a response to the client indicating whether the file existed and was overwritten
                    aResponse.setMessage("File " + aRequest.getFileName() + " was overwritten, File added successfully");
                } else {
                    aResponse.setMessage("File added successfully");
                }
            }
            else{
                aResponse.setError("File data was corrupted");
                serverFile.delete();
            }
        } catch (IOException e) {
            aResponse.setError("Error occurred while handling 'add' command: " + e.getMessage());
        }
    }

    public static void handleAppend(Request aRequest, Response aResponse, ObjectInputStream in) throws NoSuchAlgorithmException, IOException {
        // Read server file name and local file path from the client
        String serverFileName = aRequest.getFileName();

        // Check if the file already exists on the server
        File serverFile = new File(serverFileName);
        boolean fileExists = serverFile.exists();

        String[] serverFileNameSplit = serverFileName.split("\\.");
        Path tempFile = Files.createTempFile(serverFileNameSplit[0], "."+serverFileNameSplit[1]);
        Files.copy(Paths.get(STORAGE_PATH+aRequest.getFileName()), tempFile, StandardCopyOption.REPLACE_EXISTING);

        MessageDigest checkSum = MessageDigest.getInstance("SHA-256");

        if (fileExists) {
            // Read the file content to append from the client and append it to the server file
            try (FileOutputStream fos = new FileOutputStream(STORAGE_PATH+aRequest.getFileName(), true)) {

                byte[] buffer = new byte[2048];
                long bytesRead = 0;
                long totalBytes = aRequest.getFileSize();

                while (bytesRead != totalBytes) {
                    long bytesReadInThisIteration = in.read(buffer);
                    checkSum.update(buffer, 0, (int)bytesReadInThisIteration);
                    fos.write(buffer, 0, (int)bytesReadInThisIteration);
                    bytesRead += bytesReadInThisIteration;
                }

                byte[] checkSumCodeServer = checkSum.digest();

                long checkSumLength = in.readLong();

                byte[] checkSumCodeClient = new byte[(int)checkSumLength];
                in.read(checkSumCodeClient);
                if (Arrays.equals(checkSumCodeClient, checkSumCodeServer)) {
                    if (fileExists) {
                        // Send a response to the client indicating whether the file existed and was overwritten
                        aResponse.setMessage("File appended successfully. New length: " + serverFile.length());
                    }
                }
                else{
                    aResponse.setError("File data was corrupted");
                    Files.copy(tempFile, Paths.get(STORAGE_PATH+aRequest.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                }

            }catch (IOException e) {
                aResponse.setError("Error occurred while handling 'append' command: " + e.getMessage());
            }
        } else{
            aResponse.setError("File does not exist");
        }
    }
    public static void handleFetch(Request aRequest, Response aResponse, ObjectOutputStream out) throws NoSuchAlgorithmException {
        // Read server file name and local file path from the client
        String serverFileName = aRequest.getFileName();

        // Check if the file already exists on the server
        File serverFile = new File(STORAGE_PATH+serverFileName);
        long totalBytes = serverFile.length();
        boolean fileExists = serverFile.exists();

        MessageDigest checkSum = MessageDigest.getInstance("SHA-256");

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
                    checkSum.update(buffer, 0, (int) bytesReadInThisIteration);
                    out.write(buffer, 0, (int)bytesReadInThisIteration);
                    out.flush();
                    bytesRead += bytesReadInThisIteration;
                }

                // CheckSum
                byte[] checkSumCode = checkSum.digest();

                out.writeLong(checkSumCode.length);
                out.flush();

                out.write(checkSumCode);
                out.flush();

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
