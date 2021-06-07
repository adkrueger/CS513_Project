package src;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

public class Server {

    private int sockNum;
    private HashMap<String, Integer> clients;
    private Socket currClientSocket;
    private ServerSocket serverSocket;

    public static void main(String[] args) {
        // write your code here
        System.out.println("Initializing server...");
        Scanner scan = new Scanner(System.in);
        System.out.println("Please enter a socket number.\nNote that the number must be " +
                "an integer and greater than 0 (other input will cause the program to crash).");
        int currSockNum = scan.nextInt();
        Server server = new Server(currSockNum);
        System.out.println("Server initialized with socket number " + currSockNum + ".\nServer will now " +
                "listen for Clients.");
        System.out.println("Please use Ctrl+C to end the Server.");
        server.beginListening();

    }

    public Server(int sockNum) {
        try {
            this.sockNum = sockNum;
            this.serverSocket = new ServerSocket(sockNum);
            // clients will be in the format of <ConnectingSocketNumber, Nickname>
            this.clients = new HashMap<>();
        }
        catch(IOException e) {
            System.out.println("Error, couldn't start ServerSocket: " + e);
        }
    }

    private void beginListening() {
        // TODO make it so users connecting will print out a notice that they're connecting
        System.out.println("Server started, now waiting for client...");
        while(true) {
            DataInputStream clientInput;
            try {
                System.out.println("back at top, starting new thread");

                this.currClientSocket = serverSocket.accept();
                ConnectedHelper helper = new ConnectedHelper(this.currClientSocket);
                Thread thread = new Thread(helper);
                System.out.println("Client accepted! Checking for input...");

                clientInput = new DataInputStream(this.currClientSocket.getInputStream());
                String strIn = clientInput.toString();
                System.out.println("Client says: " + strIn);
                String[] inputCommands = strIn.split(" ");

                // Check if we need to add the client to our list of clients
                if(!isDuplicateName(inputCommands[0])) {
                    // TODO figure out connNum???
                    setNickname(0, inputCommands[0]);
                }

                if(inputCommands.length > 1) {
                    System.out.println("client command is: " + inputCommands[1]);

                }
            }
            catch(IOException e) {
                System.out.println("Error, couldn't accept client: " + e);
            }
        }
    }

    private boolean isDuplicateName(String nickname) {
        return clients.containsKey(nickname);
    }

    /**
     * attempts to set the nickname for a specific client. Fails if the name entered is a duplicate
     * @param connNum the client's connected socket
     * @param nickname the client's desired nickname
     * @return true if the name was not a duplicate and was successfully added to the client list, false otherwise
     */
    public boolean setNickname(int connNum, String nickname) {
        if(isDuplicateName(nickname)) {
            System.out.println("found duplicate name");
            return false;
        }
        clients.put(nickname, connNum);
        System.out.println("Successfully added " + nickname + " with connection number " + connNum);
        return true;
    }

    public HashMap<String, Integer> getClientList() {
        return clients;
    }

}
