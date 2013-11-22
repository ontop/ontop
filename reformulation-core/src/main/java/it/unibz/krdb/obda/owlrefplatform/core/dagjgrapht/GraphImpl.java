package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Property;

import java.util.LinkedHashSet;
import java.util.Set;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;


/** Use to build a simple graph.
 * <p>
 * A directed graph where multiple edges are not permitted, but loops are. 
 * It extends DefaultDirectedGraph from JGrapht
 * 
 */

public class GraphImpl extends DefaultDirectedGraph<Description,DefaultEdge> implements Graph {

	
	private static final long serialVersionUID = 6784249753145034915L;

	private Set<OClass> classes = new LinkedHashSet<OClass>();

	private Set<Property> roles = new LinkedHashSet<Property>();


	/**
	 * Constructor for a Graph 
	 * @param arg0 type of the edges
	 */

	public GraphImpl(Class<? extends DefaultEdge> arg0) {
		super(arg0);

		
	}

	/**
	 * Allows to have all named roles in the graph
	 * @return  set of all property (not inverse) in the graph
	 */

	//return all roles in the graph
	public Set<Property> getRoles(){
		for (Description r: this.vertexSet()){
			if (r instanceof Property){
				if(!((Property) r).isInverse())
				roles.add((Property)r);
			}

		}
		return roles;

	}

	/**
	 * Allows to have all named classes in the graph
	 * @return  set of all named concepts in the graph
	 */

	public Set<OClass> getClasses(){
		for (Description c: this.vertexSet()){
			if (c instanceof OClass){
				classes.add((OClass)c);
			}

		}
		return classes;

	}




}
