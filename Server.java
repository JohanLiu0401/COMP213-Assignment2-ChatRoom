import java.io.*;
import java.net.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class Server {

    private ServerSocket ss;
    private int PORT_NUMBER = 4396;
    private static long serverStartTime;
    private HashSet<String> clientNameSet = new HashSet<String>();
    private HashSet<PrintWriter> clientWriterSet = new HashSet<PrintWriter>();
    private static final String WELCOME = "Please type your username.";
    private static final String ACCEPT = "Your username is accepted. Please type messages";
    String[] commands = {"\\help: List all the commands that can be sent", "\\quit: Quit the chat room", "\\serverTime: Server total runtime", "\\clientTime: The time you have been in the chat room", "\\serverIP: Server IP adderss", "\\clientNumber: Total number of clients currently in the chat room"};

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.start();
    }

    private void start() throws IOException {
        ss = new ServerSocket(PORT_NUMBER);
        System.out.println("Server at "+InetAddress.getLocalHost()+" is waiting for connection...");
        Socket socket;
        Thread thread;
        serverStartTime = System.currentTimeMillis();
        //Waiting for connection all the time.
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

    private void broadcast(String message) {
        for (PrintWriter writer : clientWriterSet) {
            writer.println(message);
        }
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
            catch (IOException e) {
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
                System.out.println("Exception in createStreams: "+e);
            }
        }

        private void getClientUsername() {
            while (true) {
                out.println(WELCOME);
                try {
                    clientName = in.readLine();
                }
                catch (IOException e) {
                    System.out.println("Exception in getClientUsername: "+e);
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
            broadcast(clientName + " has entered the chat");
            clientStartTime = System.currentTimeMillis();
            clientWriterSet.add(out);
            System.out.println(clientName + " has entered the chat");
        }

        private void listenClientMessage() throws IOException {
            String line;
            while (in != null) {
                line = in.readLine();
                if (line == null) {
                    break;
                }
                if (line.startsWith("\\")) {
                    if (processClientRequest(line)) {
                        break;
                    }
                }
                else {
                    SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
                    broadcast(clientName + "(" + df.format(new Date()) + "): " + line);
                }
            }
        }

        boolean processClientRequest(String command) {
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
                default:
                    out.println("Invalid command");
            }
            return isQuit;
        }

        private void closeConnection() {
            if (clientName != null) {
                clientNameSet.remove(clientName);
            }
            if (out != null) {
                clientWriterSet.remove(out);
            }

            broadcast(clientName + " has left the chat.");
            
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