
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

    private static Map<String, Socket> users = new HashMap<>();

    public static Map<String, Socket> getUsers() { return users; }
    public static void addUser(String username, Socket socket) { users.put(username, socket); }
    public static HashMap<String,ArrayList<Socket>> groups = new HashMap<>();

    /**
     * main method
     **/
    public static void main(String args[]) {

        ServerSocket listenSocket;

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
            System.err.println("Error in EchoServerMultiThreaded:" + e);
        }
    }

    protected static void diffuseMessage(String message, Socket sender) {
        System.out.println("Diffusing message to " + (getUsers().size() - 1) + " users.");
        for (Socket s : getUsers().values()) {
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

    public static void createGroup(String name, ArrayList<String> usersList) {
        ArrayList<Socket> sUsers = new ArrayList<>();
        for(String u : usersList){
            sUsers.add(users.get(u));
        }
        groups.put(name,sUsers);
        System.out.println("Group created : " + usersList);
        System.out.println("Groups list : " + groups);
    }
}
