import java.util.*;

/**
 * Created by twiens on 24.08.16.
 */
public class UniqueRandomBitStringGenerator {

    private static ArrayList<BitSequence> _uniqueBitStrings = new ArrayList<>();

    private static List<BitSequence> _synchronizedList = Collections.synchronizedList(_uniqueBitStrings);

    private static final Random _random = new Random();

    public static BitSequence GenerateUniqueRandomBitSet(int lengthOfBitString) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < lengthOfBitString; i++) {
            if (_random.nextBoolean()) {
                sb.append("1");
            } else {
                sb.append("0");
            }
        }

        BitSequence resultingBitSequence = new BitSequence(sb.toString());

        while (_synchronizedList.contains(resultingBitSequence)) {
            sb = new StringBuilder();

            for (int i = 0; i < lengthOfBitString; i++) {
                if (_random.nextBoolean()) {
                    sb.append("1");
                } else {
                    sb.append("0");
                }
            }
        }

        resultingBitSequence = new BitSequence(sb.toString());

        _synchronizedList.add(resultingBitSequence);

        return resultingBitSequence;
    }
}
