import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;

public class ClientThread extends Thread {

    private Socket clientSocket;
    private String user = "unknown";
    ArrayList<String> participants = new ArrayList();
    private String groupName;
    private String group;
    private String recipient;

    public ClientThread(Socket socketClient) {
        this.clientSocket = socketClient;
    }

    public void run() {
        try {
       
            BufferedReader socIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintStream socOut = new PrintStream(clientSocket.getOutputStream());

            while (true) {
                String line = socIn.readLine();
                if (line.startsWith("<validate-user:")) {
                    String user = line.substring(15, line.length() - 1);
                    EchoServerMultiThreaded.getUsers().put(user, clientSocket);
                    this.user = user;
                    System.out.println("User " + user + " connected");
                }else if(line.startsWith("<group-name:")) {
                    groupName = line.substring(13, line.length() - 1);
                }else if(line.startsWith("<participant:")) {
                    participants.add(line.substring(13, line.length() - 1));
                }else if(line.startsWith("<done")){
                    participants.add(this.user);
                    EchoServerMultiThreaded.createGroup(groupName,participants);
                }else if(line.startsWith("<send-group-message-to:")){
                    group = line.substring(24, line.length() - 1);
                }else if(line.startsWith("<group-message:")) {
                    String message =  "Message from " + user + " at "+ group +": "+ line.substring(15, line.length() - 1);
                    System.out.println(message);
                    EchoServerMultiThreaded.sendGroupMessage(message, clientSocket, group);
                }else if(line.startsWith("<recipient-name:")) {
                    recipient = line.substring(17, line.length() - 1);
                } else if(line.startsWith("<message:")){
                    String message = "Message from " + user + ": " + line.substring(9, line.length() - 1);
                    System.out.println(message);
                    EchoServerMultiThreaded.sendPrivateMessage(message, recipient);
                }
            
            }
            
        } catch (Exception e) {
            System.err.println("Error in EchoServer:" + e);
        }
    }
    
}
