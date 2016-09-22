/**
 * Created by twiens on 22.09.2016.
 */
public class DataMessage extends AbstractMessage {
    protected String data;
    protected int position;

    public DataMessage(Node sender, Node receiver, String data, int position) {
        super(sender, receiver);
        this.data = data;
        this.position = position;
    }
}
