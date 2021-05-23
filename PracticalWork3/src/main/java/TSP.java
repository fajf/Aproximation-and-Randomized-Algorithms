import org.jgrapht.Graph;
import org.jgrapht.generate.GnpRandomGraphGenerator;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.util.SupplierUtil;

import java.util.Random;
import java.util.Set;
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
     * Generates instance of SimpleWeightedGraph with given parameters.
     * Edges are not directed, no self-loops, no multiple edges between
     * same two nodes and every edge has a weight.
     *
     * @param numberNodes number of nodes in graph
     * @param probability probability with which edge between two nodes is created
     * @param seed random generators seed
     * @return SimpleWeightedGraph instance generated from above parameters
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
                new SimpleWeightedGraph<Integer, DefaultWeightedEdge>
                        (vSupplier, SupplierUtil.createDefaultWeightedEdgeSupplier());

        // create graph generator object
        GnpRandomGraphGenerator<Integer, DefaultWeightedEdge> gnpRandomGraphGenerator =
                new GnpRandomGraphGenerator<>(numberNodes, probability, seed);

        gnpRandomGraphGenerator.generateGraph(this.graph, null);

        if (this.graph == null) return null;

        this.connectGraph(seed);

        return this.graph;
    }

    /**
     * Obeying the triangular inequality, creates a completelly connected graph.
     * Using Java.Random class object for generating random weights / distances between points.
     *
     * @param seed random generator seed
     */
    private void connectGraph(long seed) {
        Random random = new Random(seed);

        Integer[] vozlisca = this.graph.vertexSet().toArray(new Integer[0]);

        // if graph has only one point, we are done
        if (vozlisca.length < 2) return;

        for (int k = 1; k < vozlisca.length; k++) {
            this.fullyConnectVertex(vozlisca, random, k);
        }
    }

    private void fullyConnectVertex(Integer[] vozlisca, Random random, int k) {
        // choose next point "k" from the array to be a newPoint and create a connection to the first point
        // assign random distance > 1
        int newPoint = vozlisca[k];
        int defaultUpperBound = (vozlisca.length * 10);

        // add a connection between newPoint and first point on the graph
        int weight = random.nextInt(defaultUpperBound);
        if (weight == 0) weight++; // +1 because we don't want distance to be 0
        this.addEdge(0, newPoint, weight);



        // generate new edges betweeen newPoint and every other
        for (int i = 1; i < k; i++) {
            // calculate upper bound for next edges that are going to be added to this vertex
            int upperBound = this.calculateUpperBound(weight, newPoint, i, defaultUpperBound);

            weight = random.nextInt(upperBound);
            if (weight == 0) weight++; // +1 because we don't want distance to be 0

            this.addEdge(i,k,weight);
        }
    }

    private int calculateUpperBound(int newEdgeWeight, int newPoint, int connectingPoint, int defaultUpperBound) {
        // calculate the sum of lengths: (j-1) times from new point A to point B and from B to all other connected
        // points, where j is the number of connections from B to all other point
        Set<DefaultWeightedEdge> outgoingEdges = this.graph.outgoingEdgesOf(0);
        int j = outgoingEdges.size();
        int sumOfLengths = (j - 1) * newEdgeWeight;
        for (DefaultWeightedEdge tmpEdge : outgoingEdges) {
            sumOfLengths += this.graph.getEdgeWeight(tmpEdge);
        }

        int connectedVertices = newPoint + 1;
        // above sum is the upper bound for generating random length from A to some point C != {A,B}
        // we subtract the number of connections left to make from A, because we want it to be
        // strong upper bound, because we don't want distance between any two points to be 0
        int upperBound = sumOfLengths - (connectedVertices - 1 - this.graph.outDegreeOf(newPoint));
        upperBound = Math.max(upperBound, defaultUpperBound);

        // triangular inequality
        int upperTriangle = newEdgeWeight +
                (int) this.graph.getEdgeWeight(this.graph.getEdge(0,connectingPoint));

        upperBound = Math.min(upperBound, upperTriangle);

        return upperBound;
    }

    private void addEdge(int firstPoint, int secondPoint, int weight) {
        DefaultWeightedEdge newEdge = this.graph.addEdge(firstPoint, secondPoint);
        this.graph.setEdgeWeight(newEdge, weight);
    }


    // TODO simple greedy algorithm
    public void greedyNearestNeighbour () {

    }

    // TODO 2-APX algorithm

    // TODO Christofides Algorithm
}
