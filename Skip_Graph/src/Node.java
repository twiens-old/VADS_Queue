import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by twiens, fischerr, jeromeK on 8/23/16.
 */

/**
 * Represents a node of the Skip+ graph.
 * @author twiens, fischerr, jeromeK
 * @version 0.1
 */
public class Node extends Subject implements Comparable<Node> {

    /**
     * Machen wir in die skip graph klasse.
     */
    public static final int NUMBER_OF_BITS = 3;

    /**
     * Node with lowest possible id.
     */
    public static final Node minNode = new Node("");

    /**
     * Node with the highest possible id.
     */
    public static final Node maxNode = new Node("1111");    // TODO: dynamisch erzeugen

    /**
     * Identification bit sequence of the node. (id)
     */
    private BitSequence ID;

    /**
     * Array that contains the level-i-neighbours of the node at index i.
     */
    public TreeSet<Node>[] neighbours;

    /**
     * Array that contains the level-i-range of the node at index i.
     */
    public Range[] range;

    /**
     * Array that contains the level-i-predecessors of the node at index i.
     */
    public Pair[] predecessor;

    /**
     * Array that contains the level-i-predecessors of the node at index i.
     */
    public Pair[] successor;

    /**
     * Contains all the nodes received by messages.
     */
    private TreeSet<Node> receivedNodes = new TreeSet<>();



    /**
     * Constructor. Creates a new node with a random, but unique id.
     */
    public Node() {
        this.ID = UniqueRandomBitStringGenerator.generateUniqueRandomBitSequence(NUMBER_OF_BITS);
        this.neighbours = new TreeSet[NUMBER_OF_BITS];
        this.range = new Range[NUMBER_OF_BITS];
        this.predecessor = new Pair[NUMBER_OF_BITS];
        this.successor = new Pair[NUMBER_OF_BITS];

        for (int i = 0; i < NUMBER_OF_BITS; i++) {
            this.predecessor[i] = new Pair();
            this.successor[i] = new Pair();
            this.range[i] = new Range();
            this.neighbours[i] = new TreeSet<>();
        }
    }

