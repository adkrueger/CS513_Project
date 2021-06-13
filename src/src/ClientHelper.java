package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class ClientHelper implements Runnable {

    private Socket server;
    private Client client;

    public ClientHelper(Socket server, Client client) {
        this.server = server;
        this.client = client;
    }

    public void run() {
        BufferedReader serverInput = null;

        try {
            serverInput = new BufferedReader(new InputStreamReader(server.getInputStream()));
        } catch (IOException e) {
            System.out.println("Error encountered in ConnectedHelper setup: " + e);
        }

        System.out.println("Client is now accepting output from the server.");
        String curr_message;

        while (true) {
            try {
                curr_message = serverInput.readLine();
                if(curr_message.equals("duplicate")) {
                    System.out.println("received duplicate message");
                    client.setDuplicate(true);
                }
                else {
                    client.setDuplicate(false);
                    System.out.println("Server says: " + curr_message);
                    //TODO maybe some parsing in the future here to make things look nice?
                }

            } catch (IOException e) {
                System.out.println("Server disconnected.");
                return;
            } catch (NullPointerException e) {
                System.out.println("Client input caused NullPointerException: " + e);
            }

        }

    }

}
