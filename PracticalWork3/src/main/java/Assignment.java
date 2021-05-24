import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

public class Assignment {
    public static void main(String[] args) {
        // implement TSP instance generator that obeys triangular inequality
        // create completely connected graph that will be our map of cities to visit
        TSP tsp = new TSP();
        tsp.generateInstance(50,0,12774, true);
        printGraph(tsp.getGraph());

        // call different algorithms from class TSP
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> solution = tsp.greedyNearestNeighbour(54321);
        printGraph(solution);
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