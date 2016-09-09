/**
 * Created by twiens on 24.08.16.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        int numberOfBits = 5;

        SkipPlusGraph graph = new SkipPlusGraph(numberOfBits);
        Node[] nodes = new Node[(int) Math.pow(2, numberOfBits)];

        for (int i = 0; i < 20; i++) {
            BitSequence sequence = UniqueRandomBitStringGenerator.generateUniqueRandomBitSequence(numberOfBits);

            Node node =  new Node(sequence.toString());
            nodes[i] = node;
        }

        System.out.println("Finished generating BitSequences.");

        for (int i = 0; i < 20; i = i+5) {
            nodes[i+0].send(nodes[i+1]);
            nodes[i+2].send(nodes[i+0]);
            nodes[i+3].send(nodes[i+2]);
            nodes[i+4].send(nodes[i+1]);

            if (i != 0) {
                nodes[i+0].send(nodes[i-1]);
            }
        }

        System.out.println("Finished initializing starting graph.");

        for (int i = 0; i < 20; i++) {
            graph.join(nodes[i]);
        }

        // 3 sekunden laufen lassen
        Thread.sleep(30000);

        for (int i = 0; i < 20; i++) {
            nodes[i].stopSubject();
        }

        System.out.println("");
        graph.printNeighbourHoodForAllLevels();
        System.out.println("");
        System.out.println("################ Ultimativer Skip+-Graph Korrektheitstest ################");
        boolean result = graph.testSkipPlusGraph();
        System.out.println("################ Ergebnis = " + result + " ################");
    }
}
