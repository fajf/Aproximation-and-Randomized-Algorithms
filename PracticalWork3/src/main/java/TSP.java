import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.cycle.HierholzerEulerianCycle;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.alg.interfaces.SpanningTreeAlgorithm;
import org.jgrapht.alg.matching.blossom.v5.KolmogorovWeightedPerfectMatching;
import org.jgrapht.alg.spanning.KruskalMinimumSpanningTree;
import org.jgrapht.generate.GnpRandomGraphGenerator;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.WeightedMultigraph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
        GnpRandomGraphGenerator<Integer, DefaultWeightedEdge> gnpRandomGraphGenerator = null;
        if (seed != -1) gnpRandomGraphGenerator =
                new GnpRandomGraphGenerator<>(numberNodes, probability, seed);
        else gnpRandomGraphGenerator =
                new GnpRandomGraphGenerator<>(numberNodes, probability);

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

    // 2-APX algorithm
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
        solution = preorderWalk(map, solution, firstPoint);

        // add starting point at the end
        addEdge(solution,
                (int) solution.vertexSet().toArray()[solution.vertexSet().size()-1],
                firstPoint,
                (int) map.getEdgeWeight(
                        map.getEdge(
                                (int) solution.vertexSet().toArray()[solution.vertexSet().size()-1],
                                firstPoint)
                )
        );

        // return the solution
        return solution;
    }

    // Christofides Algorithm
    public SimpleWeightedGraph<Integer, DefaultWeightedEdge> christofidesAlgorithm(long seed)
    {
        // first create a hard copy of the graph, because we don't want to change the generated instance
        final SimpleWeightedGraph<Integer, DefaultWeightedEdge> map =
                (SimpleWeightedGraph<Integer, DefaultWeightedEdge>) copyAsSimpleWeightedGraph(this.graph);

        // 1. Compute a minimum spanning tree in the input graph
        SpanningTreeAlgorithm.SpanningTree kruskalMSTSpanningTree = new KruskalMinimumSpanningTree(map).getSpanningTree();
        final SimpleWeightedGraph<Integer, DefaultWeightedEdge> mst = createGraphFromSpanningTree(
                map,
                kruskalMSTSpanningTree
        );

        // 2. Find vertices with odd degree in the MST.
        final Set<Integer> oddVertices = getOddVertices(mst);

        // 3. Create induced subgraph of "map" given by the oddVertices
        final SimpleWeightedGraph<Integer, DefaultWeightedEdge> inducedSubgraph = createInducedSubgraph(map, oddVertices);

        // 4. Compute minimum weight perfect matching in the induced subgraph on odd degree vertices.
        final MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> perfectMatching = createMinimumWeightPerfectMatching(inducedSubgraph);

        // 5. Add edges from the minimum weight perfect matching to the MST (forming a pseudograph).
        final WeightedMultigraph<Integer,DefaultWeightedEdge> mstAndMatching = combineMatchingWithMST(mst, perfectMatching);

        // 6. Compute an Eulerian cycle in the obtained pseudograph
        GraphPath<Integer,DefaultWeightedEdge> eulerCycle = getEulerianTour(mstAndMatching);
        if (eulerCycle == null) {
            System.out.println("MST: " + mst.toString());
            System.out.println("Odd vertices: " + oddVertices.toString());
            System.out.println("induced subgraph: " + inducedSubgraph.toString());
            System.out.println("perfect matching: " + perfectMatching.toString());
            System.out.println("first edge from matching weight: " + map.getEdgeWeight((DefaultWeightedEdge) perfectMatching.getEdges().toArray()[0]));
            System.out.println("pseudograph: " + mstAndMatching.toString());
            return null;
        }

        // 7. Make the circuit found in previous step into a Hamiltonian circuit by skipping repeated vertices (shortcutting).
        List<Integer> hamiltonCycle = eulerCycle.getVertexList().stream().distinct().collect(Collectors.toCollection(LinkedList::new));

        // 8. create a graph from hamilton cycle
        return createGraphFromList(map, hamiltonCycle);
    }

    private SimpleWeightedGraph<Integer, DefaultWeightedEdge> createGraphFromList(
            Graph<Integer, DefaultWeightedEdge> map,
            List<Integer> hamiltonCycle)
    {
        SimpleWeightedGraph<Integer,DefaultWeightedEdge> solution = (SimpleWeightedGraph<Integer, DefaultWeightedEdge>)
                generateInstance(0,0,1,false);

        Iterator<Integer> iterator = hamiltonCycle.listIterator();

        int startPoint = iterator.next();
        int first = startPoint;
        while (iterator.hasNext())
        {
            int second = iterator.next();
            addVerticesAndEdge(solution, first, second, (int)map.getEdgeWeight(map.getEdge(first,second)));

            first = second;
        }
        // add connection from last point to starting point
        addEdge(solution,first,startPoint,(int)map.getEdgeWeight(map.getEdge(first,startPoint)));

        return solution;
    }

    private SimpleWeightedGraph<Integer, DefaultWeightedEdge> createGraphFromSpanningTree(
            SimpleWeightedGraph<Integer, DefaultWeightedEdge> map,
            SpanningTreeAlgorithm.SpanningTree primMSTSpanningTree)
    {
        // create a copy of complete graph
        SimpleWeightedGraph<Integer,DefaultWeightedEdge> solution =
                (SimpleWeightedGraph<Integer, DefaultWeightedEdge>) copyAsSimpleWeightedGraph(map);

        // get all edges in MST
        Set<DefaultWeightedEdge> edgeSet = primMSTSpanningTree.getEdges();

        // for every edge on complete graph
        for (DefaultWeightedEdge edge : map.edgeSet())
        {
            // check if it also exists in MST
            if (!edgeSet.contains(edge))
            {
                // if not, remove it from solution
                solution.removeEdge(edge);
            }
        }
        return solution;
    }

    private Set<Integer> getOddVertices(SimpleWeightedGraph<Integer, DefaultWeightedEdge> mst) {
        Set<Integer> vertices = new HashSet<>();
        for (Integer vertex : mst.vertexSet())
        {
            if (mst.degreeOf(vertex) % 2 == 1)
            {
                vertices.add(vertex);
            }
        }
        return vertices;
    }

    private GraphPath<Integer, DefaultWeightedEdge> getEulerianTour(WeightedMultigraph<Integer, DefaultWeightedEdge> multigraph)
    {
        // a tour = cycle that visits every edge exactly once (allows revisiting vertices) and starts and ends
        // at the same vertex
        HierholzerEulerianCycle<Integer,DefaultWeightedEdge> eulerianCycleAlgorithm = new HierholzerEulerianCycle<>();
        GraphPath<Integer,DefaultWeightedEdge> cycle = null;

        if (eulerianCycleAlgorithm.isEulerian(multigraph))
            cycle = eulerianCycleAlgorithm.getEulerianCycle(multigraph);

        return cycle;
    }

    private WeightedMultigraph<Integer, DefaultWeightedEdge> combineMatchingWithMST(
            SimpleWeightedGraph<Integer, DefaultWeightedEdge> mst,
            MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> perfectMatching)
    {
        // first create a new graph which will contain a solution
        WeightedMultigraph<Integer,DefaultWeightedEdge> mstAndMatching =
                new WeightedMultigraph<Integer,DefaultWeightedEdge>(null,DefaultWeightedEdge::new);

        // copy all vertices from MST to mstAndMatching
        mst.vertexSet().forEach(v -> mstAndMatching.addVertex(v));
        // copy all edges from MST to mstAndMatching
        mst.edgeSet().forEach(e -> mstAndMatching.addEdge(mst.getEdgeSource(e), mst.getEdgeTarget(e), e));

        // add edges from perfect matching to pseudograph
        for (DefaultWeightedEdge edge : perfectMatching.getEdges())
        {
            addEdge(mstAndMatching,
                    mst.getEdgeSource(edge),
                    mst.getEdgeTarget(edge),
                    (int) mst.getEdgeWeight(edge));
        }

        return mstAndMatching;
    }

    private MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> createMinimumWeightPerfectMatching(
            SimpleWeightedGraph<Integer, DefaultWeightedEdge> subgraphFromO)
    {
        KolmogorovWeightedPerfectMatching<Integer, DefaultWeightedEdge> matchingObject = new KolmogorovWeightedPerfectMatching(subgraphFromO);
        MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching = matchingObject.getMatching();

        return matching;
    }

    private SimpleWeightedGraph<Integer, DefaultWeightedEdge> createInducedSubgraph(
            SimpleWeightedGraph<Integer, DefaultWeightedEdge> map,
            Set<Integer> oddVertices)
    {
        // create a hard copy of the graph on which we will construct a solution
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> subgraph =
                (SimpleWeightedGraph<Integer, DefaultWeightedEdge>) copyAsSimpleWeightedGraph(map);

        // check every vertex on "map" and
        // keep only those in oddVertices
        for (Integer vertex : map.vertexSet())
        {
            // is the vertex NOT in the Set?
            if (!oddVertices.contains(vertex))
            {
                // remove it if the above is correct
                subgraph.removeVertex(vertex);
            }
        }

        return subgraph;
    }

    private SimpleWeightedGraph<Integer, DefaultWeightedEdge> preorderWalk(
            SimpleWeightedGraph<Integer, DefaultWeightedEdge> map,
            SimpleWeightedGraph<Integer, DefaultWeightedEdge> solution, int firstPoint)
    {
        // new graph to which we will save the preorder walk
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> walk =
                (SimpleWeightedGraph<Integer, DefaultWeightedEdge>)
                        generateInstance(0,0,0,false);

        // add starting point to the walk
        walk.addVertex(firstPoint);

        // add all its edges
        doStep(map, solution, walk, firstPoint);

        return walk;
    }

    private void doStep(SimpleWeightedGraph<Integer, DefaultWeightedEdge> map,
                        SimpleWeightedGraph<Integer, DefaultWeightedEdge> msp,
                        SimpleWeightedGraph<Integer, DefaultWeightedEdge> walk, int sourcePoint) {
        // get all connections from MSP for source point
        Iterator<DefaultWeightedEdge> iterator = msp.edgesOf(sourcePoint).iterator();
        // add every connection in the sequence (from left to right)
        while (iterator.hasNext())
        {
            DefaultWeightedEdge edge = iterator.next();
            // but only if source point is actually the source
            if (msp.getEdgeSource(edge) == sourcePoint)
            {
                // add edge to the walk
                // starting point of edge should be previously added point
                int startingPoint = (int) walk.vertexSet().toArray()[walk.vertexSet().size()-1];
                int target = msp.getEdgeTarget(edge);
                int weight = (int) map.getEdgeWeight(map.getEdge(startingPoint,target));
                addVerticesAndEdge(walk, startingPoint, target, weight);
                // make a recursive call - depth first traversal
                doStep(map, msp, walk, target);
            }
        }
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
            addVerticesAndEdge(solution, firstPoint, target, (int) map.getEdgeWeight(shortest));
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
        Random random;
        if (seed != -1) random = new Random(seed);
        else random = new Random();

        Integer[] vozlisca = this.graph.vertexSet().toArray(new Integer[0]);

        // if graph has only one point, we are done
        if (vozlisca.length < 2) return;

        for (int k = 1; k < vozlisca.length; k++) {
            this.fullyConnectVertex(vozlisca, random, k);
        }
    }

    private void fullyConnectVertex(Integer[] vozlisca, Random random, int k) {
        // connect point "k" to the graph
        // assign random distance > 1
        int defaultUpperBound = (vozlisca.length * vozlisca.length);

        // add a connection between k and first point on the graph
        int weight = random.nextInt(defaultUpperBound);
        if (weight == 0) weight++; // +1 because we don't want distance to be 0
        addEdge((SimpleWeightedGraph<Integer, DefaultWeightedEdge>) this.graph,0, k, weight);

        // generate new edges betweeen k and every other point already on the graph
        for (int i = 1; i < k; i++) {
            // calculate upper & lower bound for next edge that is going to be added to this vertex
            int upperBound = calculateUpperBound(this.graph, k, i);
            int lowerBound = calculateLowerBound(this.graph, k, i);

            // get random value between lower and upper bounds
            int diff = upperBound - lowerBound;
            if (diff == 0)
                weight = lowerBound;
            else {
                if (diff < 1) {
                    System.out.println(this.graph.vertexSet().toString());
                    for (DefaultWeightedEdge edge : this.graph.edgeSet())
                    {
                        System.out.println(edge.toString() + " : " + this.graph.getEdgeWeight(edge));
                    }
                    System.out.println("Edge: " + k + "--" + i);
                    System.out.println("Upper bound: " + upperBound);
                    System.out.println("Lower bound: " + lowerBound);
                }
                weight = random.nextInt(diff);
                weight += lowerBound;
            }

            this.addEdge((SimpleWeightedGraph<Integer, DefaultWeightedEdge>) this.graph, i, k, weight);
        }
    }

    /**
     * Calculates maximal distance from newPoint to connectingPoint in the way triangular
     * inequality holds for all triangles on graph.
     *
     * @param graph
     * @param x id of the point we are trying to add on a graph
     * @param z id of point we are currently trying to connect to
     * @return
     */
    private int calculateUpperBound(Graph<Integer, DefaultWeightedEdge> graph, int x, int z) {
        int upperBound = Integer.MAX_VALUE;

        // for each point (z) we are connecting to, check upper bound considering triangle inequality from us (x) to
        // every other point we are already connected to (y) and than from that point (y) to our new target (z)

        // na vse to??ke na katere sem ??e povezan
        for (int y = 0; y < graph.outDegreeOf(x); y++)
        {
            int prviDel = (int) graph.getEdgeWeight(graph.getEdge(x,y));
            // in potem iz tistih to??k do to??ke, na katero se povezujem
            int drugiDel = (int) graph.getEdgeWeight(graph.getEdge(y,z));

            int val = prviDel + drugiDel;

            if (val < upperBound) upperBound = val;
        }

        return upperBound;
    }

    /**
     * Calculates minimum distance from newPoint to connectingPoint in the way triangular
     * inequality holds for all triangles on graph.
     *
     * @param graph
     * @param x id of the point we are trying to add on a graph
     * @param z id of point we are currently trying to connect to
     * @return
     */
    private int calculateLowerBound(Graph<Integer, DefaultWeightedEdge> graph, int x, int z) {
        // set to 1, because we don't want point on graph to be on the same positions
        int lowerBound = 1;

        // for each point (z) we are connecting to, check lower bound considering triangle inequality from us (x) to
        // every other point we are already connected to (y) and than from that point (y) to our new target (z)

        // na vse to??ke na katere sem ??e povezan
        for (int y = 0; y < graph.outDegreeOf(x); y++)
        {
            int prviDel = (int) graph.getEdgeWeight(graph.getEdge(x,y));
            // in potem iz tistih to??k do to??ke, na katero se povezujem
            int drugiDel = (int) graph.getEdgeWeight(graph.getEdge(y,z));

            int val = Math.abs(prviDel-drugiDel);

            if (val > lowerBound) lowerBound = val;
        }

        return lowerBound;
    }

    /**
     * Creates a connection from point "firstPoint" to point "secondPoint" with the weight "weight"
     *
     * @param firstPoint first point of connection
     * @param secondPoint second point of connection
     * @param weight weight of the edge between these two points
     */
    private void addEdge(Graph<Integer, DefaultWeightedEdge> graph, int firstPoint, int secondPoint, int weight) {
        DefaultWeightedEdge newEdge = graph.addEdge(firstPoint, secondPoint);

        if (newEdge != null) graph.setEdgeWeight(newEdge, weight);
        else newEdge = graph.getEdge(firstPoint,secondPoint);

        if (newEdge == null)
        {
            System.out.println(graph.toString());
            System.out.println("First point: " + firstPoint);
            System.out.println("Second point: " + secondPoint);
            System.out.println("Weight: " + weight);
            System.exit(101);
        }
    }

    private void addVerticesAndEdge(SimpleWeightedGraph<Integer, DefaultWeightedEdge> graph,
                                  int firstPoint, int secondPoint, int weight) {
        graph.addVertex(firstPoint);
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
        Random random;
        if (seed != -1) random = new Random(seed);
        else random = new Random();
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
            addVerticesAndEdge(solution,firstPoint,secondPoint,(int) this.graph.getEdgeWeight(edge));

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
        addVerticesAndEdge(solution,firstPoint,secondPoint,(int) this.graph.getEdgeWeight(edge));
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
}