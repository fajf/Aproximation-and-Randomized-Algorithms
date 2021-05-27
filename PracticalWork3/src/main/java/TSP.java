import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.generate.GnpRandomGraphGenerator;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;

import java.util.Iterator;
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

    public void setGraph(Graph<Integer, DefaultWeightedEdge> graph) {
        this.graph = graph;
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
    public Graph<Integer, DefaultWeightedEdge> generateInstance (int numberNodes, double probability, long seed, boolean connect)
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

        if (connect) this.completelyConnectGraph(seed);

        return this.graph;
    }

    /**
     * Solves Travelling Salesman Person problem using greedy nearest neighbour algorithm.
     *
     * @param seed seed for object of Java.Random class
     * @return graph containing constructed solution
     */
    public SimpleWeightedGraph<Integer, DefaultWeightedEdge> greedyNearestNeighbour (long seed) {
        // first create a copy of the graph, because we don't want to change the generated instance
        final SimpleWeightedGraph<Integer, DefaultWeightedEdge> map =
                (SimpleWeightedGraph<Integer, DefaultWeightedEdge>) copyAsSimpleWeightedGraph(this.graph);

        // create a new graph on which we will construct a solution
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> solution =
                (SimpleWeightedGraph<Integer, DefaultWeightedEdge>)
                        generateInstance(0,0,seed,false);

        greedyNearestNeighbourCore(map, solution, seed);

        return solution;
    }

    // TODO 2-APX algorithm
    public SimpleWeightedGraph<Integer, DefaultWeightedEdge> apxAlgorithm(long seed)
    {
        // first create a copy of the graph, because we don't want to change the generated instance
        final SimpleWeightedGraph<Integer, DefaultWeightedEdge> map =
                (SimpleWeightedGraph<Integer, DefaultWeightedEdge>) copyAsSimpleWeightedGraph(this.graph);

        // create a new graph on which we will construct a solution
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> solution =
                (SimpleWeightedGraph<Integer, DefaultWeightedEdge>)
                        generateInstance(0,0,seed,false);

        return apxCore(map, solution, seed);
    }

    private SimpleWeightedGraph<Integer, DefaultWeightedEdge> apxCore(
            SimpleWeightedGraph<Integer, DefaultWeightedEdge> map,
            SimpleWeightedGraph<Integer, DefaultWeightedEdge> solution, long seed)
    {
        // select starting point - which is also ending points
        Random random;
        if (seed == -1) random = new Random();
        else random = new Random(seed);
        int firstPoint = random.nextInt(map.vertexSet().size());

        // construct MST from starting point using Prim's Algorithm
        solution = createMinimumSpanningTree(map,solution,firstPoint);

        // List vertices visited in preorder walk of the constructed MST
        Iterator iterator = solution.outgoingEdgesOf(0).iterator();
        while (iterator.hasNext())
        {
            System.out.println(iterator.next().toString());
        }

        // add starting point at the end

        // return the solution

        return solution;
    }

    private SimpleWeightedGraph<Integer, DefaultWeightedEdge> createMinimumSpanningTree(
            SimpleWeightedGraph<Integer, DefaultWeightedEdge> map,
            SimpleWeightedGraph<Integer, DefaultWeightedEdge> solution, int firstPoint)
    {
        // add first point to the solution
        solution.addVertex(firstPoint);

        // until our MST does not include all vertices from graph instance (map)
        while (map.vertexSet().size() != solution.vertexSet().size()) {
            // for every node in solution get its shortest connection to a point not yet
            // in solution and keep the minimum of them all
            DefaultWeightedEdge shortest = new DefaultWeightedEdge();
            map.setEdgeWeight(shortest, Double.MAX_VALUE);
            for (Integer solutionPoint : solution.vertexSet()) {
                DefaultWeightedEdge tmp = getShortestConnection(map, solution, solutionPoint);
                if (map.getEdgeWeight(tmp) < map.getEdgeWeight(shortest)) shortest = tmp;
            }

            // add new vertex and edge to the solution
            firstPoint = map.getEdgeSource(shortest);
            int target = map.getEdgeTarget(shortest);
            // check if orientation is right
            if (!solution.containsVertex(firstPoint))
            {
                firstPoint = target;
                target = map.getEdgeSource(shortest);
            }
            addVertexAndEdge(solution, firstPoint, target, (int) map.getEdgeWeight(shortest));
        }

        return solution;
    }

    /**
     * Obeying the triangular inequality, creates a completely connected graph.
     * Using Java.Random class object for generating random weights / distances between points.
     *
     * @param seed random generator seed
     */
    private void completelyConnectGraph(long seed) {
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
        this.addEdge((SimpleWeightedGraph<Integer, DefaultWeightedEdge>) this.graph,0, newPoint, weight);

        // generate new edges betweeen newPoint and every other
        for (int i = 1; i < k; i++) {
            // calculate upper bound for next edges that are going to be added to this vertex
            int upperBound = this.calculateUpperBound(weight, newPoint, i, defaultUpperBound);

            weight = random.nextInt(upperBound);
            if (weight == 0) weight++; // +1 because we don't want distance to be 0

            this.addEdge((SimpleWeightedGraph<Integer, DefaultWeightedEdge>) this.graph,i,k,weight);
        }
    }

    /**
     * Calculates maximal distance from newPoint to connectingPoint in the way triangular
     * inequality holds for all triangles on graph.
     *
     * @param newEdgeWeight weight of edge between new point and first point (point zero)
     * @param newPoint id of point we are adding
     * @param connectingPoint id of point we are currently connecting new point to
     * @param defaultUpperBound default upper bound. Set -1, if you don't want to specify it
     * @return
     */
    private int calculateUpperBound(int newEdgeWeight, int newPoint, int connectingPoint, int defaultUpperBound) {
        int upperBound;

        if (defaultUpperBound == -1)
        {
            upperBound = Integer.MAX_VALUE;
        }
        else {
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
            upperBound = sumOfLengths - (connectedVertices - 1 - this.graph.outDegreeOf(newPoint));
            upperBound = Math.max(upperBound, defaultUpperBound);
        }
        // triangular inequality
        int upperTriangle = newEdgeWeight +
                (int) this.graph.getEdgeWeight(this.graph.getEdge(0,connectingPoint));

        // set to min so we hold to triangular inequality of current triangle and all other triangles
        upperBound = Math.min(upperBound, upperTriangle);

        return upperBound;
    }

    /**
     * Creates a connection from point "firstPoint" to point "secondPoint" with the weight "weight"
     *
     * @param firstPoint first point of connection
     * @param secondPoint second point of connection
     * @param weight weight of the edge between these two points
     */
    private void addEdge(SimpleWeightedGraph<Integer, DefaultWeightedEdge> graph, int firstPoint, int secondPoint, int weight) {
        DefaultWeightedEdge newEdge = graph.addEdge(firstPoint, secondPoint);
        graph.setEdgeWeight(newEdge, weight);
    }

    private void addVertexAndEdge(SimpleWeightedGraph<Integer, DefaultWeightedEdge> graph,
                                  int firstPoint, int secondPoint, int weight) {
        graph.addVertex(secondPoint);
        addEdge(graph, firstPoint, secondPoint, weight);
    }

    /**
     * Creates a hard copy of a graph for internal use
     *
     * @param graph the graph to copy
     * @return Hard copy of the graph projected to a SimpleGraph
     */
    private static <V, E> Graph<V, E> copyAsSimpleWeightedGraph(Graph<V, E> graph)
    {
        Graph<V,
                E> copy = GraphTypeBuilder
                .<V, E> undirected().weighted(true).edgeSupplier(graph.getEdgeSupplier())
                .vertexSupplier(graph.getVertexSupplier()).allowingMultipleEdges(false)
                .allowingSelfLoops(false).buildGraph();
        if (graph.getType().isSimple())
        {
            Graphs.addGraph(copy, graph);
        }
        else
        {
            // project graph to SimpleWeightedGraph
            Graphs.addAllVertices(copy, graph.vertexSet());
            for (E e : graph.edgeSet()) {
                V v1 = graph.getEdgeSource(e);
                V v2 = graph.getEdgeTarget(e);
                if (!v1.equals(v2) && !copy.containsEdge(e)) {
                    copy.addEdge(v1, v2);
                }
            }
        }
        return copy;
    }

    private void greedyNearestNeighbourCore(SimpleWeightedGraph<Integer,
            DefaultWeightedEdge> map, SimpleWeightedGraph<Integer, DefaultWeightedEdge> solution, long seed)
    {
        // choose random starting point
        Random random = new Random(seed);
        int firstPoint = random.nextInt(map.vertexSet().size());

        // add first point to solution
        solution.addVertex(firstPoint);

        // visit closest neighbour until solution graph does not contain all vertices from given graph
        while (map.vertexSet().size() != solution.vertexSet().size())
        {
            // get closest point to the firstPoint
            DefaultWeightedEdge edge = getShortestConnection(map, solution, firstPoint);
            int secondPoint =
                    firstPoint == map.getEdgeSource(edge) ?
                            map.getEdgeTarget(edge) : map.getEdgeSource(edge);

            // add secondPoint and edge to solution
            addVertexAndEdge(solution,firstPoint,secondPoint,(int) this.graph.getEdgeWeight(edge));

            // closest point is new start
            firstPoint = secondPoint;
        }
        // add connection from last to first point
        addLastToFirst(solution,map,firstPoint);
    }

    private void addLastToFirst(SimpleWeightedGraph<Integer, DefaultWeightedEdge> solution,
                                SimpleWeightedGraph<Integer, DefaultWeightedEdge> map, int firstPoint)
    {
        int secondPoint = solution.vertexSet().iterator().next();
        Set<DefaultWeightedEdge> conn = map.edgesOf(firstPoint);
        DefaultWeightedEdge edge = null;
        for (DefaultWeightedEdge tmp : conn)
        {
            if (secondPoint == map.getEdgeSource(tmp) || secondPoint == map.getEdgeTarget(tmp))
            {
                edge = tmp;
                break;
            }
        }
        addVertexAndEdge(solution,firstPoint,secondPoint,(int) this.graph.getEdgeWeight(edge));
    }

    private DefaultWeightedEdge getShortestConnection(
            SimpleWeightedGraph<Integer, DefaultWeightedEdge> map,
            SimpleWeightedGraph<Integer, DefaultWeightedEdge> solution, int firstPoint) {

        // gets all connections from firstPoint
        Set<DefaultWeightedEdge> allConn = map.edgesOf(firstPoint);

        // sets new edge to have max. possible weight
        DefaultWeightedEdge shortest = new DefaultWeightedEdge();
        map.setEdgeWeight(shortest, Double.MAX_VALUE);
        int targetVertex = -1;
        for (DefaultWeightedEdge edge : allConn)
        {
            // because we work with undirected connections, manually check which one is target
            // and set it as target
            targetVertex =
                    firstPoint == map.getEdgeSource(edge) ?
                            map.getEdgeTarget(edge) : map.getEdgeSource(edge);

            // check if current connection is shorter and
            // if solution does not yet contain target point
            if (map.getEdgeWeight(edge) < map.getEdgeWeight(shortest) &&
                !solution.containsVertex(targetVertex))
            {
                shortest = edge;
            }
        }

        return shortest;
    }

    // TODO Christofides Algorithm
}