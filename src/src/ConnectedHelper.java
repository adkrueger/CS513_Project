package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


class ConnectedHelper implements Runnable {

    private Socket client;
    private Server server;

    public ConnectedHelper(Socket client, Server server) {
        this.client = client;
        this.server = server;
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

        System.out.println("Client at port " + client.getPort() + " has a thread.");
        String curr_message;

        curr_message = Integer.toString(client.getPort());
        serverOutput.println(curr_message);
        System.out.println("Server returned: " + curr_message);

        System.out.println("currently, server has the following list:");
        System.out.println(this.server.getClientList().toString());

        while(true) {
            try {
                curr_message = clientInput.readLine();
                serverOutput.println(curr_message);
                System.out.println("Client says: " + curr_message);

                String[] inputCommands = curr_message.split(" ");

                // Check if we need to add the client to our list of clients
                if(!server.isDuplicateName(inputCommands[0])) {
                    // TODO figure out connNum???
                    server.setNickname(this.client.getPort(), inputCommands[0]);
                }

                if(inputCommands.length > 1) {
                    System.out.println("client command is: " + inputCommands[1]);

                }
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
