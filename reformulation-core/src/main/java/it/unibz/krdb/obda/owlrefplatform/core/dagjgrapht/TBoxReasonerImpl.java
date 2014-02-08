/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.AbstractGraphIterator;
import org.jgrapht.traverse.BreadthFirstIterator;

/**
 * Allows to reason over the TBox using  DAG or graph
 * 
 */

public class TBoxReasonerImpl implements TBoxReasoner {

	private final DefaultDirectedGraph<Property,DefaultEdge> propertyGraph; // test only
	private final DefaultDirectedGraph<BasicClassDescription,DefaultEdge> classGraph; // test only
	private final DefaultDirectedGraph<Description,DefaultEdge> graph; // test only

	private final EquivalencesDAG<Description> dag;
	
	private Set<OClass> classNames;
	private Set<Property> propertyNames;
	

	

	public TBoxReasonerImpl(Ontology onto) {
		propertyGraph = OntologyGraph.getPropertyGraph(onto);
		classGraph = OntologyGraph.getClassGraph(onto, propertyGraph, false);
		graph = new DefaultDirectedGraph<Description,DefaultEdge>(DefaultEdge.class);
		Graphs.addGraph(graph, propertyGraph);
		Graphs.addGraph(graph, classGraph);

		dag = new EquivalencesDAG<Description>(graph);
		DAGBuilder.choosePropertyRepresentatives(dag);
		DAGBuilder.chooseClassRepresentatives(dag);
	}

	private TBoxReasonerImpl(DefaultDirectedGraph<Property,DefaultEdge> propertyGraph, 
					DefaultDirectedGraph<BasicClassDescription,DefaultEdge> classGraph) {
		this.propertyGraph = propertyGraph;
		this.classGraph = classGraph;
		graph = new DefaultDirectedGraph<Description,DefaultEdge>(DefaultEdge.class);
		Graphs.addGraph(graph, propertyGraph);
		Graphs.addGraph(graph, classGraph);

		dag = new EquivalencesDAG<Description>(graph);
		DAGBuilder.choosePropertyRepresentatives(dag);
		DAGBuilder.chooseClassRepresentatives(dag);
	}


	@Override
	public String toString() {
		return dag.toString();
	}
	
	
	public Description getRepresentativeFor(Description v) {
		Equivalences<Description> e = dag.getVertex(v);
		if (e != null)
			return e.getRepresentative();
		return null;
	}
	
	public Description getRepresentativeFor(Equivalences<Description> nodes) {
		Description first = nodes.iterator().next();
		return getRepresentativeFor(first);
	}
	
	public boolean isCanonicalRepresentative(Description v) {
		//return (replacements.get(v) == null);
		Equivalences<Description> e = dag.getVertex(v);
		return e.getRepresentative().equals(v);
	}

	
	

	/**
	 * Allows to have all named roles in the DAG even the equivalent named roles
	 * @return  set of all property (not inverse) in the DAG
	 */
	public Set<Property> getPropertyNames() {
		if (propertyNames == null) {
			propertyNames = new LinkedHashSet<Property> ();
			for (Equivalences<Description> v: dag.vertexSet()) 
				for (Description r : v) 
					if (r instanceof Property) {
						Property p = (Property) r;
						if (!p.isInverse())
							propertyNames.add(p);
					}
		}
		return propertyNames;
	}

	/**
	 * Allows to have all named classes in the DAG even the equivalent named classes
	 * @return  set of all named concepts in the DAG
	 */
	
	public Set<OClass> getClassNames() {
		if (classNames == null) {
			 classNames = new LinkedHashSet<OClass> ();
			 for (Equivalences<Description> v: dag.vertexSet())
				for (Description e : v)
					if (e instanceof OClass)
						classNames.add((OClass)e);
		}
		return classNames;
	}


	public boolean isNamed(Description node) {
		return dag.getVertex(node).isIndexed();
	}

//	public boolean isNamed0(Description node) {
//		return getClassNames().contains(node) || getPropertyNames().contains(node);
//	}

	/**
	 * return the direct children starting from the given node of the dag
	 * 
	 * @param desc node that we want to know the direct children
	 * @return we return a set of set of description to distinguish between
	 *         different nodes and equivalent nodes. equivalent nodes will be in
	 *         the same set of description
	 */
	@Override
	public Set<Equivalences<Description>> getDirectChildren(Description desc) {
		return dag.getDirectChildren(dag.getVertex(desc));
	}


	/**
	 * return the direct parents starting from the given node of the dag
	 * 
	 * @param desc node from which we want to know the direct parents
	 *            
	 * @return we return a set of set of description to distinguish between
	 *         different nodes and equivalent nodes. equivalent nodes will be in
	 *         the same set of description
	 * */
	@Override
	public Set<Equivalences<Description>> getDirectParents(Description desc) {
		return dag.getDirectParents(dag.getVertex(desc));
	}


	/**
	 * Traverse the graph return the descendants starting from the given node of
	 * the dag
	 * 
	 * @param desc node we want to know the descendants
	 *
	 * @return we return a set of set of description to distinguish between
	 *         different nodes and equivalent nodes. equivalent nodes will be in
	 *         the same set of description
	 */
	@Override
	public Set<Equivalences<Description>> getDescendants(Description desc) {
		return dag.getDescendants(dag.getVertex(desc));
	}

