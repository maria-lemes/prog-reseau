
/***
 * ClientThread
 * Example of a TCP server
 * Date: 14/12/08
 * Authors:
 */

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class ClientThreadSend extends Thread {

    private Socket clientSocket;
    private PrintStream socOut;

    public ClientThreadSend(Socket s) throws IOException {
        this.clientSocket = s;
        this.socOut = new PrintStream(clientSocket.getOutputStream());
    }

    /**
     * receives a request from client then sends an echo to the client
     *
     **/
    public void run() {
        try {
            while (true) {

            }
        } catch (Exception e) {
            System.err.println("Error in ClientThreadSend:" + e);
        }
    }

    protected void validateUser(String username) {
        socOut.println("<validate-user:" + username + ">");
    }

    protected void sendMessage() throws IOException {
        System.out.print("Enter recipient's username: ");
        String recipient = ClientThreadController.getStdIn().readLine();
        System.out.println("Private conversation started with " + recipient);
        while (true) {
            String request = "<private-message>@next@<recipient:" + recipient + ">@next@";
            String line = ClientThreadController.getStdIn().readLine();
            if (".".equals(line))
                break;
            request += "<message:" + line + ">";
            socOut.println(request);
        }
    }

    protected void createGroup(String groupName, ArrayList<String> participants) throws IOException {
        String request = "<create-group>@next@";
        request += "<group-name:" + groupName + ">@next@";
        for (String participant : participants) {
            request += "<participant:" + participant + ">@next@"; 
        }
        socOut.println(request);
    }

    protected void groupMessage(String groupKey) throws IOException {
        while (true) {
            String request = "<group-message>@next@<group-key:" + groupKey + ">@next@";
            String line = ClientThreadController.getStdIn().readLine();
            if (".".equals(line)) break;
            request += "<message:" + line + ">";
            socOut.println(request);
            System.out.println(request);
        }
    }

    protected void getGroups() {
        socOut.println("<get-groups>");
    }

    protected void disconnect() {
        socOut.println("<disconnect>");
    }

 }
