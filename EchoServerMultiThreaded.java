
/***
 * EchoServer
 * Example of a TCP server
 * Date: 10/01/04
 * Authors:
 */

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EchoServerMultiThreaded {

    private static Map<String, Socket> users = new HashMap<>();

    public static Map<String, Socket> getUsers() { return users; }
    public static void addUser(String username, Socket socket) { users.put(username, socket); }
    public static HashMap<String,ArrayList<String>> groups = new HashMap<>();
    //history: <user,<sender,messageRecord>>
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

    protected synchronized static void diffuseMessage(String message, Socket sender) {
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

    public synchronized static void createGroup(String name, ArrayList<String> usersList) {
        //TODO: verificar se user existe na criação do grupo
        groups.put(name,usersList);
        System.out.println("Group created : " + usersList);
        System.out.println("Groups list : " + groups);
    }

    public synchronized static void sendGroupMessage(String message, String sender, String group){
        System.out.println(group);
        if(groups.get(group) != null) {
            for (String user : groups.get(group)) {
                if (!user.equals(sender)) {
                    try {
                         if(users.get(user)  == null ){
                            addToHistory(user, message, group);
                        }else{
                            PrintStream socOut = new PrintStream(users.get(user).getOutputStream());
                            socOut.println(message);
                            //addToHistory(receiver, message, group);
                        }
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

    public synchronized static void sendPrivateMessage(String message, String receiver, String sender){
       try{
           if (!users.containsKey(receiver)) {
               PrintStream socOut = new PrintStream(users.get(sender).getOutputStream());
               socOut.println("This user doesn't exist");
           } else if(users.get(receiver)  == null ){
               PrintStream socOut = new PrintStream(users.get(sender).getOutputStream());
               socOut.println("This user is offline");
               addToHistory(receiver, message, sender);
           }else{
               PrintStream socOut = new PrintStream(users.get(receiver).getOutputStream());
               socOut.println(message);
               //addToHistory(receiver, message, sender);
           }
       } catch (Exception e) {
           e.printStackTrace();
       }
    }

    public synchronized static void addToHistory(String receiver, String message, String conversation) throws IOException {
        boolean newConversation;
        if(history.get(receiver) == null){
            Map<String, ArrayList<String>> convRecord = new HashMap<>();
            ArrayList<String> messageRecord = new ArrayList<>();
            messageRecord.add(message);
            convRecord.put(conversation,messageRecord);
            history.put(receiver,convRecord);
            newConversation = true;
        }else if(history.get(receiver).get(conversation) == null){
            ArrayList<String> messageRecord = new ArrayList<>();
            messageRecord.add(message);
            history.get(receiver).put(conversation,messageRecord);
            newConversation = true;
        }else{
            history.get(receiver).get(conversation).add(message);
            newConversation = false;
        }

        saveHistory(receiver,conversation,message,newConversation);

    }

    public synchronized static void showHistory(String user){

            System.out.println("History map: " + history.get(user));
            PrintStream socOut = null;
            try {
                if( history.get(user)!= null) {
                    socOut = new PrintStream(users.get(user).getOutputStream());
                    socOut.println("Messages you received while you were offline:\n");
                    for (String conversation : history.get(user).keySet()) {
                        socOut.println("------" + history.get(user).keySet() + "-----");
                        for (String message : history.get(user).get(conversation)) {
                            socOut.println(message);
                        }
                        socOut.println("-----------------");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public synchronized static void disconectUser(String username) {
        try {
            users.get(username).close();
            users.put(username,null);
            System.out.println(users);
            System.out.println("User " + username + " disconnected");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized static void saveHistory(String receiver,String conversation, String message, boolean newFile) throws IOException {
        String content;
        File file;

        if(newFile) {
            file = new File(receiver+"-"+conversation + ".txt");
            content = "Messages sent in: " + conversation + "\n";
            file.createNewFile();
        }else{
            content = Files.readString(Path.of(receiver+"-"+conversation + ".txt"));
        }
            content = content + "\n" + message;

        FileWriter fw = new FileWriter(receiver+"-"+conversation + ".txt");
        BufferedWriter bw = new BufferedWriter(fw);

        bw.write(content);
        bw.close();


    }

}
