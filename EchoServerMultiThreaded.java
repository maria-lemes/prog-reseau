
/***
 * EchoServer
 * Example of a TCP server
 * Date: 10/01/04
 * Authors:
 */

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EchoServerMultiThreaded {

    private static Map<Socket, String> users = new HashMap<>();

    public static Map<Socket, String> getUsers() { return users; }
    public static void addUser(Socket socket, String username) { users.put(socket, username); }

    /**
     * main method
     **/
    public static void main(String args[]) {

        ServerSocket listenSocket;
        ArrayList<String> usernames = new ArrayList<>();
        usernames.add("gus");

        if (args.length != 1) {
            System.out.println("Usage: java EchoServer <EchoServer port>");
            System.exit(1);
        }
        try {
            listenSocket = new ServerSocket(Integer.parseInt(args[0])); // port
            System.out.println("Server ready...");

            while (true) {
                Socket clientSocket = listenSocket.accept();
                ClientThread ct = new ClientThread(clientSocket);
                System.out.println(users);
                ct.start();

            }
        } catch (Exception e) {
            System.err.println("Error in EchoServer:" + e);
        }
    }

    protected static void diffuseMessage(String message, Socket sender) {
        System.out.println("Diffusing message to " + (getUsers().size() - 1) + " users.");
        for (Socket s : getUsers().keySet()) {
            if (!s.equals(sender)) {
                try {
                    PrintStream socOut = new PrintStream(s.getOutputStream());
                    socOut.println(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
