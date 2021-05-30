package src;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Server {

    private int sockNum;
    private HashMap<Integer, String> clients;

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
        this.sockNum = sockNum;
        // clients will be in the format of <ConnectingSocketNumber, Nickname>
        this.clients = new HashMap<>();
    }

    private void beginListening() {
        // TODO make it so users connecting will print out a notice that they're connecting
        
    }

    private boolean isDuplicateName(String nickname) {
        for(String n : clients.values()) {
            if(n.equals(nickname)) {
                return false;
            }
        }
        return true;
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
        clients.put(connNum, nickname);
        System.out.println("Successfully added " + nickname + " with connection number " + connNum);
        return true;
    }

    public HashMap<Integer, String> getClientList() {
        return clients;
    }

}
