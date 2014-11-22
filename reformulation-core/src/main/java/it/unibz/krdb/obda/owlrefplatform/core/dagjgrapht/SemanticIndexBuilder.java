package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import it.unibz.krdb.obda.ontology.ClassExpression;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.TraversalListenerAdapter;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;


/** 
 * 
 * Build the indexes for the DAG
 * create a map with the index and the intervals for each node in the graph
 * 
 * 
 */
public class SemanticIndexBuilder  {

	private final Map<ClassExpression, SemanticIndexRange> classRanges;
	private final Map<ObjectPropertyExpression, SemanticIndexRange> opRanges;
	private final Map<DataPropertyExpression, SemanticIndexRange> dpRanges;
	
	// index_counter is changed during the traversal of all three DAGs
	private int index_counter = 1;

	/**
	 * Listener that creates the index for each node visited in depth first search.
	 * extends TraversalListenerAdapter from JGrapht
	 *
	 */
	private final class SemanticIndexer<T> extends TraversalListenerAdapter<T, DefaultEdge> {

		private T reference; 		//last root node
		private boolean newComponent = true;

		private final DirectedGraph <T,DefaultEdge> namedDAG;
		private final Map<T, SemanticIndexRange> ranges;
		
		public SemanticIndexer(DirectedGraph<T,DefaultEdge> namedDAG, Map<T, SemanticIndexRange> ranges) {
			this.namedDAG = namedDAG;
			this.ranges = ranges;
		}
		
		@Override
		public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
			newComponent = true;  // to record a new root
		}

		@Override
		public void vertexTraversed(VertexTraversalEvent<T> e) {
			T vertex = e.getVertex();

			if (newComponent) {
				reference = vertex;
				newComponent = false;
			}

			ranges.put(vertex, new SemanticIndexRange(index_counter));
			index_counter++;
		}

		@Override
		public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {
			//merge all the interval for the current root of the graph
			mergeRangeNode(reference);
		}
		/**  
		 * Merge the indexes of the current connected component 
		 * @param d  is the root node 
		 * */
		private void mergeRangeNode(T d) {
			for (T ch : Graphs.successorListOf(namedDAG, d)) { 
				if (!ch.equals(d)) { 
					mergeRangeNode(ch);

					//merge the index of the node with the index of his child
					ranges.get(d).addRange(ranges.get(ch));
				}
			}
		}
	}

	private <T> Map<T, SemanticIndexRange> createSemanticIndex(EquivalencesDAG<T> dag) {
		
		DirectedGraph<T, DefaultEdge> namedDag = getNamedDAG(dag);
		// reverse the named dag so that we give smallest indexes to ? 
		DirectedGraph<T, DefaultEdge> reversed = new EdgeReversedGraph<>(namedDag);
		
		LinkedList<T> roots = new LinkedList<>();
		for (T n : reversed.vertexSet()) 
			if ((reversed.incomingEdgesOf(n)).isEmpty()) 
				roots.add(n);
			
		Map<T,SemanticIndexRange> ranges = new HashMap<>();
		for (T root: roots) {
			// depth-first sort 
			GraphIterator<T, DefaultEdge> orderIterator = new DepthFirstIterator<>(reversed, root);
		
			// add Listener to create the ranges
			orderIterator.addTraversalListener(new SemanticIndexer<T>(reversed, ranges));
		
			// System.out.println("\nIndexing:");
			while (orderIterator.hasNext()) 
				orderIterator.next();
		}
		return ranges;
	}
	
	/**
	 * Constructor for the NamedDAG
	 * @param dag the DAG from which we want to keep only the named descriptions
	 */

	public static <T> SimpleDirectedGraph <T,DefaultEdge> getNamedDAG(EquivalencesDAG<T> dag) {
		
		SimpleDirectedGraph<T,DefaultEdge> namedDAG = new SimpleDirectedGraph<>(DefaultEdge.class); 

		for (Equivalences<T> v : dag) 
			namedDAG.addVertex(v.getRepresentative());

		for (Equivalences<T> s : dag) 
			for (Equivalences<T> t : dag.getDirectSuper(s)) 
				namedDAG.addEdge(s.getRepresentative(), t.getRepresentative());

		for (Equivalences<T> v : dag) 
			if (!v.isIndexed()) {
				// eliminate node
				for (DefaultEdge incEdge : namedDAG.incomingEdgesOf(v.getRepresentative())) { 
					T source = namedDAG.getEdgeSource(incEdge);

					for (DefaultEdge outEdge : namedDAG.outgoingEdgesOf(v.getRepresentative())) {
						T target = namedDAG.getEdgeTarget(outEdge);

						namedDAG.addEdge(source, target);
					}
				}
				namedDAG.removeVertex(v.getRepresentative());		// removes all adjacent edges as well				
			}
		return namedDAG;
	}
	
	
	/**
	 * Assign indexes for the named DAG, use a depth first listener over the DAG 
	 * @param reasoner used to know ancestors and descendants of the dag
	 */
	
	public SemanticIndexBuilder(TBoxReasoner reasoner)  {
		classRanges = createSemanticIndex(reasoner.getClassDAG());
		opRanges = createSemanticIndex(reasoner.getObjectPropertyDAG());
		dpRanges = createSemanticIndex(reasoner.getDataPropertyDAG());
	}
		
	

	public Set<Entry<ClassExpression, SemanticIndexRange>> getIndexedClasses() {
		return classRanges.entrySet();
	}
	public Set<Entry<ObjectPropertyExpression, SemanticIndexRange>> getIndexedObjectProperties() {
		return opRanges.entrySet();
	}
	public Set<Entry<DataPropertyExpression, SemanticIndexRange>> getIndexedDataProperties() {
		return dpRanges.entrySet();
	}
	
	
	
	public SemanticIndexRange getRange(OClass d) {
		return classRanges.get(d);
	}
	public SemanticIndexRange getRange(ObjectPropertyExpression d) {
		return opRanges.get(d);
	}
	public SemanticIndexRange getRange(DataPropertyExpression d) {
		return dpRanges.get(d);
	}
}
