import java.util.*;

/**
 * Created by twiens on 19.09.2016.
 */
public class QueueNode extends Node {
    private boolean queueAdministrator;
    private int first, last;

    private QueueNode zirkNode;

    private double rangeStart;

    private List<PositionRequestMessage> getPositionMessages;

    private HashMap<UUID, Queue<PositionRequestMessage>> returnAddresses;
    private HashMap<UUID, DataMessage> sentPositionRequests;
    private HashMap<Integer, DataMessage> storedElements;

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
            if (!this.queueAdministrator) {
                this.getPositionMessages.add((PositionRequestMessage) message);
            } else {
                this.handleGetPositionRequests((PositionRequestMessage) message);
            }
        } else if (message instanceof IntervalMessage) {
            this.handleIntervalMessage((IntervalMessage) message);
        } else if (message instanceof  DataMessage) {
            this.handleDataMessage((DataMessage) message);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onTimeout() {
        super.onTimeout();

        this.combine();
        this.testQueueAdministrator();
    }

    public void enqueue(DataMessage data) {
        println("Enquqing new Data Message " + this);

        PositionRequestMessage getPositionMessage = new PositionRequestMessage(this, 1);
        this.sentPositionRequests.put(getPositionMessage.uuid, data);

        this.handleGetPositionRequests(getPositionMessage);
    }

    private void combine() {
        if (!this.getPositionMessages.isEmpty()) {
            int i = 0;
            Queue<PositionRequestMessage> q = new LinkedList<>();

            for (PositionRequestMessage message : this.getPositionMessages) {
                i += message.i;
                q.add(message);
            }

            PositionRequestMessage combinedMessage = new PositionRequestMessage(this, i);

            this.returnAddresses.put(combinedMessage.uuid, q);

            this.handleGetPositionRequests(combinedMessage);

            println("COMBINE - GET POSITION REQUEST MESSAGES COUNT = " + this.getPositionMessages.size() + " Node: " + this);

            this.getPositionMessages.clear();
        }
    }

    private void split(IntervalMessage message) {
        Queue<PositionRequestMessage> queue = this.returnAddresses.get(message.requestUuid);

        if (queue == null) {
            return;
        }

        int intervalStart = message.start;

        for (PositionRequestMessage prMessage : queue) {
            prMessage.sender.send(new IntervalMessage(this, prMessage.sender, intervalStart, intervalStart + prMessage.i - 1, prMessage.uuid));
            intervalStart += prMessage.i;
        }

        this.returnAddresses.remove(message.requestUuid);
    }

    private void handleGetPositionRequests(PositionRequestMessage message) {
        if (this.queueAdministrator) {
            println("Queue Administrator received Position Request");

            IntervalMessage intervalMessage = new IntervalMessage(this, message.sender, this.last + 1, this.last + message.i, message.uuid);
            this.last += message.i;

            message.sender.send(intervalMessage);
        } else {
            TreeSet<Node> allNeighbours = new TreeSet<>();

            for (int i = 0; i < this.getID().toString().length(); i++) {
                allNeighbours.addAll(neighbours[i]);
            }
            allNeighbours.addAll(neighboursForBiDirection);

            Node smallestNode = allNeighbours.first();

            println("Sending Position Request Message - Sender: " + this + " Receiver: " + smallestNode);

            smallestNode.send(message);
        }
    }

    private void handleIntervalMessage(IntervalMessage message) {
        if (this.sentPositionRequests.containsKey(message.requestUuid)) {
            // RECEIVED POSITION INSIDE QUEUE
            DataMessage dataMessage = this.sentPositionRequests.get(message.requestUuid);
            dataMessage.position = message.start;

            println("YO HABEN POSITION ERHALTEN DIGGAH");

            this.sentPositionRequests.remove(message.requestUuid);

            this.handleDataMessage(dataMessage);
        } else {
            this.split(message);
        }
    }

    private void handleDataMessage(DataMessage message) {
        double positionHash = HashFunction.hashPosition(message.position);

        TreeSet<QueueNode> allNeighbours = new TreeSet<>();
        for (int i = 0; i < this.getID().length(); i++) {
            for (Node node : this.neighbours[i]) {
                allNeighbours.add((QueueNode) node);
            }
        }

        for (Node node : neighboursForBiDirection) {
            allNeighbours.add((QueueNode) node);
        }

        // send right
        QueueNode nextQueueNode = allNeighbours.higher(this);

        println("POSITION HASH = " + positionHash + " - RANGE START = " + this.rangeStart);

        if (positionHash >= this.rangeStart) {
            println("PositionHash > rangeStart");

            if (nextQueueNode != null && nextQueueNode.rangeStart < positionHash) {
                // next responsible

                // responsible node is somewhere right of us
                TreeSet<QueueNode> nodesGreaterThanUs = (TreeSet<QueueNode>) allNeighbours.tailSet(this, false);
                Iterator<QueueNode> iterator = nodesGreaterThanUs.descendingIterator();

                QueueNode current = iterator.next();

                while (iterator.hasNext()) {
                    QueueNode next = iterator.next();

                    if (positionHash > next.rangeStart) {
                        break;
                    }
                    current = next;
                }

                println("Sending Data from " + this + " to " + current);

                current.send(message);

            } else if (nextQueueNode == null && zirkNode.rangeStart < positionHash) {
                println("zirkNode < positionHash");
                // zirkNode responsilble
                this.zirkNode.send(message);
            } else {
                //we are responsilble
                println("Storing Element at Node " + this);
                this.storedElements.put(message.position, message);
            }
        }

        // send left
        QueueNode prevQueueNode = allNeighbours.lower(this);

        if (positionHash < this.rangeStart) {
            println("PositionHash < rangeStart");
            if (prevQueueNode == null) {
                // zirklärer Knoten zuständig
                println("ZirkNode responsible");
                this.zirkNode.send(message);
            } else {
                // responsible node is somewhere left of us
                TreeSet<QueueNode> nodesLessThanUs = (TreeSet<QueueNode>) allNeighbours.headSet(this);
                Iterator<QueueNode> iterator = nodesLessThanUs.iterator();

                QueueNode current = iterator.next();

                while (iterator.hasNext()) {
                    QueueNode next = iterator.next();

                    if (positionHash < next.rangeStart) {
                        break;
                    }
                    current = next;
                }

                println("Sending Data from " + this + " to " + current);
                current.send(message);
            }
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

    @Override
    public String toString() {
        return this.getID() + " (" + this.rangeStart + ")";
    }
}
