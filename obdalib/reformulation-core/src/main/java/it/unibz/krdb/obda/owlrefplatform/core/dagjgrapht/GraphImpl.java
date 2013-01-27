package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;
import org.jgrapht.graph.DefaultDirectedGraph;



/** Use to build a simple graph.
 * 
 * We probably don't need this class we can simply used DefaultDirectedGraph<V,E>
 *  A directed graph multiple edges are not permitted, but loops are. 
 * @author Sarah
 *
 */
public class GraphImpl <V, E> extends DefaultDirectedGraph<V,E> implements Graph {

	public GraphImpl(Class<? extends E> arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}


}
