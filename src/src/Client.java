package src;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    DataOutputStream clientOutput;
    DataInputStream serverOutput;

    public static void main(String[] args) {
        System.out.println("Enter a port number to connect to:");
        Scanner scan = new Scanner(System.in);
        int portNum = scan.nextInt();
        System.out.println("Attempting to connect at port " + portNum);
        try {
            Socket socket = new Socket("127.0.0.1", portNum);
            System.out.println("Connected! Enter your nickname for the server.");
            DataOutputStream clientOutput = new DataOutputStream(socket.getOutputStream());
            DataInputStream serverOutput = new DataInputStream( new BufferedInputStream(socket.getInputStream()));

            //TODO I think the current issue is that I need to have this code write to its own port rather than the server?
            //TODO ConnectedHelper reads from the client's port

            // try setting up the client's nickname
            scan.nextLine();
            String nickname = scan.nextLine();
            System.out.println("accepted name: " + nickname);
            clientOutput.writeUTF(nickname);
            System.out.println("nickname written to server");
            String response = serverOutput.readUTF();
            System.out.println("sent nickname, serverOutput is: " + response);
            while(response.equals("duplicate")) {
                System.out.println("Server already has nickname '" + nickname + "' on record, " +
                        "please enter a unique one.");
                nickname = scan.nextLine();
                clientOutput.writeUTF(nickname);
                response = serverOutput.readUTF();
            }
            System.out.println("nickname successfully set to " + nickname);

            // start allowing the client to write commands to the server
            System.out.println("Beginning communications... type '!help' at any time to see valid commands.");
            String curr_input = "";
            while(!curr_input.equals("!disconnect")) {
                curr_input = scan.nextLine();
                System.out.println("input: " + curr_input + ", writing to server...");
                clientOutput.writeUTF(nickname + " " + curr_input);
                response = serverOutput.readUTF();
                System.out.println("command successful, server response: " + response);
            }
        }
        catch(IOException e) {
            System.out.println("Error connecting to socket: " + e);
        }

    }

}
