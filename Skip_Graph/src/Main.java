import java.util.BitSet;
import java.util.concurrent.ThreadFactory;

/**
 * Created by twiens on 24.08.16.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        //for (int i = 0; i < Math.pow(2, 2); i++) {
        //    Node tempNode = new Node();
        //}

        int n = 8;

        Node[] nodes = new Node[n];

        String[] nodesIds = new String[]{
            "000",
            "001",
            "010",
            "011",
            "100",
            "101",
            "110",
            "111",
        };

        for (int i = 0; i < n; i++) {
            nodes[i] = new Node(nodesIds[i]);
        }

        //nodes[2].send(nodes[7]);
        //Thread.sleep(1000);
        //nodes[2].send(nodes[5]);
        nodes[0].send(nodes[1]);
        nodes[2].send(nodes[1]);
        //nodes[3].send(nodes[9]);
        nodes[4].send(nodes[2]);
        nodes[4].send(nodes[3]);
        nodes[5].send(nodes[0]);
        nodes[6].send(nodes[5]);
        nodes[6].send(nodes[7]);
        //nodes[6].send(nodes[8]);
        //nodes[9].send(nodes[8]);
        //nodes[7].send(nodes[8]);

        for (int i = 0; i < n; i++) {
            nodes[i].start();
        }

        // 3 sekunden laufen lassen
        Thread.sleep(3000);

        for (int i = 0; i < n; i++) {
            nodes[i].stopSubject();
        }

        for (int i = 0; i < n; i++) {
            nodes[i].printNeighbourhood();
        }
    }
}
