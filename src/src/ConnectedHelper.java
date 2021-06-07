package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class ConnectedHelper implements Runnable {

    private Socket client;

    public ConnectedHelper(Socket client) {
        this.client = client;
    }

    public void run() {
        BufferedReader clientInput = null;
        PrintWriter serverOutput = null;

        try {
            clientInput = new BufferedReader(new InputStreamReader(client.getInputStream()));
            serverOutput = new PrintWriter(client.getOutputStream(), true);
        }
        catch(IOException e) {
            System.out.println("Error encountered in ConnectedHelper setup: " + e);
        }

        String curr_message;

        while(true) {
            try {
                curr_message = clientInput.readLine();
                serverOutput.println(curr_message);
                System.out.println("Client says: " + curr_message);
            }
            catch(IOException e) {
                System.out.println("Error when reading client input message: " + e);
            }
            catch(NullPointerException e) {
                System.out.println("Client input caused NullPointerException: " + e);
            }

        }

    }

}
