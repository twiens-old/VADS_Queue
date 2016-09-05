import java.util.*;

/**
 * Created by twiens on 24.08.16.
 */
public class UniqueRandomBitStringGenerator {

    private static ArrayList<BitSet> _uniqueBitStrings = new ArrayList<>();

    private static List<BitSet> _synchronizedList = Collections.synchronizedList(_uniqueBitStrings);

    private static final Random _random = new Random();

    public static BitSet GenerateUniqueRandomBitSet(int lengthOfBitString) {
        BitSet resultingBitSet = new BitSet();

        for (int i = 0; i < lengthOfBitString; i++) {
            if (_random.nextBoolean()) {
                    resultingBitSet.set(i);
            }
        }

        while (_synchronizedList.contains(resultingBitSet)) {
            resultingBitSet.clear();

            for (int i = 0; i < lengthOfBitString; i++) {
                if (_random.nextBoolean()) {
                    resultingBitSet.set(i);
                }
            }
        }

        _synchronizedList.add(resultingBitSet);

        return resultingBitSet;
    }
}
