//@author Julian Powell
package flatNetworkFileSystem;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {
    public static void main(String[] args) throws IOException, ClassNotFoundException {

        // Create the server socket to accept connections
        ServerSocket serverSocket = new ServerSocket(50900);

        while (true) {
        System.out.println("Allowing connections");
        Socket aSocket = serverSocket.accept();

        ObjectOutputStream out = new ObjectOutputStream(aSocket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(aSocket.getInputStream());

        try {
            while (true) {
                Object request = in.readObject();
                Request aRequest = (Request) request;
                Response aResponse = new Response("","",null);
                switch (aRequest.getMethod()) {
                    case "add" -> {
                        handleAdd(aRequest, in, out);
                        out.writeObject("aResponse");
                    }

                    case "fetch" -> {
                        handleFetch(aRequest, aResponse, out);
                    }
                    case "append" -> {
                        handleAppend(aRequest, aResponse);
                    }
                    case "exit" -> {
                        // Optionally, you can add an "exit" command to close the connection
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
    public static String STORAGE_PATH = "/home/lynchburg.edu/wisej797/"; // Directory to store files
    //public static String STORAGE_PATH = "C:\\Users\\jacks\\Downloads\\";
    private static void handleAdd(Request aRequest, ObjectInputStream in, ObjectOutputStream out) throws IOException {
        // Read server file name and local file path from the client
        String serverFileName = aRequest.getFileName();

        // Check if the file already exists on the server
        File serverFile = new File(STORAGE_PATH+serverFileName);
        boolean fileExists = serverFile.exists();
        if (fileExists) {
            //serverFile.delete();
        }

        //Download the data to the server
        try (FileOutputStream fos = new FileOutputStream(serverFile)) {
            //FileInputStream fis = new FileInputStream(aRequest.getFileData());
            byte[] buffer = new byte[2048];
            long bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) {
                fos.write(buffer, 0, (int)bytesRead);
            }
            /*

            if (fileExists) {
                // Send a response to the client indicating whether the file existed and was overwritten
                out.writeObject("File " + aRequest.getFileName() + " was overwritten, File added successfully");
            } else {
                out.writeObject("File added successfully");
            }
            */
        } catch (IOException e) {
            out.writeObject("Error occurred while handling 'add' command: " + e.getMessage());
        }
    }

    public static void handleAppend(Request aRequest, Response aResponse){
        // Read server file name and local file path from the client
        String serverFileName = aRequest.getFileName();

        // Check if the file already exists on the server
        File serverFile = new File(serverFileName);
        boolean fileExists = serverFile.exists();

        if (fileExists) {
            // Read the file content to append from the client and append it to the server file
            try (FileOutputStream fos = new FileOutputStream(serverFileName, true)) {

                InputStream fis = new FileInputStream(aRequest.getFileData());

                byte[] buffer = new byte[64*1024];
                int bytesRead;

                while ((bytesRead = fis.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }catch (IOException e) {
                aResponse.setError("Error occurred while handling 'append' command: " + e.getMessage());
            }
            aResponse.setMessage("File appended successfully. New length: "+serverFile.length());
        } else{
            aResponse.setError("File does not exist");
        }
    }
    public static void handleFetch(Request aRequest, Response aResponse, ObjectOutputStream out) {
        // Read server file name and local file path from the client
        String serverFileName = aRequest.getFileName();

        // Check if the file already exists on the server
        File serverFile = new File(serverFileName);
        boolean fileExists = serverFile.exists();

        try (FileInputStream fis = new FileInputStream(serverFileName)) {
            if (fileExists) {
                // Read the file content from the server and write it to the client
                // FileOutputStream fos = new FileOutputStream(serverFile);
                byte[] buffer = new byte[64 * 1024];
                int bytesRead;

                while ((bytesRead = fis.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }

                aResponse.setValue(serverFile);

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
