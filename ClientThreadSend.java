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
                if (!"".equals(user)) {
                    validateUser(user, socOut);
                    break;
                }
            }

            while (true) {
                System.out.print("Menu: \n" +
                        "\t1- Send private message\n" +
                        "\t2- Create group chat\n"+
                        "\t3- Send message to a group chat\n");
                String option = stdIn.readLine();
                switch (option){
                    case "1":
                        sendMessage(stdIn,socOut);
                        break;
                    case "2":
                        createGroup(stdIn,socOut);
                        break;
                    case "3":
                        //groupMessage();
                        break;
                    default: break;
                }
                break;
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

    private static void sendMessage(BufferedReader stdIn, PrintStream socOut) throws IOException {
        while (true) {
            String line = stdIn.readLine();
            if (".".equals(line)) break;
            socOut.println(line);
        }
    }

    private static void createGroup(BufferedReader stdIn, PrintStream socOut) throws IOException {
        System.out.print("Enter the group name");
        String name = stdIn.readLine();
        socOut.println("<group-name:"+name+">");

        while (true) {
            System.out.print("Enter the name of a participant");
            String line = stdIn.readLine();
            if (".".equals(line)) break;
            socOut.println("<participant:"+line+">");
        }
    }
}

  
