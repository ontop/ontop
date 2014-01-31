/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

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

	private final DefaultDirectedGraph<Description,DefaultEdge> graph; // test only

	private final SimpleDirectedGraph <Description,DefaultEdge> dag;
	
	// maps descriptions to their equivalence classes (and their representatives)
	private final Map<Description, EquivalenceClass<Description>> equivalencesClasses; 
	
	private Set<OClass> classNames;
	private Set<Property> propertyNames;
	
	
	public TBoxReasonerImpl(Ontology onto) {
		this(DAGBuilder.getGraph(onto, false));
	}

	private TBoxReasonerImpl(DefaultDirectedGraph<Description,DefaultEdge> graph) {
		this.graph = graph;
		
		equivalencesClasses = new HashMap<Description, EquivalenceClass<Description>>();
		this.dag = DAGBuilder.getDAG(graph, equivalencesClasses);
	}


	@Override
	public String toString() {
		return dag.toString() + 
				//"\n\nReplacements\n" + replacements.toString() + 
				"\n\nEquivalenceMap\n" + equivalencesClasses;
	}
	
	
	public Description getRepresentativeFor(Description v) {
		EquivalenceClass<Description> e = equivalencesClasses.get(v);
		if (e != null)
			return e.getRepresentative();
		return null;
	}
	
	public Description getRepresentativeFor(EquivalenceClass<Description> nodes) {
		Description first = nodes.iterator().next();
		return getRepresentativeFor(first);
	}
	
	public boolean isCanonicalRepresentative(Description v) {
		//return (replacements.get(v) == null);
		EquivalenceClass<Description> e = equivalencesClasses.get(v);
		return e.getRepresentative().equals(v);
	}

	
	
	
	@Deprecated
	public DefaultDirectedGraph<Description,DefaultEdge> getGraph() {
		return graph;
	}
	
	@Deprecated // test only
	public int edges() {
		return dag.edgeSet().size();
	}

	/**
	 * Allows to have all named roles in the DAG even the equivalent named roles
	 * @return  set of all property (not inverse) in the DAG
	 */
	public Set<Property> getPropertyNames() {
		if (propertyNames == null) {
			propertyNames = new LinkedHashSet<Property> ();
			for (Description v: dag.vertexSet()) 
				if (v instanceof Property)
					for (Description r : getEquivalences(v)) {
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
			 for (Description v: dag.vertexSet())
				if (v instanceof OClass) 
					for (Description e : getEquivalences(v))
						if (e instanceof OClass)
							classNames.add((OClass)e);
		}
		return classNames;
	}


	public boolean isNamed(Description node) {
		return getClassNames().contains(node) || getPropertyNames().contains(node);
	}

	/**
	 * return the direct children starting from the given node of the dag
	 * 
	 * @param desc node that we want to know the direct children
	 * @return we return a set of set of description to distinguish between
	 *         different nodes and equivalent nodes. equivalent nodes will be in
	 *         the same set of description
	 */
	@Override
	public Set<EquivalenceClass<Description>> getDirectChildren(Description desc) {
		
		LinkedHashSet<EquivalenceClass<Description>> result = new LinkedHashSet<EquivalenceClass<Description>>();

		// take the representative node
		Description node = getRepresentativeFor(desc);

		for (DefaultEdge edge : dag.incomingEdgesOf(node)) {	
			Description source = dag.getEdgeSource(edge);

			// get the child node and its equivalent nodes
			EquivalenceClass<Description> equivalences = getEquivalences(source);
			result.add(equivalences);
		}

		return Collections.unmodifiableSet(result);
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
	public Set<EquivalenceClass<Description>> getDirectParents(Description desc) {

		LinkedHashSet<EquivalenceClass<Description>> result = new LinkedHashSet<EquivalenceClass<Description>>();
		
		// take the representative node
		Description node = getRepresentativeFor(desc);

		for (DefaultEdge edge : dag.outgoingEdgesOf(node)) {
			Description target = dag.getEdgeTarget(edge);

			// get the child node and its equivalent nodes
			EquivalenceClass<Description> equivalences = getEquivalences(target);
			result.add(equivalences);
		}

		return Collections.unmodifiableSet(result);
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
	public Set<EquivalenceClass<Description>> getDescendants(Description desc) {

		LinkedHashSet<EquivalenceClass<Description>> result = new LinkedHashSet<EquivalenceClass<Description>>();

		Description node = getRepresentativeFor(desc);
		
		// reverse the dag
		DirectedGraph<Description, DefaultEdge> reversed = getReversedDag();

		AbstractGraphIterator<Description, DefaultEdge>  iterator = 
					new BreadthFirstIterator<Description, DefaultEdge>(reversed, node);

		while (iterator.hasNext()) {
			Description child = iterator.next();

			// add the node and its equivalent nodes
			EquivalenceClass<Description> sources = getEquivalences(child);
			result.add(sources);
		}

		// add each of them to the result
		return Collections.unmodifiableSet(result);
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
	public Set<EquivalenceClass<Description>> getAncestors(Description desc) {

		LinkedHashSet<EquivalenceClass<Description>> result = new LinkedHashSet<EquivalenceClass<Description>>();

		Description node = getRepresentativeFor(desc);

		AbstractGraphIterator<Description, DefaultEdge>  iterator = 
				new BreadthFirstIterator<Description, DefaultEdge>(dag, node);

		while (iterator.hasNext()) {
			Description parent = iterator.next();

			// add the node and its equivalent nodes
			EquivalenceClass<Description> sources = getEquivalences(parent);
			result.add(sources);
		}

		// add each of them to the result
		return Collections.unmodifiableSet(result);
	}

	/**
	 * Return the equivalences starting from the given node of the dag
	 * 
	 * @param desc node we want to know the ancestors
	 *            
	 * @return we return a set of description with equivalent nodes 
	 */

	@Override
	public EquivalenceClass<Description> getEquivalences(Description desc) {
		EquivalenceClass<Description> c = equivalencesClasses.get(desc);
		return c;
	}
	
	
	/**
	 * Return all the nodes in the DAG or graph
	 * 
	 * @return we return a set of set of description to distinguish between
	 *         different nodes and equivalent nodes. equivalent nodes will be in
	 *         the same set of description
	 */

	@Override
	public Set<EquivalenceClass<Description>> getNodes() {

		LinkedHashSet<EquivalenceClass<Description>> result = new LinkedHashSet<EquivalenceClass<Description>>();

		for (Description vertex : dag.vertexSet()) 
			result.add(getEquivalences(vertex));

		return result;
	}

	// INTERNAL DETAILS
	
	
	DirectedGraph<Description, DefaultEdge> getReversedDag() {
		DirectedGraph<Description, DefaultEdge> reversed =
				new EdgeReversedGraph<Description, DefaultEdge>(dag);
		return reversed;
	}
	

	@Deprecated
	public SimpleDirectedGraph<Description, DefaultEdge> getDag() {
		return dag;
	}
	

	public static TBoxReasonerImpl getChainReasoner2(Ontology onto) {
		
		return new TBoxReasonerImpl((DAGBuilder.getGraph(onto, true)));		
	}
	
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
		DefaultDirectedGraph<Description,DefaultEdge> modifiedGraph = 
				new  DefaultDirectedGraph<Description,DefaultEdge>(DefaultEdge.class);

		// clone all the vertex and edges from dag
		for (Description v : tbox.dag.vertexSet()) {
			modifiedGraph.addVertex(v);

		}
		for (DefaultEdge e : tbox.dag.edgeSet()) {
			Description s = tbox.dag.getEdgeSource(e);
			Description t = tbox.dag.getEdgeTarget(e);
			modifiedGraph.addEdge(s, t, e);
		}

		Collection<Description> nodes = new HashSet<Description>(tbox.dag.vertexSet());
		OntologyFactory fac = OntologyFactoryImpl.getInstance();
		HashSet<Description> processedNodes = new HashSet<Description>();
		
		for (Description node : nodes) {
			if (!(node instanceof PropertySomeRestriction) || processedNodes.contains(node)) 
				continue;

			/*
			 * Adding a cycle between exists R and exists R- for each R.
			 */

			PropertySomeRestriction existsNode = (PropertySomeRestriction) node;
			Description existsInvNode = tbox.getRepresentativeFor(
						fac.createPropertySomeRestriction(existsNode.getPredicate(), !existsNode.isInverse()));
			
			for (EquivalenceClass<Description> children : tbox.getDirectChildren(existsNode)) {
				Description child = children.getRepresentative(); 
				if (!child.equals(existsInvNode))
					modifiedGraph.addEdge(child, existsInvNode);
			}
			for (EquivalenceClass<Description> children : tbox.getDirectChildren(existsInvNode)) {
				Description child = children.getRepresentative(); 
				if (!child.equals(existsNode))
					modifiedGraph.addEdge(child, existsNode);
			}

			for (EquivalenceClass<Description> parents : tbox.getDirectParents(existsNode)) {
				Description parent = parents.getRepresentative(); 
				if (!parent.equals(existsInvNode))
					modifiedGraph.addEdge(existsInvNode, parent);
			}

			for (EquivalenceClass<Description> parents : tbox.getDirectParents(existsInvNode)) {
				Description parent = parents.getRepresentative(); 
				if (!parent.equals(existsInvNode))
					modifiedGraph.addEdge(existsNode, parent);
			}

			processedNodes.add(existsNode);
			processedNodes.add(existsInvNode);
		}

		/* Collapsing the cycles */
		return new TBoxReasonerImpl(modifiedGraph);
	}

}
