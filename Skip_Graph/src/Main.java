import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by twiens on 24.08.16.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        //for (int i = 0; i < Math.pow(2, 2); i++) {
        //    Node tempNode = new Node();
        //}

        int n = 8;

        Node[] nodes = new Node[n];

        String[] nodesIds = new String[]{
            "000",
            "001",
            "010",
            "011",
            "100",
            "101",
            "110",
            "111",
        };

        for (int i = 0; i < n; i++) {
            nodes[i] = new Node(nodesIds[i]);
        }

        //nodes[2].send(nodes[7]);
        //Thread.sleep(1000);
        //nodes[2].send(nodes[5]);
        nodes[0].send(nodes[1]);
        nodes[2].send(nodes[1]);
        //nodes[3].send(nodes[9]);
        nodes[4].send(nodes[2]);
        nodes[4].send(nodes[3]);
        nodes[5].send(nodes[0]);
        nodes[5].send(nodes[4]);        // TODO: funktioniert nicht, wenn diese Kante entfernt wird ... WARUM?!?!
        nodes[0].send(nodes[3]);        // TODO:
        nodes[6].send(nodes[5]);
        nodes[6].send(nodes[7]);
        //nodes[6].send(nodes[8]);
        //nodes[9].send(nodes[8]);
        //nodes[7].send(nodes[8]);

        for (int i = 0; i < n; i++) {
            nodes[i].start();
        }

        // 3 sekunden laufen lassen
        Thread.sleep(3000);

        for (int i = 0; i < n; i++) {
            nodes[i].stopSubject();
        }

        for (int i = 0; i < n; i++) {
            nodes[i].printNeighbourhood();
        }

        System.out.println("################ Ultimativer Skip+-Graph Korrektheitstest ################");
        System.out.println("Ergebnis = " + testSkipGraph(nodes));
    }

    private static boolean testSkipGraph(Node[] nodes) {
        TreeSet<Node> treeSet = new TreeSet<>();
        for (Node node : nodes) {
            treeSet.add(node);
        }

        for (Node node : nodes) {
            // hole alle Nachfolger vom aktuellen Knoten
            SortedSet<Node> successors = treeSet.tailSet(node, false);

            // hole alle Vorgägner vom aktuellen Knoten
            SortedSet<Node> predecessors = treeSet.headSet(node, false).descendingSet();    // invertiere die Menge, damit nächster Vorgänger
                                                                                            // vorne ist

            Range[] ranges = new Range[Node.NUMBER_OF_BITS];
            for (int i = 0; i < Node.NUMBER_OF_BITS; i++) {
                ranges[i] = new Range();

                // Nachfolger bestimmen
                boolean foundSuccessor0 = false;
                boolean foundSuccessor1 = false;
                Node successor1 = Node.minNode;
                Node successor0 = Node.minNode;
                for (Node successor : successors) {
                    if (!foundSuccessor0 && Node.prefixMatch(i, node, successor, 0)) {
                        successor0 = successor;
                        foundSuccessor0 = true;

                        if (node.successor[i].getNodeZero() != successor) {
                            System.out.println("Successor 0 is wrong");
                            System.out.println("Level: " + i + " Node: " + node.getID());
                            System.out.println("Successor: " + node.successor[i].getNodeZero().getID() + " BUT should be " + successor.getID());
                            return false;
                        }
                    }

                    if (!foundSuccessor1 && Node.prefixMatch(i, node, successor, 1)) {
                        successor1 = successor;
                        foundSuccessor1 = true;
                        if (node.successor[i].getNodeOne() != successor) {
                            System.out.println("Successor 1 is wrong");
                            System.out.println("Level: " + i + " Node: " + node.getID());
                            System.out.println("Successor: " + node.successor[i].getNodeOne().getID() + " BUT should be " + successor.getID());
                            return false;
                        }
                    }
                }

                if (successor0.isGreaterThan(successor1)) {
                    ranges[i].setEnd(successor0);
                } else if (successor1.isGreaterThan(successor0)) {
                    ranges[i].setEnd(successor1);
                }

                // Vorgänger bestimmen
                boolean foundPredecessor0 = false;
                boolean foundPredecessor1 = false;
                Node predecessor0 = Node.minNode;
                Node predecessor1 = Node.minNode;
                for (Node predecessor : predecessors) {
                    if (!foundPredecessor0 && Node.prefixMatch(i, node, predecessor, 0)) {
                        foundPredecessor0 = true;
                        if (node.predecessor[i].getNodeZero() != predecessor) {
                            System.out.println("Predecessor 0 is wrong");
                            return false;
                        }

                        predecessor0 = predecessor;
                    }

                    if (!foundPredecessor1 && Node.prefixMatch(i, node, predecessor, 1)) {
                        foundPredecessor1 = true;
                        if (node.predecessor[i].getNodeOne() != predecessor) {
                            System.out.println("Predecessor 1 is wrong");
                            return false;
                        }

                        predecessor1 = predecessor;
                    }
                }

                if (predecessor0.isLessThan(predecessor1)) {
                    ranges[i].setBegin(predecessor0);
                } else if (predecessor1.isLessThan(predecessor0)) {
                    ranges[i].setBegin(predecessor1);
                }

                // vergleiche Ranges
                if (node.range[i].getBegin() != ranges[i].getBegin()) {
                    System.out.println("Range begin is wrong");
                    System.out.println("Level: " + i);
                    System.out.println("Node: " + node.getID() + " " + node.range[i] + " BUT should be " + ranges[i]);
                    return false;
                }

                if (node.range[i].getEnd() != ranges[i].getEnd()) {
                    System.out.println("Range end is wrong");
                    System.out.println("Level: " + i);
                    System.out.println("Node: " + node.getID() + " " + node.range[i] + " BUT should be " + ranges[i]);
                    return false;
                }

                // vergleiche Nachbarschaften
                for (Node n : nodes) {
                    if (!n.equals(node) && node.range[i].isNodeInsideRange(n) && node.prefixMatch(i, n)) {
                        if (!node.neighbours[i].contains(n)) {
                            System.out.println("Neighbourhood is wrong");
                            System.out.println("Level: " + i);
                            System.out.println("Node: " + node.getID());
                            System.out.println("Neighbourhoud");
                            node.printNeighbourhood(i);
                            System.out.println(" BUT should contain " + n.getID());
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }
}
