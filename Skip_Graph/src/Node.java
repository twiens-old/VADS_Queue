import java.util.BitSet;
import java.util.Set;

/**
 * Created by twiens, fischerr, jeromeK on 8/23/16.
 */
public class Node extends Subject {
    private static final int NUMBER_OF_BITS = 3;

    private final BitSet ID;
    private Set<Node>[] neighbours;
    private Range[] range;
    private Pair[] predecessor;
    private Pair[] successor;

    public Node() {
        this.ID = UniqueRandomBitStringGenerator.GenerateUniqueRandomBitSet(this.NUMBER_OF_BITS);

        println(this.ID.toString());
    }

    @Override
    protected void init() {

    }

    @Override
    protected void onMessageReceived(Object message) {

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

        for (int i = 0; i < NUMBER_OF_BITS; i++) {
            // predecessors
            if (this.isGreaterThan(v)) {                              // überprüfe ob v Vorgänger von this
                if (prefixMatch(i, this, v, 1)) {                       // prüfe ob prefix_i konkateniert mit 1 mit id(v) übereinstimmt
                    if (predecessor[i].getNodeOne().isLessThan(v)) {  // prüfe ob besserer nächste Vorgänger existiert
                        // TODO: delgiere alten Vorgänger zu Knoten weiter mit größerer Prefixübereinstimmung

                        predecessor[i].setNodeOne(v);
                        this.updateRange(i);
                    }
                }

                if (prefixMatch(i, this, v, 0)) {          // prüfe ob prefix_i konkateniert mit 0 mit id(v) übereinstimmt
                    if (predecessor[i].getNodeZero().isLessThan(v)) {
                        // TODO: delgiere alten Nachfolger zu Knoten weiter mit größerer Prefixübereinstimmung

                        predecessor[i].setNodeZero(v);
                        this.updateRange(i);
                    }
                }

                // successors
                if (this.isLessThan(v)) {                   // überprüfe ob v Nachfolger von this
                    if (prefixMatch(i, this, v, 1)) {   // prüfe ob prefix_i konkateniert mit 1 mit id(v) übereinstimmt
                        if (successor[i].getNodeOne().isGreaterThan(v)) {  // prüfe ob besserer nächste Nachfolger existiert
                            // TODO: delgiere alten Nachfolger zu Knoten weiter mit größerer Prefixübereinstimmung

                            successor[i].setNodeOne(v);
                            this.updateRange(i);
                        }
                    }

                    if (prefixMatch(i, this, v, 0)) {          // prüfe ob prefix_i konkateniert mit 0 mit id(v) übereinstimmt
                        if (successor[i].getNodeZero().isGreaterThan(v)) {
                            // TODO: delgiere alten Nachfolger zu Knoten weiter mit größerer Prefixübereinstimmung

                            successor[i].setNodeZero(v);
                            this.updateRange(i);
                        }
                    }
                }
            }
        }
    }

    private BitSet getID() {
        return this.ID;
    }

    private void updateRange(int i) {
        // TODO: Exceptions für fehlerhafte Ranges (z.B. Anfang > Ende)

        if (predecessor[i].getNodeZero().isLessThan(predecessor[i].getNodeOne())) {
            range[i].setBegin(predecessor[i].getNodeZero());
        } else {
            range[i].setBegin(predecessor[i].getNodeOne());
        }

        if (successor[i].getNodeZero().isGreaterThan(successor[i].getNodeOne())) {
            range[i].setBegin(successor[i].getNodeZero());
        } else {
            range[i].setBegin(successor[i].getNodeOne());
        }
    }

    private void updateNeighbours(int i) {
        for (Node node : neighbours[i]) {
            if (!range[i].isNodeInsideRange(node)) {
                // TODO: delegiere weiter und lösche aus Range
            }
        }
    }

    private boolean isGreaterThan(Node anotherNode) {
            return convertBitSetToInt(anotherNode.getID()) > convertBitSetToInt(this.getID());
    }

    private boolean isLessThan(Node anotherNode) {
        return convertBitSetToInt(anotherNode.getID()) < convertBitSetToInt(this.getID());
    }

    private static boolean prefixMatch(int i, Node firstNode, Node secondNode, int b) {
        BitSet firstId = firstNode.getID().get(0, i);       // exceptions werfen wenn i > Bitlänge-1
        if (b == 1) {
            firstId.set(i+1);
        } else {
            firstId.clear(i+1);
        }

        BitSet secondId = secondNode.getID().get(0, i+1);

        for (int j = 0; j < i; j++) {
            if (firstId.get(j) != secondId.get(j)) {
                return false;
            }
        }

        return true;
    }

    private static int convertBitSetToInt(BitSet bits) {
        int value = 0;
        for (int i = 0; i < bits.length(); ++i) {
            value += bits.get(i) ? (1 << i) : 0;
        }
        return value;
    }
}
