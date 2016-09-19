import java.util.HashMap;
import java.util.Queue;
import java.util.UUID;

/**
 * Created by twiens on 19.09.2016.
 */
public class QueueNode extends Node {
    private boolean queueAdministrator;
    private int first, last;

    private double rangeStart;

    private HashMap<UUID, QueueNode> returnAddresses;

    private HashMap<Integer, String> storedElements;

    public QueueNode(String sequence) {
        super(sequence);

        this.queueAdministrator = false;
        this.first = 1;
        this.last = 1;

        this.returnAddresses = new HashMap<>();
        this.storedElements = new HashMap<>();
    }

    public void enqueue(String data) {

    }
}
