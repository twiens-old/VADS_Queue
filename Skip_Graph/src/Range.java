import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by twiens on 05.09.16.
 */
public class Range {
    private Node begin;
    private Node end;

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

    // TODO: still to implement
    public boolean isNodeInsideRange(Node node) {
        throw new NotImplementedException();
    }
}
