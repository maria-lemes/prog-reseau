
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
import java.util.*;

public class EchoServerMultiThreaded {

    private static Map<String, Socket> users = new HashMap<>();

    public static Map<String, Socket> getUsers() { return users; }
    public static void addUser(String username, Socket socket) { users.put(username, socket); }
    public static HashMap<String,ArrayList<String>> groups = new HashMap<>();
    //history: <user,<sender,messageRecord>>
    public static Map<String,Map<String,ArrayList<String>>> offlineHistory = new HashMap<>();
    public static Map<String, Set<String>> privateChats = new HashMap<>();

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

    public synchronized static void sendGroupMessage(String message, String sender, String group) throws IOException {
        System.out.println(group);
        if(groups.get(group) != null) {
            for (String user : groups.get(group)) {
                if (!user.equals(sender)) {
                    try {
                        //si l'utilisateur n'est pas enligne
                         if(users.get(user)  == null ){
                            addToOfflineHistory(user, message, group);
                        }else{
                            PrintStream socOut = new PrintStream(users.get(user).getOutputStream());
                            socOut.println(message);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            saveFullHistory(group,message);
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
        String chatName;
       try{
           if (!users.containsKey(receiver)) {
               PrintStream socOut = new PrintStream(users.get(sender).getOutputStream());
               socOut.println("This user doesn't exist");
           } else {
               if (users.get(receiver) == null) {
                   //si l'utilisateur n'est pas enligne
                   PrintStream socOut = new PrintStream(users.get(sender).getOutputStream());
                   socOut.println("This user is offline");
                   addToOfflineHistory(receiver, message, sender);
               } else {
                   PrintStream socOut = new PrintStream(users.get(receiver).getOutputStream());
                   socOut.println(message);
               }
               //création une conversation privée avec un nom pour sauvegarder l'historique
               if(privateChats.containsKey(receiver+"-"+sender)){
                   chatName = receiver+"-"+sender;
               }else if(privateChats.containsKey(sender+"-"+receiver)){
                   chatName = sender+"-"+receiver;
               }else{
                   Set<String> privateChat = new HashSet<String>();
                   privateChat.add(receiver);
                   privateChat.add(sender);
                   privateChats.put(receiver+"-"+sender,privateChat);
                   chatName = receiver+"-"+sender;
               }
               saveFullHistory(chatName,message);
           }

       } catch (Exception e) {
           e.printStackTrace();
       }
    }

    public synchronized static void addToOfflineHistory(String receiver, String message, String conversation) throws IOException {

        if(offlineHistory.get(receiver) == null){
            Map<String, ArrayList<String>> convRecord = new HashMap<>();
            ArrayList<String> messageRecord = new ArrayList<>();
            messageRecord.add(message);
            convRecord.put(conversation,messageRecord);
            offlineHistory.put(receiver,convRecord);

        }else if(offlineHistory.get(receiver).get(conversation) == null){
            ArrayList<String> messageRecord = new ArrayList<>();
            messageRecord.add(message);
            offlineHistory.get(receiver).put(conversation,messageRecord);

        }else{
            offlineHistory.get(receiver).get(conversation).add(message);

        }

        saveOfflineHistory(receiver);

    }


    //version non persistante
    public synchronized static boolean showOfflineHistory(String user){

                System.out.println("History map: " + offlineHistory.get(user));
                PrintStream socOut = null;
                try {
                    if (offlineHistory.get(user) != null) {
                        socOut = new PrintStream(users.get(user).getOutputStream());
                        socOut.println("Messages you received while offline:\n");
                        for (String conversation : offlineHistory.get(user).keySet()) {
                            socOut.println("------" + conversation + "-----");
                            for (String message : offlineHistory.get(user).get(conversation)) {
                                socOut.println(message);
                            }
                            socOut.println("-----------------\n");
                        }
                    } else{checkOfflineHistory(user);}
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
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

    public synchronized static void saveOfflineHistory(String user) throws IOException {
        File file = new File(user+".txt");
        String content = "Messages received while offline:\n";
        file.createNewFile();

        if (offlineHistory.get(user) != null) {
                for (String conversation : offlineHistory.get(user).keySet()) {
                    content = content + "------" + conversation + "-----\n";
                    for (String message : offlineHistory.get(user).get(conversation)) {
                       content = content + message + "\n";
                    }
                   content = content + "-----------------\n";
                }
        }else{
            content = "You don't have new messages";
        }
        FileWriter fw = new FileWriter(user+".txt", false); //le fichier sera toujours réécrit
        BufferedWriter bw = new BufferedWriter(fw);

        bw.write(content);
        bw.close();

    }


    public synchronized static void saveFullHistory(String conversation,String message) throws IOException {
        File file = new File(conversation+".txt");

        FileWriter fw = new FileWriter(conversation+".txt", true); //le fichier sera completé
        BufferedWriter bw = new BufferedWriter(fw);

        bw.write(message+"\n");
        bw.close();

    }

    public synchronized static void checkHistory(String user, String conversation) throws IOException {
        String chatName = null;
        PrintStream socOut = new PrintStream(users.get(user).getOutputStream());

        //verifie si on prend un groupChat ou un privateChat
        if(users.get(conversation) != null){
            if(privateChats.get(user+"-"+conversation) != null) chatName = user+"-"+conversation;
            else if(privateChats.get(conversation+"-"+user) != null)chatName = conversation+"-"+user;
        }else{
            chatName = conversation;
        }

        File file = new File(chatName+".txt");

        if(file.exists()) {
            String content = Files.readString(Path.of(chatName + ".txt"));
            if (content != null) {
                socOut.println("--------"+conversation+"--------");
                socOut.println(content);
            }
        }else{
            socOut.println("This conversation is empty");
        }
    }


    //version persistante
    public synchronized static void checkOfflineHistory(String receiver) throws IOException {
        File file = new File(receiver+".txt");
        if(file.exists()) {
            String content = Files.readString(Path.of(receiver + ".txt"));
            PrintStream socOut = new PrintStream(users.get(receiver).getOutputStream());
            if (content != null) {
                socOut.println(content);
            } else {
                socOut.println("You don't have new messages");
            }
        }
    }

    //une fois que l'utilisateur se connecte son historique de messages offline est supprimé
    public synchronized static void cleanOfflineHistory(String user){
         offlineHistory.put(user,null);
        try {
            saveOfflineHistory(user);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
