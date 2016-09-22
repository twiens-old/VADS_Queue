import java.util.*;

/**
 * Created by twiens on 19.09.2016.
 */
public class QueueNode extends Node {
    private boolean queueAdministrator;
    private int first, last;

    private double rangeStart;

    private List<PositionRequestMessage> getPositionMessages;

    private HashMap<UUID, Queue<PositionRequestMessage>> returnAddresses;
    private HashMap<Integer, String> storedElements;

    public QueueNode(String sequence) {
        super(sequence);

        this.queueAdministrator = false;
        this.first = 1;
        this.last = 1;

        this.returnAddresses = new HashMap<>();
        this.storedElements = new HashMap<>();
        this.getPositionMessages = new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onMessageReceived(Object message) {
        super.onMessageReceived(message);

        if (message instanceof PositionRequestMessage) {
            if (!queueAdministrator) {
                this.getPositionMessages.add((PositionRequestMessage) message);
            } else {

            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onTimeout() {
        super.onTimeout();

        this.combine();
    }

    public void enqueue(String data) {
        PositionRequestMessage getPositionMessage = new PositionRequestMessage(this, 1);

        this.sendGetPositionRequestToSmallestNode(getPositionMessage);
    }

    private void combine() {
        int i = 0;
        Queue<PositionRequestMessage> q = new LinkedList<>();
        for (PositionRequestMessage message : this.getPositionMessages) {
            i += message.i;
            q.add(message);
        }

        PositionRequestMessage combinedMessage = new PositionRequestMessage(this, i);

        this.returnAddresses.put(combinedMessage.uuid, q);

        this.sendGetPositionRequestToSmallestNode(combinedMessage);
    }

    private void sendGetPositionRequestToSmallestNode(PositionRequestMessage message) {
        TreeSet<Node> allNeighbours = new TreeSet<>();

        for (int i = 0; i < this.getID().toString().length(); i++) {
            allNeighbours.addAll(neighbours[i]);
        }
        allNeighbours.addAll(neighboursForBiDirection);

        Node smallestNode = allNeighbours.first();

        smallestNode.send(message);
    }

    private void handleGetPositionRequests(PositionRequestMessage message) {
    }

    private void testQueueAdministrator() {
        boolean temp = true;
        for (Pair pair : this.predecessor) {
            if (pair.getNodeOne() != null || pair.getNodeZero() != null) {
                temp = false;
                break;
            }
        }
        this.queueAdministrator = temp;
    }
}
