package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Property;

import java.util.LinkedHashSet;
import java.util.Set;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;


/** Use to represent a simple graph of a TBox.
 * <p>
 * A directed graph where multiple edges are not permitted, but loops are. 
 * It extends DefaultDirectedGraph from JGrapht
 * 
 */

public class TBoxGraph extends DefaultDirectedGraph<Description,DefaultEdge> {

	private static final long serialVersionUID = 6784249753145034915L;

	private Set<OClass> classes = null;
	private Set<Property> roles = null;


	public TBoxGraph() {
		super(DefaultEdge.class);		
	}

	/** 
	 * @return  set of all property names (not inverse) in the graph
	 */
	
	public Set<Property> getRoles() {
		if (roles == null) {
			// caching
			roles = new LinkedHashSet<Property>();
			
			for (Description r: this.vertexSet()) {
				if (r instanceof Property) {
					if(!((Property) r).isInverse())
					roles.add((Property)r);
				}
			}
		}
		return roles;
	}

	/**
	 * @return  set of all concept names in the graph
	 */

	public Set<OClass> getClasses() {
		if (classes == null) {
			// caching 
			classes = new LinkedHashSet<OClass>();
			
			for (Description c: this.vertexSet()) {
				if (c instanceof OClass) {
					classes.add((OClass)c);
				}
			}
		}
		return classes;
	}
}
