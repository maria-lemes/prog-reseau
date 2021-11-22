import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class User {

    private String username;
    private Socket socket;
    private Map<String, ArrayList<String>> messageHistory;
    private Map<String, ArrayList<String>> pendingMessages;

    public User(String username, Socket socket) {
        this.username = username;
        this.socket = socket;
        this.messageHistory = new HashMap<>();
        this.pendingMessages = new HashMap<>();
    }
    
    public void disconnectUser() {
        sendServerMessage("<disconnected>");
        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.socket = null;
    }

    public void connectUser(Socket socket) {
        this.socket = socket;
        sendPendingMessages();
    }

    public String getUsername() {
        return username;
    }

    public Socket getSocket() {
        return socket;
    }

    public boolean isConnected() {
        return socket != null; 
    }

    public synchronized boolean sendMessage(String sender, String message) {
        try {
            //User connected - send message now
            if (socket != null) {
                System.out.println("Sending message : " + message);
                PrintStream socOut = new PrintStream(socket.getOutputStream());
                socOut.println(sender + ": " +message);
            } 
            //User disconnected, save message
            else {
                if (pendingMessages.get(sender) == null) {
                    pendingMessages.put(sender, new ArrayList<String>());
                }
                pendingMessages.get(sender).add(message);
            }
            //Add message to message history
            if (messageHistory.get(sender) == null) {
                messageHistory.put(sender, new ArrayList<String>());
            }
            messageHistory.get(sender).add(message);

            return true;
        } catch (Exception e) {
            System.err.println(e);
            return false;
        }
    }

    private int sendPendingMessages() {
        int msgSent = 0;
        if (socket != null) {
            if (pendingMessages.keySet().size() > 0) {
                sendServerMessage("Messages received while offline:");
            }
            for (String sender : pendingMessages.keySet()) {
                for (String message : pendingMessages.get(sender)) {
                    if (sendMessage(sender, message)) {
                        msgSent++;
                        pendingMessages.get(sender).remove(message);
                    }
                }
                if (pendingMessages.get(sender).size() == 0) {
                    pendingMessages.remove(sender);
                }
            }
        }
        return msgSent;
    }

    public synchronized void sendServerMessage(String message) {
        try {
            //User connected - send message now
            if (socket != null) {
                PrintStream socOut = new PrintStream(socket.getOutputStream());
                socOut.println(message);
            } 
      
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User)) return false;
        User u = (User)o;
        return u.getUsername().equals(username);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + username.hashCode();
        return result;
    }
 }
