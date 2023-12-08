package flatNetworkFileSystem;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Scanner;

public class Client {
        private Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private static final int TIMEOUT_MS = 10000; // 10 seconds timeout


    public Client(String host, int port) throws IOException {
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        }

        public void sendRequest(Request request) {
            try {
                socket.setSoTimeout(TIMEOUT_MS);
                out.writeObject(request);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void add(String serverFileName, String localFilePath) throws IOException {
            File file = new File(localFilePath);
            long totalBytes = file.length();

            Request request = new Request("add",serverFileName,totalBytes);
            sendRequest(request);

            //Send the file data over
            try (FileInputStream fis = new FileInputStream(localFilePath)){
                byte[] buffer = new byte[2048];
                long bytesRead = 0;

                while(bytesRead != totalBytes){
                    long bytesReadInThisIteration = fis.read(buffer);
                    out.write(buffer, 0, (int)bytesReadInThisIteration);
                    out.flush();
                    bytesRead += bytesReadInThisIteration;
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

        public void fetch(String serverFileName, String localFilePath) throws IOException {

            File file = new File(localFilePath);
            long totalBytes = file.length();

            Request request = new Request("fetch", serverFileName, totalBytes);
            sendRequest(request);

            // Downloads the file
            try(OutputStream fos = new FileOutputStream(localFilePath)){
                byte[] buffer = new byte[2048];
                long bytesRead = 0;
                totalBytes = in.readLong();

                while (bytesRead != totalBytes) {
                    long bytesReadInThisIteration = in.read(buffer);
                    fos.write(buffer, 0, (int)bytesReadInThisIteration);
                    bytesRead += bytesReadInThisIteration;
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

            File file = new File(localFilePath);
            long totalBytes = file.length();

            Request request = new Request("append",serverFileName,totalBytes);
            sendRequest(request);

            //Send the file data over
            try (FileInputStream fis = new FileInputStream(localFilePath)){
                byte[] buffer = new byte[2048];
                long bytesRead = 0;

                while(bytesRead != totalBytes){
                    long bytesReadInThisIteration = fis.read(buffer);
                    out.write(buffer, 0, (int)bytesReadInThisIteration);
                    out.flush();
                    bytesRead += bytesReadInThisIteration;
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

        public void quit() throws IOException {
            // Send an exit command to the server
            Request request = new Request("quit", "", 0);
            sendRequest(request);

            // Close resources
            out.close();
            in.close();
            socket.close();
        }

        public static void main(String[] args) throws IOException, InterruptedException {
            Client client = new Client("localhost", 50900);
            Scanner scan = new Scanner(System.in);
            PrintStream out = new PrintStream(System.out);

            client.add("bunny.jpg","C:\\Users\\powellj387\\Downloads\\alice.txt");

            //Thread.sleep(10000);

            //client.quit();
            //client.add("alice.txt", "C:\\Users\\powellj387\\Downloads\\alice.txt");
           // client.append("alice.txt", "C:\\Users\\jacks\\Downloads\\alice (1).txt");

            //client.fetch("alice.txt","C:\\Users\\jacks\\Downloads\\aliceFetched.txt");
            //client.fetch( "bunny.jpg","C:\\Users\\jacks\\Downloads\\bunnyFetched.jpg");*/
        }
    }
