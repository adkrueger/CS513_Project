package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


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

        try {
            // set up our streams to allow for input/output
            clientInput = new BufferedReader(new InputStreamReader(client.getInputStream()));
            serverOutput = new PrintWriter(client.getOutputStream(), true);
        }
        catch(IOException e) {
            System.out.println("Error encountered in ConnectedHelper setup: " + e);
            return;
        }

        System.out.println("Client at port " + this.clientPort + " has a thread.");
        String curr_message;

        System.out.println("Currently, server has the following list of clients:");
        System.out.println(this.server.getClientList().toString());

        // attempt to set nickname; if this is false, then the client disconnected
        if(!attemptNicknameSet(clientInput, serverOutput, false)) {
            return;
        }

        // just grab Client input as it comes in until the client disconnects
        while(true) {
            try {
                curr_message = clientInput.readLine();
                System.out.println("Client '" + clientNick + "' says: " + curr_message);

                // parse the commands up to the third possible word, as that would contain the message in some commands
                String[] inputCommands = curr_message.split(" ", 3);

                // make sure input is valid
                if(inputCommands.length >= 1) {
                    String clientCommand = inputCommands[0];

                    if(clientCommand.equals("!list")) {
                        // send a lit of users to the Client
                        serverOutput.println(server.getClientList().toString());
                    }
                    // basically anything else has length >=2
                    else if(inputCommands.length >= 2) {
                        // the client wants to send a message to all other clients
                        if(clientCommand.equals("!message")) {
                            // have to deal with a slight detail of parsing the strings; easier to use just an if than change the whole infrastructure
                            if(inputCommands.length == 2) {
                                server.messageAll(inputCommands[1], this.clientNick);
                                System.out.println("User '" + this.clientNick + "' sent a message to everyone saying: " + inputCommands[1]);
                            }
                            else {
                                server.messageAll(inputCommands[1] + " " + inputCommands[2], this.clientNick);
                                System.out.println("User '" + this.clientNick + "' sent a message to everyone saying: " + inputCommands[1] + " " + inputCommands[2]);
                            }
                        }
                        // the client wants to rename themselves
                        else if(clientCommand.equals("!rename")) {
                            String oldNick = this.clientNick;
                            if(!attemptNicknameSet(clientInput, serverOutput, inputCommands[1], true)) {
                                // renaming failed, or user disconnected in the middle of it
                                return;
                            }
                            else {
                                System.out.println("User '" + oldNick + "' successfully renamed to '" + this.clientNick + "'");
                                // remove the Client's old name but don't remove their connection
                                server.removeUser(oldNick, false);
                            }
                        }
                        // the client wants to whisper to another client
                        else if(clientCommand.equals("!whisper")) {
                            if(inputCommands.length >= 3) {
                                // check that the user actually exists before we try to whisper to them
                                if(server.userExists(inputCommands[1])) {
                                    // try to whisper, and handle the case where it fails for some reason
                                    if(server.whisper(this.clientNick, inputCommands[1], inputCommands[2])) {
                                        System.out.println("User '" + this.clientNick + "' whispered to user '" + inputCommands[1] + "': " + inputCommands[2]);
                                        serverOutput.println("You whispered to user '" + inputCommands[1] + "'.");
                                    }
                                    else {
                                        System.out.println("User '" + this.clientNick + "' failed to whisper to user '" + inputCommands[1] + "'");
                                        serverOutput.println("Whisper to user '" + inputCommands[1] + "' failed. Please try again.");
                                    }
                                }
                                else {
                                    // client target does not exist
                                    serverOutput.println("User " + inputCommands[1] + " does not exist!");
                                    System.out.println("User '" + this.clientNick + "' failed to whisper to user '" + inputCommands[1] + "'");
                                }
                            }
                            else {
                                // whisper format was invalid
                                serverOutput.println("Whisper not sent, please type !help for !whisper usage.");
                            }
                        }
                    }
                }
            }
            catch(IOException e) {
                System.out.println("Client '" + this.clientNick + "' disconnected.");
                server.removeUser(this.clientNick, true);
                return;
            }
            catch(NullPointerException e) {
                System.out.println("Client input caused NullPointerException: " + e);
            }

        }

    }

    // try to set the client's nickname
    private boolean attemptNicknameSet(BufferedReader clientInput, PrintWriter serverOutput, boolean isRename) {
        while(true) {
            try {
                String curr_message = clientInput.readLine();
                System.out.println("While renaming, client says: " + curr_message);

                String[] inputCommands = curr_message.split(" ");
                // before adding the nickname, check that it's not a duplicate (checking here allows us to tell the Client)
                if (!server.isDuplicateName(inputCommands[0])) {
                    server.setNickname(this.clientPort, inputCommands[0], isRename);
                    this.clientNick = inputCommands[0];
                    serverOutput.println("name '" + curr_message + "' accepted!");
                    return true;
                } else {
                    serverOutput.println("duplicate");
                }
            }
            catch(IOException e) {
                // the client disconnected, so remove them
                System.out.println("Client '" + this.clientNick + "' disconnected.");
                server.removeUser(this.clientNick, true);
                return false;
            }
        }
    }

    // helper method that allows us to try renaming once before jumping into the full loop (above)
    private boolean attemptNicknameSet(BufferedReader clientInput, PrintWriter serverOutput, String nickname, boolean isRename) {
        // check if the name is a duplicate
        if (!server.isDuplicateName(nickname)) {
            server.setNickname(this.clientPort, nickname, isRename);
            this.clientNick = nickname;
            serverOutput.println("name '" + nickname + "' accepted!"); // send their name back as acknowledgement
            return true;
        } else {
            serverOutput.println("duplicate");
            return attemptNicknameSet(clientInput, serverOutput, isRename);
        }
    }

}
