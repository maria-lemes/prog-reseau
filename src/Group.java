import java.util.ArrayList;

public class Group {
    
    private String name;
    private ArrayList<User> participants;
    private User creator;

    public Group(String name, User creator) {
        this.name = name;
        this.creator = creator;
        this.participants = new ArrayList<>();
        participants.add(creator);
    }

    public String getName() {
        return name;
    }

    public ArrayList<User> getParticipants() {
        return participants;
    }

    public User getCreator() {
        return creator;
    }

    public boolean isParticipant(String username) {
        return participants.contains(new User(username, null));
    }

    public boolean addParticipant(User participant) {
        boolean participantAdded = false;
        if (!participants.contains(participant)) {
            participants.add(participant);
            participantAdded = true;
            String serverResponse = "<added-to:" + name + ":" + creator + ">";
            EchoServerMultiThreaded.sendServerResponse(serverResponse, participant.getUsername());
        }
        return participantAdded;
    }

    public boolean removeParticipant(User participant) {
        return participants.removeIf(user -> user.equals(participant));
    }

    public void sendMessage(String message, User sender) {
        for (User u : participants) {
            if (!u.equals(sender)) {
                u.sendMessage(sender.getUsername() + " on " + name, message);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Group)) return false;
        Group g = (Group)o;
        if (creator.equals(g.getCreator()) && name.equals(g.getName())) return true;
        else return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + creator.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }
 
}
