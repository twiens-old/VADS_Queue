import java.util.*;

/**
 * Created by twiens, fischerr, jeromeK on 8/23/16.
 */

/**
 * Represents a destination of the Skip+ graph.
 * @author twiens, fischerr, jeromeK
 * @version 0.1
 */
public class Node extends Subject implements Comparable<Node> {
    private final int INITIALISATION_PAHSE = 100;
    private final int HEATBEAT_WINDOW =20;

    private int initialCounter = 0;

    /**
     * Identification bit sequence of the destination. (id)
     */
    protected BitSequence ID;

    /**
     * Array that contains the level-i-neighbours of the destination at index i.
     */
    public TreeSet<Node>[] neighbours;

    public TreeSet<Node> neighboursForBiDirection;

    /**
     * Array that contains the level-i-range of the destination at index i.
     */
    public Range[] range;

    /**
     * Array that contains the level-i-predecessors of the destination at index i.
     */
    public Pair[] predecessor;

    public HashMap<BitSequence, Integer> timeoutCounters = new HashMap<>();

    /**
     * Array that contains the level-i-predecessors of the destination at index i.
     */
    public Pair[] successor;

    protected Node zirkNode;

    /**
     * Contains all the nodes received by messages.
     */
    private TreeSet<Node> receivedNodes = new TreeSet<>();

    private TreeSet<Node> leavingNodes = new TreeSet<>();

    /**
     * Constructor. Creates a new destination with a random, but unique id.
     */
    public Node(int NUMBER_OF_BITS) {
        this.ID = UniqueRandomBitStringGenerator.generateUniqueRandomBitSequence(NUMBER_OF_BITS);
        this.neighbours = new TreeSet[NUMBER_OF_BITS];
        this.range = new Range[NUMBER_OF_BITS];
        this.predecessor = new Pair[NUMBER_OF_BITS];
        this.successor = new Pair[NUMBER_OF_BITS];

        this.neighboursForBiDirection = new TreeSet<>();

        for (int i = 0; i < NUMBER_OF_BITS; i++) {
            this.predecessor[i] = new Pair();
            this.successor[i] = new Pair();
            this.range[i] = new Range();
            this.neighbours[i] = new TreeSet<>();
        }
    }

