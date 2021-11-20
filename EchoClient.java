
/***
 * EchoClient
 * Example of a TCP client 
 * Date: 10/01/04
 * Authors:
 */

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class EchoClient {

    private static BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

    /**
     * main method accepts a connection, receives a message from client then sends
     * an echo to the client
     **/
    public static void main(String[] args) throws IOException {

        Socket echoSocket = null;

        if (args.length != 2) {
            System.out.println("Usage: java EchoClient <EchoServer host> <EchoServer port>");
            System.exit(1);
        }

        try {
            // creation socket ==> connexion
            echoSocket = new Socket(args[0], Integer.parseInt(args[1]));

            while (true) {
                System.out.print("User: ");
                String user = stdIn.readLine();
                if (".".equals(user)) break;
                else if (validateUser(user, echoSocket)) {
                    System.out.println("User connected");
                    break;
                }
                System.out.println("User doesn't exist");
            }

            ClientThreadSend cts = new ClientThreadSend(echoSocket);
            ClientThreadReceive ctr = new ClientThreadReceive(echoSocket);
            cts.start();
            ctr.start();

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host:" + args[0]);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for " + "the connection to:" + args[0]);
            System.exit(1);
        }

    }

    private static boolean validateUser(String user, Socket socket) {
        boolean valid = true;
        try {
            PrintStream socOut = new PrintStream(socket.getOutputStream());
            BufferedReader socIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send validate request to server
            socOut.println("<validate-user:" + user + ">");
            String serverResponse;

            while (!(serverResponse = socIn.readLine()).startsWith("<validate-user:")) 

            if (serverResponse.substring(16, 17).equals("t")) valid = true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return valid;
    }

}
