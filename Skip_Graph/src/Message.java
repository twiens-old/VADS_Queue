import java.util.UUID;

/**
 * Created by twiens on 9/14/16.
 */
public class Message {




    // abstract
    public final UUID uuid;

    public Node sender;
    public Node destination;
    public boolean arrived = false;

    //node
    public enum MessageType {
        LEAVE,
        FORCE_DELETE,
        ROUTING,
        FORCE,
        INTRODUCE,
        GET_POSITION,
        RETURN_POSITION
    }
    public MessageType type;
    // + node

    // string
    public StringBuilder message;

    // positon
    public int i;

    // intervall
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
