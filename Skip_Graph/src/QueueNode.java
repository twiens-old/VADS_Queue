import java.util.*;

/**
 * Created by twiens on 19.09.2016.
 */
public class QueueNode extends Node {
    private boolean queueAdministrator;
    private int first, last;

    private double rangeStart;

    private List<Message> getPositionMessages;

    private HashMap<UUID, Queue<Message>> returnAddresses;
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

        if (message instanceof Message && ((Message) message).type == Message.MessageType.GET_POSITION) {
            if (!queueAdministrator) {
                this.getPositionMessages.add((Message) message);
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
        Message getPositionMessage = new Message(null, Message.MessageType.GET_POSITION, new StringBuilder(), this);
        getPositionMessage.i = 1;
        this.sendGetPositionRequestToSmallestNode(getPositionMessage);
    }

    private void combine() {
        int i = 0;
        Queue<Message> q = new LinkedList<>();
        for (Message message : this.getPositionMessages) {
            i += message.i;
            q.add(message);
        }

        Message combinedMessage = new Message(null, Message.MessageType.GET_POSITION, new StringBuilder(), this);

        this.returnAddresses.put(combinedMessage.uuid, q);

        this.sendGetPositionRequestToSmallestNode(combinedMessage);
    }

    private void sendGetPositionRequestToSmallestNode(Message message) {
        TreeSet<Node> allNeighbours = new TreeSet<>();

        for (int i = 0; i < this.getID().toString().length(); i++) {
            allNeighbours.addAll(neighbours[i]);
        }
        allNeighbours.addAll(neighboursForBiDirection);

        Node smallestNode = allNeighbours.first();

        smallestNode.send(message);
    }

    private void handleGetPositionRequests(Message message) {
        Message intervallMessage = new Message();
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
