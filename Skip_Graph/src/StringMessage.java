/**
 * Created by rtf on 9/22/16.
 */
public class StringMessage extends AbstractMessage{

    protected StringBuilder message;

    public StringMessage(Node sender, Node receiver, String message) {
        super(sender, receiver);
        this.message = new StringBuilder(message);
    }
}
