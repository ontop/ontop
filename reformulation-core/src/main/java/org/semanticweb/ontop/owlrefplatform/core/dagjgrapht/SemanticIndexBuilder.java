package org.semanticweb.ontop.owlrefplatform.core.dagjgrapht;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.TraversalListenerAdapter;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;
import org.semanticweb.ontop.ontology.BasicClassDescription;
import org.semanticweb.ontop.ontology.Description;
import org.semanticweb.ontop.ontology.PropertyExpression;


/** 
 * 
 * Build the indexes for the DAG
 * create a map with the index and the intervals for each node in the graph
 * 
 * 
 */
public class SemanticIndexBuilder  {

	private final TBoxReasoner reasoner;
	private Map< Description, Integer> indexes = new HashMap<Description, Integer>();
	private Map< Description, SemanticIndexRange> ranges = new HashMap<Description, SemanticIndexRange>();
	private int index_counter = 1;
	private final NamedDAG namedDAG;

	
	/**
	 * Listener that creates the index for each node visited in depth first search.
	 * extends TraversalListenerAdapter from JGrapht
	 *
	 */
	private final class IndexListener extends TraversalListenerAdapter<Description, DefaultEdge> {

		private Description reference; 		//last root node
		private boolean newComponent = true;

		
		//search for the new root in the graph
		@Override
		public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
			newComponent = true;
		}

		@Override
		public void vertexTraversed(VertexTraversalEvent<Description> e) {

			Description vertex = e.getVertex();

			if (newComponent) {
				reference = vertex;
				newComponent = false;
			}

			indexes.put(vertex, index_counter);
			ranges.put(vertex, new SemanticIndexRange(index_counter, index_counter));
			index_counter++;
		}

		public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {
			//merge all the interval for the current root of the graph
			mergeRangeNode(reference);
		}
		/**  
		 * Merge the indexes of the current connected component 
		 * @param d  is the root node 
		 * */
		private void mergeRangeNode(Description d) {

			if (d instanceof PropertyExpression) {
				for (Description ch : namedDAG.getPredecessors((PropertyExpression)d)) { 
					if (!ch.equals(d)) { // Roman: was !=
						mergeRangeNode(ch);

						//merge the index of the node with the index of his child
						ranges.get(d).addRange(ranges.get(ch));
					}
				}
			}
			else {
				for (Description ch : namedDAG.getPredecessors((BasicClassDescription)d)) { 
					if (!ch.equals(d)) { // Roman: was !=
						mergeRangeNode(ch);

						//merge the index of the node with the index of his child
						ranges.get(d).addRange(ranges.get(ch));
					}
				}
				
			}
		}
	}

	/**
	 * Assign indexes for the named DAG, use a depth first listener over the DAG 
	 * @param reasoner used to know ancestors and descendants of the dag
	 */
	
	public SemanticIndexBuilder(TBoxReasoner reasoner)  {
		this.reasoner = reasoner;
		
		namedDAG = new NamedDAG(reasoner);
		
		//test with a reversed graph so that the smallest index will be given to the higher ancestor
		DirectedGraph<Description, DefaultEdge> reversed = namedDAG.getReversedDag();

		LinkedList<Description> roots = new LinkedList<Description>();
		for (Description n : reversed.vertexSet()) {
			if ((reversed.incomingEdgesOf(n)).isEmpty()) {
				roots.add(n);
			}
		}
		
		for (Description root: roots) {
		//A depth first sort 
			GraphIterator<Description, DefaultEdge> orderIterator 
				= new DepthFirstIterator<Description, DefaultEdge>(reversed, root);
		
			//add Listener to create the indexes and ranges
			orderIterator.addTraversalListener(new IndexListener());
		

			//		System.out.println("\nIndexing:");
			while (orderIterator.hasNext()) {
				orderIterator.next();
			}
		}
		index_counter = 1;
	}
	
	public NamedDAG getNamedDAG() {
		return namedDAG;
	}

	public int getIndex(Description d) {
		Integer idx = indexes.get(d); 
		if (idx != null)
			return idx;
		return -1;
	}
	
	
	public List<Interval> getIntervals(Description d) {

		Description node;
		if (d instanceof PropertyExpression)
			node = reasoner.getProperties().getVertex((PropertyExpression)d).getRepresentative();
		else
			node = reasoner.getClasses().getVertex((BasicClassDescription)d).getRepresentative();
		
		SemanticIndexRange range = ranges.get(node);
		if (range == null)
			range = new SemanticIndexRange(-1, -1);
		return range.getIntervals();
	}
	
	
	// TEST ONLY
	public SemanticIndexRange getRange(Description d) {
		return ranges.get(d);
	}

	public Set<Description> getIndexed() {
		return indexes.keySet();
	}
}
