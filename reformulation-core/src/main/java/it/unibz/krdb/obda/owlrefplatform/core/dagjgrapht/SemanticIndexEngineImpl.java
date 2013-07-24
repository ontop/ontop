package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Interval;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.TraversalListenerAdapter;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;


/** Build the indexes for the DAG
 * create a map with the index and the intervals for each node in the graph
 * 
 * 
 */
public class SemanticIndexEngineImpl implements SemanticIndexEngine{

	private DAGImpl namedDag;
	private Map< Description, Integer> indexes = new HashMap<Description, Integer>();
	private Map< Description, SemanticIndexRange> ranges = new HashMap<Description, SemanticIndexRange>();

	//listener on the depth first sort of the graph
	public class IndexListener extends TraversalListenerAdapter<Description, DefaultEdge> {


		private int index_counter = 1;



		DirectedGraph <Description, DefaultEdge> g;
		private boolean newComponent;

		//last root node
		private Description reference;

		public IndexListener(DirectedGraph<Description, DefaultEdge> g) {
			this.g = g;
		}

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
	}

	/**  Merge the indexes of the current connected component 
	 * @param Description d  is the root node */
	private void mergeRangeNode(Description d) {

		DirectedGraph<Description, DefaultEdge> reversed =
				new EdgeReversedGraph<Description, DefaultEdge>(namedDag);
		//successorList gives the direct children of the node without the equivalences
		for (Description ch : Graphs.successorListOf(reversed, d)) {
			if (ch != d) {
				mergeRangeNode(ch);

				//merge the index of the node with the index of his child
				ranges.get(d).addRange(ranges.get(ch));
			}

		}


	}

	/**
	 * Assign indexes for the named DAG, use a depth first listener over the DAG 
	 * @param reasoner used to know ancestors and descendants of the dag
	 */


	public SemanticIndexEngineImpl(TBoxReasoner reasoner) {


		namedDag=(DAGImpl) reasoner.getDAG();
		if (namedDag.isaNamedDAG())
		{
			construct();
		}

	}
	private void construct(){
		GraphIterator<Description, DefaultEdge> orderIterator;

		//test with a reversed graph so that the smallest index will be given to the higher ancestor
		DirectedGraph<Description, DefaultEdge> reversed =
				new EdgeReversedGraph<Description, DefaultEdge>(namedDag);

		//A depth first sort 
		orderIterator =
				new DepthFirstIterator<Description, DefaultEdge>(reversed);

		//add Listener to create the indexes and ranges
		orderIterator.addTraversalListener(new IndexListener(reversed));

		//		System.out.println("\nIndexing:");
		while (orderIterator.hasNext()) {
			orderIterator.next();

		}
		//		System.out.println(indexes);
		//		System.out.println(ranges);

	}

	@Override
	public int getIndex(Description d) {
		if(indexes.get(d)!=null)
			return indexes.get(d);
		return -1;


	}
	
	public void setIndex(Description d, int index) {
		indexes.put(d, index);

	}

	@Override
	public List<Interval> getIntervals(Description d) {

		List<Interval> list= new LinkedList<Interval>();
		if(ranges.get(d)!=null)
			return ranges.get(d).getIntervals();
		SemanticIndexRange range= new SemanticIndexRange(-1, -1);
		return range.getIntervals();

	}
	
	public void setRange(Description d, SemanticIndexRange range) {
		ranges.put(d, range);

	}

	@Override
	public Map<Description, Integer> getIndexes() {
		return indexes;


	}

	@Override
	public Map<Description, SemanticIndexRange> getIntervals() {

		return ranges;

	}
	
	


}
