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

        HashMap<String, File> fileServer = new HashMap<>();

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

                        if (fileServer.containsKey(aRequest.getFileName())) {
                            fileServer.remove(aRequest.getFileName()); // If a file with the same name exists, overwrite it.
                            aResponse.setMessage("File " + aRequest.getFileName() + " was overwritten\nFile added successfully");
                        }
                        else{
                            aResponse.setMessage("File added successfully");
                        }


                        File fileData = aRequest.getFileData();
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
