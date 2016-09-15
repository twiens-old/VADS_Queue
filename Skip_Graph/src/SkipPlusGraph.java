import java.util.ArrayList;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by twiens on 9/9/16.
 */
public class SkipPlusGraph {

    public final int numberOfBits;

    private ArrayList<Node> nodes;

    /**
     * Constructor for creating a new instance of the {@link SkipPlusGraph}
     * @param numberOfBits
     */
    public SkipPlusGraph(int numberOfBits) {
        this.numberOfBits = numberOfBits;
        nodes = new ArrayList<>();
    }

    public void join(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("The node added to the skip graph cannot be null.");
        }

        if (!node.isAlive()) {
            node.start();
        }

        // if there are other nodes contained in the graph, the joining node has to be send to one of these nodes
        if (nodes.size() > 0) {
            int responsibleNodePosition = randInt(0, nodes.size() - 1);
            //int responsibleNodePosition = 0;

            if (nodes.get(responsibleNodePosition).equals(node)) {
                System.out.println("JOIN SELF INTRODUCTION");
                System.exit(-1);
            }

            nodes.get(responsibleNodePosition).send(node);
        }

        nodes.add(node);
    }

    public void leave(Node node) {      // TODO: evtl. über hearbeating den Fall abdecken, dass ein Prozess einfach ausfällt
        if (node == null) {
            throw new IllegalArgumentException("The node removed from skip graph cannot be null.");
        }

        node.leave();

        if (node.isAlive()) {
            node.stopSubject();
        }

        nodes.remove(node);
    }

    public void printNeighbourHoodForAllLevels() {
        for (Node node : this.nodes) {
            node.printNeighbourhood();
        }
    }

    public boolean testSkipPlusGraph() {
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

            Range[] ranges = new Range[this.numberOfBits];
            for (int i = 0; i < this.numberOfBits; i++) {
                ranges[i] = new Range();

                // Nachfolger bestimmen
                boolean foundSuccessor0 = false;
                boolean foundSuccessor1 = false;
                Node successor1 = Node.maxNode;
                Node successor0 = Node.maxNode;
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
                            if (node.successor[i].getNodeOne() != null) {
                                System.out.println("Successor: " + node.successor[i].getNodeOne().getID() + " BUT should be " + successor.getID());
                            } else {
                                System.out.println("Successor: is null BUT should be " + successor.getID());
                            }

                            return false;
                        }
                    }
                }

                if (successor0.equals(Node.maxNode) && !successor1.equals(Node.maxNode)) {
                    ranges[i].setEnd(successor1);
                } else if (!successor0.equals(Node.maxNode) && successor1.equals(Node.maxNode)) {
                    ranges[i].setEnd(successor0);
                } else if (successor0.equals(Node.maxNode) && successor1.equals(Node.maxNode)) {
                    ranges[i].setEnd(successor0);
                } else if (successor0.isGreaterThan(successor1)) {
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
                            System.out.println("Level: " + i + " Node: " + node.getID());
                            if (node.predecessor[i].getNodeZero() != null) {
                                System.out.println("Predecessor: " + node.predecessor[i].getNodeZero().getID() + " BUT should be " + predecessor.getID());
                            } else {
                                System.out.println("Predecessor: is null BUT should be " + predecessor.getID());
                            }

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

                if (predecessor0.equals(Node.minNode) && !predecessor1.equals(Node.minNode)) {
                    ranges[i].setBegin(predecessor1);
                } else if (!predecessor0.equals(Node.minNode) && predecessor1.equals(Node.minNode)) {
                    ranges[i].setBegin(predecessor0);
                } else if (predecessor0.equals(Node.minNode) && predecessor1.equals(Node.minNode)) {
                    ranges[i].setBegin(predecessor0);
                } else if (predecessor0.isLessThan(predecessor1)) {
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

    private static int randInt(int min, int max) {
        Random rand = new Random();

        return rand.nextInt((max-min) + 1) + min;
    }
}
