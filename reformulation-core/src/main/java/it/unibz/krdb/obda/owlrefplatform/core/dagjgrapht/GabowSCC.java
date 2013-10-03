package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import java.util.*;

import org.jgrapht.*;



public class GabowSCC<V, E>
{
    //~ Instance fields --------------------------------------------------------

    // the graph to compute the strongly connected sets for
    private final DirectedGraph<V, E> graph;

    // stores the vertices
    private Deque<VertexNumber<V>> stack = new ArrayDeque<VertexNumber<V>>();
    

    // the result of the computation, cached for future calls
    private List<Set<V>> stronglyConnectedSets;


    // maps vertices to their VertexNumber object
    private Map<V, VertexNumber<V>> vertexToVertexName;
    private Deque<Integer> B = new ArrayDeque<Integer>();
    
    private int c; 

    //~ Constructors -----------------------------------------------------------

    /**
     * The constructor of  GabowSCC class.
     *
     * @param directedGraph the graph to inspect
     *
     * @throws IllegalArgumentException
     */
    public GabowSCC(DirectedGraph<V, E> directedGraph)
    {
        if (directedGraph == null) {
            throw new IllegalArgumentException("null not allowed for graph!");
        }

        graph = directedGraph;
        vertexToVertexName = null;
 
        
        stronglyConnectedSets = null;
        
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Returns the graph inspected 
     *
     * @return the graph inspected 
     */
    public DirectedGraph<V, E> getGraph()
    {
        return graph;
    }

    /**
     * Returns true if the graph instance is strongly connected.
     *
     * @return true if the graph is strongly connected, false otherwise
     */
    public boolean isStronglyConnected()
    {
        return stronglyConnectedSets().size() == 1;
    }

    /**
     * Computes a {@link List} of {@link Set}s, where each set contains vertices
     * which together form a strongly connected component within the given
     * graph.
     *
     * @return <code>List</code> of <code>Set</code> s containing the strongly
     * connected components
     */
    public List<Set<V>> stronglyConnectedSets()
    {
        if (stronglyConnectedSets == null) {
            
            stronglyConnectedSets = new Vector<Set<V>>();

            
            // create VertexData objects for all vertices, store them
            createVertexData();

            // perform  DFS
            for (VertexNumber<V> data : vertexToVertexName.values()) {
//            	if(data.getVertex().toString().equals("http://www.owl-ontologies.com/Ontology1207768242.owl#StockBroker"))
//            		System.out.println(Graphs.successorListOf(graph, data.getVertex()) );
                if (data.getNumber()==0) {
                    dfsVisit(graph, data);
                }
            }

       
            
            vertexToVertexName = null;
            stack =null;
            B=null;
        }

        return stronglyConnectedSets;
    }

   
    /*
     * Creates a VertexNumber object for every vertex in the graph and stores
     * them
     * in a HashMap.
     */
    private void createVertexData()
    {
    	c=graph.vertexSet().size();
        vertexToVertexName =
            new HashMap<V, VertexNumber<V>>(c);

        for (V vertex : graph.vertexSet()) {
            vertexToVertexName.put(
                vertex,
                new VertexNumber<V>(vertex, 0));
            
        }
        
        stack = new ArrayDeque<VertexNumber<V>>(c);
        B = new ArrayDeque<Integer>(c);
    }

    /*
     * The subroutine of DFS. 
     */
    private void dfsVisit(
        DirectedGraph<V, E> visitedGraph,
        VertexNumber<V> v)
    {
    	 VertexNumber<V> w;
    	 stack.add(v);
    	 B.add(v.setNumber(stack.size()-1));
//    	 vertexToVertexName.put( v.getVertex(),v);
//    	 System.out.println(vertexToVertexData.get(v.getVertex()).getNumber());
    	

                // follow all edges
               	
                	for (E edge : visitedGraph.outgoingEdgesOf(v.getVertex())) {
                		w =  vertexToVertexName.get(
                                visitedGraph.getEdgeTarget(edge));
                
                    
//                    if(w.getVertex().toString().equals("http://www.owl-ontologies.com/Ontology1207768242.owl#StockBroker"))
//                		System.out.println(Graphs.successorListOf(visitedGraph, v.getVertex())+ " "+v.getVertex());

                    if (w.getNumber()==0) {
                        dfsVisit(graph, w);
                    }
                    else { /*contract if necessary*/
                    	while (w.getNumber() < B.getLast()) 
                    	 B.removeLast(); 
                    	 } 
                    	 }
                Set<V> L = new HashSet<V>(); 
                if (v.getNumber() == (B.getLast())) { 
                	/* number vertices of the next
                		strong component */
                 B.removeLast(); 
              
                c++;
                while (v.getNumber() <= (stack.size()-1)) {
                	VertexNumber<V> r= stack.removeLast();
                 L.add(r.getVertex()); 
//                 stack.remove(stack.size() - 1); 
                 r.setNumber(c);
//                 vertexToVertexName.put( r.getVertex(),r);
                } 
                stronglyConnectedSets.add(L); 
                 } 
    }

  
   
    
    private static final class VertexNumber<V>
    
{
    V vertex;
    int number=0;
    	
    private VertexNumber(
        V vertex,
        int number)
    {
        this.vertex=vertex;
        this.number=number;
    }

    int getNumber()
    {
        return number;
    }

    V getVertex()
    {
        return vertex ;
    }
    Integer setNumber( int n){
    	return number=n;
    	
    }
}

   
}

// End 
