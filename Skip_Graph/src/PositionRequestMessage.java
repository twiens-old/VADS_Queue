/**
 * Created by rtf on 9/22/16.
 */
public class PositionRequestMessage extends AbstractMessage {

    int i;

    public PositionRequestMessage(Node sender, int i) {
        super(sender, null);
        this.i = i;

    }

}
