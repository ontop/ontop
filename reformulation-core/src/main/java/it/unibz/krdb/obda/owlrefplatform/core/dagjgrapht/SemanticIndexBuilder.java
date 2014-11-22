package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import it.unibz.krdb.obda.ontology.ClassExpression;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.TraversalListenerAdapter;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.EdgeReversedGraph;
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

	private final TBoxReasoner reasoner;
	private final Map<ClassExpression, Integer> classIndexes = new HashMap<>();
	private final Map<ClassExpression, SemanticIndexRange> classRanges = new HashMap<>();
	private final Map<ObjectPropertyExpression, Integer> opIndexes = new HashMap<>();
	private final Map<ObjectPropertyExpression, SemanticIndexRange> opRanges = new HashMap<>();
	private final Map<DataPropertyExpression, Integer> dpIndexes = new HashMap<>();
	private final Map<DataPropertyExpression, SemanticIndexRange> dpRanges = new HashMap<>();
	
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
		private final Map<T, Integer> indexes;
		private final Map<T, SemanticIndexRange> ranges;
		
		public SemanticIndexer(DirectedGraph<T,DefaultEdge> namedDAG, Map<T, Integer> indexes, Map<T, SemanticIndexRange> ranges) {
			this.namedDAG = namedDAG;
			this.indexes = indexes;
			this.ranges = ranges;
		}
		
		//search for the new root in the graph
		@Override
		public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
			newComponent = true;
		}

		@Override
		public void vertexTraversed(VertexTraversalEvent<T> e) {
			T vertex = e.getVertex();

			if (newComponent) {
				reference = vertex;
				newComponent = false;
			}

			indexes.put(vertex, index_counter);
			ranges.put(vertex, new SemanticIndexRange(index_counter, index_counter));
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

	private <T> void createSemanticIndex(DirectedGraph<T, DefaultEdge> dag, Map<T,Integer> indexes, Map<T,SemanticIndexRange> ranges) {
		DirectedGraph<T, DefaultEdge> reversed = new EdgeReversedGraph<>(dag);
		
		LinkedList<T> roots = new LinkedList<>();
		for (T n : reversed.vertexSet()) {
			if ((reversed.incomingEdgesOf(n)).isEmpty()) {
				roots.add(n);
			}
		}
		
		for (T root: roots) {
			// depth-first sort 
			GraphIterator<T, DefaultEdge> orderIterator = new DepthFirstIterator<>(reversed, root);
		
			//add Listener to create the indexes and ranges
			orderIterator.addTraversalListener(new SemanticIndexer<T>(reversed, indexes, ranges));
		
			//		System.out.println("\nIndexing:");
			while (orderIterator.hasNext()) 
				orderIterator.next();
		}
	}
	
	/**
	 * Assign indexes for the named DAG, use a depth first listener over the DAG 
	 * @param reasoner used to know ancestors and descendants of the dag
	 */
	
	public SemanticIndexBuilder(TBoxReasoner reasoner)  {
		this.reasoner = reasoner;
		
		NamedDAG namedDAG = new NamedDAG(reasoner);
		
		//test with a reversed graph so that the smallest index will be given to the higher ancestor
		createSemanticIndex(namedDAG.getClassDag(), classIndexes, classRanges);
		createSemanticIndex(namedDAG.getObjectPropertyDag(), opIndexes, opRanges);
		createSemanticIndex(namedDAG.getDataPropertyDag(), dpIndexes, dpRanges);
		
		index_counter = 1;
	}
	
	public int getIndex(OClass d) {
		Integer idx = classIndexes.get(d); 
		if (idx != null)
			return idx;
		return -1;
	}
	public int getIndex(ObjectPropertyExpression d) {
		Integer idx = opIndexes.get(d); 
		if (idx != null)
			return idx;
		return -1;
	}
	public int getIndex(DataPropertyExpression d) {
		Integer idx = dpIndexes.get(d); 
		if (idx != null)
			return idx;
		return -1;
	}
	
	
	public List<Interval> getIntervals(OClass d) {

		Description node = reasoner.getClassDAG().getVertex(d).getRepresentative();
		
		SemanticIndexRange range = classRanges.get(node);
		if (range == null)
			range = new SemanticIndexRange(-1, -1);
		return range.getIntervals();
	}

	public List<Interval> getIntervals(ObjectPropertyExpression d) {

		Description node = reasoner.getObjectPropertyDAG().getVertex(d).getRepresentative();
		
		SemanticIndexRange range = opRanges.get(node);
		if (range == null)
			range = new SemanticIndexRange(-1, -1);
		return range.getIntervals();
	}
	
	public List<Interval> getIntervals(DataPropertyExpression d) {

		Description node = reasoner.getDataPropertyDAG().getVertex(d).getRepresentative();
		
		SemanticIndexRange range = dpRanges.get(node);
		if (range == null)
			range = new SemanticIndexRange(-1, -1);
		return range.getIntervals();
	}
	

	public Set<ClassExpression> getIndexedClasses() {
		return classIndexes.keySet();
	}
	public Set<ObjectPropertyExpression> getIndexedObjectProperties() {
		return opIndexes.keySet();
	}
	public Set<DataPropertyExpression> getIndexedDataProperties() {
		return dpIndexes.keySet();
	}
	
	
	// TEST ONLY
	
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
