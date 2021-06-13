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

                String[] inputCommands = curr_message.split(" ", 3);
//                System.out.println(inputCommands);

                if(inputCommands.length >= 1) {
//                    System.out.println("client command is: " + inputCommands[1]);
                    String clientCommand = inputCommands[0];

                    if(clientCommand.equals("!list")) {
                        serverOutput.println(server.getClientList().toString());
                    }
                    else if(inputCommands.length >= 2) {
                        if(clientCommand.equals("!message")) {
                            server.messageAll(inputCommands[1] + " " + inputCommands[2], this.clientNick);
                            System.out.println("User '" + this.clientNick + "' sent a message to everyone saying: " + inputCommands[1]);
                        }
                        else if(clientCommand.equals("!rename")) {
                            String oldNick = this.clientNick;
                            System.out.println("TRYING TO SET NICKNAME " + inputCommands[1]);
                            if(!attemptNicknameSet(clientInput, serverOutput, inputCommands[1])) {
                                return;
                            }
                            else {
                                System.out.println("User '" + oldNick + "' successfully renamed to '" + this.clientNick + "'.");
                                server.removeUser(oldNick);
                            }
                        }
                        else if(clientCommand.equals("!whisper")) {
                            if(inputCommands.length >= 3) {
                                if(server.userExists(inputCommands[1])) {
                                    System.out.println("client exists, attempting to whisper...........................");
                                    if(server.whisper(this.clientNick, inputCommands[1], inputCommands[2])) {
                                        System.out.println("User '" + this.clientNick + "' whispered to user '" + inputCommands[1] + "': " + inputCommands[2]);
                                        serverOutput.println("You whispered to user '" + inputCommands[1] + "'.");
                                    }
                                    else {
                                        System.out.println("User '" + this.clientNick + "' failed to whisper to user '" + inputCommands[1]);
                                        serverOutput.println("Whisper to user '" + inputCommands[1] + "' failed. Please try again.");
                                    }
                                }
                                else {
                                    serverOutput.println("User " + inputCommands[1] + " does not exist!");
                                    System.out.println("User '" + this.clientNick + "' failed to whisper to user '" + inputCommands[1]);
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
            System.out.println("in big attempt nickname set");
            try {
                String curr_message = clientInput.readLine();
                System.out.println("Client says: " + curr_message);

                String[] inputCommands = curr_message.split(" ");
                if (!server.isDuplicateName(inputCommands[0])) {
                    server.setNickname(this.clientPort, inputCommands[0]);
                    this.clientNick = inputCommands[0];
                    serverOutput.println("name '" + curr_message + "' accepted!");
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
        System.out.println("in attempt nickname set (baby version)");
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
