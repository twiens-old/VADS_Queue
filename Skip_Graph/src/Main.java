/**
 * Created by twiens on 24.08.16.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        SkipPlusGraph graph = new SkipPlusGraph(3);

        Node n0 = new Node("000");
        Node n1 = new Node("001");
        Node n2 = new Node("010");
        Node n3 = new Node("011");
        Node n4 = new Node("100");
        Node n5 = new Node("101");
        Node n6 = new Node("110");
        Node n7 = new Node("111");

        graph.join(n0);
        graph.join(n1);
        graph.join(n2);
        graph.join(n3);
        graph.join(n4);
        graph.join(n5);
        graph.join(n6);
        graph.join(n7);

        //region alter schwacher Graph
/*        //nodes[2].send(nodes[7]);
        //Thread.sleep(1000);
        //nodes[2].send(nodes[5]);
        nodes[0].send(nodes[1]);
        nodes[2].send(nodes[1]);
        //nodes[3].send(nodes[9]);
        nodes[4].send(nodes[2]);
        nodes[4].send(nodes[3]);
        nodes[5].send(nodes[0]);
        nodes[5].send(nodes[4]);        // TODO: funktioniert nicht, wenn diese Kante entfernt wird ... WARUM?!?!
        nodes[0].send(nodes[3]);        // TODO:
        nodes[1].send(nodes[4]);        // TODO:
        nodes[2].send(nodes[4]);        // TODO:
        nodes[4].send(nodes[6]);        // TODO:
        nodes[5].send(nodes[3]);        // TODO:
        nodes[0].send(nodes[4]);        // TODO:
        nodes[6].send(nodes[3]);        // TODO:
        nodes[7].send(nodes[3]);        // TODO:
        nodes[7].send(nodes[4]);        // TODO:
        nodes[7].send(nodes[5]);        // TODO:
        nodes[6].send(nodes[5]);
        nodes[6].send(nodes[7]);
        //nodes[6].send(nodes[8]);
        //nodes[9].send(nodes[8]);
        //nodes[7].send(nodes[8]); */
        //endregion

        // 3 sekunden laufen lassen
        Thread.sleep(3000);

        System.out.println("");
        graph.printNeighbourHoodForAllLevels();
        System.out.println("");
        System.out.println("################ Ultimativer Skip+-Graph Korrektheitstest ################");
        boolean result = graph.testSkipPlusGraph();
        System.out.println("################ Ergebnis = " + result + " ################");

        graph.leave(n0);
        graph.leave(n1);
        graph.leave(n2);
        graph.leave(n3);
        graph.leave(n4);
        graph.leave(n5);
        graph.leave(n6);
        graph.leave(n7);
    }
}
