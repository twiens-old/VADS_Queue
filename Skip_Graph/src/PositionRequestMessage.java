/**
 * Created by rtf on 9/22/16.
 */
public class PositionRequestMessage extends AbstractMessage {
    MessageType type;
    int i;

    public PositionRequestMessage(Node sender, int i, MessageType type) {
        super(sender, null);
        this.i = i;
        this.type = type;
    }

}
