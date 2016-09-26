/**
 * Created by twiens on 26.09.2016.
 */
public class DequeueMessage extends AbstractMessage {
    int position;

    public DequeueMessage(QueueNode sender, int position){
        super(sender, null);

        this.position = position;
    }
}