    /**
     * Constructor. Creates a new node with a specific id.
     * @param sequence The id as bit sequence.
     */
    public Node (String sequence) {
        this.ID = new BitSequence(sequence);
        this.neighbours = new TreeSet[NUMBER_OF_BITS];
        this.range = new Range[NUMBER_OF_BITS];
        this.predecessor = new Pair[NUMBER_OF_BITS];
        this.successor = new Pair[NUMBER_OF_BITS];

        for (int i = 0; i < NUMBER_OF_BITS; i++) {
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
        if (message instanceof Node) {
            this.receivedNodes.add((Node) message);
            this.linearize((Node) message);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onTimeout() {
        this.linearizeRule1a();
        this.bridgeRule1b();
    }

    private void linearizeRule1a() {
        // Regel 1a
        // Für jeden Level i stellt jeder Knoten u periodisch alle Knoten in N_i(u) vor, dass diese eine sortierte Liste
        // bilden
        for (int i = 0; i < NUMBER_OF_BITS; i++) {
            SortedSet<Node> nodesLessThanMe = this.neighbours[i].headSet(this);
            TreeSet<Node> nodesLessEqualMe = new TreeSet<>(nodesLessThanMe);
            nodesLessEqualMe.add(this);

            TreeSet<Node> nodesGreaterThanMe = (TreeSet<Node>) this.neighbours[i].tailSet(this, false);
            TreeSet<Node> nodesGreaterEqualMe = new TreeSet<>(nodesGreaterThanMe);
            nodesGreaterEqualMe.add(this);

            for (Node node : nodesLessThanMe) {
                Node next = nodesLessEqualMe.higher(node);
                node.send(next);
            }

            for (Node node : nodesGreaterEqualMe) {
                Node next = nodesGreaterThanMe.higher(node);

                if (next != null) {
                    next.send(node);
                }
            }
        }
    }

    private void bridgeRule1b() {
        // Regel 1b
        // Für jeden Level i stellt jeder Knoten u periodisch seinen nächsten Vorgängern und Nachfolgern den Knoten
        // v_j e N_i(v) vor, dass alle Knoten links von u den Nachfolger rechts von u kennenlernen und alle Knoten
        // rechts von u den Vorgänger links von u kennenlernen

        for (int i = 0; i < NUMBER_OF_BITS; i++) {

            Node v0 = this.predecessor[i].getNodeZero();
            Node v1 = this.predecessor[i].getNodeOne();
            Node w0 = this.successor[i].getNodeZero();
            Node w1 = this.successor[i].getNodeOne();

            SortedSet<Node> nodes_less_than_us = this.neighbours[i].headSet(this);
            for (Node current : nodes_less_than_us) {
                if (w0 != null && current.range[i].isNodeInsideRange(w0)) {
                    this.printSendingInformation(this, current, w0);
                    current.send(w0);
                }

                if (w1 != null && current.range[i].isNodeInsideRange(w1)) {
                    this.printSendingInformation(this, current, w1);
                    current.send(w1);
                }
            }

            SortedSet<Node> nodes_greater_than_us = this.neighbours[i].tailSet(this, false);
            for (Node current : nodes_greater_than_us) {
                if (v0 != null && current.range[i].isNodeInsideRange(v0)) {
                    this.printSendingInformation(this, current, v0);
                    current.send(v0);
                }

                if (v1 != null && current.range[i].isNodeInsideRange(v1)) {
                    this.printSendingInformation(this, current, v1);
                    current.send(v1);
                }
            }
        }
    }

    private void introduceAllNeighboursAtLevelToEachOther(int i) {
        for (Node node1 : this.neighbours[i]) {
            for (Node node2 : this.neighbours[i]) {
                if (!node1.equals(node2)) {
                    node1.send(node2);
                    node2.send(node1);
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

        boolean temporary = true;
        for (int i = 0; i < NUMBER_OF_BITS; i++) {
            // predecessors
            if (this.isGreaterThan(v)) {                              // überprüfe ob v Vorgänger von this
                if (prefixMatch(i, this, v, 1)) {                       // prüfe ob prefix_i konkateniert mit 1 mit id(v) übereinstimmt
                    if (predecessor[i].getNodeOne() == null || predecessor[i].getNodeOne().isLessThan(v)) {  // prüfe ob besserer nächste Vorgänger existiert
                        predecessor[i].setNodeOne(v);
                        this.updateRange(i);
                        this.updateNeighbours(i);

                        if (!this.neighbours[i].contains(v)) {
                            this.neighbours[i].add(v);
                            v.send(this);

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
                            this.neighbours[i].add(v);
                            v.send(this);

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
                            this.neighbours[i].add(v);
                            v.send(this);

                            this.introduceAllNeighboursAtLevelToEachOther(i);
                            temporary = false;
                        }
                    }
                }

                if (prefixMatch(i, this, v, 0)) {          // prüfe ob prefix_i konkateniert mit 0 mit id(v) übereinstimmt
                    if (successor[i].getNodeZero() == null || successor[i].getNodeZero().isGreaterThan(v)) {
                        successor[i].setNodeZero(v);
                        this.updateRange(i);
                        this.updateNeighbours(i);

                        if (!this.neighbours[i].contains(v)) {
                            this.neighbours[i].add(v);
                            v.send(this);

                            this.introduceAllNeighboursAtLevelToEachOther(i);
                            temporary = false;
                        }
                    }
                }
            }

            // fügt Knoten in die Nachbarschaft ein, falls Prefix gleich und inerhalb des Ranges

            if (this.prefixMatch(i, v) && range[i].isNodeInsideRange(v)) {
                if (!this.neighbours[i].contains(v)) {
                    this.neighbours[i].add(v);
                    v.send(this);

                    this.introduceAllNeighboursAtLevelToEachOther(i);       // Nötig
                    temporary = false;
                }
            }
        }

        Node largestCommonPrefixNode = this.getNodeWithLargestCommonPrefix(v);
        if (temporary && largestCommonPrefixNode != null) {
            largestCommonPrefixNode.send(v);        // Nötig
        }
    }

    /**
     * Returns the id of the node.
     * @return
     */
    public BitSequence getID() {
        return this.ID;
    }

    // TODO: Exceptions für fehlerhafte Ranges (z.B. Anfang > Ende)
    /**
     * Checks and updates the range at level i if it is not valid.
     * The valid range of level i is:
     *     range_i(v) = [min_b(pred_i(v,b)), max_b(succ_i(v,b))], b={0,1}
     * This method is called after a successor or predecessor of the node changed.
     * @param i The level of range to check and update.
     */
    private void updateRange(int i) {
        if (i < 0 || i > NUMBER_OF_BITS) {
            throw new IllegalArgumentException("Level i not legal");
        }

        if (predecessor[i].getNodeZero() == null && predecessor[i].getNodeOne() != null) {
            range[i].setBegin(predecessor[i].getNodeOne());
        } else if (predecessor[i].getNodeOne() == null && predecessor[i].getNodeZero() != null) {
            range[i].setBegin(predecessor[i].getNodeZero());
        } else if (predecessor[i].getNodeZero() == null && predecessor[i].getNodeOne() == null) {
            // do nothing
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
            // do nothing
        } else if (successor[i].getNodeZero().isGreaterThan(successor[i].getNodeOne())) {
            range[i].setEnd(successor[i].getNodeZero());
        } else {
            range[i].setEnd(successor[i].getNodeOne());
        }
    }

    // TODO: think about better naming
    /**
     * Updates the level-i-neighbours of the node. Every neighbour that
     * is not inside the range at level i will be removed from the neighbourhood
     * at this level and delegated to the node with biggest matching prefix known.
     * This method is called after a successor or predecessor of the node changed.
     * @param i The level of the skip+ graph.
     */
    private void updateNeighbours(int i) {
        TreeSet<Node> removedNeighbours = new TreeSet<>();

        for (Node node : neighbours[i]) {
            if (!range[i].isNodeInsideRange(node)) {
                removedNeighbours.add(node);

                // Find the node with biggest matching prefix.
                for (int j = NUMBER_OF_BITS-1; j >= 0; j--) { // checken ob Ausgangspunkt von i reicht
                    boolean neighbourDelegated = false;
                    for (Node neighbour : neighbours[j]) {
                        if (!neighbour.equals(node) && prefixMatch(j, neighbour)) {
                            this.printSendingInformation(this, neighbour, node);
                            neighbour.send(node);
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
    }

    /**
     * Checks if this node is greater than another node (more specific, it compares the IDs).
     * @param anotherNode
     * @return true, if this node is strictly greater. false, if not.
     */
    public boolean isGreaterThan(Node anotherNode) {
        if (anotherNode == null) {
            return true;
        }

        return this.getID().isGreaterThan(anotherNode.getID());
    }

    /**
     * Checks if this node is smaller than another node (more specific, it compares the IDs).
     * @param anotherNode
     * @return true, if this node is strictly less. false, if not.
     */
    public boolean isLessThan(Node anotherNode) {
        if (anotherNode == null) {
            return true;
        }

        return this.getID().isLessThan(anotherNode.getID());
    }

    public Node getNodeWithLargestCommonPrefix(Node node) {
        TreeSet<Node> neighboursFromAllLevels = new TreeSet<>();

        for (int i = 0; i < NUMBER_OF_BITS; i++) {
            for (Node neighbour : neighbours[i]) {
                neighboursFromAllLevels.add(neighbour);
            }
        }

        for (int i = NUMBER_OF_BITS-1; i >= 0; i--) {
            for (Node neighbour : neighboursFromAllLevels) {
                if (neighbour.prefixMatch(i, node)) {
                    return neighbour;
                }
            }
        }

        return null;
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
     * Compares the prefixes of this node to another node.
     * @param i Length of prefix to compare.
     * @param anotherNode
     * @return
     */
    public boolean prefixMatch(int i, Node anotherNode) {
        BitSequence thisPrefix = this.getID().getPrefix(i);
        BitSequence anotherPrefix = anotherNode.getID().getPrefix(i);

        return thisPrefix.equals(anotherPrefix);
    }

    public synchronized void printNeighbourhood() {
        println("########################################################################");

        println("Knoten ID: " + this.getID());

        print("Received Nodes: ");
        Iterator<Node> iterator = receivedNodes.iterator();
        while(iterator.hasNext()) {
            print(iterator.next().getID() + ", ");
        }
        println("");

        for (int i = 0; i < NUMBER_OF_BITS; i++) {
            println("Level " + i +  "  Pred: " + this.predecessor[i] + " Succ:" + this.successor[i]);
            for (Node neighbour : this.neighbours[i]) {
                print("\t" + neighbour.getID() + ", ");
            }
            println("");
        }

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
        if (i > NUMBER_OF_BITS-1) {
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
            throw new IllegalArgumentException("Object compared to node is null.");
        }
    }

    public void printSendingInformation(Node sender, Node receiver, Node message) {
        if (message.getID().toString().equals("")) {
            println("##############\nSender: " + sender.getID() + "\nReceiver: " + receiver.getID() + "\nMessage: " + message.getID() + "\n##############");
        }
    }
}
