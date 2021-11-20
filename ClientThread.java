import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;

public class ClientThread extends Thread {

    private Socket clientSocket;
    private String user = "unknown";

    public ClientThread(Socket socketClient) {
        this.clientSocket = socketClient;
    }

    public void run() {
        try {
       
            BufferedReader socIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintStream socOut = new PrintStream(clientSocket.getOutputStream());

            System.out.println("Client started ... ");
            while (true) {
                String line = socIn.readLine();
                if (line.startsWith("<validate-user:")) {
                    String user = line.substring(15, line.length() - 1);
                    EchoServerMultiThreaded.getUsers().put(clientSocket, user);
                    this.user = user;
                    System.out.println("User " + user + " connected");
                } else {
                    String message = "Message from " + user + ": " + line;
                    System.out.println(message);
                    EchoServerMultiThreaded.diffuseMessage(message, clientSocket);
                }
            
            }
            
        } catch (Exception e) {
            System.err.println("Error in EchoServer:" + e);
        }
    }
    
}
