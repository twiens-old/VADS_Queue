/**
 * Created by twiens on 22.09.2016.
 */
public class DataMessage extends AbstractMessage {
    protected String data;
    protected Integer position;
    protected MessageType type;

    // Sender is optional
    public DataMessage(Node sender, Node receiver, String data, Integer position, MessageType type) {
        super(sender, receiver);
        this.data = data;
        this.position = position;
        this.type = type;
    }
}
