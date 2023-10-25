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

        public void add(String fileName, String localPath) throws IOException {
            File file = new File(localPath+fileName);
            byte[] fileData = Files.readAllBytes(file.toPath());

            Request request = new Request("add",fileName,fileData);
            Response response =  sendRequest(request);
            if (!Objects.equals(response.getMessage(), "")) {
                System.out.println("Response: " + response.getMessage());
            } else {
                System.out.println("Error: " + response.getError());
            }
        }

        public void fetch(String fileName, String localPath) throws IOException {

            File file = new File(localPath+fileName);
            byte[] fileData = Files.readAllBytes(file.toPath());

            Request request = new Request("fetch", fileName, fileData);
            Response response = sendRequest(request);

            // Downloads the file

            if (!Objects.equals(response.getMessage(), "")) {
                System.out.println("Response: " + response.getMessage());
            } else {
                System.out.println("Error: " + response.getError());
            }
        }

        public void append(String fileName, String localPath) throws IOException {

            File accessedFile = new File(localPath+fileName);
            byte[] byteBuffer = Files.readAllBytes(accessedFile.toPath());

            Request request = new Request("append", fileName, byteBuffer);
            Response response = sendRequest(request);

            if (!Objects.equals(response.getMessage(), "")) {
                System.out.println("Response: " + response.getMessage());
            } else {
                System.out.println("Error: " + response.getError());
            }
        }

        public static void main(String[] args) throws IOException {
            Client client = new Client("localhost", 50702);

            Scanner scan = new Scanner(System.in);
            PrintStream out = new PrintStream(System.out);

            client.add("bunny.jpg", "C:\\Users\\jacks\\Downloads\\");
            client.fetch("bunny.jpg", "\\Users\\jacks\\Downloads\\");
        }
    }
