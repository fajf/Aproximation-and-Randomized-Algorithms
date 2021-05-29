import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.Random;

public class Assignment {
    static long instanceSeed = 6576784240994791645L;
    static long algorithmsSeed = 123;
    final static int numNodes = 400;
    final static int numOfRepetitions = 1000;
    final static boolean randomGraph = false;

    public static void main(String[] args) {
        // implement TSP instance generator that obeys triangular inequality
        // create completely connected graph that will be our map of cities to visit
        TSP tsp = new TSP();
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> solutionGreedy;
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> solutionApx;
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> solutionChristofides;
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> instance = null;
        int i = 0;

        do {
            if (randomGraph)
            {
                Random random = new Random();
                instanceSeed = random.nextLong();
            }

            if (randomGraph || i == 0)
                instance =
                        (SimpleWeightedGraph<Integer, DefaultWeightedEdge>)
                                tsp.generateInstance(numNodes, 0, instanceSeed, true);

            if (!randomGraph) algorithmsSeed++;

            // call greedy neighbour first algorithm
            solutionGreedy = tsp.greedyNearestNeighbour(algorithmsSeed);

            // reset graph instance, to be extra safe
            tsp.setGraph(instance);
            // call 2-apx algorithm
            solutionApx = tsp.apxAlgorithm(algorithmsSeed);

            // reset graph instance, to be extra safe
            tsp.setGraph(instance);
            // Christofides Algorithm
            solutionChristofides = tsp.christofidesAlgorithm(algorithmsSeed);
            if (solutionChristofides == null){
                System.out.println("Christofides is NULL");
                break;
            }

            System.out.println("Greedy: " + getDistance(solutionGreedy));
            System.out.println("2-Apx: " + getDistance(solutionApx));
            System.out.println("Christofides: " + getDistance(solutionChristofides));

            i++;
            System.out.println(i);
        } while (
                getDistance(solutionGreedy)*2 >= getDistance(solutionApx) &&
                getDistance(solutionGreedy)*1.5 >= getDistance(solutionChristofides) &&
                i < numOfRepetitions);

        //printGraph(instance);
        //printGraph(solutionGreedy);
        //printGraph(solutionApx);
        //printGraph(solutionChristofides);
        //System.out.println("Greedy: " + getDistance(solutionGreedy));
        //System.out.println("2-Apx: " + getDistance(solutionApx));
        //System.out.println("i: " + i);
        System.out.println("Instance seed: " + instanceSeed);

        // TODO analyze performance and runtime
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
        if (graph == null) return;
        System.out.println("-----------------");
        System.out.println(graph.vertexSet().toString());

        for (DefaultWeightedEdge edge : graph.edgeSet())
        {
            System.out.println(edge.toString() + " " + graph.getEdgeWeight(edge));
        }
        System.out.println("-----------------");
    }
}
