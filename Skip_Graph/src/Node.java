import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by twiens, fischerr, jeromeK on 8/23/16.
 */
public class Node extends Subject {
    private static final int NUMBER_OF_BITS = 3;

    private BitSequence ID;
    private HashSet<Node>[] neighbours;
    private Range[] range;
    private Pair[] predecessor;
    private Pair[] successor;

    public Node() {
        this.ID = UniqueRandomBitStringGenerator.GenerateUniqueRandomBitSet(this.NUMBER_OF_BITS);
        this.neighbours = new HashSet[NUMBER_OF_BITS];
        this.range = new Range[NUMBER_OF_BITS];
        this.predecessor = new Pair[NUMBER_OF_BITS];
        this.successor = new Pair[NUMBER_OF_BITS];

        for (int i = 0; i < NUMBER_OF_BITS; i++) {
            this.predecessor[i] = new Pair();
            this.successor[i] = new Pair();
            this.range[i] = new Range();
            this.neighbours[i] = new HashSet<>();
        }
    }

    public Node (String sequence) {
        println("Constructor " + sequence);

        this.ID = new BitSequence(sequence);
        this.neighbours = new HashSet[NUMBER_OF_BITS];
        this.range = new Range[NUMBER_OF_BITS];
        this.predecessor = new Pair[NUMBER_OF_BITS];
        this.successor = new Pair[NUMBER_OF_BITS];

        for (int i = 0; i < NUMBER_OF_BITS; i++) {
            this.predecessor[i] = new Pair();
            this.successor[i] = new Pair();
            this.range[i] = new Range();
            this.neighbours[i] = new HashSet<>();
        }
    }

    @Override
    protected void init() {

    }

    @Override
    protected void onMessageReceived(Object message) {
        if (message instanceof Node) {
            this.linearize((Node) message);
        }
    }

    @Override
    protected void onTimeout() {
        // Regel 1a
        // Für jeden Level i stellt jeder Knoten u periodisch alle Knoten in N_i(u) vor, dass diese eine sortierte Liste
        // bilden

        // Regel 1b
        // Für jeden Level i stellt jeder Knoten u periodisch seinen nächsten Vorgängern und Nachfolgern den Knoten
        // v_j e N_i(v) vor, dass alle Knoten links von u den Nachfolger rechts von u kennenlernen und alle Knoten
        // rechts von u den Vorgänger links von u kennenlernen
    }

    private void linearize(Node v) {
        // Bei Erhalt von v aktualisiert u seine Ranges und Nachbarschaften für jeden Level i.
        // Für jeden Knoten w, den u nicht mehr benötigt (da er in keinem range_i(u) ist), delegiert u w zu dem Knoten
        // w' in seiner neuen Nachbarschaft mit größter Präfixübereinstimmung zwischen w' und w, der am nächsten zu w
        // ist.
        // Sei i der maximale Wert mit prefix_i(u)=prefix_i(w). Dann muss gelten, dass prefix_i+1(w)=prefix_i+1(w') für
        // den Knoten w', zu dem w delegiert wird. Solch ein Knoten existiert immer, da w !e range_i(u), und er muss
        // zwischen u und w liegen, d.h. der ID-Bereich von (w',w) ist innerhalb des ID-Bereichs von (u,w)

        println("Start linearize from node " + v.getID());

        for (int i = 0; i < NUMBER_OF_BITS; i++) {
            // fügt Knoten in die Nachbarschaft ein, falls Prefix gleich und inerhalb des Ranges
            if (this.prefixMatch(i, v) && range[i].isNodeInsideRange(v)) {
                println("Knoten " + this.getID() + " überprüft ob Knoten " + v.getID() +
                        " in seiner Range ist und der Prefix matched. [Level " + i + "]");
                this.neighbours[i].add(v);
            }

            // predecessors
            if (this.isGreaterThan(v)) {                              // überprüfe ob v Vorgänger von this
                if (prefixMatch(i, this, v, 1)) {                       // prüfe ob prefix_i konkateniert mit 1 mit id(v) übereinstimmt
                    if (predecessor[i].getNodeOne() == null || predecessor[i].getNodeOne().isLessThan(v)) {  // prüfe ob besserer nächste Vorgänger existiert
                        // TODO: delgiere alten Vorgänger zu Knoten weiter mit größerer Prefixübereinstimmung

                        predecessor[i].setNodeOne(v);
                        this.updateRange(i);
                        this.updateNeighbours(i);
                    }
                }

                if (prefixMatch(i, this, v, 0)) {          // prüfe ob prefix_i konkateniert mit 0 mit id(v) übereinstimmt
                    if (predecessor[i].getNodeZero() == null || predecessor[i].getNodeZero().isLessThan(v)) {
                        // TODO: delgiere alten Nachfolger zu Knoten weiter mit größerer Prefixübereinstimmung

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
                    // TODO: delgiere alten Nachfolger zu Knoten weiter mit größerer Prefixübereinstimmung

                    successor[i].setNodeOne(v);
                    this.updateRange(i);
                    this.updateNeighbours(i);
                    }
                }

                if (prefixMatch(i, this, v, 0)) {          // prüfe ob prefix_i konkateniert mit 0 mit id(v) übereinstimmt
                    if (successor[i].getNodeZero() == null || successor[i].getNodeZero().isGreaterThan(v)) {
                        // TODO: delgiere alten Nachfolger zu Knoten weiter mit größerer Prefixübereinstimmung

                        successor[i].setNodeZero(v);
                            this.updateRange(i);
                            this.updateNeighbours(i);
                    }
                }
            }
        }
    }

