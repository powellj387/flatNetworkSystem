package flatNetworkFileSystem;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
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

        public void add(String serverFileName, String localFilePath) throws IOException, NoSuchAlgorithmException {
            File file = new File(localFilePath);
            long totalBytes = file.length();

            Request request = new Request("add",serverFileName,totalBytes);
            sendRequest(request);

            MessageDigest checkSum = MessageDigest.getInstance("SHA-256");

            //Send the file data over
            try (FileInputStream fis = new FileInputStream(localFilePath)){
                byte[] buffer = new byte[2048];
                long bytesRead = 0;

                while (bytesRead != totalBytes) {
                    long bytesReadInThisIteration = fis.read(buffer);
                    checkSum.update(buffer, 0, (int) bytesReadInThisIteration);
                    out.write(buffer, 0, (int) bytesReadInThisIteration);
                    out.flush();
                    bytesRead += bytesReadInThisIteration;
                }

                // CheckSum
                byte[] checkSumCode = checkSum.digest();

                out.writeLong(checkSumCode.length);
                out.flush();

                out.write(checkSumCode);
                out.flush();
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

        public void fetch(String serverFileName, String localFilePath) throws IOException, NoSuchAlgorithmException {

            File file = new File(localFilePath);
            long totalBytes = file.length();

            Request request = new Request("fetch", serverFileName, totalBytes);
            sendRequest(request);

            MessageDigest checkSum = MessageDigest.getInstance("SHA-256");
            boolean isCorrupt = false;

            // Downloads the file
            try(OutputStream fos = new FileOutputStream(file)){
                byte[] buffer = new byte[2048];
                long bytesRead = 0;
                totalBytes = in.readLong();

                while (bytesRead != totalBytes) {
                    long bytesReadInThisIteration = in.read(buffer);
                    checkSum.update(buffer, 0, (int)bytesReadInThisIteration - 1);
                    fos.write(buffer, 0, (int)bytesReadInThisIteration);
                    bytesRead += bytesReadInThisIteration;
                }
                fos.close();

                byte[] checkSumCodeServer = checkSum.digest();

                long checkSumLength = in.readLong();

                byte[] checkSumCodeClient = new byte[(int)checkSumLength];
                in.read(checkSumCodeClient);

                if (!Arrays.equals(checkSumCodeClient, checkSumCodeServer)){
                    isCorrupt = true;
                    file.delete();
                }
            }

            Response response = null;
            try {
                response = (Response)in.readObject();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            if (!Objects.equals(response.getMessage(), "")) {
                if (!isCorrupt) {
                    System.out.println("Response: " + response.getMessage());
                }
                else{
                    System.out.println("Response: File data was corrupted");
                }
            } else {
                System.out.println("Error: " + response.getError());
            }
        }

        public void append(String serverFileName, String localFilePath) throws IOException, NoSuchAlgorithmException {

            File file = new File(localFilePath);
            long totalBytes = file.length();

            Request request = new Request("append",serverFileName,totalBytes);
            sendRequest(request);

            // Checksum
            MessageDigest checkSum = MessageDigest.getInstance("SHA-256");

            //Send the file data over
            try (FileInputStream fis = new FileInputStream(localFilePath)){
                byte[] buffer = new byte[2048];
                long bytesRead = 0;

                while(bytesRead != totalBytes){
                    long bytesReadInThisIteration = fis.read(buffer);
                    checkSum.update(buffer, 0, (int) bytesReadInThisIteration);
                    out.write(buffer, 0, (int)bytesReadInThisIteration);
                    out.flush();
                    bytesRead += bytesReadInThisIteration;
                }
            }

            // CheckSum
            byte[] checkSumCode = checkSum.digest();

            out.writeLong(checkSumCode.length);
            out.flush();

            out.write(checkSumCode);
            out.flush();

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

        public static void main(String[] args) throws IOException, InterruptedException, NoSuchAlgorithmException {
            Client client = new Client("pie.lynchburg.edu", 50900);
            Scanner scan = new Scanner(System.in);
            PrintStream out = new PrintStream(System.out);

            client.add("bunnyFetched.jpg","C:\\Users\\jacks\\Downloads\\bunny.jpg");
            client.add("alice.txt","C:\\Users\\jacks\\Downloads\\alice.txt");

            client.append("alice.txt", "C:\\Users\\jacks\\Downloads\\alice.txt");

            client.fetch("alice.txt","C:\\Users\\jacks\\Downloads\\aliceFetched.txt");
        }
    }
