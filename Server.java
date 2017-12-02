import java.io.*;
import java.net.*;
import java.util.HashSet;

public class Server {

    private ServerSocket ss;
    private int PORT_NUMBER = 4396;
    private static final String WELCOME = "Please type your username.";
    private static final String ACCEPT = "Your username is accepted. Please type messages";
    private HashSet<String> clientNameSet = new HashSet<String>();
    private HashSet<PrintWriter> clientWriterSet = new HashSet<PrintWriter>();

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.start();
    }

    private void start() throws IOException {
        ss = new ServerSocket(PORT_NUMBER);
        System.out.println("Server at "+InetAddress.getLocalHost()+" is waiting for connection...");
        Socket socket;
        Thread thread;
        //Waiting for connection all the time.
        try{
            while(true) {
                socket = ss.accept();
                thread = new Thread(new HandleSession(socket));
                thread.start();
            }
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
        }
        finally{
            shutDown();
        }
    }

    private void shutDown() {
        try{
            ss.close();
            System.out.println("The server has shut down.");
        } 
        catch (Exception e) {
            System.out.println("Problem with shutting down the server.");
            System.out.println(e.getMessage());
        }
    }

    private void broadcast(String message){
        for(PrintWriter writer : clientWriterSet) {
            writer.println(message);
        }
    }

    class HandleSession implements Runnable {
        private String clientName;
        private Socket socket;
        BufferedReader in = null;
        PrintWriter out = null;
        HandleSession(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                createStreams();
                getClientUsername();
                listenClientMessage();
            }
            catch(IOException e) {
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
                clientWriterSet.add(out);
                System.out.println("One connetion is established");
            }
            catch(IOException e) {
                System.out.println("Exception in createStreams: "+e);
            }
        }

        private void getClientUsername() {
            while(true) {
                out.println(WELCOME);
                try {
                    clientName = in.readLine();
                }
                catch(IOException e) {
                    System.out.println("Exception in getClientUsername: "+e);
                }
    
                if(clientName == null) {
                    return;
                }
                if(!clientNameSet.contains(clientName)) {
                    clientNameSet.add(clientName);
                    break;
                }
                out.println("Sorry, this usrename is unavailable");
            }
            out.println(ACCEPT);
            broadcast(clientName + " has entered the chat");
            System.out.println(clientName + " has entered the chat");
        }

        private void listenClientMessage() throws IOException {
            String line;
            while(in != null) {//用 true 可以吗
                line = in.readLine();
                if(line == null) {
                    break;
                }
                if(line.startsWith("\\")){

                }
                else {
                    broadcast(clientName + " said: " + line);
                }
            }
        }


        private void closeConnection() {
            if(clientName != null) {
                broadcast(clientName + " has left the chat.");
                clientNameSet.remove(clientName);
            }
            if(out != null){
                clientWriterSet.remove(out);
            }

            try{
                socket.close();
                System.out.println("The connection of " + clientName + " is closed" );
            }
            catch(IOException e) {
                System.err.println("Exception when closing the socket");
                System.err.println(e.getMessage());
            }
        }
        
    }// end of the HandleSession class
}// end of the Server class