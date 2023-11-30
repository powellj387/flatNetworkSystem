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

        public void sendRequest(Request request) {
            try {
                out.writeObject(request);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void add(String serverFileName, String localFilePath) throws IOException {
            File file = new File("C:\\Users\\jacks\\Downloads\\bunny.jpg");
           // byte[] fileData = Files.readAllBytes(file.toPath());

            Request request = new Request("add",serverFileName,file);
            sendRequest(request);


            //Send the file data over
            try (FileInputStream fis = new FileInputStream(localFilePath)){
                byte[] buffer = new byte[2048];
                long bytesRead = 0;

                while((bytesRead =fis.read(buffer)) != -1){
                    out.write(buffer, 0, (int)bytesRead);
                }
            }
            System.out.println("Test");

            out.flush();

            /*
            String response = null;
            try {
                response = (String)in.readObject();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            if (!Objects.equals(response, "")) {
                System.out.println("Response: " + response);
            } else {
                System.out.println("Error: " + response);
            }
            */
        }

        public void fetch(String serverFileName, String localFilePath) throws IOException {

            File file = new File(localFilePath);
            //byte[] fileData = Files.readAllBytes(file.toPath());

            Request request = new Request("fetch", serverFileName, file);
            sendRequest(request);

            // Downloads the file
            try(OutputStream fos = new FileOutputStream(localFilePath)){
                byte[] buffer = new byte[64*1024];
                long bytesRead = 0;

                while ((bytesRead = in.read(buffer)) != -1) {
                    fos.write(buffer, 0, (int) bytesRead);
                }
            }

            Response response = null;
            try {
                response = (Response)in.readObject();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            if (!Objects.equals(response.getMessage(), "")) {
                System.out.println("Response: " + response.getMessage());
            } else {
                System.out.println("Error: " + response.getError());
            }
        }

        public void append(String serverFileName, String localFilePath) throws IOException {

            File accessedFile = new File(localFilePath);
            //byte[] byteBuffer = Files.readAllBytes(accessedFile.toPath());

            Request request = new Request("append", serverFileName, accessedFile);
            sendRequest(request);

            Response response = null;
            try {
                response = (Response)in.readObject();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            if (!Objects.equals(response.getMessage(), "")) {
                System.out.println("Response: " + response.getMessage());
            } else {
                System.out.println("Error: " + response.getError());
            }
        }

        public static void main(String[] args) throws IOException {
            Client client = new Client("pie.lynchburg.edu", 50900);

            Scanner scan = new Scanner(System.in);
            PrintStream out = new PrintStream(System.out);

            client.add("bunny.jpg","C:\\Users\\jacks\\Downloads\\bunny.jpg");
            client.add("alice.txt", "C:\\Users\\jacks\\Downloads\\alice.txt");
           // client.append("alice", "C:\\Users\\powellj387\\Downloads\\alice (1).txt");

            //client.fetch("alice","C:\\Users\\powellj387\\Downloads\\aliceFetched.txt");
            //client.fetch( "bunny","C:\\Users\\powellj387\\Downloads\\bunnyFetched.jpg");
        }
    }
