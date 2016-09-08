/**
 * Created by twiens on 05.09.16.
 */
public class Pair {
    private Node nodeZero;
    private Node nodeOne;

    public Pair(){}

    public Pair(Node nodeZero, Node nodeOne) {
        this.setNodeZero(nodeZero);
        this.setNodeOne(nodeOne);
    }

    public void setNodeZero(Node nodeZero) {
        this.nodeZero = nodeZero;
    }

    public void setNodeOne(Node nodeOne) {
        this.nodeOne = nodeOne;
    }

    public Node getNodeZero() {
        return this.nodeZero;
    }

    public Node getNodeOne() {
        return this.nodeOne;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (this.nodeZero == null) {
            sb.append("(null, ");
        } else {
            sb.append("(" + this.nodeZero.getID() + ", ");
        }

        if (this.nodeOne == null) {
            sb.append("null)");
        } else {
            sb.append(this.nodeOne.getID() + ")");
        }

        return sb.toString();
    }
}
