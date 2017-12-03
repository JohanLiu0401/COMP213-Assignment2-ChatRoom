import java.io.*;
import java.net.Socket;

public class Client {
    public static void main(String[] args) throws Exception {
        ClientInstance client = new ClientInstance();
        client.start();
    }
}

class ClientInstance {
    private int portNumber = 4396;
    private static final String WELCOME = "Please type your username.";
    private static final String ACCEPT = "Your username is accepted. Please type messages";

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean isServerConnected = false;
    private boolean isAllowedToChat = false;
    private String clientName = null;

    public void start() {
        establishConnection();
        handleIncomingMessages();
        handleOutgoingMessages();
    }

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

    private String getClientInput(String hint) {
        String message = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            if (hint != null) {
               System.out.println(hint);
            }
            message = reader.readLine();
            if (!isAllowedToChat) {
                clientName = message;
            }
        } catch (Exception e) {
            System.err.println("Exception in getClientInput()" + e.getMessage());
        }
        return message;
    }

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