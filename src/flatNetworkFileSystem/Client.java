package flatNetworkFileSystem;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Scanner;

public class Client {
        private Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;

        public Client(String host, int port) throws IOException {
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        }

        public Response sendRequest(Request request) {
            try {
                out.writeObject(request);
                return (Response) in.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }

        public void add(String serverFileName, String localFilePath) throws IOException {
            File file = new File(localFilePath);
            //File fileData = Files.readAllBytes(file.toPath());

            Request request = new Request("add",serverFileName,file);
            Response response =  sendRequest(request);
            if (!Objects.equals(response.getMessage(), "")) {
                System.out.println("Response: " + response.getMessage());
            } else {
                System.out.println("Error: " + response.getError());
            }
        }

        public void fetch(String serverFileName, String localFilePath) throws IOException {

            File file = new File(localFilePath);
            //File fileData = Files.readAllBytes(file.toPath());

            Request request = new Request("fetch", serverFileName, file);
            Response response = sendRequest(request);

            // Downloads the file
            try(OutputStream fos = new FileOutputStream(response.getValue())){ }

            if (!Objects.equals(response.getMessage(), "")) {
                System.out.println("Response: " + response.getMessage());
            } else {
                System.out.println("Error: " + response.getError());
            }
        }

        public void append(String serverFileName, String localFilePath) throws IOException {
            File file = new File(localFilePath);
            //File fileData = Files.readAllBytes(file.toPath());

            Request request = new Request("append",serverFileName,file);

            Response response =  sendRequest(request);
            if (!Objects.equals(response.getMessage(), "")) {
                System.out.println("Response: " + response.getMessage());
            } else {
                System.out.println("Error: " + response.getError());
            }
            /*File accessedFile = new File(localFilePath);
            //File byteBuffer = Files.readAllBytes(accessedFile.toPath());

            Request request = new Request("append", serverFileName, accessedFile);
            Response response = sendRequest(request);

            try(FileOutputStream targetOutputStream = new FileOutputStream(response.getValue())) {
                long fileSize = response.getValue().length();
                byte[] buffer = new byte[64 * 1024];
                int bytesRead;
                while (fileSize > 0 && (bytesRead = in.read(buffer)) != -1) {
                    targetOutputStream.write(buffer, 0, bytesRead);
                    fileSize -= bytesRead;
                }
                targetOutputStream.close();
            }

            if (!Objects.equals(response.getMessage(), "")) {
                System.out.println("Response: " + response.getMessage());
            } else {
                System.out.println("Error: " + response.getError());
            }*/
        }

        public static void main(String[] args) throws IOException {
            Client client = new Client("localhost", 50702);

            Scanner scan = new Scanner(System.in);
            PrintStream out = new PrintStream(System.out);

            client.add("bunny", "C:\\Users\\jacks\\Downloads\\bunny.jpg");
            client.fetch("bunny", "\\Users\\jacks\\Downloads\\bunny.jpg");
            client.add("bunny-2", "C:\\Users\\jacks\\Downloads\\bunny-2.jpg");
            //client.append("bunny", "C:\\Users\\jacks\\Downloads\\bunny-2.jpg");
            client.fetch("bunny-2", "\\Users\\jacks\\Downloads\\bunny-2.jpg");
        }
    }