    /**
     * Constructor. Creates a new destination with a specific id.
     * @param sequence The id as bit sequence.
     */
    public Node (String sequence) {
        this.ID = new BitSequence(sequence);
        this.neighbours = new TreeSet[sequence.length()];
        this.range = new Range[sequence.length()];
        this.predecessor = new Pair[sequence.length()];
        this.successor = new Pair[sequence.length()];

        this.neighboursForBiDirection = new TreeSet<>();

        for (int i = 0; i < sequence.length(); i++) {
            this.predecessor[i] = new Pair();
            this.successor[i] = new Pair();
            this.range[i] = new Range();
            this.neighbours[i] = new TreeSet<>();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void init() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onMessageReceived(Object message) {
        if (message instanceof NodeMessage) {
            Node node = ((NodeMessage)message).node;
            switch (((NodeMessage)message).type) {
                case LEAVE:
                    this.handleLeave(node);
                    break;
                case INTRODUCE:
                    this.receivedNodes.add(node);
                    this.linearize(node);
                    break;
                case FORCE:
                    this.timeoutCounters.put(node.getID(), 0);
                    this.neighboursForBiDirection.add(node);
                    break;
                case FORCE_DELETE:
                    this.neighboursForBiDirection.remove(node);
                    break;
                case CIRCULAR_REFERENCE:
                    this.handleCircularReference((NodeMessage) message);
                    break;
                default:
                    break;
            }
        } else if (message instanceof StringMessage) {
            this.routing((StringMessage)message);
        } else if (message instanceof HeartbeatMessage) {
            this.timeoutCounters.put(((HeartbeatMessage) message).sender.getID(), 0);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onTimeout() {
        this.heartbeat();
        this.checkTimeoutCounters();
        
        this.linearizeRule1a();
        this.bridgeRule1b();

        for (int i = 0; i < this.getID().length(); i++) {
            for (Node neighbour : neighbours[i]) {
                neighbour.send(new NodeMessage(this, neighbour, this, NodeMessage.MessageType.INTRODUCE));
            }

            this.updateNeighbours(i);
        }

        this.checkAndSendCircularReference();
    }

    private void heartbeat() {
        HeartbeatMessage heartbeat = new HeartbeatMessage(this);

        for (int i = 0; i < this.getID().toString().length(); i++) {
            for (Node neighbor : this.neighbours[i]) {
                neighbor.send(heartbeat);
            }
        }

        for (Node neighbour : this.neighboursForBiDirection) {
            neighbour.send(heartbeat);
        }

        if (this.zirkNode != null) {
            this.zirkNode.send(heartbeat);
        }
    }
    
    private void checkTimeoutCounters() {
        if (this.initialCounter > INITIALISATION_PAHSE) {
            Iterator<Map.Entry<BitSequence, Integer>> it = this.timeoutCounters.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<BitSequence, Integer> entry = it.next();

                BitSequence nodeId = entry.getKey();
                int counter = entry.getValue();

                if (counter > HEATBEAT_WINDOW) {
                    it.remove();

                    for (int i = 0; i < this.getID().toString().length(); i++) {
                        Iterator<Node> neighborIt = this.neighbours[i].iterator();
                        while (neighborIt.hasNext()) {
                            Node neighbor = neighborIt.next();

                            if (neighbor.equals(nodeId)) {
                                neighborIt.remove();
                            }
                        }
                    }

                    Iterator<Node> neighborIt = this.neighboursForBiDirection.iterator();
                    while (neighborIt.hasNext()) {
                        Node neighbor = neighborIt.next();

                        if (neighbor.equals(nodeId)) {
                            neighborIt.remove();
                        }
                    }

                    if (this.zirkNode != null && this.zirkNode.getID().equals(nodeId)) {
                        this.zirkNode = null;
                    }
                } else {
                    this.timeoutCounters.put(nodeId, ++counter);
                }
            }
        } else {
            this.initialCounter++;
        }
    }

    private void linearizeRule1a() {
        // Regel 1a
        // Für jeden Level i stellt jeder Knoten u periodisch alle Knoten in N_i(u) vor, dass diese eine sortierte Liste
        // bilden
        for (int i = 0; i < this.getID().length(); i++) {
            SortedSet<Node> nodesLessThanMe = this.neighbours[i].headSet(this);
            TreeSet<Node> nodesLessEqualMe = new TreeSet<>(nodesLessThanMe);
            nodesLessEqualMe.add(this);

            TreeSet<Node> nodesGreaterThanMe = (TreeSet<Node>) this.neighbours[i].tailSet(this, false);
            TreeSet<Node> nodesGreaterEqualMe = new TreeSet<>(nodesGreaterThanMe);
            nodesGreaterEqualMe.add(this);

            for (Node node : nodesLessThanMe) {
                Node next = nodesLessEqualMe.higher(node);
                node.send(new NodeMessage(this, node, next, NodeMessage.MessageType.INTRODUCE));
            }

            for (Node node : nodesGreaterEqualMe) {
                Node next = nodesGreaterThanMe.higher(node);

                if (next != null) {
                    next.send(new NodeMessage(this, node, next, NodeMessage.MessageType.INTRODUCE));
                }
            }
        }
    }

    private void bridgeRule1b() {
        // Regel 1b
        // Für jeden Level i stellt jeder Knoten u periodisch seinen nächsten Vorgängern und Nachfolgern den Knoten
        // v_j e N_i(v) vor, dass alle Knoten links von u den Nachfolger rechts von u kennenlernen und alle Knoten
        // rechts von u den Vorgänger links von u kennenlernen

        for (int i = 0; i < this.getID().length(); i++) {

            Node v0 = this.predecessor[i].getNodeZero();
            Node v1 = this.predecessor[i].getNodeOne();
            Node w0 = this.successor[i].getNodeZero();
            Node w1 = this.successor[i].getNodeOne();

            SortedSet<Node> nodes_less_than_us = this.neighbours[i].headSet(this);
            for (Node current : nodes_less_than_us) {

                if (w0 != null && current.range[i].isNodeInsideRange(w0)) {
                    current.send(new NodeMessage(this, current, w0, NodeMessage.MessageType.INTRODUCE));
                }

                if (w1 != null && current.range[i].isNodeInsideRange(w1)) {
                    current.send(new NodeMessage(this, current, w1, NodeMessage.MessageType.INTRODUCE));
                }
            }

            SortedSet<Node> nodes_greater_than_us = this.neighbours[i].tailSet(this, false);
            for (Node current : nodes_greater_than_us) {
                if (v0 != null && current.range[i].isNodeInsideRange(v0)) {
                    current.send(new NodeMessage(this, current, v0, NodeMessage.MessageType.INTRODUCE));
                }

                if (v1 != null && current.range[i].isNodeInsideRange(v1)) {
                    current.send(new NodeMessage(this, current, v1, NodeMessage.MessageType.INTRODUCE));
                }
            }
        }
    }

    private void introduceAllNeighboursAtLevelToEachOther(int i) {
        for (Node node1 : this.neighbours[i]) {
            for (Node node2 : this.neighbours[i]) {
                if (!node1.equals(node2)) {
                    node1.send(new NodeMessage(this, node1, node2, NodeMessage.MessageType.INTRODUCE));
                    node2.send(new NodeMessage(this, node2, node1, NodeMessage.MessageType.INTRODUCE));
                }
            }

            for (Node node3 : this.neighboursForBiDirection) {
                if (!node1.equals(node3)) {
                    node1.send(new NodeMessage(this, node1, node3, NodeMessage.MessageType.INTRODUCE));
                    node3.send(new NodeMessage(this, node3, node1, NodeMessage.MessageType.INTRODUCE));
                }
            }
        }
    }

    private void linearize(Node v) {
        // Bei Erhalt von v aktualisiert u seine Ranges und Nachbarschaften für jeden Level i.
        // Für jeden Knoten w, den u nicht mehr benötigt (da er in keinem range_i(u) ist), delegiert u w zu dem Knoten
        // w' in seiner neuen Nachbarschaft mit größter Präfixübereinstimmung zwischen w' und w, der am nächsten zu w
        // ist.
        // Sei i der maximale Wert mit prefix_i(u)=prefix_i(w). Dann muss gelten, dass prefix_i+1(w)=prefix_i+1(w') für
        // den Knoten w', zu dem w delegiert wird. Solch ein Knoten existiert immer, da w !e range_i(u), und er muss
        // zwischen u und w liegen, d.h. der ID-Bereich von (w',w) ist innerhalb des ID-Bereichs von (u,w)

        if (this.leavingNodes.contains(v) || v.equals(this)) {
            return;
        }

        boolean temporary = true;
        for (int i = 0; i < this.getID().length(); i++) {
            // predecessors
            if (this.isGreaterThan(v)) {                              // überprüfe ob v Vorgänger von this
                if (prefixMatch(i, this, v, 1)) {                       // prüfe ob prefix_i konkateniert mit 1 mit id(v) übereinstimmt
                    if (predecessor[i].getNodeOne() == null || predecessor[i].getNodeOne().isLessThan(v)) {  // prüfe ob besserer nächste Vorgänger existiert
                        predecessor[i].setNodeOne(v);
                        this.updateRange(i);
                        this.updateNeighbours(i);

                        if (!this.neighbours[i].contains(v)) {
                            this.timeoutCounters.put(v.getID(), 0);
                            this.neighbours[i].add(v);
                            v.send(new NodeMessage(this, v, this, NodeMessage.MessageType.INTRODUCE));
                            v.send(new NodeMessage(this, v, this, NodeMessage.MessageType.FORCE));

                            this.introduceAllNeighboursAtLevelToEachOther(i);
                            temporary = false;
                        }
                    }
                }

                if (prefixMatch(i, this, v, 0)) {          // prüfe ob prefix_i konkateniert mit 0 mit id(v) übereinstimmt
                    if (predecessor[i].getNodeZero() == null || predecessor[i].getNodeZero().isLessThan(v)) {
                        predecessor[i].setNodeZero(v);
                        this.updateRange(i);
                        this.updateNeighbours(i);

                        if (!this.neighbours[i].contains(v)) {
                            this.timeoutCounters.put(v.getID(), 0);
                            this.neighbours[i].add(v);
                            v.send(new NodeMessage(this, v, this, NodeMessage.MessageType.INTRODUCE));
                            v.send(new NodeMessage(this, v, this, NodeMessage.MessageType.FORCE));

                            this.introduceAllNeighboursAtLevelToEachOther(i);
                            temporary = false;
                        }
                    }
                }
            }

            // successors
            if (this.isLessThan(v)) {                   // überprüfe ob v Nachfolger von this
                if (prefixMatch(i, this, v, 1)) {   // prüfe ob prefix_i konkateniert mit 1 mit id(v) übereinstimmt
                    if (successor[i].getNodeOne() == null || successor[i].getNodeOne().isGreaterThan(v)) {  // prüfe ob besserer nächste Nachfolger existiert
                        successor[i].setNodeOne(v);
                        this.updateRange(i);
                        this.updateNeighbours(i);

                        if (!this.neighbours[i].contains(v)) {
                            this.timeoutCounters.put(v.getID(), 0);
                            this.neighbours[i].add(v);
                            v.send(new NodeMessage(this, v, this, NodeMessage.MessageType.INTRODUCE));
                            v.send(new NodeMessage(this, v, this, NodeMessage.MessageType.FORCE));

                            this.introduceAllNeighboursAtLevelToEachOther(i);
                        }

                        temporary = false;
                    }
                }

                if (prefixMatch(i, this, v, 0)) {          // prüfe ob prefix_i konkateniert mit 0 mit id(v) übereinstimmt
                    if (successor[i].getNodeZero() == null || successor[i].getNodeZero().isGreaterThan(v)) {
                        successor[i].setNodeZero(v);
                        this.updateRange(i);
                        this.updateNeighbours(i);

                        if (!this.neighbours[i].contains(v)) {
                            this.timeoutCounters.put(v.getID(), 0);
                            this.neighbours[i].add(v);
                            v.send(new NodeMessage(this, v, this, NodeMessage.MessageType.INTRODUCE));
                            v.send(new NodeMessage(this, v, this, NodeMessage.MessageType.FORCE));

                            this.introduceAllNeighboursAtLevelToEachOther(i);
                        }

                        temporary = false;
                    }
                }
            }

            // fügt Knoten in die Nachbarschaft ein, falls Prefix gleich und inerhalb des Ranges

            if (this.prefixMatch(i, v) && range[i].isNodeInsideRange(v)) {
                if (!this.neighbours[i].contains(v)) {
                    this.neighbours[i].add(v);
                    v.send(new NodeMessage(this, v, this, NodeMessage.MessageType.FORCE));
                    v.send(new NodeMessage(this, v, this, NodeMessage.MessageType.INTRODUCE));

                    this.introduceAllNeighboursAtLevelToEachOther(i);       // Nötig
                }

                temporary = false;
            }
        }

        Node largestCommonPrefixNode = this.getNodeWithLargestCommonPrefix(v);
        if (temporary && largestCommonPrefixNode != null) {
            largestCommonPrefixNode.send(new NodeMessage(this, largestCommonPrefixNode, v, NodeMessage.MessageType.INTRODUCE));        // Nötig
        }
    }

    /**
     * Returns the id of the destination.
     * @return
     */
    public BitSequence getID() {
        return this.ID;
    }

    public void checkAndSendCircularReference() {
        if (this.predecessor[0].getNodeOne() == null && this.predecessor[0].getNodeZero() == null) {
            NodeMessage circMessage = new NodeMessage(this, null, this, AbstractMessage.MessageType.CIRCULAR_REFERENCE);
            this.sendToLargestNode(circMessage);
        }

        if (this.successor[0].getNodeOne() == null && this.successor[0].getNodeZero() == null) {
            NodeMessage circMessage = new NodeMessage(this, null, this, AbstractMessage.MessageType.CIRCULAR_REFERENCE);
            this.sendToSmallestNode(circMessage);
        }

        if (this.zirkNode != null && this.zirkNode.getID().isGreaterThan(this.getID()) && (this.predecessor[0].getNodeOne() != null || this.predecessor[0].getNodeZero() != null)) {
            this.zirkNode = null;
        }

        if (this.zirkNode != null && this.zirkNode.getID().isLessThan(this.getID()) && (this.successor[0].getNodeOne() != null || this.successor[0].getNodeZero() != null)) {
            this.zirkNode = null;
        }
    }

    // TODO: Exceptions für fehlerhafte Ranges (z.B. Anfang > Ende)
    /**
     * Checks and updates the range at level i if it is not valid.
     * The valid range of level i is:
     *     range_i(v) = [min_b(pred_i(v,b)), max_b(succ_i(v,b))], b={0,1}
     * This method is called after a successor or predecessor of the destination changed.
     * @param i The level of range to check and update.
     */
    private void updateRange(int i) {
        if (i < 0 || i > this.getID().length()) {
            throw new IllegalArgumentException("Level i not legal");
        }

        if (predecessor[i].getNodeZero() == null && predecessor[i].getNodeOne() != null) {
            range[i].setBegin(predecessor[i].getNodeOne());
        } else if (predecessor[i].getNodeOne() == null && predecessor[i].getNodeZero() != null) {
            range[i].setBegin(predecessor[i].getNodeZero());
        } else if (predecessor[i].getNodeZero() == null && predecessor[i].getNodeOne() == null) {
            range[i].setBegin(null);
        } else if (predecessor[i].getNodeZero().isLessThan(predecessor[i].getNodeOne())) {
            range[i].setBegin(predecessor[i].getNodeZero());
        } else {
            range[i].setBegin(predecessor[i].getNodeOne());
        }

        if (successor[i].getNodeZero() == null && successor[i].getNodeOne() != null) {
            range[i].setEnd(successor[i].getNodeOne());
        } else if (successor[i].getNodeOne() == null && successor[i].getNodeZero() != null) {
            range[i].setEnd(successor[i].getNodeZero());
        } else if (successor[i].getNodeZero() == null && successor[i].getNodeOne() == null) {
            range[i].setEnd(null);
        } else if (successor[i].getNodeZero().isGreaterThan(successor[i].getNodeOne())) {
            range[i].setEnd(successor[i].getNodeZero());
        } else {
            range[i].setEnd(successor[i].getNodeOne());
        }
    }

    // TODO: think about better naming
    /**
     * Updates the level-i-neighbours of the destination. Every neighbour that
     * is not inside the range at level i will be removed from the neighbourhood
     * at this level and delegated to the destination with biggest matching prefix known.
     * This method is called after a successor or predecessor of the destination changed.
     * @param i The level of the skip+ graph.
     */
    private void updateNeighbours(int i) {
        TreeSet<Node> removedNeighbours = new TreeSet<>();

        for (Node node : neighbours[i]) {
            if (!range[i].isNodeInsideRange(node)) {
                removedNeighbours.add(node);
                node.send(new NodeMessage(this, node, this, NodeMessage.MessageType.FORCE_DELETE));

                // Find the destination with biggest matching prefix.
                for (int j = this.getID().length()-1; j >= 0; j--) { // checken ob Ausgangspunkt von i reicht
                    boolean neighbourDelegated = false;
                    for (Node neighbour : neighbours[j]) {
                        if (!neighbour.equals(node) && prefixMatch(j, neighbour)) {
                            neighbour.send(new NodeMessage(this, neighbour, node, NodeMessage.MessageType.INTRODUCE));
                            neighbourDelegated = true;

                            continue;
                        }
                    }

                    if (neighbourDelegated) {
                        continue;
                    }
                }
            }
        }

        for (Node node : removedNeighbours) {
            this.neighbours[i].remove(node);
        }
    }

    /**
     * Checks if this destination is greater than another destination (more specific, it compares the IDs).
     * @param anotherNode
     * @return true, if this destination is strictly greater. false, if not.
     */
    public boolean isGreaterThan(Node anotherNode) {
        if (anotherNode == null) {
            return true;
        }

        return this.getID().isGreaterThan(anotherNode.getID());
    }

    /**
     * Checks if this destination is smaller than another destination (more specific, it compares the IDs).
     * @param anotherNode
     * @return true, if this destination is strictly less. false, if not.
     */
    public boolean isLessThan(Node anotherNode) {
        if (anotherNode == null) {
            return true;
        }

        return this.getID().isLessThan(anotherNode.getID());
    }

    public Node getNodeWithLargestCommonPrefix(Node node) {
        TreeSet<Node> neighboursFromAllLevels = new TreeSet<>();

        for (int i = 0; i < this.getID().length(); i++) {
            neighboursFromAllLevels.addAll(this.neighbours[i]);
        }

        neighboursFromAllLevels.addAll(this.neighboursForBiDirection);

        for (int i = this.getID().length()-1; i >= 0; i--) {
            for (Node neighbour : neighboursFromAllLevels) {
                if (neighbour.prefixMatch(i, node)) {
                    return neighbour;
                }
            }
        }

        return null;
    }

    public void leave() {
        TreeSet<Node> allNeighbours = new TreeSet<>();

        for (int i = 0; i < this.getID().length(); i++) {
            allNeighbours.addAll(neighbours[i]);
        }
        allNeighbours.addAll(neighboursForBiDirection);

        for (Node neighbour : allNeighbours) {
            neighbour.send(new NodeMessage(this, neighbour, this, NodeMessage.MessageType.LEAVE));
        }
    }

    public void handleLeave(Node leavingNode) {
        this.leavingNodes.add(leavingNode);

        // gebe den Unmittelbaren Nachbarn bescheid, dass diese die Referenzen auf einen Löschen sollen
        for (int i = 0; i < this.getID().length(); i++) {
            if (this.predecessor[i].getNodeZero() != null && this.predecessor[i].getNodeZero().equals(leavingNode)) {
                this.predecessor[i].setNodeZero(null);
            }

            if (this.predecessor[i].getNodeOne() != null && this.predecessor[i].getNodeOne().equals(leavingNode)) {
                this.predecessor[i].setNodeOne(null);
            }

            if (this.successor[i].getNodeZero() != null && this.successor[i].getNodeZero().equals(leavingNode)) {
                this.successor[i].setNodeZero(null);
            }

            if (this.successor[i].getNodeOne() != null && this.successor[i].getNodeOne().equals(leavingNode)) {
                this.successor[i].setNodeOne(null);
            }
            
            this.neighbours[i].remove(leavingNode);
            this.updateRange(i);
        }

        this.neighboursForBiDirection.remove(leavingNode);
    }

    public void routing(AbstractMessage message) {
        Node destination = message.receiver;

        if (destination.equals(this)) {
            message.arrived = true;
            if (message instanceof DataMessage) {
                println("Data Arrived -> " + ((DataMessage)message).data);
            } else if (message instanceof StringMessage) {
                println("Data Arrived -> " + ((StringMessage)message).message);
            }

            return;
        }

        int numberOfMatchingPrefixes = this.getNumberOfMatchingPrefixes(destination);
        boolean bit = destination.getID().getBit(numberOfMatchingPrefixes+1);

        Node pred0 = this.predecessor[numberOfMatchingPrefixes].getNodeZero();
        Node pred1 = this.predecessor[numberOfMatchingPrefixes].getNodeOne();
        Node succ0 = this.successor[numberOfMatchingPrefixes].getNodeZero();
        Node succ1 = this.successor[numberOfMatchingPrefixes].getNodeOne();

        if (destination.getID().isLessThan(this.getID())) {
            if (!bit && pred0 != null) {
                pred0.send(message);
            } else if (bit && pred1 != null) {
                pred1.send(message);
            } else {
                throw new UnsupportedOperationException("Skip Graph Failure");
            }
        } else {
            if (!bit && succ0 != null) {
                succ0.send(message);
            } else if (bit && succ1 != null) {
                succ1.send(message);
            } else {
                throw new UnsupportedOperationException("Skip Graph Failure");
            }
        }
    }

    public void handleCircularReference(NodeMessage message) {
        if (message.sender.getID().isLessThan(this.getID())) {
            if (successor[0].getNodeZero() != null || successor[0].getNodeOne() != null) {
                // rechts weiterleiten
                this.sendToLargestNode(message);
            } else {
                if (this.zirkNode == null || message.node.getID().isLessThan(this.zirkNode.getID())) {
                    this.timeoutCounters.put(message.node.getID(), 0);
                    this.zirkNode = message.node;
                }
            }
        }

        if (message.sender.getID().isGreaterThan(this.getID())) {
            if (predecessor[0].getNodeZero() != null || predecessor[0].getNodeOne() != null) {
                // links weiterleiten
                this.sendToSmallestNode(message);
            } else {
                if (this.zirkNode == null || message.node.getID().isGreaterThan(this.zirkNode.getID())) {
                    this.timeoutCounters.put(message.node.getID(), 0);
                    this.zirkNode = message.node;
                }
            }
        }
    }

    protected void sendToSmallestNode(AbstractMessage message) {
        TreeSet<Node> allNeighbours = new TreeSet<>();

        for (int i = 0; i < this.getID().toString().length(); i++) {
            allNeighbours.addAll(neighbours[i]);
        }
        allNeighbours.addAll(neighboursForBiDirection);
        SortedSet<Node> nodesLessThanMe = allNeighbours.headSet(this, false);

        if (!nodesLessThanMe.isEmpty()) {
            Node smallestNode = nodesLessThanMe.first();

            smallestNode.send(message);
        }
    }

    protected void sendToLargestNode(AbstractMessage message) {
        TreeSet<Node> allNeighbours = new TreeSet<>();

        for (int i = 0; i < this.getID().toString().length(); i++) {
            allNeighbours.addAll(neighbours[i]);
        }
        allNeighbours.addAll(neighboursForBiDirection);
        SortedSet<Node> nodesGreaterThanMe = allNeighbours.tailSet(this, false);

        if (!nodesGreaterThanMe.isEmpty()) {
            Node largestNode = nodesGreaterThanMe.last();

            largestNode.send(message);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object anotherObject) {
        if (!(anotherObject instanceof Node)) {
            return false;
        }

        return this.getID().equals(((Node) anotherObject).getID());
    }

    /**
     * Compares the prefixes of this destination to another destination.
     * @param i Length of prefix to compare.
     * @param anotherNode
     * @return
     */
    public boolean prefixMatch(int i, Node anotherNode) {
        BitSequence thisPrefix = this.getID().getPrefix(i);
        BitSequence anotherPrefix = anotherNode.getID().getPrefix(i);

        return thisPrefix.equals(anotherPrefix);
    }

    public int getNumberOfMatchingPrefixes(Node anotherNode) {
        for (int i = 1; i < this.getID().length(); i++) {
            if (!this.prefixMatch(i, anotherNode)) {
                return i-1;
            }
        }

        return 0;
    }

    public synchronized void printNeighbourhood() {
        println("########################################################################");

        println("Knoten ID: " + this.getID());

//        print("Received Nodes: ");
//        Iterator<Node> iterator = receivedNodes.iterator();
//        while(iterator.hasNext()) {
//            print(iterator.next().getID() + ", ");
//        }
//        println("");

        for (int i = 0; i < this.getID().length(); i++) {
            println("Level " + i +  "  Pred: " + this.predecessor[i] + " Succ:" + this.successor[i]);
            for (Node neighbour : this.neighbours[i]) {
                print("\t" + neighbour.getID() + ", ");
            }
            println("");
        }

        println("Bidirected neighbours: ");
        for (Node neighbour : this.neighboursForBiDirection) {
            print("\t" + neighbour.getID() + ", ");
        }
        println("");

        String zirkNode = "NULL";
        if (this.zirkNode != null) {
            zirkNode = this.zirkNode.getID().toString();
        }

        println("Zirk Node: ");
        print("\t" + zirkNode + ", ");
        println("");

        println("########################################################################");
    }

    public synchronized void printNeighbourhood(int i) {
        println("########################################################################");

        println("Knoten ID: " + this.getID());

        print("Received Nodes: ");
        Iterator<Node> iterator = receivedNodes.iterator();
        while(iterator.hasNext()) {
            print(iterator.next().getID() + ", ");
        }
        println("");

        println("Level " + i +  "  Pred: " + this.predecessor[i] + " Succ:" + this.successor[i]);
        for (Node neighbour : this.neighbours[i]) {
            print("\t" + neighbour.getID() + ", ");
        }
        println("");

        println("########################################################################");
    }

    public static boolean prefixMatch(int i, Node firstNode, Node secondNode, int b) {
        if (i > firstNode.getID().toString().length()-1) {
            throw new UnsupportedOperationException();
        }

        BitSequence firstId = firstNode.getID().getPrefix(i);       // exceptions werfen wenn i > Bitlänge-1
        if (b == 1) {
            firstId = firstId.append(true);
        } else {
            firstId = firstId.append(false);
        }

        BitSequence secondId = secondNode.getID().getPrefix(i+1);

        return firstId.equals(secondId);
    }

    @Override
    public int compareTo(Node anotherObject) {
        if ((anotherObject != null)) {
            if (this.isLessThan(anotherObject)) {
                return -1;
            }

            if (this.isGreaterThan(anotherObject)) {
                return 1;
            }

            return 0;
        } else {
            throw new IllegalArgumentException("Object compared to destination is null.");
        }
    }

    public void printSendingInformation(Node sender, Node receiver, Node message) {
        println("##############\nSender: " + sender.getID() + "\nReceiver: " + receiver.getID() + "\nMessage: " + message.getID() + "\n##############");
    }
}
