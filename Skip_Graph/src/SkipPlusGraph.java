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
            throw new IllegalArgumentException("The destination added to the skip graph cannot be null.");
        }

        if (!node.isAlive()) {
            node.start();
        }

        // if there are other nodes contained in the graph, the joining destination has to be send to one of these nodes
        if (nodes.size() > 0) {
            int responsibleNodePosition = randInt(0, nodes.size() - 1);
            //int responsibleNodePosition = 0;

            if (nodes.get(responsibleNodePosition).equals(node)) {
                System.out.println("JOIN SELF INTRODUCTION");
                System.exit(-1);
            }

            nodes.get(responsibleNodePosition).send(new NodeMessage(null, nodes.get(responsibleNodePosition), node, NodeMessage.MessageType.INTRODUCE));
        }

        nodes.add(node);
    }

    public void leave(Node node) {      // TODO: evtl. über hearbeating den Fall abdecken, dass ein Prozess einfach ausfällt
        if (node == null) {
            throw new IllegalArgumentException("The destination removed from skip graph cannot be null.");
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
        StringBuilder sb = new StringBuilder();
        boolean result = true;

        TreeSet<Node> treeSet = new TreeSet<>();
        for (Node node : nodes) {
            treeSet.add(node);
        }

        Node firstNode = treeSet.first();
        Node lastNode = treeSet.last();

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
                Node successor1 = null;
                Node successor0 = null;
                for (Node successor : successors) {
                    if (!foundSuccessor0 && Node.prefixMatch(i, node, successor, 0)) {
                        successor0 = successor;
                        foundSuccessor0 = true;

                        if (node.successor[i].getNodeZero() != successor) {
                            sb.append("Successor 0 is wrong\n");
                            sb.append("Level: " + i + " Node: " + node.getID() + "\n");
                            sb.append("Successor: " + node.successor[i].getNodeZero().getID() + " BUT should be " + successor.getID() + "\n");
                            result = false;
                        }
                    }

                    if (!foundSuccessor1 && Node.prefixMatch(i, node, successor, 1)) {
                        successor1 = successor;
                        foundSuccessor1 = true;
                        if (node.successor[i].getNodeOne() != successor) {
                            sb.append("Successor 1 is wrong\n");
                            sb.append("Level: " + i + " Node: " + node.getID() + "\n");
                            if (node.successor[i].getNodeOne() != null) {
                                sb.append("Successor: " + node.successor[i].getNodeOne().getID() + " BUT should be " + successor.getID() + "\n");
                            } else {
                                sb.append("Successor: is null BUT should be " + successor.getID() + "\n");
                            }

                            result = false;
                        }
                    }
                }

                if (successor0 == null && successor1 != null) {
                    ranges[i].setEnd(successor1);
                } else if (successor0 != null && successor1 == null) {
                    ranges[i].setEnd(successor0);
                } else if (successor0 == null && successor1 == null) {
                    ranges[i].setEnd(successor0);
                } else if (successor0.isGreaterThan(successor1)) {
                    ranges[i].setEnd(successor0);
                } else if (successor1.isGreaterThan(successor0)) {
                    ranges[i].setEnd(successor1);
                }

                // Vorgänger bestimmen
                boolean foundPredecessor0 = false;
                boolean foundPredecessor1 = false;
                Node predecessor0 = null;
                Node predecessor1 = null;
                for (Node predecessor : predecessors) {
                    if (!foundPredecessor0 && Node.prefixMatch(i, node, predecessor, 0)) {
                        foundPredecessor0 = true;
                        if (node.predecessor[i].getNodeZero() != predecessor) {
                            sb.append("Predecessor 0 is wrong\n");
                            sb.append("Level: " + i + " Node: " + node.getID() + "\n");
                            if (node.predecessor[i].getNodeZero() != null) {
                                sb.append("Predecessor: " + node.predecessor[i].getNodeZero().getID() + " BUT should be " + predecessor.getID() + "\n");
                            } else {
                                sb.append("Predecessor: is null BUT should be " + predecessor.getID() + "\n");
                            }

                            result = false;
                        }

                        predecessor0 = predecessor;
                    }

                    if (!foundPredecessor1 && Node.prefixMatch(i, node, predecessor, 1)) {
                        foundPredecessor1 = true;
                        if (node.predecessor[i].getNodeOne() != predecessor) {
                            sb.append("Predecessor 1 is wrong\n");
                            result = false;
                        }

                        predecessor1 = predecessor;
                    }
                }

                if (predecessor0 == null && predecessor1 != null) {
                    ranges[i].setBegin(predecessor1);
                } else if (predecessor0 != null && predecessor1 == null) {
                    ranges[i].setBegin(predecessor0);
                } else if (predecessor0 == null && predecessor1 == null) {
                    ranges[i].setBegin(predecessor0);
                } else if (predecessor0.isLessThan(predecessor1)) {
                    ranges[i].setBegin(predecessor0);
                } else if (predecessor1.isLessThan(predecessor0)) {
                    ranges[i].setBegin(predecessor1);
                }

                // vergleiche Ranges
                if (node.range[i].getBegin() != ranges[i].getBegin()) {
                    sb.append("Range begin is wrong\n");
                    sb.append("Level: " + i + "\n");
                    sb.append("Node: " + node.getID() + " " + node.range[i] + " BUT should be " + ranges[i] + "\n");
                    result = false;
                }

                if (node.range[i].getEnd() != ranges[i].getEnd()) {
                    sb.append("Range end is wrong\n");
                    sb.append("Level: " + i + "\n");
                    sb.append("Node: " + node.getID() + " " + node.range[i] + " BUT should be " + ranges[i] + "\n");
                    result = false;
                }

                // vergleiche Nachbarschaften
                for (Node n : nodes) {
                    if (!n.equals(node) && node.range[i].isNodeInsideRange(n) && node.prefixMatch(i, n)) {
                        if (!node.neighbours[i].contains(n)) {
                            sb.append("Neighbourhood is wrong\n");
                            sb.append("Level: " + i + "\n");
                            sb.append("Node: " + node.getID() + "\n");
                            sb.append("Neighbourhoud\n");
//                            node.printNeighbourhood(i);
                            sb.append(" BUT should contain " + n.getID() + "\n");
                            result = false;
                        }
                    } else {
                        if (node.neighbours[i].contains(n)) {
                            sb.append("Neighbourhood is wrong\n");
                            sb.append("Level: " + i + "\n");
                            sb.append("Node: " + node.getID() + "\n");
                            sb.append("Neighbourhoud\n");
//                            node.printNeighbourhood(i);
                            sb.append(" BUT should NOT contain " + n.getID() + "\n");

                            result = false;
                        }
                    }
                }
            }

            // checke Zirkulären Knoten
            if (node.equals(firstNode)) {
                if (!node.zirkNode.equals(lastNode)) {
                    sb.append("\n Zirk Node in " + node.getID() + " is " + node.zirkNode.getID() + " BUT should be " + lastNode.getID());

                    result = false;
                }
            }

            if (node.equals(lastNode)) {
                if (node.equals(lastNode) && !node.zirkNode.equals(firstNode)){
                    sb.append("\n Zirk Node in " + node.getID() + " is " + node.zirkNode.getID() + " BUT should be " + firstNode.getID());

                    result = false;
                }
            }

            if (!node.equals(firstNode) && !node.equals(lastNode)) {
                if (node.zirkNode != null) {
                    sb.append("\n Zirk Node in " + node.getID() + " is " + node.zirkNode.getID() + " BUT should be NULL");

                    result = false;
                }
            }
        }

        if (!result) {
            this.printNeighbourHoodForAllLevels();
            System.out.println(sb.toString());
        }

        return result;
    }

    private static int randInt(int min, int max) {
        Random rand = new Random();

        return rand.nextInt((max-min) + 1) + min;
    }
}