    public BitSequence getID() {
        return this.ID;
    }

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
            range[i].setBegin(successor[i].getNodeOne());
        } else if (successor[i].getNodeOne() == null && successor[i].getNodeZero() != null) {
            range[i].setBegin(successor[i].getNodeZero());
        } else if (successor[i].getNodeZero() == null && successor[i].getNodeOne() == null) {
            // Nix machen
        } else if (successor[i].getNodeZero().isGreaterThan(successor[i].getNodeOne())) {
            range[i].setBegin(successor[i].getNodeZero());
        } else {
            range[i].setBegin(successor[i].getNodeOne());
        }
    }

    // TODO: think about better naming
    private void updateNeighbours(int i) {
        for (Node node : neighbours[i]) {
            if (!range[i].isNodeInsideRange(node)) {
                for (int j = NUMBER_OF_BITS-1; j >= 0; j--) { // checken ob Ausgangspunkt von i reicht
                    for (Node neighbour : neighbours[j]) {
                        if (prefixMatch(j, neighbour)) {
                            neighbour.send(node);

                            neighbours[i].remove(node);
                        }
                    }
                }
            }
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

    private boolean prefixMatch(int i, Node anotherNode) {
        BitSequence thisPrefix = this.getID().getPrefix(i);
        BitSequence anotherPrefix = anotherNode.getID().getPrefix(i);

        return thisPrefix.equals(anotherPrefix);
    }

    public void printNeighbourhood() {
        println("This Knoten ID " + this.getID());

        for (int i = 0; i < NUMBER_OF_BITS; i++) {
            println("Ausgabe der Nachbarn Level " + i);
            for (Node neighbour : this.neighbours[i]) {
                print(" Neighbour: " + neighbour.getID());
            }
            println("");
        }

        println("####################################");
    }

    public boolean equals(Object anotherObject) {
        if (!(anotherObject instanceof Node)) {
            return false;
        }

        return this.getID().equals(((Node) anotherObject).getID());
    }

    private static boolean prefixMatch(int i, Node firstNode, Node secondNode, int b) {
        if (i > NUMBER_OF_BITS-1) {
            throw new UnsupportedOperationException();
        }

        BitSequence firstId = firstNode.getID().getPrefix(i);       // exceptions werfen wenn i > Bitlänge-1
        if (b == 1) {
            firstId = firstId.append(true);
        } else {
            firstId.append(false);
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
}
