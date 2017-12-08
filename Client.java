import java.io.*;
import java.net.Socket;

/**
 * public Client is used to connect to the chat room.
 */
public class Client {

    /**
     * The entry of Client Program.
     * 
     * @param args the information from console
     */
    public static void main(String[] args) throws Exception {
        ClientInstance client = new ClientInstance();
        client.start();
    }
}

/**
 * ClientInstance is used by Client.
 */
class ClientInstance {

    /**
     * The port number of client.
     */
    private int portNumber = 4396;

    /**
     * The information of welcome.
     */
    private static final String WELCOME = "Please type your username.";

    /**
     * The information of accept.
     */
    private static final String ACCEPT = "Your username is accepted. Please type messages";

    /**
     * The socket of client.
     */
    private Socket socket;

    /**
     * The input stream of client.
     */
    private BufferedReader in;

    /**
     * The outout stream of client.
     */
    private PrintWriter out;

    /**
     * To denote whether client connects to the server.
     */
    private boolean isServerConnected = false;

    /**
     * To denote whether client is allowed to chat.
     */
    private boolean isAllowedToChat = false;

    /**
     * Start the whole process of client.
     */
    public void start() {
        establishConnection();
        handleIncomingMessages();
        handleOutgoingMessages();
    }

    /**
     * Establish the connection to the server
     */
    private void establishConnection() {
        String serverAddress = getClientInput("What is the address of the server that you wish to connect to?");
        try {
            socket = new Socket(serverAddress, portNumber);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            isServerConnected = true;
        }
        catch (IOException e) {
            System.err.println("Exception in connection:" + e.getMessage());
        }
        profileSetUp();
    }

    /**
     * Handle the input from the user.
     * 
     * @param hint The hint information for the input.
     * @return String
     */
    private String getClientInput(String hint) {
        String message = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            if (hint != null) {
               System.out.println(hint);
            }
            message = reader.readLine();
        } 
        catch (IOException e) {
            System.err.println("Exception in getClientInput()" + e.getMessage());
        }
        return message;
    }

    /**
     * Set the name information before being allowed to chat.
     */
    private void profileSetUp() {
        String line = null;
        while (!isAllowedToChat) {
            try {
                line = in.readLine();
            }
            catch (IOException e) {
                System.err.println("Exception in profiles set up:" + e.getMessage());
            }
            if (line.startsWith(WELCOME)) {
                out.println(getClientInput(WELCOME));
            }
            else if (line.startsWith(ACCEPT)) {
                isAllowedToChat = true;
                System.out.println(ACCEPT);
                System.out.println("To see a list of commands, please type \\help.");
            }
            else {
                System.out.println(line);
            }
        }
    }

    /**
     * Send the outgoing messages to server.
     */
    private void handleOutgoingMessages() {
        Thread sendThread = new Thread(new Runnable() {
            public void run() {
                while (isServerConnected) {
                    out.println(getClientInput(null));
                }
            }
        });
        sendThread.start();
    }

    /**
     * Receive the incoming messages from server.
     */
    public void handleIncomingMessages() {
        Thread listenThread = new Thread(new Runnable() {
            public void run() {
                while (isServerConnected) {
                    String line = null;
                    try {
                        line = in.readLine();
                        if (line == null) {
                            isServerConnected = false;
                            System.err.println("Disconnected from the server.");
                            closeConnection();
                        }
                        System.out.println(line);
                    }
                    catch (IOException e) {
                        isServerConnected = false;
                        System.err.println("Exception in handleIncominMgessages()");
                        break;
                    }
                }
            }
        });
        listenThread.start();
    }

    /**
     * Close the connection between this client to the server.
     */
    private void closeConnection() {
        try {
            socket.close();
            System.exit(0);
        }
        catch (IOException e) {
            System.err.println("Exception in closing the socket.");
            System.err.println(e.getMessage());
        }
    }

}