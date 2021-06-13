package src;

import java.io.DataInputStream;
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
    private volatile HashMap<String, String> messages;
    private volatile HashMap<Integer, Socket> clientSockets;
    private Socket currClientSocket;
    private ServerSocket serverSocket;

    public int getSockNum() {
        return sockNum;
    }

    public void setSockNum(int sockNum) {
        this.sockNum = sockNum;
    }

    public HashMap<String, Integer> getClients() {
        return clients;
    }

    public void setClients(HashMap<String, Integer> clients) {
        this.clients = clients;
    }

    public Socket getCurrClientSocket() {
        return currClientSocket;
    }

    public void setCurrClientSocket(Socket currClientSocket) {
        this.currClientSocket = currClientSocket;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

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
            this.clientSockets = new HashMap<>();
        }
        catch(IOException e) {
            System.out.println("Error, couldn't start ServerSocket: " + e);
        }
    }

    private void beginListening() {
        System.out.println("Server started, now waiting for client...");
        while(true) {
            DataInputStream clientInput;
            try {
                System.out.println("back at top, starting new thread");

                this.currClientSocket = serverSocket.accept();
//                System.out.println(this.currClientSocket.getPort());
//                System.out.println(this.currClientSocket);
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

    public void removeUser(String nickname) {
        clientSockets.remove(clients.get(nickname));
        clients.remove(nickname);
    }

    public void messageAll(String message, String source) {
        for(Map.Entry<Integer, Socket> c : clientSockets.entrySet()) {
            try {
                PrintWriter serverOutput = new PrintWriter(c.getValue().getOutputStream(), true);
                serverOutput.println(source + " says: " + message);
            }
            catch(IOException e) {
                System.out.println("Could not send message to client at port " + c.getKey());
            }
        }
    }

    public boolean whisper(String source, String target, String message) {
        System.out.println("Trying to whisper between " + source + " and " + target);
        System.out.println("detail: " + clients.get(target));
        System.out.println("detail 2: " + clientSockets.get(clients.get(target)));
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
