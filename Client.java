import java.io.*;
import java.net.Socket;

/**
 * public Client is used to acted as a chat room client.
 * It is the entry of client program.
 * 
 * @author Zhiyong Liu
 */
public class Client {

    /**
     * The entry of Client Program.
     * 
     * @param args the information from console
     * @throws Exception if some exception occurs
     */
    public static void main(String[] args) throws Exception {
        ClientInstance client = new ClientInstance();
        client.start();
    }
}

/**
 * ClientInstance is used by Client.
 * <br>It contains members: WELCOME, ACCEPT, socket, in, out, isServerConnected, isAllowedToChat,
 * and methods: start(), establishConnection(), getClientInput(String hint), profileSetUp(),
 * handleOutgoingMessages(), handleIncomingMessages(), closeConnection().
 */
class ClientInstance {

    /**
     * The port number of client.
     */
    private static final int PORT_NUMBER = 4396;

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
     * Start the whole process of client program.
     */
    public void start() {
        establishConnection();
        handleIncomingMessages();
        handleOutgoingMessages();
    }

    /**
     * Establish the connection to the server.
     * <br>Additional feature: it can ask user to retype the IP address in two situations:
     * <br>1. The input IP address is not valid.
     * <br>2. Server goes offline when user inputs username.
     */
    private void establishConnection() {
        boolean isEnterChatRoom = false;
        while (!isEnterChatRoom) {
            String serverAddress = getClientInput("What is the address of the server that you wish to connect to?");
            try {
                socket = new Socket(serverAddress, PORT_NUMBER);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                isServerConnected = true;
                profileSetUp();
                isEnterChatRoom = true;
            }
            catch (IOException e) {
                System.err.println("IOException in connection: " + e.getMessage());
            }
            catch (NullPointerException e) {
                //Catch the NullPointerException in profileSetUp().
                System.err.println("NullPointerException in profileSetUP(): " + e.getMessage());
                System.err.println("The server may has been shut down.");
            }
        }
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
            System.err.println("Exception in getClientInput():" + e.getMessage());
        }
        return message;
    }

    /**
     * Set the name information before being allowed to chat.
     * <br>Additional feature: it can test whether the server is offline when user inputs name.
     * If server goes offline, after user input username, it will throw NullPointerException to the establishConnection()
     * and ask user to retype the ip address they want to connect.
     */
    private void profileSetUp() throws NullPointerException {
        String line = null;
        while (!isAllowedToChat) {
            try {
                line = in.readLine();
            }
            catch (IOException e) {
                System.err.println("Exception in profiles set up:" + e.getMessage());
            }
            if (line.startsWith(WELCOME)) {
                // If server is offline at this time, it will throw NullPointerException.
                out.println(getClientInput(WELCOME));
            }
            else if (line.startsWith(ACCEPT)) {
                isAllowedToChat = true;
                System.out.println(ACCEPT);
                System.out.println("------ Command List: \\help   Quit: \\quit ------");
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
    private void handleIncomingMessages() {
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
                        System.err.println("Exception in handleIncominMgessages():" + e.getMessage());
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