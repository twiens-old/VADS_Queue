/**
 * Created by twiens on 24.08.16.
 */
public class Main {
    private static final int NUMBER_OF_BITS = 3;
    private static final int NUMBER_OF_NODES = 8;

    public static void main(String[] args) throws Exception {

//        int numberOfTests = 40;

//        for (int i = 0; i < numberOfTests; i++) {
//            boolean success = testGetPositionRequest();

//            if (!success) {
//                break;
//            }
//        }

        enqueueAndDequeueData();
    }

    private static boolean buildSkip() throws InterruptedException{
        SkipPlusGraph graph = new SkipPlusGraph(NUMBER_OF_BITS);
        Node[] nodes = new Node[NUMBER_OF_NODES];

        for (int i = 0; i < NUMBER_OF_NODES; i++) {
            BitSequence sequence = UniqueRandomBitStringGenerator.generateUniqueRandomBitSequence(NUMBER_OF_BITS);

            Node node =  new Node(sequence.toString());
            nodes[i] = node;
        }

        for (int i = 0; i < NUMBER_OF_NODES; i++) {
            graph.join(nodes[i]);
            Thread.sleep(500);
        }

        boolean result = graph.testSkipPlusGraph();

        graph.printNeighbourHoodForAllLevels();

        for (int i = 0; i < NUMBER_OF_NODES; i++) {
            if (nodes[i] != null) {
                nodes[i].stopSubject();
            }
        }

        return result;
    }

    private static boolean buildSkipAndLeave() throws InterruptedException {
        SkipPlusGraph graph = new SkipPlusGraph(NUMBER_OF_BITS);
        Node[] nodes = new Node[NUMBER_OF_NODES];

        for (int i = 0; i < NUMBER_OF_NODES; i++) {
            BitSequence sequence = UniqueRandomBitStringGenerator.generateUniqueRandomBitSequence(NUMBER_OF_BITS);

            Node node =  new Node(sequence.toString());
            nodes[i] = node;
        }

        for (int i = 0; i < NUMBER_OF_NODES; i++) {
            graph.join(nodes[i]);
            Thread.sleep(500);
        }

        boolean result = graph.testSkipPlusGraph();

        graph.printNeighbourHoodForAllLevels();

        graph.leave(nodes[2]);
        System.out.println("\n" + nodes[2].getID() + " leaving.");

        Thread.sleep(5000);

        result = graph.testSkipPlusGraph();

        graph.printNeighbourHoodForAllLevels();

        for (int i = 0; i < NUMBER_OF_NODES; i++) {
            if (nodes[i] != null) {
                nodes[i].stopSubject();
            }
        }

        return result;
    }

    private static void enqueueAndDequeueData() throws InterruptedException {
        SkipPlusGraph graph = new SkipPlusGraph(NUMBER_OF_BITS);
        QueueNode[] nodes = new QueueNode[NUMBER_OF_NODES];

        for (int i = 0; i < NUMBER_OF_NODES; i++) {
            BitSequence sequence = UniqueRandomBitStringGenerator.generateUniqueRandomBitSequence(NUMBER_OF_BITS);

            QueueNode node =  new QueueNode(sequence.toString());
            nodes[i] = node;
        }

        for (int i = 0; i < NUMBER_OF_NODES; i++) {
            graph.join(nodes[i]);
            Thread.sleep(500);
        }

        Thread.sleep(10000);

        graph.testSkipPlusGraph();


        for (int i=0; i < 100; i++) {
            int rand = ((int)(Math.random() * 10)) % 8;
            nodes[rand].enqueue("Test Data " + i);
        }
        nodes[3].enqueue("A");
        nodes[3].enqueue("B");
        nodes[3].enqueue("C");

        Thread.sleep(5000);

        for (int i=0; i < 103; i++)
            nodes[3].dequeue();

        //nodes[1].dequeue();
        //nodes[2].dequeue();
        //nodes[3].dequeue();

        Thread.sleep(5000);

        System.out.println("Stored elements: " + QueueNode.storeCounter);

        for (int i = 0; i < NUMBER_OF_NODES; i++) {
            if (nodes[i] != null) {
                nodes[i].stopSubject();
            }
        }
    }

    private static boolean testLaufSkipGraph() throws InterruptedException{
        UniqueRandomBitStringGenerator.ResetBitStrings();

        int numberOfNodes = 8;

        int numberOfBits = 3;

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
        Thread.sleep(10000);

        // 2 sekunden laufen lassen

        System.out.println("");
        System.out.println("################ Ultimativer Skip+-Graph Korrektheitstest ################");
        boolean result = graph.testSkipPlusGraph();
        System.out.println("################ Ergebnis = " + result + " ################\n\n");

        StringMessage[] messages = new StringMessage[10];
        for (int i = 0; i < 6; i++) {
            messages[i] = new StringMessage(null, nodes[i], "Start: " + nodes[i].getID() + " - Destination: " + nodes[i+1].getID() + "\n");

            nodes[i].send(messages[i]);
            Thread.sleep(500);
        }

//        nodes[2].printNeighbourhood();
//        nodes[6].printNeighbourhood();
//        nodes[5].printNeighbourhood();

        for (int i = 0; i < 6; i++) {
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
