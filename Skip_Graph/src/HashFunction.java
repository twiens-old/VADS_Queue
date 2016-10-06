import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by twiens on 19.09.2016.
 */
public class HashFunction {
    public static double hashPosition(int pos) {
        return ( Math.pow(pos, 2) % 100) / 100.0;
    }

    public static double hashQueueNode(BitSequence id) {
        // starting position = Integer(destination.getId()) * 1/(2^numberOfBits+1)

        return ((double) id.toInt()) / (Math.pow(2, id.length()));
    }
}
