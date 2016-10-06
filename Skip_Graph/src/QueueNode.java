import javax.xml.crypto.Data;
import java.rmi.UnexpectedException;
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
    private HashMap<Integer, DataMessage> storedElements;

    public QueueNode(String sequence) {
        super(sequence);

        this.queueAdministrator = false;
        this.first = 1;
        this.last = 0;

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
            if (((DataMessage) message).type == AbstractMessage.MessageType.ENQUEUE) {
                this.handleDataMessage((DataMessage) message);
            }

            if (((DataMessage) message).type == AbstractMessage.MessageType.DEQUEUE) {
                this.routing((DataMessage) message);
            }
        } else if (message instanceof DequeueMessage) {
            this.handleDataMessage((DequeueMessage) message);
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

    public void enqueue(String data) {
        DataMessage dataMessage = new DataMessage(this, null, data, 0, AbstractMessage.MessageType.ENQUEUE);

        PositionRequestMessage getPositionMessage = new PositionRequestMessage(this, 1, PositionRequestMessage.MessageType.ENQUEUE);
        this.sentPositionRequests.put(getPositionMessage.uuid, dataMessage);

        this.handleGetPositionRequests(getPositionMessage);
    }

    public void dequeue() {

        PositionRequestMessage getPositionMessage = new PositionRequestMessage(this, 1, PositionRequestMessage.MessageType.DEQUEUE);
        this.sentPositionRequests.put(getPositionMessage.uuid, null);

        this.handleGetPositionRequests(getPositionMessage);
    }

    private void combine() {
        if (!this.getPositionMessages.isEmpty()) {
            int i = 0;
            int j = 0;
            Queue<PositionRequestMessage> enqueueQueue = new LinkedList<>();
            Queue<PositionRequestMessage> dequeueQueue = new LinkedList<>();

            for (PositionRequestMessage message : this.getPositionMessages) {
                if (message.type == PositionRequestMessage.MessageType.ENQUEUE) {
                    i += message.i;
                    enqueueQueue.add(message);
                } else if (message.type == PositionRequestMessage.MessageType.DEQUEUE) {
                    j += message.i;
                    dequeueQueue.add(message);
                }
            }

            PositionRequestMessage combinedEnqueueMessage = new PositionRequestMessage(this, i, PositionRequestMessage.MessageType.ENQUEUE);
            PositionRequestMessage combinedDequeueMessage = new PositionRequestMessage(this, j, PositionRequestMessage.MessageType.DEQUEUE);

            if (!enqueueQueue.isEmpty()) {
                this.returnAddresses.put(combinedEnqueueMessage.uuid, enqueueQueue);

                this.handleGetPositionRequests(combinedEnqueueMessage);

                println("COMBINE - GET POSITION REQUEST MESSAGES COUNT = " + this.getPositionMessages.size() + " Node: " + this);
            }

            if (!dequeueQueue.isEmpty()) {
                this.returnAddresses.put(combinedDequeueMessage.uuid, dequeueQueue);

                this.handleGetPositionRequests(combinedDequeueMessage);

                println("COMBINE - GET POSITION REQUEST MESSAGES COUNT = " + this.getPositionMessages.size() + " Node: " + this);
            }

            this.getPositionMessages.clear();
        }
    }

    private void split(IntervalMessage message) {
        Queue<PositionRequestMessage> queue = this.returnAddresses.get(message.requestUuid);

        if (queue == null) {
            return;
        }

        int bandwidth = Math.max(message.end - message.start, 0);

        int intervalStart = message.start;

        if (message.type == IntervalMessage.MessageType.ENQUEUE) {
            for (PositionRequestMessage prMessage : queue) {
                prMessage.sender.send(new IntervalMessage(this, prMessage.sender, intervalStart, intervalStart + prMessage.i - 1, prMessage.uuid, IntervalMessage.MessageType.ENQUEUE));
                intervalStart += prMessage.i;
            }
        } else if (message.type == IntervalMessage.MessageType.DEQUEUE) {
            for (PositionRequestMessage prMessage : queue) {
                prMessage.sender.send(new IntervalMessage(this, prMessage.sender, intervalStart, intervalStart + Math.min(bandwidth, prMessage.i) - 1, prMessage.uuid, IntervalMessage.MessageType.DEQUEUE));
                bandwidth = Math.max(bandwidth-prMessage.i, 0);
                intervalStart += prMessage.i;
            }
        }

        this.returnAddresses.remove(message.requestUuid);
    }

    private void handleGetPositionRequests(PositionRequestMessage message) {
        if (this.queueAdministrator) {
            if (message.type == PositionRequestMessage.MessageType.ENQUEUE) {
                IntervalMessage intervalMessage = new IntervalMessage(this, message.sender, this.last + 1, this.last + message.i, message.uuid, IntervalMessage.MessageType.ENQUEUE);
                this.last += message.i;

                println("First = " + this.first + " - Last = " + this.last + "; Intervalstart = " + intervalMessage.start + " - Intervallend = " + intervalMessage.end);

                message.sender.send(intervalMessage);
            } else if (message.type == PositionRequestMessage.MessageType.DEQUEUE) {
                IntervalMessage intervalMessage = new IntervalMessage(this, message.sender, this.first, Math.min(this.first+message.i-1, this.last), message.uuid, IntervalMessage.MessageType.DEQUEUE);
                this.first = Math.min(this.first+message.i, this.last+1);

                println("First = " + this.first + " - Last = " + this.last + "; Intervalstart = " + intervalMessage.start + " - Intervallend = " + intervalMessage.end);

                message.sender.send(intervalMessage);
            }
        } else {
            this.sendToSmallestNode(message);
        }
    }

    private void handleIntervalMessage(IntervalMessage message) {
        if (this.sentPositionRequests.containsKey(message.requestUuid)) {
            if (message.type == IntervalMessage.MessageType.ENQUEUE) {
                // RECEIVED POSITION INSIDE QUEUE
                DataMessage dataMessage = this.sentPositionRequests.get(message.requestUuid);
                dataMessage.position = message.start;

                this.sentPositionRequests.remove(message.requestUuid);

                this.handleDataMessage(dataMessage);
            } else if (message.type == IntervalMessage.MessageType.DEQUEUE) {
                if (message.start > message.end) {
                    // intentionally left empty
                } else {
                    DequeueMessage dequeueMessage = new DequeueMessage(this, message.start);
                    this.handleDataMessage(dequeueMessage);
                }
            }
        } else {
            this.split(message);
        }
    }

    private void handleDataMessage(AbstractMessage message) {
        double positionHash;
        if (message instanceof DataMessage) {
            positionHash = HashFunction.hashPosition(((DataMessage) message).position);
        } else if (message instanceof DequeueMessage) {
            positionHash = HashFunction.hashPosition(((DequeueMessage) message).position);
        } else {
            return;
        }

        TreeSet<QueueNode> allNeighbours = new TreeSet<>();
        for (int i = 0; i < this.getID().length(); i++) {
            for (Node node : this.neighbours[i]) {
                allNeighbours.add((QueueNode) node);
            }
        }

        for (Node node : neighboursForBiDirection) {
            allNeighbours.add((QueueNode) node);
        }

        if (!message.sentByCircularNode) {
            // send right
            QueueNode nextQueueNode = allNeighbours.higher(this);

            if (positionHash >= this.rangeStart) {

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

                    current.send(message);

                } else if (nextQueueNode == null && ((QueueNode)zirkNode).rangeStart < positionHash) {
                    // zirkNode responsilble
                    message.sentByCircularNode = true;
                    this.zirkNode.send(message);
                } else {
                    //we are responsilble
                    if (message instanceof DataMessage) {
                        println("Storing Element " + ((DataMessage) message).data + " at Node " + this);
                        this.storedElements.put(((DataMessage)message).position, ((DataMessage)message));
                    } else if (message instanceof DequeueMessage) {
                        DataMessage storedData = this.storedElements.get(((DequeueMessage) message).position);

                        this.storedElements.remove(((DequeueMessage) message).position);
                        storedData.receiver = message.sender;
                        storedData.sender = this;
                        storedData.type = AbstractMessage.MessageType.DEQUEUE;

                        this.routing(storedData);
                    } else {
                        return;
                    }
                }
            }

            // send left
            QueueNode prevQueueNode = allNeighbours.lower(this);

            if (positionHash < this.rangeStart) {
                if (prevQueueNode == null) {
                    // zirklärer Knoten zuständig
                    message.sentByCircularNode = true;
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

                    current.send(message);
                }
            }
        } else {
            //we are responsilble
            if (message instanceof DataMessage) {
                println("Storing Element " + ((DataMessage) message).data + " at Node " + this);
                this.storedElements.put(((DataMessage)message).position, ((DataMessage)message));
            } else if (message instanceof DequeueMessage) {
                DataMessage storedData = this.storedElements.get(((DequeueMessage) message).position);
                this.storedElements.remove(((DequeueMessage) message).position);
                storedData.receiver = message.sender;
                storedData.sender = this;
                storedData.type = AbstractMessage.MessageType.DEQUEUE;

                this.routing(storedData);
            } else {
                return;
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
