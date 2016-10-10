/**
 * Created by twiens on 24.08.16.
 */
public class Main {
    private static final int NUMBER_OF_BITS = 6;
    private static final int NUMBER_OF_NODES = 50;

    public static void main(String[] args) throws Exception {
        //buildSkip();
        //buildSkipAndLeave();

        //enqueueAndDequeueData();

        leaveAndExchangeData();

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

        while (QueueNode.storeCounter < 100)
            Thread.sleep(1000);
        nodes[3].enqueue("A");
        nodes[3].enqueue("B");
        nodes[3].enqueue("C");

        for (int i=0; i < 103; i++)
            nodes[3].dequeue();

        while (QueueNode.dequeuedElements != 103)
            Thread.sleep(1000);


        Thread.sleep(5000);

        System.out.println("Successful stored elements: " + QueueNode.storeCounter);
        System.out.println("Successful dequeued elements: " + QueueNode.dequeuedElements);

        for (int i = 0; i < NUMBER_OF_NODES; i++) {
            if (nodes[i] != null) {
                nodes[i].stopSubject();
            }
        }
    }

    private static void leaveAndExchangeData() throws InterruptedException {
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

        boolean result = graph.testSkipPlusGraph();


        graph.leave(nodes[7]);
        System.out.println("\n" + nodes[2].getID() + " leaving.");

        Thread.sleep(6000);

        result = graph.testSkipPlusGraph();

        graph.printNeighbourHoodForAllLevels();

        for (int i = 0; i < NUMBER_OF_NODES; i++) {
            if (nodes[i] != null) {
                nodes[i].stopSubject();
            }
        }
    }
}
