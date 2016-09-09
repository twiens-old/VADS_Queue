import java.util.BitSet;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by twiens, fischerr, jeromeK on 8/23/16.
 */
public class Node extends Subject implements Comparable<Node> {
    public static final int NUMBER_OF_BITS = 3;
    public static final Node minNode = new Node("");
    public static final Node maxNode = new Node("1111");    // TODO: dynamisch erzeugen

    private BitSequence ID;
    public TreeSet<Node>[] neighbours;
    public Range[] range;
    public Pair[] predecessor;
    public Pair[] successor;

    private TreeSet<Node> receivedNodes = new TreeSet<>();

    public Node() {
        this.ID = UniqueRandomBitStringGenerator.GenerateUniqueRandomBitSet(this.NUMBER_OF_BITS);
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

    @Override
    protected void init() {

    }

    @Override
    protected void onMessageReceived(Object message) {
        if (message instanceof Node) {
            this.receivedNodes.add((Node) message);
            this.linearize((Node) message);
        }
    }

    @Override
    protected void onTimeout() {
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

        // Regel 1b
        // Für jeden Level i stellt jeder Knoten u periodisch seinen nächsten Vorgängern und Nachfolgern den Knoten
        // v_j e N_i(v) vor, dass alle Knoten links von u den Nachfolger rechts von u kennenlernen und alle Knoten
        // rechts von u den Vorgänger links von u kennenlernen

        for (int i = 0; i < NUMBER_OF_BITS; i++) {
            //region Alte Version der Regel 1b
            /*Node v = this.neighbours[i].lower(this);
            Node w = this.neighbours[i].higher(this);

            if (w != null) {
                SortedSet<Node> nodes_less_than_us = this.neighbours[i].headSet(this);
                for (Node current : nodes_less_than_us) {
                    if (current.range[i].isNodeInsideRange(w)) {
                        this.printSendingInformation(this, current, w);

                        current.send(w);
                    }
                }
            }

            if (v != null && w != null) {
                SortedSet<Node> nodes_greater_than_us = this.neighbours[i].tailSet(this, false);
                for (Node current : nodes_greater_than_us) {
                    if (current.range[i].isNodeInsideRange(v)) {
                        this.printSendingInformation(this, current, v);

                        current.send(v);
                    }
                }
            } */
            //endregion

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

    private void linearize(Node v) {
        // Bei Erhalt von v aktualisiert u seine Ranges und Nachbarschaften für jeden Level i.
        // Für jeden Knoten w, den u nicht mehr benötigt (da er in keinem range_i(u) ist), delegiert u w zu dem Knoten
        // w' in seiner neuen Nachbarschaft mit größter Präfixübereinstimmung zwischen w' und w, der am nächsten zu w
        // ist.
        // Sei i der maximale Wert mit prefix_i(u)=prefix_i(w). Dann muss gelten, dass prefix_i+1(w)=prefix_i+1(w') für
        // den Knoten w', zu dem w delegiert wird. Solch ein Knoten existiert immer, da w !e range_i(u), und er muss
        // zwischen u und w liegen, d.h. der ID-Bereich von (w',w) ist innerhalb des ID-Bereichs von (u,w)

        for (int i = 0; i < NUMBER_OF_BITS; i++) {
            // predecessors
            if (this.isGreaterThan(v)) {                              // überprüfe ob v Vorgänger von this
                if (prefixMatch(i, this, v, 1)) {                       // prüfe ob prefix_i konkateniert mit 1 mit id(v) übereinstimmt
                    if (predecessor[i].getNodeOne() == null || predecessor[i].getNodeOne().isLessThan(v)) {  // prüfe ob besserer nächste Vorgänger existiert
                        predecessor[i].setNodeOne(v);
                        this.updateRange(i);
                        this.updateNeighbours(i);
                    }
                }

                if (prefixMatch(i, this, v, 0)) {          // prüfe ob prefix_i konkateniert mit 0 mit id(v) übereinstimmt
                    if (predecessor[i].getNodeZero() == null || predecessor[i].getNodeZero().isLessThan(v)) {
                        predecessor[i].setNodeZero(v);
                        this.updateRange(i);
                        this.updateNeighbours(i);
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
                    }
                }

                if (prefixMatch(i, this, v, 0)) {          // prüfe ob prefix_i konkateniert mit 0 mit id(v) übereinstimmt
                    if (successor[i].getNodeZero() == null || successor[i].getNodeZero().isGreaterThan(v)) {
                        successor[i].setNodeZero(v);
                            this.updateRange(i);
                            this.updateNeighbours(i);
                    }
                }
            }

            // fügt Knoten in die Nachbarschaft ein, falls Prefix gleich und inerhalb des Ranges
            if (this.prefixMatch(i, v) && range[i].isNodeInsideRange(v)) {
                if (!this.neighbours[i].contains(v)) {
                    this.neighbours[i].add(v);

                    for (Node node1 : this.neighbours[i]) {
                        for (Node node2 : this.neighbours[i]) {
                            if (!node1.equals(node2)) {
                                node1.send(node2);
                                node2.send(node1);
                            }
                        }
                    }
                }
            }
        }
    }

    public BitSequence getID() {
        return this.ID;
    }

    // range_i(v) = [min_b(pred_i(v,b)), max_b(succ_i(v,b))] b={0,1}
    private void updateRange(int i) {
        // TODO: Exceptions für fehlerhafte Ranges (z.B. Anfang > Ende)

        if (predecessor[i].getNodeZero() == null && predecessor[i].getNodeOne() != null) {
            range[i].setBegin(predecessor[i].getNodeOne());
        } else if (predecessor[i].getNodeOne() == null && predecessor[i].getNodeZero() != null) {
            range[i].setBegin(predecessor[i].getNodeZero());
        } else if (predecessor[i].getNodeZero() == null && predecessor[i].getNodeOne() == null) {
            // Nix machen
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
            // Nix machen
        } else if (successor[i].getNodeZero().isGreaterThan(successor[i].getNodeOne())) {
            range[i].setEnd(successor[i].getNodeZero());
        } else {
            range[i].setEnd(successor[i].getNodeOne());
        }
    }

    // TODO: think about better naming
    private void updateNeighbours(int i) {
        TreeSet<Node> removedNeighbours = new TreeSet<>();

        for (Node node : neighbours[i]) {
            if (!range[i].isNodeInsideRange(node)) {
                removedNeighbours.add(node);
                                                             // finde Knoten mit größer Präfixübereinstimmung
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

        StringBuilder sb = new StringBuilder();
        sb.append("########################################################################\n");
        sb.append("Removed Neighbours in Node " + this.getID() + " - Range: " + this.range[i] + "\n");
        sb.append("Level = " + i + "\n");

        for (Node node : removedNeighbours) {
            this.neighbours[i].remove(node);
            sb.append(node.getID() + ", ");
        }

        sb.append("\n########################################################################");

        if (removedNeighbours.size() > 0) {
            println(sb.toString());
        }
    }

    public boolean isGreaterThan(Node anotherNode) {
        if (anotherNode == null) {
            return true;
        }

        return this.getID().isGreaterThan(anotherNode.getID());
    }

    public boolean isLessThan(Node anotherNode) {
        if (anotherNode == null) {
            return true;
        }

        return this.getID().isLessThan(anotherNode.getID());
    }

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

    public boolean equals(Object anotherObject) {
        if (!(anotherObject instanceof Node)) {
            return false;
        }

        return this.getID().equals(((Node) anotherObject).getID());
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

    private static int convertBitSetToInt(BitSet bits) {
        int value = 0;
        for (int i = 0; i < bits.length(); ++i) {
            value += bits.get(i) ? (1 << i) : 0;
        }
        return value;
    }

    private static String convertBitSetToString(BitSet bitSet) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < NUMBER_OF_BITS; i++) {
            if (bitSet.get(i)) {
                sb.insert(0, 1);
            } else {
                sb.insert(0, 0);
            }
        }

        return sb.toString();
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
