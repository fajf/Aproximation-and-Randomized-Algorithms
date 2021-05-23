import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.generate.GnpRandomGraphGenerator;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.util.SupplierUtil;

import java.util.*;
import java.util.function.Supplier;

public class TSP {

    private Graph<Integer, DefaultWeightedEdge> graph = null;


    /**
     * @return graph instance of this object
     */
    public Graph<Integer, DefaultWeightedEdge> getGraph() {
        return this.graph;
    }


    /**
     * @param numberNodes number of nodes in graph
     * @param probability probability with which edge between two nodes is created
     * @param seed random generators seed
     * @return DefaultUndirectedWeightedGraph instance generated from above parameters
     */
    public Graph<Integer, DefaultWeightedEdge> generateInstance (int numberNodes, double probability, int seed)
    {
        // Create the VertexFactory so the generator can create vertices
        Supplier<Integer> vSupplier = new Supplier<Integer>()
        {
            private int id = 0;

            @Override
            public Integer get()
            {
                return id++;
            }
        };

        // create the graph object
        this.graph =
                new DefaultUndirectedWeightedGraph<Integer, DefaultWeightedEdge>
                        (vSupplier, SupplierUtil.createDefaultWeightedEdgeSupplier());

        // create graph generator object
        GnpRandomGraphGenerator<Integer, DefaultWeightedEdge> gnpRandomGraphGenerator =
                new GnpRandomGraphGenerator<>(numberNodes, probability, seed);

        gnpRandomGraphGenerator.generateGraph(this.graph, null);

        this.ensureGraphConnectivity();

        this.setRandomWeightToAllEdges(seed);

        return this.graph;
    }

    /**
     * Ensures that there exists a path between any two vertices (nodes) in graph.
     * If necessary method adds as many new connections as needed - but it keeps the number of
     * new connections to a minimum.
     */
    private void ensureGraphConnectivity() {
        ConnectivityInspector connectivityInspector = new ConnectivityInspector(this.graph);
        List<Set> seznamMnozic = connectivityInspector.connectedSets();

        // connect sets through vertices in sets sequence
        ListIterator<Set> listIterator = seznamMnozic.listIterator();
        Set<Integer> firstSet = listIterator.next();
        while (listIterator.hasNext()){
            Set<Integer> secondSet = listIterator.next();

            // get first node from first set
            Integer firstSetNode = firstSet.iterator().next();
            // get first node from second set
            Integer secondSetNode = secondSet.iterator().next();

            // connect the two nodes
            this.graph.addEdge(firstSetNode, secondSetNode);

            // loop iteration over objects
            firstSet = secondSet;
        }
    }

    /**
     * Sets a random double value between 0 and 1 to weight of every edge in the graph.
     * Using Java.Random class object for generating random weights.
     *
     * @param seed random generator seed
     */
    private void setRandomWeightToAllEdges(long seed) {
        Random random = new Random(seed);

        for (DefaultWeightedEdge edge : this.graph.edgeSet())
        {
            this.graph.setEdgeWeight(edge, random.nextDouble());
        }
    }


    // TODO simple greedy algorithm
    public void greedyNearestNeighbour () {

    }

    // TODO 2-APX algorithm

    // TODO Christofides Algorithm
}
