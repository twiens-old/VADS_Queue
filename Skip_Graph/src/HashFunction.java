import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by twiens on 19.09.2016.
 */
public class HashFunction {
    public static float hashPosition(int pos) {
        throw new NotImplementedException();
    }

    public static double hashQueueNode(QueueNode node) {
        // starting position = Integer(destination.getId()) * 1/(2^numberOfBits+1)

        return node.getID().toInt() / (Math.pow(2, node.getID().toString().length()));
    }
}
