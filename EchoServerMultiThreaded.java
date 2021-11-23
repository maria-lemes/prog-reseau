
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
    public static HashMap<String,ArrayList<String>> groups = new HashMap<>();
    public static Map<String,Map<String,ArrayList<String>>> history = new HashMap<>();

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
        groups.put(name,usersList);
        System.out.println("Group created : " + usersList);
        System.out.println("Groups list : " + groups);
    }

    public static void sendGroupMessage(String message, String sender, String group){
        System.out.println(group);
        if(groups.get(group) != null) {
            for (String user : groups.get(group)) {
                if (!user.equals(sender)) {
                    try {
                        PrintStream socOut = new PrintStream(users.get(user).getOutputStream());
                        socOut.println(message);
                        addToHistory(user, message, group);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else{
            //TODO: podemos affichar uma lista dos grupos aos quais o user pertence
            try {
                PrintStream socOut = new PrintStream(users.get(sender).getOutputStream());
                socOut.println("This group doesn't exist");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void sendPrivateMessage(String message, String receiver, String sender){
       try{
           if(users.containsKey(receiver)) {
               PrintStream socOut = new PrintStream(users.get(receiver).getOutputStream());
               socOut.println(message);
               addToHistory(receiver, message, sender);
           }else{
               PrintStream socOut = new PrintStream(users.get(sender).getOutputStream());
               socOut.println("This user doesn't exist");
           }
       } catch (Exception e) {
           e.printStackTrace();
       }
    }

    public static void addToHistory(String receiver, String message, String conversation){
        if(history.get(receiver) == null){
            Map<String, ArrayList<String>> convRecord = new HashMap<>();
            ArrayList<String> messageRecord = new ArrayList<>();
            messageRecord.add(message);
            convRecord.put(conversation,messageRecord);
            history.put(receiver,convRecord);
        }else if(history.get(receiver).get(conversation) == null){
            ArrayList<String> messageRecord = new ArrayList<>();
            messageRecord.add(message);
            history.get(receiver).put(conversation,messageRecord);
        }else{
            history.get(receiver).get(conversation).add(message);
        }

        showHistory();

    }

    public static void showHistory(){
        System.out.println("History map: " + history);
    }

}
