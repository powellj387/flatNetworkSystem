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

        HashMap<String, byte[]> fileServer = new HashMap<>();

        while (true) {
        System.out.println("Allowing connections");
        Socket aSocket = serverSocket.accept();

        ObjectOutputStream out = new ObjectOutputStream(aSocket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(aSocket.getInputStream());

        try {
            while (true) {
                Object request = in.readObject();
                Request aRequest = (Request) request;
                Response aResponse = new Response("","",new byte[0]);
                switch (aRequest.getMethod()) {
                    case "add":
                        if (fileServer.containsKey(aRequest.getFileName())) {
                            fileServer.remove(aRequest.getFileName()); // If a file with the same name exists, overwrite it.
                            aResponse.setMessage("File " + aRequest.getFileName() + " was overwritten\nFile added successfully");
                        }
                        else{
                            aResponse.setMessage("File added successfully");
                        }


                        byte[] fileData = aRequest.getFileData();
                        fileServer.put(aRequest.getFileName(), fileData);

                        break;

                    case "fetch":
                        if (fileServer.containsKey(aRequest.getFileName())){
                            fileData = fileServer.get(aRequest.getFileName());
                            aResponse.setValue(fileData);
                            aResponse.setMessage("File retrieved successfully");
                        } else{
                           aResponse.setError("Error: File does not exist");
                        }
                        break;

                    case "append":
                        if(fileServer.containsKey(aRequest.getFileName())){
                            byte[] originalFileData = fileServer.get(aRequest.getFileName());
                            byte[] fileDataToAppend = aRequest.getFileData();
                            byte[] newFileData = new byte[originalFileData.length + fileDataToAppend.length];

                            System.arraycopy(originalFileData, 0, newFileData, 0, originalFileData.length);
                            System.arraycopy(fileDataToAppend, 0, newFileData, fileDataToAppend.length, newFileData.length);
                            aResponse.setValue(newFileData);

                            aResponse.setMessage("Data appended successfully. Size: " + newFileData.length);
                        } else{
                            aResponse.setError("Error: File does not exist");
                        }

                        /*//serverFile = new File(STORAGE_PATH + aRequest.getFileName());
                        String fileName = aRequest.getFileName();

                        if (!fileServer.containsKey(fileName)) {
                            // Server should return an error if the file does not exist
                            aResponse.setError("File not found");
                            out.writeObject(aResponse);
                            return;
                        }

                        //try (){

                            byte[] temp = new byte[64];
                            byte[] originalFile = fileServer.get(fileName);
                            byte[] newData = new byte[originalFile.length + temp.length];

                            System.arraycopy(originalFile, 0, newData, 0, newData.length);
                            System.arraycopy(temp, 0, newData, originalFile.length, temp.length);

                            fileServer.put(fileName, newData);
                        //}
                        // Server response to a successful append including the new file size
                        aResponse.setMessage("File appended successfully");*/
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
