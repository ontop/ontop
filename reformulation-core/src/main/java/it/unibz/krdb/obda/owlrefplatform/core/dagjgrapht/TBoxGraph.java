package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.Axiom;
import it.unibz.krdb.obda.ontology.ClassDescription;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.ontology.impl.SubClassAxiomImpl;
import it.unibz.krdb.obda.ontology.impl.SubPropertyAxiomImpl;

import java.util.LinkedHashSet;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;


/** Use to build a simple graph.
 * <p>
 * A directed graph where multiple edges are not permitted, but loops are. 
 * It extends DefaultDirectedGraph from JGrapht
 * 
 */

public class TBoxGraph  {

	private DefaultDirectedGraph<Description,DefaultEdge> graph;
	private Set<OClass> classes = new LinkedHashSet<OClass>();
	private Set<Property> roles = new LinkedHashSet<Property>();


	/**
	 * Constructor for a Graph 
	 */

	private TBoxGraph(DefaultDirectedGraph<Description,DefaultEdge> graph) {
		this.graph = graph;
	}

	/**
	 * Allows to have all named roles in the graph
	 * @return  set of all property (not inverse) in the graph
	 */

	//return all roles in the graph
	public Set<Property> getRoles(){
		for (Description r: graph.vertexSet()) {
			if (r instanceof Property) {
				if (!((Property) r).isInverse())
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
		for (Description c: graph.vertexSet()) {
			if (c instanceof OClass) 
				classes.add((OClass)c);
		}
		return classes;
	}
	
	public DirectedGraph<Description, DefaultEdge> getGraph() {
		return graph;
	}	

	public Set<Description> vertexSet() {
		return graph.vertexSet();
	}
	
	public Set<DefaultEdge> edgeSet() {
		return graph.edgeSet();
	}


	public Description getEdgeTarget(DefaultEdge edge) {
		return graph.getEdgeTarget(edge);
	}
	
	public Description getEdgeSource(DefaultEdge edge) {
		return graph.getEdgeSource(edge);
	}

	public Set<DefaultEdge> outgoingEdgesOf(Description n) {
		return graph.outgoingEdgesOf(n);
	}
	
	public Set<DefaultEdge> incomingEdgesOf(Description n) {
		return graph.incomingEdgesOf(n);
	}

	public int inDegreeOf(Description vertex) {
		return graph.inDegreeOf(vertex);
	}

	public int outDegreeOf(Description vertex) {
		return graph.outDegreeOf(vertex);
	}
	
	public DefaultDirectedGraph<Description,DefaultEdge> getCopy() {
		
		DefaultDirectedGraph<Description,DefaultEdge> copy = 
				new DefaultDirectedGraph<Description,DefaultEdge>(DefaultEdge.class);

		for (Description v : graph.vertexSet()) {
			copy.addVertex(v);
		}

		for (DefaultEdge e : graph.edgeSet()) {
			Description s = graph.getEdgeSource(e);
			Description t = graph.getEdgeTarget(e);

			copy.addEdge(s, t, e);
		}
		return copy;
	}
	

	
	/**
	 * Build the graph from the TBox axioms of the ontology
	 * 
	 * @param ontology TBox containing the axioms
	 * @param chain 
	 * Modifies the DAG so that \exists R = \exists R-, so that the reachability
	 * relation of the original DAG gets extended to the reachability relation
	 * of T and Sigma chains.
	 */
	public static TBoxGraph getGraph (Ontology ontology, boolean chain) {
		
		DefaultDirectedGraph<Description,DefaultEdge> graph = new  DefaultDirectedGraph<Description,DefaultEdge>(DefaultEdge.class);
		OntologyFactory descFactory = OntologyFactoryImpl.getInstance();
		
		
		for (Predicate conceptp : ontology.getConcepts()) {
			ClassDescription concept = descFactory.createClass(conceptp);
			graph.addVertex(concept);
		}

		/*
		 * For each role we add nodes for its inverse, its domain and its range
		 */
		for (Predicate rolep : ontology.getRoles()) {
			Property role = descFactory.createProperty(rolep);
			Property roleInv = descFactory.createProperty(role.getPredicate(), !role.isInverse());
			PropertySomeRestriction existsRole = descFactory.getPropertySomeRestriction(role.getPredicate(), role.isInverse());
			PropertySomeRestriction existsRoleInv = descFactory.getPropertySomeRestriction(role.getPredicate(), !role.isInverse());
			graph.addVertex(role);
			graph.addVertex(roleInv);
			graph.addVertex(existsRole);
			graph.addVertex(existsRoleInv);
			if (chain) {
				graph.addEdge(existsRoleInv, existsRole);				
				graph.addEdge(existsRole, existsRoleInv);				
			}
		}

		for (Axiom assertion : ontology.getAssertions()) {

			if (assertion instanceof SubClassAxiomImpl) {
				SubClassAxiomImpl clsIncl = (SubClassAxiomImpl) assertion;
				ClassDescription parent = clsIncl.getSuper();
				ClassDescription child = clsIncl.getSub();
				graph.addVertex(child);
				graph.addVertex(parent);
				graph.addEdge(child, parent);
			} 
			else if (assertion instanceof SubPropertyAxiomImpl) {
				SubPropertyAxiomImpl roleIncl = (SubPropertyAxiomImpl) assertion;
				Property parent = roleIncl.getSuper();
				Property child = roleIncl.getSub();
				Property parentInv = descFactory.createProperty(parent.getPredicate(), !parent.isInverse());
				Property childInv = descFactory.createProperty(child.getPredicate(), !child.isInverse());

				// This adds the direct edge and the inverse, e.g., R ISA S and
				// R- ISA S-,
				// R- ISA S and R ISA S-
				graph.addVertex(child);
				graph.addVertex(parent);
				graph.addVertex(childInv);
				graph.addVertex(parentInv);
				graph.addEdge(child, parent);
				graph.addEdge(childInv, parentInv);
				
				//add also edges between the existential
				ClassDescription existsParent = descFactory.getPropertySomeRestriction(parent.getPredicate(), parent.isInverse());
				ClassDescription existChild = descFactory.getPropertySomeRestriction(child.getPredicate(), child.isInverse());
				ClassDescription existsParentInv = descFactory.getPropertySomeRestriction(parent.getPredicate(), !parent.isInverse());
				ClassDescription existChildInv = descFactory.getPropertySomeRestriction(child.getPredicate(), !child.isInverse());
				graph.addVertex(existChild);
				graph.addVertex(existsParent);
				graph.addEdge(existChild, existsParent);
				
				graph.addVertex(existChildInv);
				graph.addVertex(existsParentInv);
				graph.addEdge(existChildInv, existsParentInv);
				
			}
		}
		return new TBoxGraph(graph);
	}
	
	public static TBoxGraph getGraph (Ontology ontology) {
		return getGraph(ontology, false);
	}

}
