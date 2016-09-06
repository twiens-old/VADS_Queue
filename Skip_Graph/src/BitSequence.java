import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by twiens on 06.09.16.
 */
public class BitSequence {
    private final int NUMBER_OF_BITS;
    private boolean[] bitSequence;

    public BitSequence(boolean[] sequence) {
        NUMBER_OF_BITS = sequence.length;
        bitSequence = sequence;
    }

    public BitSequence(String bitString) {
        NUMBER_OF_BITS = bitString.length();

        bitSequence = new boolean[NUMBER_OF_BITS];

        for (int i = 0; i < bitString.length(); i++) {              // "10010" => [1,0,0,1,0]
            if (bitString.charAt(i) == '1') {
                this.bitSequence[i] = true;
            } else if (bitString.charAt(i) == '0') {
                this.bitSequence[i] = false;
            } else {
                throw new IllegalArgumentException("bitString is invalid.");
            }
        }
    }

    public BitSequence getPrefix(int i) {
        boolean[] prefix = new boolean[i];

        for (int j = 0; j < i; j++) {
            prefix[j] = this.bitSequence[j];
        }

        return new BitSequence(prefix);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < this.bitSequence.length; i++) {
            if (this.bitSequence[i]) {
                sb.append("1");
            } else {
                sb.append("0");
            }
        }

        return sb.toString();
    }

    public BitSequence append(boolean b) {
        boolean[] appended = new boolean[this.bitSequence.length+1];

        for (int i = 0; i < this.bitSequence.length; i++) {
            appended[i] = this.bitSequence[i];
        }

        appended[this.bitSequence.length] = b;

        return new BitSequence(appended);
    }

    @Override
    public boolean equals(Object anotherObject) {
        if (!(anotherObject instanceof BitSequence) || this.bitSequence.length != ((BitSequence) anotherObject).bitSequence.length) {
            return false;
        }

        for (int i = 0; i < this.bitSequence.length; i++) {
            if (this.bitSequence[i] != ((BitSequence) anotherObject).bitSequence[i]) {
                return false;
            }
        }

        return true;
    }

    public boolean isGreaterThan(BitSequence anotherBitSequence){
        for (int i = 0; i < this.bitSequence.length; i++) {
            if (this.bitSequence[i] && !anotherBitSequence.bitSequence[i]) {
                return true;
            } else if (!this.bitSequence[i] && anotherBitSequence.bitSequence[i]) {
                return false;
            }
        }

        return false;   // Beide Sequenzen sind gleich
    }

    public boolean isLessThan(BitSequence anotherBitSequence){
        for (int i = 0; i < this.bitSequence.length; i++) {
            if (!this.bitSequence[i] && anotherBitSequence.bitSequence[i]) {
                return true;
            } else if (this.bitSequence[i] && !anotherBitSequence.bitSequence[i]) {
                return false;
            }
        }

        return false;   // Beide Sequenzen sind gleich
    }
}
