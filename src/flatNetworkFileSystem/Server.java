//@author Julian Powell
package flatNetworkFileSystem;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        //final String STORAGE_PATH = "server_storage/"; // Directory to store files

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
                Response aResponse = new Response("","",null);
                switch (aRequest.getMethod()) {
                    case "add" -> {

                        // Read server file name and local file path from the client
                        String serverFileName = aRequest.getFileName();

                        // Check if the file already exists on the server
                        File serverFile = new File(serverFileName);
                        boolean fileExists = serverFile.exists();
                        if (fileExists) {
                            serverFile.delete();
                        }
                        try (FileOutputStream fos = new FileOutputStream(serverFileName)) {
                            FileInputStream fis = new FileInputStream(aRequest.getFileData());
                            byte[] buffer = new byte[1024];
                            int bytesRead;

                            while ((bytesRead = fis.read(buffer)) != -1) {
                                fos.write(buffer, 0, bytesRead);
                            }

                            if (fileExists) {
                                // Send a response to the client indicating whether the file existed and was overwritten
                                aResponse.setMessage("File " + aRequest.getFileName() + " was overwritten\nFile added successfully");
                            } else {
                                aResponse.setMessage("File added successfully");
                            }
                        } catch (IOException e) {
                            aResponse.setError("Error occurred while handling 'add' command: " + e.getMessage());
                        }
                    }
                    case "fetch" -> {
                        // Read server file name and local file path from the client
                        String serverFileName = aRequest.getFileName();

                        // Check if the file already exists on the server
                        File serverFile = new File(serverFileName);
                        boolean fileExists = serverFile.exists();

                        if (fileExists) {
                            // Read the file content from the server and write it to the client
                            try (FileInputStream fis = new FileInputStream(serverFileName)) {
                                FileOutputStream fos = new FileOutputStream(aResponse.getValue());
                                byte[] buffer = new byte[1024];
                                int bytesRead;

                                while ((bytesRead = fis.read(buffer)) != -1) {
                                    fos.write(buffer, 0, bytesRead);
                                }

                            }
                        } else {
                            // Respond to the client that the file doesn't exist
                            aResponse.setError("File '" + serverFileName + "' does not exist on the server.");
                        }

                    }
                    case "append" -> {
                        // Read server file name and local file path from the client
                        String serverFileName = aRequest.getFileName();

                        // Check if the file already exists on the server
                        File serverFile = new File(serverFileName);
                        boolean fileExists = serverFile.exists();

                        if (fileExists) {
                            // Read the file content to append from the client and append it to the server file
                            try (FileOutputStream fos = new FileOutputStream(serverFileName, true)) {
                                FileInputStream fis = new FileInputStream(aRequest.getFileData());
                                byte[] buffer = new byte[1024];
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
}
