import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;

public class ClientThread extends Thread {

    private Socket clientSocket;
    private ArrayList<Socket> users;

    public ClientThread(Socket socketClient, ArrayList<Socket> users) {
        this.clientSocket = socketClient;
        this.users = users;
    }

    public void run() {
        try {
       
            BufferedReader socIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintStream socOut = new PrintStream(clientSocket.getOutputStream());

            while (true) {
                String line = socIn.readLine();
                if (line.startsWith("<validate-user:")) {
                    String user = line.substring(16, line.length() - 2);
                    System.out.println("User validation : " + user);
                } else {
                    System.out.println("Message received: " + line);
                    socOut.println("Server responded: " + line);
                    EchoServerMultiThreaded.diffuseMessage(line, clientSocket, users);
                }
            
            }
            
        } catch (Exception e) {
            System.err.println("Error in EchoServer:" + e);
        }
    }
    
}
