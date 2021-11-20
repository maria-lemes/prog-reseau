
/***
 * EchoServer
 * Example of a TCP server
 * Date: 10/01/04
 * Authors:
 */

import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;

public class EchoServerMultiThreaded {

    public static ArrayList<Socket> users = new ArrayList<>();

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
                ClientThread ct = new ClientThread(clientSocket, users);
                users.add(clientSocket);
                System.out.println(users);
                ct.start();

            }
        } catch (Exception e) {
            System.err.println("Error in EchoServer:" + e);
        }
    }

    protected static void diffuseMessage(String message, Socket sender ,ArrayList<Socket> users) {
        System.out.println("Diffusing message to " + users.size() + " users.");
        for (Socket s : users) {
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
