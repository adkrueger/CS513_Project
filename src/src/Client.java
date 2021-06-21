package src;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private boolean isDuplicate;
    private String[] legalCommands = {"!help", "!list", "!message", "!rename", "!whisper"};

    // ClientHelper uses this to tell us if the name is a duplicate or not
    public void setDuplicate(boolean duplicate) {
        isDuplicate = duplicate;
    }

    private Client() {}

    public static void main(String[] args) {
        // let the user try to connect to a specific port
        System.out.println("Enter a port number to connect to:");
        Scanner scan = new Scanner(System.in);
        int portNum = scan.nextInt();
        System.out.println("Attempting to connect at port " + portNum);
        Client client = new Client();

        // start communicating with the Server
        client.beginServerCommunication(portNum);
    }

    private void beginServerCommunication(int portNum) {
        Scanner scan = new Scanner(System.in);
        try {
            // attempt to connect, and create Streams to handle input/output
            Socket socket = new Socket("127.0.0.1", portNum);
            System.out.println("Successfully connected to the server!");
            PrintWriter clientOutput = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader serverOutput = new BufferedReader( new InputStreamReader(socket.getInputStream()));

            // try setting up the client's nickname
            System.out.println("Please enter your nickname for the server.");
            String nickname = enterNamingLoop(scan, clientOutput, serverOutput);
            if(nickname == null) {
                System.out.println("Failed to set nickname.");
                closeStreams(clientOutput, serverOutput);
                return;
            }

            // start allowing the client to write commands to the server
            System.out.println("Beginning communications... type '!help' at any time to see valid commands.");
            String curr_input = "";

            // start a thread so that we don't have to worry about reading server output here
            ClientHelper helper = new ClientHelper(socket, this);
            Thread thread = new Thread(helper);
            thread.start();

            // accept input until the user tries to disconnect or hit Ctrl+C
            while(!curr_input.equals("!disconnect")) {
                curr_input = scan.nextLine();
                // grab the command the user is giving
                String currCommand = curr_input.split(" ")[0];

                // check the command is valid before proceeding
                if(curr_input.length() >= 5 && isLegalCommand(currCommand)) {
                    // see if the user needs help before we send anything to the server
                    if(!currCommand.equals("!help")) {
                        // handle renames slightly differently than most messages since we might need to loop
                        if(currCommand.equals("!rename")) {
                            // try to rename once, then see if the user entered a duplicate (below)
                            nickname = enterRenamingLoop(scan, clientOutput, "!rename " + curr_input.split(" ")[1]);
                            if(nickname == null) {
                                System.out.println("Failed to set nickname.");
                                closeStreams(clientOutput, serverOutput);
                                return;
                            }

                            // if the user asks for a duplicate nickname, keep prompting them for a new one
                            while (isDuplicate) {
                                System.out.println("Server already has nickname '" + nickname + "' on record, " +
                                        "please enter a unique one:");
                                nickname = scan.nextLine();
                                nickname = enterRenamingLoop(scan, clientOutput, nickname);
                            }
                        }
                        else {
                            // otherwise, just send our command to the server
                            System.out.println("input: " + curr_input + ", writing to server...");
                            clientOutput.println(curr_input);
                        }
                    }
                    // the user wants some help, so print it
                    else {
                        System.out.println("----------------------------------------\n");
                        System.out.println("Usage:");
                        System.out.println("!list");
                        System.out.println("\tGets a list of all users currently connected to the server.\n");
                        System.out.println("!rename <new name>");
                        System.out.println("\tAttempts to set your nickname to <new name>.");
                        System.out.println("\t\tnote: <new name> must not be the same as your current nickname or that of any other connected user.\n");
                        System.out.println("!message <message>");
                        System.out.println("\tSends the message <message> to all users connected to the server (including you).\n");
                        System.out.println("!whisper <target> <message>");
                        System.out.println("\tSends the message <message> to user with nickname <target>.");
                        System.out.println("\t\tnote: fails if user <target> is not connected to the server.\n");
                        System.out.println("!disconnect");
                        System.out.println("\tDisconnects from the server.\n");
                    }
                }
                else {
                    System.out.println("Unrecognized command, please type '!help' to see valid commands.");
                }
            }
            // client wants to disconnect, so do so
            closeStreams(clientOutput, serverOutput);
            return;
        }
        catch(IOException e) {
            System.out.println("Error connecting to server socket: " + e);
            return;
        }

    }

    // checks that the user gave a legal command
    private boolean isLegalCommand(String command) {
        for(String c : legalCommands) {
            if(c.equals(command)) { return true; }
        }
        return false;
    }

    // tries to name the client
    private String enterNamingLoop(Scanner scan, PrintWriter clientOutput, BufferedReader serverOutput) {
        try {
            // prompt user for a nickname
            String nickname = scan.nextLine();
            System.out.println("accepted name: " + nickname);

            // check that the nickname is valid
            while(nickname.trim().isEmpty()) {
                System.out.println("Nickname must not be whitespace or empty. Please try again:");
                nickname = scan.nextLine();
            }

            // write the nickname to the server, then read the server's response
            clientOutput.println(nickname);
            System.out.println("nickname written to server");
            String response = serverOutput.readLine();
            System.out.println("serverOutput is: " + response);

            // if the name was a duplicate, try prompting the user again
            while (response.equals("duplicate")) {
                System.out.println("Server already has nickname '" + nickname + "' on record, " +
                        "please enter a unique one.");
                nickname = scan.nextLine();
                clientOutput.println(nickname);
                response = serverOutput.readLine();
            }

            // success!
            System.out.println("nickname successfully set to " + nickname);
            return nickname;
        }
        catch(IOException e) {
            // server socket closed, so return
            System.out.println("Error connecting to server socket: " + e);
            return null;
        }
    }

    // try to rename the client (note that the actual loop is handled outside this function
    private String enterRenamingLoop(Scanner scan, PrintWriter clientOutput, String nickname) {
        // check that the nickname is valid
        while(nickname.trim().isEmpty()) {
            System.out.println("Nickname must not be whitespace or empty. Please try again:");
            nickname = scan.nextLine();
        }
        clientOutput.println(nickname);
        System.out.println("nickname written to server");

        // sleep for a second so that we can check for server output
        try {
            Thread.sleep(1000);
        }
        catch(InterruptedException e) {
            System.out.println("Sleep interrupted: " + e);
        }

        return nickname;
    }

    // try to close the Client's input/output streams
    private void closeStreams(PrintWriter clientOutput, BufferedReader serverOutput) {
        try {
            clientOutput.close();
            serverOutput.close();
        }
        catch(IOException e) {
            System.out.println("Error closing server output stream: " + e);
        }
    }

}
