package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;


class ConnectedHelper implements Runnable {

    private Socket client;
    private Server server;
    private int clientPort;
    private String clientNick;

    public ConnectedHelper(Socket client, Server server) {
        this.client = client;
        this.server = server;
        this.clientPort = client.getPort();
        this.clientNick = null;
    }

    public void run() {
        BufferedReader clientInput = null;
        PrintWriter serverOutput = null;
        boolean noNickname = true;

        try {
            clientInput = new BufferedReader(new InputStreamReader(client.getInputStream()));
            serverOutput = new PrintWriter(client.getOutputStream(), true);
        }
        catch(IOException e) {
            System.out.println("Error encountered in ConnectedHelper setup: " + e);
        }

        System.out.println("Client at port " + this.clientPort + " has a thread.");
        String curr_message;

//        curr_message = Integer.toString(this.clientPort);
//        serverOutput.println(curr_message);
//        System.out.println("Server returned: " + curr_message);

        System.out.println("currently, server has the following list:");
        System.out.println(this.server.getClientList().toString());

        // attempt to set nickname; if this is false, then the client disconnected
        if(!attemptNicknameSet(clientInput, serverOutput)) {
            return;
        }

        // give the client a chance to set their nickname
        while(true) {
            try {
                curr_message = clientInput.readLine();
//                serverOutput.println(curr_message);
                System.out.println("Client says: " + curr_message);

                String[] inputCommands = curr_message.split(" ");

                if(inputCommands.length > 1) {
                    System.out.println("client command is: " + inputCommands[1]);

                }
            }
            catch(IOException e) {
                System.out.println("Client '" + this.clientNick + "' disconnected.");
                server.removeUser(this.clientNick);
                return;
            }
            catch(NullPointerException e) {
                System.out.println("Client input caused NullPointerException: " + e);
            }

        }

    }

    private boolean attemptNicknameSet(BufferedReader clientInput, PrintWriter serverOutput) {
        while(true) {
            try {
                String curr_message = clientInput.readLine();
                System.out.println("Client says: " + curr_message);

                String[] inputCommands = curr_message.split(" ");
                if (!server.isDuplicateName(inputCommands[0])) {
                    server.setNickname(this.clientPort, inputCommands[0]);
                    this.clientNick = inputCommands[0];
                    serverOutput.println("name '" + curr_message + "' accepted!"); // send their name back as acknowledgement
                    return true;
                } else {
                    serverOutput.println("duplicate");
                }
            }
            catch(IOException e) {
                System.out.println("Client '" + this.clientNick + "' disconnected.");
                server.removeUser(this.clientNick);
                return false;
            }
        }
    }

}
