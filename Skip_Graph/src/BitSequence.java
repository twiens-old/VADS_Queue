/**
 * Created by twiens, jeromeK, fischerr on 06.09.16.
 */
public class BitSequence {
    private final int numberOfBits;
    private final boolean[] bitSequence;

    public BitSequence(final String bitString) {
        numberOfBits = bitString.length();

        bitSequence = new boolean[numberOfBits];

        // "10010" => [1,0,0,1,0]
        for (int i = 0; i < bitString.length(); i++) {
            if (bitString.charAt(i) == '1') {
                this.bitSequence[i] = true;
            } else if (bitString.charAt(i) == '0') {
                this.bitSequence[i] = false;
            } else {
                throw new IllegalArgumentException("bitString is invalid.");    // bitstring does not consist of 0 and 1
            }
        }
    }

    public BitSequence(final boolean[] sequence) {
        numberOfBits = sequence.length;
        bitSequence = sequence;
    }

    public boolean getBit(int i) {
        if (i <= 0 || i >= this.bitSequence.length) {
            throw new IllegalArgumentException("Index i is out of bounds.");
        }

        return this.bitSequence[i-1];
    }

    public BitSequence getPrefix(int prefixBits) {
        boolean[] prefix = new boolean[prefixBits];

        for (int j = 0; j < prefixBits; j++) {
            prefix[j] = this.bitSequence[j];
        }

        return new BitSequence(prefix);
    }

    public BitSequence append(boolean b) {
        boolean[] appendedSequence = new boolean[this.bitSequence.length+1];

        for (int i = 0; i < this.bitSequence.length; i++) {
            appendedSequence[i] = this.bitSequence[i];
        }

        appendedSequence[this.bitSequence.length] = b;

        return new BitSequence(appendedSequence);
    }

    // wir gehend davon aus, dass die längere BitSequence auch immer größer ist
    public boolean isGreaterThan(BitSequence anotherBitSequence){
        if (this.bitSequence.length < anotherBitSequence.bitSequence.length) {
            return false;
        }

        if (this.bitSequence.length > anotherBitSequence.bitSequence.length) {
            return true;
        }

        for (int i = 0; i < this.bitSequence.length; i++) {
            if (this.bitSequence[i] && !anotherBitSequence.bitSequence[i]) {
                return true;
            }

            if (!this.bitSequence[i] && anotherBitSequence.bitSequence[i]) {
                return false;
            }
        }

        return false;   // Beide Sequenzen sind gleich
    }

    public boolean isLessThan(BitSequence anotherBitSequence){
        if (this.bitSequence.length < anotherBitSequence.bitSequence.length) {
            return true;
        }

        if (this.bitSequence.length > anotherBitSequence.bitSequence.length) {
            return false;
        }

        for (int i = 0; i < this.bitSequence.length; i++) {
            if (!this.bitSequence[i] && anotherBitSequence.bitSequence[i]) {
                return true;
            }

            if (this.bitSequence[i] && !anotherBitSequence.bitSequence[i]) {
                return false;
            }
        }

        return false;   // Beide Sequenzen sind gleich
    }

    public int toInt() {
        int result = 0;

        for (int i = 0; i < this.bitSequence.length; i++) {
            if (this.bitSequence[i]) {
                result += Math.pow(2, numberOfBits-1-i);
            }
        }

        return result;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();

        if (this.bitSequence.length == 0) {
            sb.append("NaN");
        }

        for(int i = 0; i < this.bitSequence.length; i++) {
            if (this.bitSequence[i]) {
                sb.append("1");
            } else {
                sb.append("0");
            }
        }

        return sb.toString();
    }

    @Override
    public boolean equals(Object anotherObject) {
        if (!(anotherObject instanceof BitSequence)) {
            throw new IllegalArgumentException("Only two instances of BitSequence can be compared.");
        }

        BitSequence anotherBitSequence = (BitSequence) anotherObject;

        if (this.bitSequence.length != anotherBitSequence.bitSequence.length) {
            return false;
        }

        for (int i = 0; i < this.bitSequence.length; i++) {
            if (this.bitSequence[i] != anotherBitSequence.bitSequence[i]) {
                return false;
            }
        }

        return true;
    }
}
