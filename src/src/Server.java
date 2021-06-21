package src;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Server {

    private int sockNum;
    private volatile HashMap<String, Integer> clients;
    private volatile HashMap<Integer, Socket> clientSockets;
    private Socket currClientSocket;
    private ServerSocket serverSocket;

    public static void main(String[] args) {
        // give user some output to guide them through creating the server
        System.out.println("Initializing server...");
        Scanner scan = new Scanner(System.in);
        System.out.println("Please enter a socket number.\nNote that the number must be " +
                "an integer and greater than 0 (other input will cause the program to crash).");
        int currSockNum = scan.nextInt();
        Server server = new Server(currSockNum);
        System.out.println("Server initialized with socket number " + currSockNum + ".\nServer will now " +
                "listen for Clients.");
        System.out.println("Please use Ctrl+C to end the Server.");

        // have the server start listening and spawning threads
        server.beginListening();

    }

    public Server(int sockNum) {
        try {
            this.sockNum = sockNum;
            this.serverSocket = new ServerSocket(sockNum);
            // clients will be in the format of <Nickname, ConnectingSocketNumber>
            this.clients = new HashMap<>();
            this.clientSockets = new HashMap<>();
        }
        catch(IOException e) {
            System.out.println("Error, couldn't start ServerSocket: " + e);
        }
    }

    private void beginListening() {
        System.out.println("Server started, now waiting for client...");

        // essentially just accept connections until the user hits Ctrl+C
        while(true) {
            try {
                System.out.println("Server is waiting for next client connection request...");

                this.currClientSocket = serverSocket.accept();

                // once the Client has connected, add them to the list of connections and start a thread
                clientSockets.put(this.currClientSocket.getPort(), this.currClientSocket);
                ConnectedHelper helper = new ConnectedHelper(this.currClientSocket, this);
                Thread thread = new Thread(helper);
                thread.start();
            }
            catch(IOException e) {
                System.out.println("Error, couldn't accept client: " + e);
            }
        }
    }

    public boolean isDuplicateName(String nickname) {
        return clients.containsKey(nickname);
    }

    /**
     * attempts to set the nickname for a specific client. Fails if the name entered is a duplicate
     * @param connNum the client's connected socket
     * @param nickname the client's desired nickname
     * @return true if the name was not a duplicate and was successfully added to the client list, false otherwise
     */
    public boolean setNickname(int connNum, String nickname, boolean isRename) {
        if(isDuplicateName(nickname)) {
            System.out.println("Found duplicate name.");
            return false;
        }

        String oldNick = null;

        // if we're renaming, record the old nickname for printing later
        if(isRename) {
            oldNick = getNicknameFromPort(connNum);
        }

        // put the Client in our list of nicknames
        clients.put(nickname, connNum);
        System.out.println("Successfully added " + nickname + " with connection number " + connNum);

        // print something slightly different depending on if the user is renaming or not
        if(isRename) {
            messageAll("User " + oldNick + " renamed to " + nickname + ".", "server");
        }
        else {
            messageAll("User " + nickname + " connected to the server!", "server");
        }

        return true;
    }

    public HashMap<String, Integer> getClientList() {
        return clients;
    }

    // get the client's nickname based on their port number
    public String getNicknameFromPort(int portNum) {
        if(clients.containsValue(portNum)) {
            for(Map.Entry<String, Integer> entry : clients.entrySet()) {
                if(entry.getValue() == portNum) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    // remove the Client (and possibly their connection to the server)
    public void removeUser(String nickname, boolean removeConnection) {
        if(removeConnection) {
            System.out.println("Disconnecting user " + nickname + ".");
            messageAll("User " + nickname + " disconnected.", "server");
            clientSockets.remove(clients.get(nickname));
        }
        clients.remove(nickname);
    }

    // send a message to all connected Clients
    public void messageAll(String message, String source) {
        for(Map.Entry<Integer, Socket> c : clientSockets.entrySet()) {
            try {
                PrintWriter serverOutput = new PrintWriter(c.getValue().getOutputStream(), true);

                // slight differentiation between if the server is sending a message or if it's just a Client
                if(source.equals("server")) {
                    serverOutput.println(message);
                }
                else {
                    serverOutput.println(source + " says: " + message);
                }
            }
            catch(IOException e) {
                System.out.println("Could not send message to client at port " + c.getKey());
            }
        }
    }

    // allow Client source to whisper to Client target
    public boolean whisper(String source, String target, String message) {
        try {
            PrintWriter serverOutput = new PrintWriter(clientSockets.get(clients.get(target)).getOutputStream(), true);
            serverOutput.println("whisper from user '" + source + "' to '" + target + "': " + message);
            return true;
        }
        catch(IOException e) {
            System.out.println("Failed to send message from user " + source + " to user " + target);
            return false;
        }
    }

    public boolean userExists(String nickname) {
        return clients.containsKey(nickname);
    }
}
