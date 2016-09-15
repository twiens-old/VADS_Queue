/**
 * Created by twiens on 24.08.16.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {

        int numberOfTests = 1;

        for (int i = 0; i < numberOfTests; i++) {
            boolean success = testLauf();

            if (!success) {
                break;
            }
        }

    }

    private static boolean testLauf() throws InterruptedException{
        UniqueRandomBitStringGenerator.ResetBitStrings();

        int numberOfNodes = 21;

        int numberOfBits = 5;

        SkipPlusGraph graph = new SkipPlusGraph(numberOfBits);
        Node[] nodes = new Node[(int) Math.pow(2, numberOfBits)];

        for (int i = 0; i < numberOfNodes; i++) {
            BitSequence sequence = UniqueRandomBitStringGenerator.generateUniqueRandomBitSequence(numberOfBits);

            Node node =  new Node(sequence.toString());
            nodes[i] = node;
        }

        System.out.println("Finished generating BitSequences.");

//        for (int i = 0; i < 20; i = i+5) {
//            nodes[i+0].send(nodes[i+1]);
//            nodes[i+2].send(nodes[i+0]);
//            nodes[i+3].send(nodes[i+2]);
//            nodes[i+4].send(nodes[i+1]);

//            if (i != 0) {
//                nodes[i+0].send(nodes[i-1]);
//            }
//        }

        System.out.println("Finished initializing starting graph.");

        for (int i = 0; i < numberOfNodes; i++) {
            graph.join(nodes[i]);
            Thread.sleep(1000);
        }

        // 2 sekunden laufen lassen
        Thread.sleep(10000);

//        graph.leave(nodes[2]);
//        Thread.sleep(1000);
//        graph.leave(nodes[6]);
//        Thread.sleep(1000);
//        graph.leave(nodes[7]);

        // 2 sekunden laufen lassen
        Thread.sleep(2000);

        System.out.println("");
        graph.printNeighbourHoodForAllLevels();
        System.out.println("");
        System.out.println("################ Ultimativer Skip+-Graph Korrektheitstest ################");
        boolean result = graph.testSkipPlusGraph();
        System.out.println("################ Ergebnis = " + result + " ################\n\n");

        Message[] messages = new Message[10];
        for (int i = 10; i < 20; i++) {
            messages[i-10] = new Message(nodes[i+1], Message.MessageType.ROUTING, new StringBuilder("Start: " + nodes[i].getID() + " - Destination: " + nodes[i+1].getID() + "\n"));

            nodes[i].send(messages[i-10]);
            Thread.sleep(500);
        }

        Thread.sleep(5000);

        for (int i = 0; i < 10; i++) {
            System.out.println(messages[i].message.toString());
        }

        for (int i = 0; i < numberOfNodes; i++) {
            if (nodes[i] != null) {
                nodes[i].stopSubject();
            }
        }

        return result;
    }
}
