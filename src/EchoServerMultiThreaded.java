
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

    private static Map<String, User> users = new HashMap<>();
    private static Map<String, Group> groups = new HashMap<>();

    /**
     * main method
     **/
    public static void main(String args[]) {

        if (args.length != 1) {
            System.out.println("Usage: java EchoServer <EchoServer port>");
            System.exit(1);
        }
        try {
            ServerSocket listenSocket = new ServerSocket(Integer.parseInt(args[0])); // port
            System.out.println("Server ready...");

            while (true) {
                Socket clientSocket = listenSocket.accept();
                ClientThread ct = new ClientThread(clientSocket);
                ct.start();
            }

        } catch (Exception e) {
            System.err.println("Error in EchoServerMultiThreaded:" + e);
        }
    }

    /**
     * Users getter
     * @return
     */
    public synchronized static Map<String, User> getUsers() {
        return users;
    }

    /**
     * Adds an user to application list if the username isn't already been in use.
     * 
     * @param username
     * @param socket
     * @return true if user added successfully.
     */
    public synchronized static boolean addUser(String username, Socket socket) {
        boolean userAdded = false;
        if (users.containsKey(username)) {
            User user = users.get(username);
            if (user.isConnected()) {
                System.out.println("User " + username + " already exists");
            } else {
                user.connectUser(socket);
                System.out.println("User " + username + " connected");
            }
        } else {
            users.put(username, new User(username, socket));
            System.out.println("User " + username + " connected");
            userAdded = true;
        }
        return userAdded;
    }

    public synchronized static void disconectUser(String username) {
        users.get(username).disconnectUser();
        System.out.println("User " + username + " disconnected");
    }

    /**
     * Groups getter
     * 
     * @return
     */
    public synchronized static Map<String, Group> getGroups() {
        return groups;
    }

    /**
     * Creates a group associated with the group crator if it doesn't exist already.
     * Automatically adds group creator to the group
     * 
     * @param groupName
     * @param groupCreator
     * @return true if the group was created
     */
    public synchronized static boolean createGroup(String groupName, String groupCreatorName) {
        boolean groupCreated = false;
        User groupCreator = users.get(groupCreatorName);
        if (groupCreator != null) {
            Group newGroup = new Group(groupName, groupCreator);
            String groupKey = groupName + ":" + groupCreatorName;
            if (groups.containsKey(groupKey)) {
                System.err.println("Group " + groupName + " by " + groupCreatorName + " already exists");
            } else {
                groups.put(groupKey, newGroup);
                groupCreated = true;
            }
        } else {
            System.err.println("Error creating group");
        }
        return groupCreated;
    }

    /**
     * Add user to a group
     * 
     * @param groupName
     * @param userName
     */
    public synchronized static boolean addUserToGroup(String groupName, String groupCreator, String userName) {
        String groupKey = groupName + ":" + groupCreator;
        Group g;
        if ((g = groups.get(groupKey)) != null) {
            User participant = users.get(userName);
            if (participant != null) {
                if (g.addParticipant(participant)) {
                    System.out.println("User " + userName + " added to group " + groupName);
                    return true;
                } else {
                    System.err.println("Error adding " + userName + " to group " + groupName);
                }
            } else {
                System.err.println("Error adding " + userName + " to group" + groupName + ". User doesn't exist");
            }
        } else {
            System.err.println("Group " + groupName + " by " + groupCreator + " doesn't exist");
        }
        return false;
    }

    /**
     * Remove an user from a group
     * 
     * @param groupName
     * @param userName
     */
    public synchronized static boolean removeUserFromGroup(String groupName, String groupCreator, String userName) {
        String groupKey = groupName + ":" + groupCreator;
        Group g = groups.get(groupKey);
        if (g != null) {
            User participant = users.get(userName);
            if (participant != null) {
                if (g.removeParticipant(participant)) {
                    System.out.println("User " + userName + " removed from group " + groupName);
                    return true;
                } else {
                    System.err.println("Error removing " + userName + " from group " + groupName);
                }
            } else {
                System.err.println("Error removing " + userName + " from group " + groupName + ". User doesn't exist");
            }
        } else {
            System.err.println("Group " + groupName + " by " + groupCreator + " doesn't exist");
        }
        return false;
    }

    public synchronized static String sendGroupMessage(String message, String senderName, String groupKey) {
        String serverResponse = "done";
        Group g = groups.get(groupKey);
        System.out.println("Group message: " + g);
        if (g != null) {
            User sender = users.get(senderName);
            g.sendMessage(message, sender);
        } else {
            serverResponse = "Error sending group message. Groupkey " + groupKey + " doesn't exist";
        }
        return serverResponse;
    }

    public synchronized static String sendPrivateMessage(String message, String sender, String recipientName) {
        User recipient = users.get(recipientName);
        String serverResponse = "done";
        if (recipient != null) {
            if (!recipient.sendMessage(sender, message)) serverResponse = "Error sending message";
        } else {
            serverResponse = "Error sending message: " + recipientName + " doesn't exist.";
        }
        return serverResponse;
    }

    public synchronized static String getUserGroups(String username) {
        String usersGroups = "<user-groups>@next@";
        for (String groupKey : groups.keySet()) {
            if (groups.get(groupKey).isParticipant(username)) {
                usersGroups += "<groupkey:" + groupKey + ">@next@";
            }
        }
        return usersGroups;
    }

    public synchronized static void sendServerResponse(String message, String recipientName) {
        User recipient = users.get(recipientName);
        recipient.sendServerMessage(message);
    }

}
