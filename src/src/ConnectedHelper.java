package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;


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
                System.out.println("Client '" + clientNick + "' says: " + curr_message);

                String[] inputCommands = curr_message.split(" ", 4);
                System.out.println(inputCommands);

                if(inputCommands.length > 1) {
//                    System.out.println("client command is: " + inputCommands[1]);
                    String clientCommand = inputCommands[1];

                    if(clientCommand.equals("!list")) {
                        serverOutput.println(server.getClientList().toString());
                    }
                    else if(inputCommands.length > 2) {
                        if(clientCommand.equals("!message")) {
                            server.messageAll(inputCommands[2]);
                            System.out.println("User '" + this.clientNick + "' sent a message to everyone saying: " + inputCommands[2]);
                        }
                        else if(clientCommand.equals("!rename")) {
                            String oldNick = this.clientNick;
                            if(!attemptNicknameSet(clientInput, serverOutput, inputCommands[2])) {
                                return;
                            }
                            else {
                                System.out.println("User '" + oldNick + "' successfully renamed to '" + this.clientNick + "'.");
                            }
                        }
                        else if(clientCommand.equals("!whisper")) {
                            if(inputCommands.length >= 4) {
                                if(server.userExists(inputCommands[2])) {
                                    if(server.whisper(this.clientNick, inputCommands[2], inputCommands[3])) {
                                        System.out.println("User '" + this.clientNick + "' whispered to user '" + inputCommands[2] + "': " + inputCommands[3]);
                                        serverOutput.println("You whispered to user '" + inputCommands[2] + "'.");
                                    }
                                    else {
                                        System.out.println("User '" + this.clientNick + "' failed to whisper to user '" + inputCommands[2]);
                                        serverOutput.println("Whisper to user '" + inputCommands[2] + "' failed. Please try again.");
                                    }
                                }
                                else {
                                    serverOutput.println("User " + inputCommands[2] + " does not exist!");
                                    System.out.println("User '" + this.clientNick + "' failed to whisper to user '" + inputCommands[2]);
                                }
                            }
                            else {
                                serverOutput.println("Whisper not sent, please type !help for !whisper usage.");
                            }
                        }
                    }
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

    private boolean attemptNicknameSet(BufferedReader clientInput, PrintWriter serverOutput, String nickname) {
        if (!server.isDuplicateName(nickname)) {
            server.setNickname(this.clientPort, nickname);
            this.clientNick = nickname;
            serverOutput.println("name '" + nickname + "' accepted!"); // send their name back as acknowledgement
            return true;
        } else {
            serverOutput.println("duplicate");
            return attemptNicknameSet(clientInput, serverOutput);
        }
    }

}
