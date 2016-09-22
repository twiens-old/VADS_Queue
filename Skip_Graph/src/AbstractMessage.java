import java.util.UUID;

/**
 * Created by rtf on 9/22/16.
 */
public abstract class AbstractMessage {

    protected UUID uuid;
    protected Node sender;
    protected Node receiver;
    protected boolean arrived;

    public AbstractMessage(Node sender, Node receiver) {
        this.uuid = UUID.randomUUID();
        this.sender = sender;
        this.receiver = receiver;
        this.arrived = false;
    }


}