	/**
	 * Traverse the graph return the ancestors starting from the given node of
	 * the dag
	 * 
	 * @param desc node we want to know the ancestors
	 *
	 * @return we return a set of set of description to distinguish between
	 *         different nodes and equivalent nodes. equivalent nodes will be in
	 *         the same set of description
	 */

	@Override
	public Set<Equivalences<Description>> getAncestors(Description desc) {
		return dag.getAncestors(dag.getVertex(desc));
	}

	/**
	 * Return the equivalences starting from the given node of the dag
	 * 
	 * @param desc node we want to know the ancestors
	 *            
	 * @return we return a set of description with equivalent nodes 
	 */

	@Override
	public Equivalences<Description> getEquivalences(Description desc) {
		return dag.getVertex(desc);
	}
	
	
	/**
	 * Return all the nodes in the DAG or graph
	 * 
	 * @return we return a set of set of description to distinguish between
	 *         different nodes and equivalent nodes. equivalent nodes will be in
	 *         the same set of description
	 */

	@Override
	public Set<Equivalences<Description>> getNodes() {
		return dag.vertexSet();
	}

	// INTERNAL DETAILS
	

	
	@Deprecated // test only
	public DefaultDirectedGraph<Description,DefaultEdge> getGraph() {
		return graph;
	}
	
	@Deprecated // test only
	public EquivalencesDAG<Description> getDAG() {
		return dag;
	}
	
	
	
	
	
	

//	public static TBoxReasonerImpl getChainReasoner2(Ontology onto) {
//		
//		return new TBoxReasonerImpl((OntologyGraph.getGraph(onto, true)));		
//	}
	
	/***
	 * Modifies the DAG so that \exists R = \exists R-, so that the reachability
	 * relation of the original DAG gets extended to the reachability relation
	 * of T and Sigma chains.
	 * 
	 * 
	 */
	
	public static TBoxReasonerImpl getChainReasoner(Ontology onto) {
		TBoxReasonerImpl tbox = new TBoxReasonerImpl(onto);
		
		
		// move everything to a graph that admits cycles
		DefaultDirectedGraph<BasicClassDescription,DefaultEdge> modifiedGraph = 
				new  DefaultDirectedGraph<BasicClassDescription,DefaultEdge>(DefaultEdge.class);

		// clone all the vertex and edges from dag
		for (Equivalences<Description> v : tbox.dag.vertexSet()) {
			if (v.getRepresentative() instanceof BasicClassDescription)
				modifiedGraph.addVertex((BasicClassDescription)v.getRepresentative());
		}
		for (Equivalences<Description> v : tbox.dag.vertexSet()) 
			if (v.getRepresentative() instanceof BasicClassDescription) {
				BasicClassDescription s = (BasicClassDescription)v.getRepresentative();
				for (Equivalences<Description> vp : tbox.dag.getDirectParents(v))
					modifiedGraph.addEdge(s, (BasicClassDescription)vp.getRepresentative());
			}

		Collection<Equivalences<Description>> nodes = new HashSet<Equivalences<Description>>(tbox.dag.vertexSet());
		OntologyFactory fac = OntologyFactoryImpl.getInstance();
		HashSet<Description> processedNodes = new HashSet<Description>();
		
		for (Equivalences<Description> n : nodes) {
			Description node = n.getRepresentative();
			
			if (!(node instanceof BasicClassDescription))
				continue;
			
			if (!(node instanceof PropertySomeRestriction) || processedNodes.contains(node)) 
				continue;

			/*
			 * Adding a cycle between exists R and exists R- for each R.
			 */

			PropertySomeRestriction existsNode = (PropertySomeRestriction) node;
			BasicClassDescription existsInvNode = (BasicClassDescription)tbox.getRepresentativeFor(
						fac.createPropertySomeRestriction(existsNode.getPredicate(), !existsNode.isInverse()));
			
			for (Equivalences<Description> children : tbox.getDirectChildren(existsNode)) {
				BasicClassDescription child = (BasicClassDescription)children.getRepresentative(); 
				if (!child.equals(existsInvNode))
					modifiedGraph.addEdge(child, existsInvNode);
			}
			for (Equivalences<Description> children : tbox.getDirectChildren(existsInvNode)) {
				BasicClassDescription child = (BasicClassDescription)children.getRepresentative(); 
				if (!child.equals(existsNode))
					modifiedGraph.addEdge(child, existsNode);
			}

			for (Equivalences<Description> parents : tbox.getDirectParents(existsNode)) {
				BasicClassDescription parent = (BasicClassDescription)parents.getRepresentative(); 
				if (!parent.equals(existsInvNode))
					modifiedGraph.addEdge(existsInvNode, parent);
			}

			for (Equivalences<Description> parents : tbox.getDirectParents(existsInvNode)) {
				BasicClassDescription parent = (BasicClassDescription)parents.getRepresentative(); 
				if (!parent.equals(existsInvNode))
					modifiedGraph.addEdge(existsNode, parent);
			}

			processedNodes.add(existsNode);
			processedNodes.add(existsInvNode);
		}

		/* Collapsing the cycles */
		return new TBoxReasonerImpl(tbox.propertyGraph, modifiedGraph);
	}

}
