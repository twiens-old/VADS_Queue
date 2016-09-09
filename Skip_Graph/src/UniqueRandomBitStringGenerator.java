import java.util.*;

/**
 * Created by twiens on 24.08.16.
 */
public class UniqueRandomBitStringGenerator {

    private static ArrayList<BitSequence> uniqueBitStrings = new ArrayList<>();

    private static List<BitSequence> synchronizedList = Collections.synchronizedList(uniqueBitStrings);

    private static final Random _random = new Random();

    public static BitSequence generateUniqueRandomBitSequence(int lengthOfBitString) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < lengthOfBitString; i++) {
            if (_random.nextBoolean()) {
                sb.append("1");
            } else {
                sb.append("0");
            }
        }

        BitSequence resultingBitSequence = new BitSequence(sb.toString());

        while (synchronizedList.contains(resultingBitSequence)) {
            sb = new StringBuilder();

            for (int i = 0; i < lengthOfBitString; i++) {
                if (_random.nextBoolean()) {
                    sb.append("1");
                } else {
                    sb.append("0");
                }
            }

            resultingBitSequence = new BitSequence(sb.toString());
        }

        resultingBitSequence = new BitSequence(sb.toString());

        synchronizedList.add(resultingBitSequence);

        return resultingBitSequence;
    }
}
