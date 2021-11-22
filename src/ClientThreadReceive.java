
/***
 * ClientThread
 * Example of a TCP server
 * Date: 14/12/08
 * Authors:
 */

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class ClientThreadReceive extends Thread {

    private Socket clientSocket;
    private ClientThreadController ctc;

    public ClientThreadReceive(Socket s, ClientThreadController ctc) {
        this.clientSocket = s;
        this.ctc = ctc;
    }

    /**
     * receives a request from client then sends an echo to the client
     *
     **/
    public void run() {
        try {

            BufferedReader socIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            while (true) {
                String line = socIn.readLine();

                if (line.startsWith("<user-groups>")) {
                    ctc.setUserGroups(line);
                }

                else if (line.startsWith("<disconnected>")) {
                    ctc.closeConnection();
                    break;
                }

                else if (line.startsWith("<added-to:")) {
                    String groupKey = line.substring(line.indexOf(":") + 1, line.length() - 1);
                    ctc.addGroup(groupKey);
                    break;
                }

                else {
                    System.out.println(line);
                }
            }

        } catch (Exception e) {
            System.err.println("Error in ClientThreadReceiver:" + e);
        }
    }
}
