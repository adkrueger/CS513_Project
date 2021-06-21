package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientHelper implements Runnable {

    private Socket server;
    private Client client;

    public ClientHelper(Socket server, Client client) {
        this.server = server;
        this.client = client;
    }

    public void run() {
        BufferedReader serverInput = null;

        // set up server input stream
        try {
            serverInput = new BufferedReader(new InputStreamReader(server.getInputStream()));
        } catch (IOException e) {
            System.out.println("Error encountered in ConnectedHelper setup: " + e);
        }

        System.out.println("Client is now accepting output from the server.");
        String curr_message;

        // just keep reading messages as the server outputs them
        while (true) {
            try {
                curr_message = serverInput.readLine();
                // talk to the Client in case they were told that they sent a duplicate nickname
                if(curr_message.equals("duplicate")) {
                    System.out.println("received duplicate message");
                    client.setDuplicate(true);
                }
                else {
                    // otherwise, just print out whatever the server sent
                    client.setDuplicate(false);
                    System.out.println(">> " + curr_message);
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
