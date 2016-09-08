/**
 * Created by twiens on 05.09.16.
 */
public class Range {
    private Node begin;
    private Node end;

    public Range(){
        this.begin = Node.minNode;
        this.end = Node.maxNode;
    }

    public Range(Node begin, Node end) {
        this.setBegin(begin);
        this.setEnd(end);
    }

    public Node getEnd() {
        return end;
    }

    public void setEnd(Node end) {
        this.end = end;
    }

    public Node getBegin() {
        return begin;
    }

    public void setBegin(Node begin) {
        this.begin = begin;
    }

    public boolean isNodeInsideRange(Node node) {
        return (node.equals(begin) || node.isGreaterThan(begin)) && (node.isLessThan(end) || node.equals(end));
    }

    @Override
    public String toString(){
        return "[" + this.begin.getID() + ", " + this.end.getID() + "]";
    }
}
