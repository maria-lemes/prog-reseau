import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;

import javax.lang.model.util.ElementScanner8;

public class ClientThread extends Thread {

    private Socket clientSocket;
    private ArrayList<Socket> users;
    private ArrayList<String> usernames;

    public ClientThread(Socket socketClient, ArrayList<Socket> users, ArrayList<String> usernames) {
        this.clientSocket = socketClient;
        this.users = users;
        this.usernames = usernames;
    }

    public void run() {
        try {
       
            BufferedReader socIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintStream socOut = new PrintStream(clientSocket.getOutputStream());

            while (true) {
                String line = socIn.readLine();
                if (line.startsWith("<validate-user:")) {
                    String user = line.substring(15, line.length() - 1);
                    if (usernames.contains(user)) {
                        System.out.println("User " + user + " connected");
                        socOut.println("<validate-user:t>");
                    } else {
                        System.out.println("User " + user + " doesn't exist");
                        socOut.println("<validate-user:f>");
                    }
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
