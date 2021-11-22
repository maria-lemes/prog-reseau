import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ClientThreadController {

    private Socket clientSocket;
    private ClientThreadSend cts;
    private ClientThreadReceive ctr;
    private static final BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
    private Map<String, String> groups;
    private String username;

    public ClientThreadController(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        cts = new ClientThreadSend(clientSocket);
        ctr = new ClientThreadReceive(clientSocket, this);
        groups = new HashMap<>();
        start();
    } 


    private void start() throws IOException {
        cts.start();
        ctr.start();

        System.out.print("User: ");
        username = stdIn.readLine();
        cts.validateUser(username);

        while (true) {
            System.out.print("Menu: \n" +
                    "\t1- Send private message\n" +
                    "\t2- Create group chat\n"+
                    "\t3- Send message to a group chat\n"+
                    "\t0- Exit\n");
            String option = stdIn.readLine();
            switch (option){
                case "1":
                    cts.sendMessage();
                    break;
                case "2":
                    createGroup();
                    break;
                case "3":
                    groupMessage();
                    break;
                case "0":
                    cts.disconnect();
                    return;
                default: break;
            }
        }
    }

    protected static BufferedReader getStdIn() {
        return stdIn;
    }

    protected synchronized void closeConnection() throws IOException {
        cts.interrupt();
        ctr.interrupt();
        clientSocket.close();
        System.exit(0);
    }

    protected synchronized void setUserGroups(String groups) {
        String[] requests = groups.split("@next@");
        for (String request : requests) {
            if (request.startsWith("<groupkey:")) {
                String groupKey = request.substring(request.indexOf(":") + 1, request.length() - 1);
                String groupName = groupKey.substring(groupKey.indexOf(":") + 1);
                this.groups.put(groupName, groupKey);
            }
        }
    }

    protected void createGroup() throws IOException {
        System.out.print("Enter the group name: ");
        String groupName = stdIn.readLine();
        ArrayList<String> participants = new ArrayList<>();
        while (true) {
            System.out.println("Enter the name of a participant to add to the group or 'done' to end");
            String line = stdIn.readLine();
            if ("done".equals(line)) {
                break;
            } else {
                participants.add(line);
            }
        }
        groups.put(groupName, groupName + ":" + username);
        cts.createGroup(groupName, participants);
    }

    protected synchronized void groupMessage() throws IOException {
        System.out.print("Enter the group name: ");
        String name = stdIn.readLine();
        String groupKey = groups.get(name);
        if (groupKey != null) {
            cts.groupMessage(groupKey);
        }
    }

    protected synchronized void addGroup(String groupKey) {
        String groupName = groupKey.substring(0, groupKey.indexOf(":"));
        groups.put(groupName, groupKey);
        System.out.println("You've been added to the group " + groupName);
    }
}
