
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

    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
            System.out.println("Usage: java EchoClient <EchoServer host> <EchoServer port>");
            System.exit(1);
        }

        try {
            //Data declaration
            ArrayList<String> myGroups;
            final String ip = args[0];
            final int port = Integer.parseInt(args[1]);

            //Connection setup
            Socket echoSocket = new Socket(ip, port);
            ClientThreadController ctc = new ClientThreadController(echoSocket);


        } catch (UnknownHostException e) {
            System.err.println("Don't know about host:" + args[0]);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for " + "the connection to:" + args[0]);
            System.exit(1);
        }
    }
}
