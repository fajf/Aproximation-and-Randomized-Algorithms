import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

public class Assignment {
    final static long seed = 12774;
    final static int numNodes = 5;

    public static void main(String[] args) {
        // implement TSP instance generator that obeys triangular inequality
        // create completely connected graph that will be our map of cities to visit
        TSP tsp = new TSP();
        final SimpleWeightedGraph<Integer, DefaultWeightedEdge> instance =
                (SimpleWeightedGraph<Integer, DefaultWeightedEdge>)
                        tsp.generateInstance(numNodes,0,seed, true);
        printGraph(tsp.getGraph());

        // call different algorithms from class TSP
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> solutionGreedy = tsp.greedyNearestNeighbour(seed);
        printGraph(solutionGreedy);

        // reset graph instance, to be extra safe
        tsp.setGraph(instance);
        tsp.apxAlgorithm(seed);

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
