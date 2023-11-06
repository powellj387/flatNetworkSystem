//@author Julian Powell
package flatNetworkFileSystem;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

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
                Response aResponse = new Response("","",new File(""));
                switch (aRequest.getMethod()) {
                    case "add":
                        boolean isPresent = false;
                        //create a path for the file on the local computer
                        Path fileToAdd = Path.of(STORAGE_PATH+aRequest.getFileName());
                        //Delete the old one with the same name if it exists
                        isPresent = Files.deleteIfExists(fileToAdd);

                        //Make sure there is somewhere to put the file
                        Files.createDirectories(fileToAdd.getParent()); // Ensure parent directories exist

                        try (InputStream fileInputStream = new FileInputStream(aRequest.getFileData());
                             FileOutputStream fileOutputStream = new FileOutputStream(STORAGE_PATH + aRequest.getFileName())) {

                            byte[] buffer = new byte[1024];
                            int bytesRead;

                            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                                fileOutputStream.write(buffer, 0, bytesRead);
                            }
                            if(isPresent){
                                aResponse.setMessage("File " + aRequest.getFileName() + " overridden successfully");
                            }else{
                                aResponse.setMessage("File " + aRequest.getFileName() + " added successfully");
                            }
                        }catch (IOException e) {
                            e.printStackTrace();
                            aResponse.setError("Error while adding file");
                        }
                        break;

                    case "fetch":
                        Path fileToFetch = Path.of(STORAGE_PATH+aRequest.getFileName());
                        if (Files.exists(fileToFetch)) {
                            //
                            try (InputStream fileInputStream = new FileInputStream(STORAGE_PATH + aRequest.getFileName());
                                 FileOutputStream fileOutputStream = new FileOutputStream(dataFile) {

                                byte[] buffer = new byte[1024];
                                int bytesRead;

                                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                                    fileOutputStream.write(buffer, 0, bytesRead);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                aResponse.setError("Error while fetching");
                            }
                        }else{
                            aResponse.setError("File doesn't exist");
                        }
                        break;

                    case "append":
                        if (fileServer.containsKey(aRequest.getFileName())){
                            File originalFile = fileServer.get(aRequest.getFileName());
                            File fileToAppend = aRequest.getFileData();

                            if(originalFile != null && originalFile.exists()){
                                try (OutputStream fileOutput = new FileOutputStream(originalFile, true);
                                InputStream fileInput = new FileInputStream(fileToAppend)){
                                    byte[] buffer = new byte[64*1024];
                                    int bytesRead;
                                    while ((bytesRead = fileInput.read(buffer)) != -1){
                                        fileOutput.write(buffer, 0, bytesRead);
                                    }
                                }
                            }
                        } else{
                            aResponse.setError("File does not exist");
                        }
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
