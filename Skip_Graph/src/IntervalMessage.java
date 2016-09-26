import java.util.UUID;

/**
 * Created by rtf on 9/22/16.
 */
public class IntervalMessage extends AbstractMessage{

    protected UUID requestUuid;
    protected Integer start;
    protected Integer end;
    protected MessageType type;

    public IntervalMessage(Node sender, Node receiver, Integer start, Integer end, UUID requestUuid, MessageType type) {
        super(sender, receiver);
        this.start = start;
        this.end = end;
        this.requestUuid = requestUuid;
        this.type = type;
    }
}
