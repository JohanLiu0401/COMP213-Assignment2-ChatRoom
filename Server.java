import java.io.*;
import java.net.*;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * public Server is used to act as chat room server. It contains members: ss, PORT_NUMBER, serverStartTime, clientNameSet,
 * clientWriterSet, WELCOME, ACCEPT, commands, emoji, and methods: start(), shutDown(), broadcast(String message).
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
    private static final int PORT_NUMBER = 4396;

    /**
     * The start time of server.
     */
    private long serverStartTime;

    /**
     * Store all the client names.
     */
    private HashSet<String> clientNameSet = new HashSet<String>();

    /**
     * Store all the output streams.
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
     * The list of commands available to clients.
     */
    private String[] commands = {"\\help: List all the commands that can be sent", "\\quit: Quit the chat room", "\\serverTime: Server total runtime", "\\clientTime: The time you have been in the chat room", "\\serverIP: Server IP adderss", "\\clientNumber: Total number of clients currently in the chat room","\\emoji: The emoji you can send"};

    /**
     * The list of emoji available to clients.
     */
    private String[] emoji = {"~^o^~", "\\(╯-╰)/", "//(ㄒoㄒ)//", "(^_^)/~~"};

    /**
     * The entry of Server program.
     * 
     * @param args the information from console
     * @throws IOException if an I/O error occurs
     */
    public static void main(String[] args) throws IOException {
        try{
            Server server = new Server();
            server.start();
        }
        catch (BindException e) {
            System.out.println("The port number already has been used.");
            System.exit(0);
        }
    }

    /**
     * Start the chat room server.
     * 
     * @throws IOException if an I/O error occurs
     */
    private void start() throws IOException {
        ss = new ServerSocket(PORT_NUMBER);
        serverStartTime = System.currentTimeMillis();
        System.out.println("Server at "+InetAddress.getLocalHost()+" is waiting for connection...\nInput \\quit to close the chat room server.");
        Socket socket;
        Thread thread;
        waitQuit();
        try {
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
     * Wait for input command to close the server.
     * <br>Additional feature: The server can be closed by command.
     */
    private void waitQuit() {
        Thread waitQuitThread = new Thread(new Runnable(){
            public void run() {
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                String quitCommand = null;
                try{
                    while(in != null) {
                        quitCommand = in.readLine();
                        if (quitCommand.equals("\\quit")) {
                            broadcast("The server is shut down.");
                            System.exit(0);
                        }
                        else {
                            System.out.println("command input wrong.");
                        }
                    }
                }
                catch(IOException e) {
                    System.err.println("IOException in waitQuit():\n" + e.getMessage());
                }
            }
        });
        waitQuitThread.start();
    }

    /**
     * Shut down the chat room server.
     */
    private void shutDown() {
        try {
            ss.close();
            System.out.println("The server has shut down.");
            System.exit(0);
        } 
        catch (Exception e) {
            System.out.println("Problem with shutting down the server.");
            System.out.println(e.getMessage());
        }
    }

    /**
     * Broadcast the message to all clients.
     * <br>Additional feature: It will print the time when the message
     * is broadcast or someone enter the chat room.
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

    /**
     * HandleSession is an inner class of Server. It contains members: clientName, clientStartName, socket, in, out,
     * and methods: run(), createStreams(), getClientUsername(), listenClientMessage(), processClientRequest(String command),
     * sendEmoji(), closeConnection().
     * 
     * @author Zhiyong Liu
     */
    class HandleSession implements Runnable {

        /**
         * The name of client in this session.
         */
        private String clientName;

        /**
         * The time when client enters the chat room.
         */
        private long clientStartTime;

        /**
         * The socket to connect to the client.
         */
        private Socket socket;

        /**
         * The input stream of server.
         */
        private BufferedReader in = null;

        /**
         * The output stream of server.
         */
        private PrintWriter out = null;

        /**
         * The constructor of HandleSession class.
         * 
         * @param socket the socket for the client
         */
        HandleSession(Socket socket) {
            this.socket = socket;
        }

        /**
         * Configure and start the session with client.
         */
        public void run() {
            try {
                createStreams();
                getClientUsername();
                listenClientMessage();
            }
            catch (Exception e) {
                System.err.println("Some Exception occurs:\n" + e.getMessage());
            }
            finally {
                closeConnection();
            }
        }

        /**
         * Create the input and output stream of server.
         */
        private void createStreams() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(),true);
                System.out.println("One connetion is established");
            }
            catch (IOException e) {
                System.err.println("Exception in createStreams:\n" + e.getMessage());
            }
        }

        /**
         * Get the username from client.
         * <br>Additional feature: It can also test whether the input name is empty.
         */
        private void getClientUsername() {
            while (true) {
                out.println(WELCOME);
                try {
                    clientName = in.readLine();
                }
                catch (IOException e) {
                    System.err.println("Exception in getClientUsername:\n" + e.getMessage());
                }
    
                if (clientName == null) {
                    return;
                }
                
                if (clientName.equals("")) {
                    out.println("Sorry, you can not set the name as empty");
                    continue;
                }
                
                synchronized(clientNameSet){
                    if (!clientNameSet.contains(clientName)) {
                        clientNameSet.add(clientName);
                        break;
                    }
                }
                out.println("Sorry, this usrename is unavailable");
            }
            out.println(ACCEPT);
            clientStartTime = System.currentTimeMillis();
            broadcast(clientName + " has entered the chat (Current online: " + clientNameSet.size() + ")");
            clientWriterSet.add(out);
        }

        /**
         * Listen the message from client.
         * 
         * @throws IOException if an I/O error occurs
         */
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

        /**
         * Process the command from client.
         * <br>Additional feature:
         * <br>1. It can test if the command is valid.
         * <br>2. It can allow client to send four different emojis.
         * 
         * @param command the command user input
         * @throws IOException if an I/O error occurs
         * @return boolean
         */
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
                    out.println("------ case sensitive ------");
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

        /**
         * Process the request for sending emoji from client.
         * It is an additional command which allows client to send emoji.
         * 
         * @throws IOException if an I/O error occurs
         */
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

        /**
         * Close the session with the client.
         */
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