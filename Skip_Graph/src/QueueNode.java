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
    private HashMap<UUID, DataMessage> sentPositionRequests;
    private HashMap<Integer, String> storedElements;

    public QueueNode(String sequence) {
        super(sequence);

        this.queueAdministrator = false;
        this.first = 1;
        this.last = 1;

        this.returnAddresses = new HashMap<>();
        this.storedElements = new HashMap<>();
        this.getPositionMessages = new ArrayList<>();
        this.sentPositionRequests = new HashMap<>();

        this.rangeStart = HashFunction.hashQueueNode(this.ID);
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

    public void enqueue(DataMessage data) {
        PositionRequestMessage getPositionMessage = new PositionRequestMessage(this, 1);
        this.sentPositionRequests.put(getPositionMessage.uuid, data);

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

    private void split(IntervalMessage message) {
        Queue<PositionRequestMessage> queue = this.returnAddresses.get(message.requestUuid);

        int intervalStart = message.start;

        for (PositionRequestMessage prMessage : queue) {
            prMessage.sender.send(new IntervalMessage(this, prMessage.sender, intervalStart, intervalStart+prMessage.i-1, prMessage.uuid));
            intervalStart += prMessage.i;
        }
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
        IntervalMessage intervalMessage = new IntervalMessage(this, message.sender, this.last+1, this.last+message.i, message.uuid);
        this.last += message.i;

        message.sender.send(intervalMessage);
    }

    private void handleIntervalMessage(IntervalMessage message) {
        if (this.sentPositionRequests.containsKey(message.requestUuid)) {
            // RECEIVED POSITION INSIDE QUEUE
            DataMessage dataMessage = this.sentPositionRequests.get(message.requestUuid);
            dataMessage.position = message.start;

            this.sendToNearestResponsibleNode(dataMessage);
        } else {
            this.split(message);
        }
    }

    private void sendToNearestResponsibleNode(DataMessage message) {
        float positionHash = HashFunction.hashPosition(message.position);

        TreeSet<QueueNode> allNeighbours = new TreeSet<>();
        for (int i = 0; i < this.getID().length(); i++) {
            for (Node node : this.neighbours[i]) {
                allNeighbours.add((QueueNode) node);
            }
        }

        for (Node node : neighboursForBiDirection) {
            allNeighbours.add((QueueNode) node);
        }

        for (Node neighbour : allNeighbours) {
            neighbour.send(new NodeMessage(this, neighbour, this, NodeMessage.MessageType.LEAVE));

        }
    }

    @Override
    public int compareTo(Node anotherObject) {
        if ((anotherObject != null)) {
            if (anotherObject instanceof QueueNode) {
                Double d = new Double(this.rangeStart);

                return d.compareTo(((QueueNode)anotherObject).rangeStart);
            } else {
                return super.compareTo(anotherObject);
            }
        } else {
            throw new IllegalArgumentException("Object compared to destination is null.");
        }
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
