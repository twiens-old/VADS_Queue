/**
 * Created by rtf on 9/22/16.
 */
public class IntervalMessage extends AbstractMessage{

    protected int start;
    protected int end;

    public IntervalMessage(Node sender, Node receiver, int start, int end) {
        super(sender, receiver);
        this.start = start;
        this.end = end;
    }
}
