package src;

import java.io.*;
import java.net.Socket;
import java.nio.Buffer;
import java.util.Scanner;

public class Client {

    private boolean isDuplicate;
    private String[] legalCommands = {"!help", "!list", "!message", "!rename", "!whisper"};

    public void setDuplicate(boolean duplicate) {
        isDuplicate = duplicate;
    }

    private Client() {}

    public static void main(String[] args) {
        System.out.println("Enter a port number to connect to:");
        Scanner scan = new Scanner(System.in);
        int portNum = scan.nextInt();
        System.out.println("Attempting to connect at port " + portNum);
        Client client = new Client();
        client.beginServerCommunication(portNum);
    }

    private void beginServerCommunication(int portNum) {
        Scanner scan = new Scanner(System.in);
        try {
            Socket socket = new Socket("127.0.0.1", portNum);
            System.out.println("Connected! Enter your nickname for the server.");
            PrintWriter clientOutput = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader serverOutput = new BufferedReader( new InputStreamReader(socket.getInputStream()));

            // try setting up the client's nickname
            System.out.println("Please enter your nickname for the server.");
            String nickname = enterNamingLoop(scan, clientOutput, serverOutput);
            if(nickname == null) {
                System.out.println("Failed to set nickname.");
                return;
            }

            // start allowing the client to write commands to the server
            System.out.println("Beginning communications... type '!help' at any time to see valid commands.");
            String curr_input = "";

            // start a thread so that we don't have to worry about reading server output here
            ClientHelper helper = new ClientHelper(socket, this);
            Thread thread = new Thread(helper);
            thread.start();

            while(!curr_input.equals("!disconnect")) {
                curr_input = scan.nextLine();
                String currCommand = curr_input.split(" ")[0];
                if(curr_input.length() >= 5 && isLegalCommand(currCommand)) {
//                    System.out.println(curr_input.substring(5));
//                    System.out.println(curr_input.substring(5).equals("!help"));
                    if(!currCommand.equals("!help")) {
                        if(currCommand.equals("!rename")) {
                            nickname = enterRenamingLoop(scan, clientOutput, serverOutput, "!rename " + curr_input.split(" ")[1]);
                            if(nickname == null) {
                                System.out.println("Failed to set nickname.");
                                return;
                            }
                            while (isDuplicate) {
                                System.out.println("Server already has nickname '" + nickname + "' on record, " +
                                        "please enter a unique one:");
                                nickname = scan.nextLine();
                                nickname = enterRenamingLoop(scan, clientOutput, serverOutput, nickname);
                            }
                        }
                        else {
                            System.out.println("input: " + curr_input + ", writing to server...");
                            clientOutput.println(curr_input);
                        }
                    }
                    else {
                        System.out.println("----------------------------------");
                        System.out.println("Usage:");
                        System.out.println("!list");
                        System.out.println("\tGets a list of all users currently connected to the server.");
                        System.out.println("!rename <new name>");
                        System.out.println("\tAttempts to set your nickname to <new name>.");
                        System.out.println("\t\tnote: <new name> must not be the same as your current nickname or that of any other connected user.");
                        System.out.println("!message <message>");
                        System.out.println("\tSends the message <message> to all users connected to the server (including you).");
                        System.out.println("!whisper <target> <message>");
                        System.out.println("\tSends the message <message> to user with nickname <target>.");
                        System.out.println("\t\tnote: fails if user <target> is not connected to the server.");
                    }
                }
                else {
                    System.out.println("Unrecognized command, please type '!help' to see valid commands.");
                }
            }
        }
        catch(IOException e) {
            System.out.println("Error connecting to server socket: " + e);
        }

    }

    private boolean isLegalCommand(String command) {
        for(String c : legalCommands) {
            if(c.equals(command)) { return true; }
        }
        return false;
    }

    public String enterNamingLoop(Scanner scan, PrintWriter clientOutput, BufferedReader serverOutput) {
        try {
            String nickname = scan.nextLine();
            System.out.println("accepted name: " + nickname);
            while(nickname.trim().isEmpty()) {
                System.out.println("Nickname must not be whitespace or empty. Please try again:");
                nickname = scan.nextLine();
            }
            clientOutput.println(nickname);
            System.out.println("nickname written to server");
            String response = serverOutput.readLine();
            System.out.println("sent nickname, serverOutput is: " + response);
            while (response.equals("duplicate")) {
                System.out.println("Server already has nickname '" + nickname + "' on record, " +
                        "please enter a unique one.");
                nickname = scan.nextLine();
                clientOutput.println(nickname);
                response = serverOutput.readLine();
            }
            System.out.println("nickname successfully set to " + nickname);
            return nickname;
        }
        catch(IOException e) {
            System.out.println("Error connecting to server socket: " + e);
            return null;
        }
    }

    public String enterRenamingLoop(Scanner scan, PrintWriter clientOutput, BufferedReader serverOutput, String nickname) {
        while(nickname.trim().isEmpty()) {
            System.out.println("Nickname must not be whitespace or empty. Please try again:");
            nickname = scan.nextLine();
        }
        clientOutput.println(nickname);
        System.out.println("nickname written to server");
        try {
            Thread.sleep(1000);
        }
        catch(InterruptedException e) {
            System.out.println("Sleep interrupted: " + e);
        }

//            String response = serverOutput.readLine();
//            System.out.println("sent nickname, serverOutput is: " + response);

//        System.out.println("nickname successfully set to " + nickname);
        return nickname;
    }

}
