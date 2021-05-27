import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.Random;

public class Assignment {
    static long instanceSeed = 6485888164064736330L;
    static long algorithmsSeed = 123;
    final static int numNodes = 5;

    public static void main(String[] args) {
        // implement TSP instance generator that obeys triangular inequality
        // create completely connected graph that will be our map of cities to visit
        TSP tsp = new TSP();
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> solutionGreedy;
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> solutionApx;
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> instance;

        do {
            if (instanceSeed == -1 || String.valueOf(instanceSeed).length() < 5)
            {
                Random random = new Random();
                instanceSeed = random.nextLong();
            }

            instance =
                    (SimpleWeightedGraph<Integer, DefaultWeightedEdge>)
                            tsp.generateInstance(numNodes, 0, instanceSeed, true);

            // call different algorithms from class TSP
            solutionGreedy = tsp.greedyNearestNeighbour(algorithmsSeed);


            // reset graph instance, to be extra safe
            tsp.setGraph(instance);
            solutionApx = tsp.apxAlgorithm(algorithmsSeed);
        } while (getDistance(solutionGreedy) >= getDistance(solutionApx));

        printGraph(instance);
        printGraph(solutionGreedy);
        printGraph(solutionApx);
        printDistance(solutionGreedy);
        printDistance(solutionApx);
        System.out.println("Instance seed: " + instanceSeed);

        // TODO
        // analyze their performance and runtime
        // TODO
    }

    private static int getDistance(SimpleWeightedGraph<Integer, DefaultWeightedEdge> graph) {
        double distance = 0;
        for (DefaultWeightedEdge edge : graph.edgeSet())
        {
            distance += graph.getEdgeWeight(edge);
        }
        return (int)distance;
    }

    private static void printDistance(SimpleWeightedGraph<Integer, DefaultWeightedEdge> graph) {
        double distance = 0;
        for (DefaultWeightedEdge edge : graph.edgeSet())
        {
            distance += graph.getEdgeWeight(edge);
        }
        System.out.println(distance);
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
