import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;

public class ClientThread extends Thread {

    private Socket clientSocket;
    private String username = "unknown";

    public ClientThread(Socket socketClient) {
        this.clientSocket = socketClient;
    }

    public void run() {
        try {
       
            BufferedReader socIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintStream socOut = new PrintStream(clientSocket.getOutputStream());

            while (true) {
                String line = socIn.readLine();

                //Client disconnected
                if (line.startsWith("<disconnect>")) {
                    EchoServerMultiThreaded.disconectUser(username);
                    break;
                }

                else if (line.startsWith("<validate-user:")) {
                    String user = line.substring(line.indexOf(":") + 1, line.length() - 1);
                    this.username = user;
                    EchoServerMultiThreaded.addUser(user, clientSocket);
                }

                else if (line.startsWith("<get-groups>")) {
                    String groups = EchoServerMultiThreaded.getUserGroups(username);
                    EchoServerMultiThreaded.sendServerResponse(groups, username);
                }

                else if (line.startsWith("<create-group>")) {
                    String[] requests = line.split("@next@");
                    String groupName = "";
                    for (String request : requests) {
                        if (request.startsWith("<group-name:")) {
                            groupName = request.substring(request.indexOf(":") + 1, request.length() - 1);
                            EchoServerMultiThreaded.createGroup(groupName, username);

                        } else if (request.startsWith("<participant:")) {
                            String participantName = request.substring(request.indexOf(":") + 1, request.length() - 1);
                            EchoServerMultiThreaded.addUserToGroup(groupName, username, participantName);
                        }
                    }
                }

                else if (line.startsWith("<group-message>")) {
                    String[] requests = line.split("@next@");
                    String groupKey = "";
                    for (String request : requests) {
                        if (request.startsWith("<group-key:")) {
                            groupKey = request.substring(request.indexOf(":") + 1, request.length() - 1);
                        } else if (request.startsWith("<message:")) {
                            String message = request.substring(request.indexOf(":") + 1, request.length() - 1);
                            EchoServerMultiThreaded.sendGroupMessage(message, username, groupKey);
                        }
                    }
                } 
                else if (line.startsWith("<private-message>")) {
                    String[] requests = line.split("@next@");
                    String recipient = "";
                    for (String request : requests) {
                        if (request.startsWith("<recipient:")) {
                            recipient = request.substring(request.indexOf(":") + 1, request.length() - 1);
                        } else if (request.startsWith("<message:")) {
                            String message = request.substring(request.indexOf(":") + 1, request.length() - 1);
                            String serverResponse = EchoServerMultiThreaded.sendPrivateMessage(message, username, recipient);
                            if (!serverResponse.equals("done")) {
                                socOut.println(serverResponse);
                            }
                        }
                    }
                }
            
            }
        } catch (Exception e) {
            System.err.println("Server error:" + e);
        }
    }
    
}
