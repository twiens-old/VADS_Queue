/**
 * Created by twiens on 9/14/16.
 */
public class Message {
    public enum MessageType {
        LEAVE,
        FORCE_DELETE,
        ROUTING,
        FORCE,
        INTRODUCE
    }

    public Node node;
    public MessageType type;
    public StringBuilder message;

    public Message(Node node, String message) {
        this.node = node;
        this.message = new StringBuilder(message);
    }

    public Message(Node node, MessageType type, StringBuilder message) {
        this.node = node;
        this.message = message;
        this.type = type;
    }
}
