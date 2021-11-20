/***
 * ClientThread
 * Example of a TCP server
 * Date: 14/12/08
 * Authors:
 */

import java.io.*;
import java.net.*;

public class ClientThreadSend
        extends Thread {

    private Socket clientSocket;

    public ClientThreadSend(Socket s) {
        this.clientSocket = s;
    }

    /**
     * receives a request from client then sends an echo to the client
     *
     * @param clientSocket the client socket
     **/
    public void run() {
        BufferedReader stdIn = null;
        PrintStream socOut = null;

        try {
            stdIn = new BufferedReader(new InputStreamReader(System.in));
            socOut = new PrintStream(clientSocket.getOutputStream());

            while (true) {
                System.out.print("User: ");
                String user = stdIn.readLine();
                if (".".equals(user)) break;
                else if (!"".equals(user)) {
                    validateUser(user, socOut);
                    break;
                }
            }
    
            while (true) {
                String line = stdIn.readLine();
                if (".".equals(line)) break;
                socOut.println(line);
            }

            if (stdIn != null) stdIn.close();
            if (socOut != null) socOut.close();
            
        } catch (Exception e) {
            System.err.println("Error in EchoServer:" + e);
        }
    }

    private static void validateUser(String user, PrintStream socOut) {
        try {
            socOut.println("<validate-user:" + user + ">");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

  
