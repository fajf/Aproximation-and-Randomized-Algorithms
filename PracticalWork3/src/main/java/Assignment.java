import com.opencsv.CSVWriter;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.io.File;
import java.io.FileWriter;
import java.util.Random;

public class Assignment {
    static long instanceSeed = -1;
    static long algorithmsSeed = 123;
    static int numNodes = 200;
    final static int numOfRepetitions = 1000;
    static boolean randomGraph = true;

    public static void main(String[] args) {
        // implement TSP instance generator that obeys triangular inequality
        // create completely connected graph that will be our map of cities to visit
        TSP tsp = new TSP();
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> solutionGreedy;
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> solutionApx;
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> solutionChristofides;
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> instance = null;

        // double check
        if (instanceSeed <= 0) randomGraph = true;

        int[] numNodesList = {10, 50, 100, 200, 400};

        for (int x = 0; x < numNodesList.length; x++) {
            int i = 0;
            numNodes = numNodesList[x];

            String fileName = String.valueOf(numNodes) + "-Vertices.csv";
            File file = new File(fileName);
            FileWriter outputfile = null;
            CSVWriter writer = null;
            try {
                outputfile = new FileWriter(file);
                writer = new CSVWriter(outputfile);
            } catch (Exception e) {
                e.printStackTrace();
            }

            do {
                if (randomGraph) {
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
                if (solutionChristofides == null) {
                    System.out.println("Christofides is NULL");
                    break;
                }

                writeToFile(writer, getDistance(solutionGreedy), getDistance(solutionApx), getDistance(solutionChristofides));

                i++;
                System.out.println(i);
            } while (
                    getDistance(solutionGreedy) * 2 >= getDistance(solutionApx) &&
                            getDistance(solutionGreedy) * 1.5 >= getDistance(solutionChristofides) &&
                            i < numOfRepetitions);

            try {
                writer.close();
                outputfile.close();
            } catch (Exception e) {e.printStackTrace();}
        }

        //printGraph(instance);
        //printGraph(solutionGreedy);
        //printGraph(solutionApx);
        //printGraph(solutionChristofides);
        //System.out.println("Greedy: " + getDistance(solutionGreedy));
        //System.out.println("2-Apx: " + getDistance(solutionApx));
        //System.out.println("i: " + i);
        //System.out.println("Instance seed: " + instanceSeed);
    }

    private static void writeToFile(CSVWriter writer, int distance, int distance1, int distance2)
    {
        String[] data = {String.valueOf(distance), String.valueOf(distance1), String.valueOf(distance2)};
        writer.writeNext(data);
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
