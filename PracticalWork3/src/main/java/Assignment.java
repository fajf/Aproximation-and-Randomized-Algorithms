import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

public class Assignment {
    public static void main(String[] args) {
        // implement TSP instance generator that obeys triangular inequality
        TSP tsp = new TSP();
        tsp.generateInstance(10,0.2,12345);

        printGraph(tsp.getGraph());

        // call different algorithms from class TSP
        // TODO
        // analyze their performance and runtime
        // TODO
    }

    public static void printGraph (Graph<Integer, DefaultWeightedEdge> graph)
    {
        System.out.println(graph.vertexSet().toString());

        for (DefaultWeightedEdge edge : graph.edgeSet())
        {
            System.out.println(edge.toString() + " " + graph.getEdgeWeight(edge));
        }
    }
}
