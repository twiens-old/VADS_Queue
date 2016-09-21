import java.util.UUID;

/**
 * Created by twiens on 9/14/16.
 */
public class Message {
    public enum MessageType {
        LEAVE,
        FORCE_DELETE,
        ROUTING,
        FORCE,
        INTRODUCE,
        GET_POSITION,
        RETURN_POSITION
    }

    public final UUID uuid;

    public Node sender;
    public Node destination;
    public MessageType type;
    public StringBuilder message;
    public boolean arrived = false;
    public int i;
    public int intervallBeginning;
    public int intervallEnding;

    public Message(Node destination, String message, Node sender) {
        this.destination = destination;
        this.message = new StringBuilder(message);
        this.uuid = UUID.randomUUID();
        this.sender = sender;
    }

    public Message(Node destination, MessageType type, StringBuilder message, Node sender) {
        this.destination = destination;
        this.message = message;
        this.type = type;
        this.uuid = UUID.randomUUID();
        this.sender = sender;
    }
}
