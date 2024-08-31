package client;

import java.io.*;
import java.net.*;
import java.security.KeyStore;
import javax.net.ssl.*;
import java.util.Scanner;

public class SecureAdditionClient {
    private InetAddress host;
    private int port;
    static final int DEFAULT_PORT = 8189;
    static final String KEYSTORE = "/Users/klarabratteby/eclipse-workspace/TNM031/Lab3/Lab3/src/client/LIUkeystore.ks";
    static final String TRUSTSTORE = "/Users/klarabratteby/eclipse-workspace/TNM031/Lab3/Lab3/src/client/LIUtruststore.ks";
    static final String KEYSTOREPASS = "123456";
    static final String TRUSTSTOREPASS = "abcdef";

    public SecureAdditionClient(InetAddress host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() {
        try {
        	KeyStore ks = KeyStore.getInstance("JCEKS");
            ks.load(new FileInputStream(KEYSTORE), KEYSTOREPASS.toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            SSLSocketFactory sslFact = sslContext.getSocketFactory();
            SSLSocket client = (SSLSocket) sslFact.createSocket(host, port);
            client.setEnabledCipherSuites(new String[]{"TLS_AES_256_GCM_SHA384"});
            client.setNeedClientAuth(true);
            System.out.println("\n>>>> SSL/TLS handshake completed");

            DataInputStream socketIn = new DataInputStream(client.getInputStream());
            DataOutputStream socketOut = new DataOutputStream(client.getOutputStream());

            printMenu();

            Scanner sc = new Scanner(System.in);
            int inputValue = sc.nextInt();

            String fileName;
            switch (inputValue) {
                case 1:
                    System.out.print("Enter filename: ");
                    fileName = sc.next();
                    downloadFile(fileName, socketIn, socketOut, inputValue);
                    break;
                case 2:
                    System.out.print("Enter filename: ");
                    fileName = sc.next();
                    uploadFile(fileName, socketIn, socketOut, inputValue);
                    break;
                case 3:
                    System.out.print("Enter filename: ");
                    fileName = sc.next();
                    deleteFile(fileName, socketIn, socketOut, inputValue);
                    break;
                default:
                    System.out.println("Invalid option. Please choose between 1-3.");
            }
        } catch (Exception x) {
            System.out.println(x);
            x.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            InetAddress host = InetAddress.getLocalHost();
            int port = DEFAULT_PORT;
            if (args.length > 0) {
                port = Integer.parseInt(args[0]);
            }
            if (args.length > 1) {
                host = InetAddress.getByName(args[1]);
            }
            SecureAdditionClient addClient = new SecureAdditionClient(host, port);
            addClient.run();
        } catch (UnknownHostException uhx) {
            System.out.println(uhx);
            uhx.printStackTrace();
        }
    }

    private void downloadFile(String fileName, DataInputStream socketIn, DataOutputStream socketOut, int inputValue) throws IOException {
        socketOut.writeInt(inputValue);
        socketOut.writeUTF(fileName);

        int fileLength = socketIn.readInt();

        if (fileLength == -1) {
            System.out.println("File not found on the server.");
            return;
        }

        byte[] fileData = new byte[fileLength];
        socketIn.readFully(fileData);

        FileOutputStream fos = new FileOutputStream("/Users/klarabratteby/eclipse-workspace/TNM031/Lab3/Lab3/src/client" + fileName);
        fos.write(fileData);
        fos.close();

        System.out.println("The file has been downloaded");
    }

    private void uploadFile(String fileName, DataInputStream socketIn, DataOutputStream socketOut, int inputValue) throws IOException {
        socketOut.writeInt(inputValue);
        socketOut.writeUTF(fileName);
        socketOut.flush();

        String filePath = "/Users/klarabratteby/eclipse-workspace/TNM031/Lab3/Lab3/src/client" + fileName;
        File file = new File(filePath);

        if (!file.exists()) {
            System.out.println("File not found locally.");
            socketOut.writeInt(-1);
            socketOut.flush();
            return;
        }

        FileInputStream fis = new FileInputStream(file);
        byte[] fileData = fis.readAllBytes();

        socketOut.writeInt(fileData.length);
        socketOut.flush();

        socketOut.write(fileData);
        socketOut.flush();

        fis.close();

        System.out.println("The file has been uploaded");
    }

    private void deleteFile(String fileName, DataInputStream socketIn, DataOutputStream socketOut, int inputValue) throws IOException {
        socketOut.writeInt(inputValue);
        socketOut.writeUTF(fileName);
        socketOut.flush();
        String filePath = "/Users/klarabratteby/eclipse-workspace/TNM031/Lab3/Lab3/src/client" + fileName;

        String response = socketIn.readUTF();

        if (response.equals("OK")) {
            System.out.println("File deleted successfully.");
        } else if (response.equals("FileNotFound")) {
            System.out.println("File not found on the server.");
        } else {
            System.out.println("Failed to delete the file.");
        }

        System.out.println("The file has been deleted");
    }

    public void printMenu() {
        System.out.println("SSL Lab 3 Menu");
        System.out.println("1. Download file from server");
        System.out.println("2. Upload file to server");
        System.out.println("3. Delete file from server");
        System.out.print("Select an option (1-3): ");
    }
}