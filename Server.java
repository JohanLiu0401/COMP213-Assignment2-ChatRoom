import java.io.*;
import java.net.*;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * public Server is used to handle the session with clients.
 * 
 * @author Zhiyong Liu
 */
public class Server {

    /**
     * The socket of server.
     */
    private ServerSocket ss;

    /**
     * The port number of server.
     */
    private int PORT_NUMBER = 4396;

    /**
     * The start time of server.
     */
    private long serverStartTime;

    /**
     * Store all the client name.
     */
    private HashSet<String> clientNameSet = new HashSet<String>();

    /**
     * Store all the output stream.
     */
    private HashSet<PrintWriter> clientWriterSet = new HashSet<PrintWriter>();

    /**
     * The information of welcome.
     */
    private static final String WELCOME = "Please type your username.";

    /**
     * The information of accept.
     */
    private static final String ACCEPT = "Your username is accepted. Please type messages";

    /**
     * The list of commands available for clients.
     */
    String[] commands = {"\\help: List all the commands that can be sent", "\\quit: Quit the chat room", "\\serverTime: Server total runtime", "\\clientTime: The time you have been in the chat room", "\\serverIP: Server IP adderss", "\\clientNumber: Total number of clients currently in the chat room","\\emoji: The emoji you can send"};

    /**
     * The list of emoji available for clients.
     */
    String[] emoji = {"~^o^~", "\\(╯-╰)/", "//(ㄒoㄒ)//", "(^_^)/~~"};

    /**
     * The entry of Server program.
     * 
     * @param args the information from console
     */
    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.start();
    }

    /**
     * Star the server.
     */
    private void start() throws IOException {
        ss = new ServerSocket(PORT_NUMBER);
        serverStartTime = System.currentTimeMillis();
        System.out.println("Server at "+InetAddress.getLocalHost()+" is waiting for connection...");
        Socket socket;
        Thread thread;
        try{
            while (true) {
                socket = ss.accept();
                thread = new Thread(new HandleSession(socket));
                thread.start();
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        finally {
            shutDown();
        }
    }

    /**
     * Shut down the server.
     */
    private void shutDown() {
        try {
            ss.close();
            System.out.println("The server has shut down.");
        } 
        catch (Exception e) {
            System.out.println("Problem with shutting down the server.");
            System.out.println(e.getMessage());
        }
    }

    /**
     * Broadcast the message to all clients.
     * 
     * @param message the message to broadcast
     */
    private void broadcast(String message) {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        for (PrintWriter writer : clientWriterSet) {
            writer.println("(Time: " + df.format(new Date()) + ") " + message);
        }
        System.out.println("(Time: " + df.format(new Date()) + ") " + message);
    }

    class HandleSession implements Runnable {
        private String clientName;
        private long clientStartTime;
        private Socket socket;
        private BufferedReader in = null;
        private PrintWriter out = null;

        HandleSession(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                createStreams();
                getClientUsername();
                listenClientMessage();
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }
            finally {
                closeConnection();
            }
        }

        private void createStreams() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(),true);
                System.out.println("One connetion is established");
            }
            catch (IOException e) {
                System.err.println("Exception in createStreams: "+e);
            }
        }

        private void getClientUsername() {
            while (true) {
                out.println(WELCOME);
                try {
                    clientName = in.readLine();
                }
                catch (IOException e) {
                    System.err.println("Exception in getClientUsername: "+e);
                }
    
                if (clientName == null) {
                    return;
                }
                
                if (!clientNameSet.contains(clientName)) {
                    clientNameSet.add(clientName);
                    break;
                }
                out.println("Sorry, this usrename is unavailable");
            }
            out.println(ACCEPT);
            clientStartTime = System.currentTimeMillis();
            broadcast(clientName + " has entered the chat (Current online: " + clientNameSet.size() + ")");
            clientWriterSet.add(out);
        }

        private void listenClientMessage() throws IOException {
            String line = null;
            while (true) {
                line = in.readLine(); 
                if(line == null){
                    break;
                }
                if (line.startsWith("\\")) {
                    if (processClientRequest(line)) {
                        break;
                    }
                }
                else {
                    broadcast(clientName + ": " + line);
                }
            }
        }

        private boolean processClientRequest(String command) throws IOException {
            boolean isQuit = false;
            switch (command) {
                case "\\quit":
                    isQuit = true;
                    break;
                case "\\help": 
                    for (String c : commands) {
                        out.println("Command " + c);
                    }
                    break;
                case "\\serverTime":
                    long serverRunTime = (System.currentTimeMillis()-serverStartTime)/1000/60;
                    out.println("server has run for " + serverRunTime + " minutes");
                    break;
                case "\\clientTime":
                    long clientRunTime = (System.currentTimeMillis()-clientStartTime)/1000/60;
                    out.println("you has been in chat room for " + clientRunTime + " minutes");
                    break;
                case "\\serverIP":
                    InetAddress ip = socket.getLocalAddress();
                    out.println("server IP: " + ip);
                    break;
                case "\\clientNumber":
                    out.println("client numbers: " + clientNameSet.size());
                    break;
                case "\\emoji":
                    sendEmoji();
                    break;
                default:
                    out.println("Invalid command");
            }
            return isQuit;
        }

        private void sendEmoji() throws IOException {
            out.println("Please select the emoji you want to send: (enter the number)\n1. Greet\n2. Bored\n3. Sad\n4. Bye");
            boolean isValid = false;
            String line = null;
            while (!isValid) {
                line = in.readLine();
                if(line == null){
                    break;
                }
                if(line.equals("1")){
                    broadcast(clientName + ": " + emoji[0]);
                    isValid = true;
                }
                else if(line.equals("2")){
                    broadcast(clientName + ": " + emoji[1]);
                    isValid = true;
                }
                else if(line.equals("3")){
                    broadcast(clientName + ": " + emoji[2]);
                    isValid = true;
                }
                else if(line.equals("4")){
                    broadcast(clientName + ": " + emoji[3]);
                    isValid = true;
                }
                else{
                    out.println("Invalid emoji, select again:");
                }
            }
        }

        private void closeConnection() {
            if (clientName != null) {
                clientNameSet.remove(clientName);
                broadcast(clientName + " has left the chat.");
            }
            if (out != null) {
                clientWriterSet.remove(out);
            }

            try {
                socket.close();
                System.out.println("The connection of " + clientName + " is closed" );
            }
            catch (IOException e) {
                System.err.println("Exception when closing the socket");
                System.err.println(e.getMessage());
            }
        }
        
    }// end of the HandleSession class
}// end of the Server class