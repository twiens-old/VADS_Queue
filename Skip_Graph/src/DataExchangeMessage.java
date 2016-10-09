import java.util.HashMap;

/**
 * Created by twiens on 09.10.2016.
 */
public class DataExchangeMessage extends AbstractMessage {
    public HashMap<Integer, DataMessage> data;

    public DataExchangeMessage(Node sender, Node receiver, HashMap<Integer, DataMessage> data) {
        super(sender, receiver);

        this.data = data;
    }
}
