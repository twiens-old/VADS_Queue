import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by twiens on 19.09.2016.
 */
public class HashFunction {
    public static float hashPosition(int pos) {
        return ((float) pos % 100) / 100;
    }

    public static double hashQueueNode(BitSequence id) {
        // starting position = Integer(destination.getId()) * 1/(2^numberOfBits+1)

        return id.toInt() / (Math.pow(2, id.length()));
    }
}
