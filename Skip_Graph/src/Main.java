/**
 * Created by twiens on 24.08.16.
 */
public class Main {
    public static void main(String[] args) throws Exception {

        int numberOfTests = 40;

        for (int i = 0; i < numberOfTests; i++) {
            boolean success = testLaufSkipGraph();

            if (!success) {
                break;
            }
        }
    }

    private static boolean testGetPositionRequest() throws Exception {
        UniqueRandomBitStringGenerator.ResetBitStrings();

        int numberOfNodes = 30;

        int numberOfBits = 6;

        SkipPlusGraph graph = new SkipPlusGraph(numberOfBits);
        QueueNode[] nodes = new QueueNode[(int) Math.pow(2, numberOfBits)];

        UniqueRandomBitStringGenerator.uniqueBitStrings.add(new BitSequence("000000"));
        for (int i = 1; i < numberOfNodes; i++) {
            BitSequence sequence = UniqueRandomBitStringGenerator.generateUniqueRandomBitSequence(numberOfBits);

            QueueNode node =  new QueueNode(sequence.toString());
            nodes[i] = node;
        }
        nodes[0] = new QueueNode("000000");

        System.out.println("Finished generating BitSequences.");

        for (int i = 0; i < numberOfNodes; i++) {
            graph.join(nodes[i]);
            Thread.sleep(500);
        }

        System.out.println("Finished initializing starting graph.");

        // 2 sekunden laufen lassen
        Thread.sleep(1000);

        System.out.println("");
        graph.printNeighbourHoodForAllLevels();
        System.out.println("");
        System.out.println("################ Ultimativer Skip+-Graph Korrektheitstest ################");
        boolean result = graph.testSkipPlusGraph();
        System.out.println("################ Ergebnis = " + result + " ################\n\n");

        if (!result) {
            throw new Exception("Skip+ Graph is wrong!");
        }

        nodes[4].enqueue(new DataMessage(null, null, "Test Data 1", 0, AbstractMessage.MessageType.ENQUEUE));
        nodes[0].enqueue(new DataMessage(null, null, "Test Data 2", 0, AbstractMessage.MessageType.ENQUEUE));
        nodes[9].enqueue(new DataMessage(null, null, "Test Data 3", 0, AbstractMessage.MessageType.ENQUEUE));
        nodes[4].enqueue(new DataMessage(null, null, "Test Data 4", 0, AbstractMessage.MessageType.ENQUEUE));
        nodes[8].enqueue(new DataMessage(null, null, "Test Data 5", 0, AbstractMessage.MessageType.ENQUEUE));
        nodes[8].enqueue(new DataMessage(null, null, "Test Data 6", 0, AbstractMessage.MessageType.ENQUEUE));
        nodes[8].enqueue(new DataMessage(null, null, "Test Data 7", 0, AbstractMessage.MessageType.ENQUEUE));
        nodes[8].enqueue(new DataMessage(null, null, "Test Data 8", 0, AbstractMessage.MessageType.ENQUEUE));
        nodes[8].enqueue(new DataMessage(null, null, "Test Data 9", 0, AbstractMessage.MessageType.ENQUEUE));
        nodes[8].enqueue(new DataMessage(null, null, "Test Data 10", 0, AbstractMessage.MessageType.ENQUEUE));

        Thread.sleep(10000);

        nodes[1].dequeue();
        nodes[2].dequeue();
        nodes[3].dequeue();
        nodes[4].dequeue();
        nodes[1].dequeue();
        nodes[2].dequeue();
        nodes[3].dequeue();
        nodes[4].dequeue();
        nodes[1].dequeue();
        nodes[2].dequeue();
        nodes[3].dequeue();
        nodes[4].dequeue();

        Thread.sleep(Integer.MAX_VALUE);

        for (int i = 0; i < numberOfNodes; i++) {
            if (nodes[i] != null) {
                nodes[i].stopSubject();
            }
        }

        return result;
    }

    private static boolean testLaufSkipGraph() throws InterruptedException{
        UniqueRandomBitStringGenerator.ResetBitStrings();

        int numberOfNodes = 30;

        int numberOfBits = 6;

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
            Thread.sleep(500);
        }

        // 2 sekunden laufen lassen
        Thread.sleep(1000);

        graph.leave(nodes[2]);
        System.out.println(nodes[2].getID() + " leaving.");
        Thread.sleep(1000);

        graph.leave(nodes[6]);
        System.out.println(nodes[6].getID() + " leaving.");
        Thread.sleep(1000);

        graph.leave(nodes[7]);
        System.out.println(nodes[7].getID() + " leaving.");
        Thread.sleep(1000);

        // 2 sekunden laufen lassen

        System.out.println("");
        graph.printNeighbourHoodForAllLevels();
        System.out.println("");
        System.out.println("################ Ultimativer Skip+-Graph Korrektheitstest ################");
        boolean result = graph.testSkipPlusGraph();
        System.out.println("################ Ergebnis = " + result + " ################\n\n");

        StringMessage[] messages = new StringMessage[10];
        for (int i = 10; i < 20; i++) {
            messages[i-10] = new StringMessage(null, nodes[i], "Start: " + nodes[i].getID() + " - Destination: " + nodes[i+1].getID() + "\n");

            nodes[i].send(messages[i-10]);
            Thread.sleep(500);
        }

        nodes[2].printNeighbourhood();
        nodes[6].printNeighbourhood();
        nodes[7].printNeighbourhood();

        for (int i = 0; i < 10; i++) {
            if (!messages[i].arrived) {
                System.out.println(messages[i].message.toString());
            } else {
                System.out.println(i + "arrived.");
            }
        }

        for (int i = 0; i < numberOfNodes; i++) {
            if (nodes[i] != null) {
                nodes[i].stopSubject();
            }
        }

        return result;
    }
}
