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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.AbstractGraphIterator;
import org.jgrapht.traverse.BreadthFirstIterator;

/**
 * Allows to reason over the TBox using  DAG or graph
 * 
 */

public class TBoxReasonerImpl implements TBoxReasoner {

	private DAGImpl dag;
	private DefaultDirectedGraph<Description,DefaultEdge> graph; // test only

	/**
	 * Constructor using a DAG or a named DAG
	 * @param dag DAG to be used for reasoning
	 */
	private TBoxReasonerImpl(DAGImpl dag) {
		this.dag = dag;
	}
	
	public TBoxReasonerImpl(Ontology onto) {
		graph = DAGBuilder.getGraph(onto, false);
		
		this.dag = DAGBuilder.getDAG(graph);
	}

	public Description getRepresentativeFor(EquivalenceClass<Description> nodes) {
		Description first = nodes.iterator().next();
		return dag.getRepresentativeFor(first);
	}

	public boolean hasReplacementFor(Description v) {
		return dag.hasReplacementFor(v);
	}
	
	@Deprecated
	public Description getReplacementFor(Description v) {
		return dag.getReplacementFor(v);
	}
	
	public DAGImpl getDag() {
		return dag;
	}

	@Deprecated
	public DefaultDirectedGraph<Description,DefaultEdge> getGraph() {
		return graph;
	}
	
	@Deprecated // test only
	public int edges() {
		return dag.getDag().edgeSet().size();
	}

	public Set<Property> getPropertyNames() {
		return dag.getPropertyNames();
	}

	public Set<OClass> getClassNames() {
		return dag.getClassNames();
	}
	
	public Description getNode(Description node) {
		return dag.getNode(node);
	}
	
	@Deprecated
	public Set<Description> vertexSet() {
		return dag.getDag().vertexSet();
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
		Description node = dag.getRepresentativeFor(desc);

		for (DefaultEdge edge : dag.getDag().incomingEdgesOf(node)) {	
			Description source = dag.getDag().getEdgeSource(edge);

			// get the child node and its equivalent nodes
			EquivalenceClass<Description> equivalences = getEquivalences(source);
			if (!equivalences.isEmpty())
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
		Description node = dag.getRepresentativeFor(desc);

		for (DefaultEdge edge : dag.getDag().outgoingEdgesOf(node)) {
			Description target = dag.getDag().getEdgeTarget(edge);

			// get the child node and its equivalent nodes
			EquivalenceClass<Description> equivalences = getEquivalences(target);
			if (!equivalences.isEmpty())
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

		Description node = dag.getRepresentativeFor(desc);
		
		// reverse the dag
		DirectedGraph<Description, DefaultEdge> reversed = dag.getReversedDag();

		AbstractGraphIterator<Description, DefaultEdge>  iterator = 
					new BreadthFirstIterator<Description, DefaultEdge>(reversed, node);

		// I don't want to consider the current node
		iterator.next();

		Description startNode = desc;
		EquivalenceClass<Description> sourcesStart = getEquivalences(startNode);
		Set<Description> sourcesStartnoNode = new HashSet<Description>();
		for (Description equivalent : sourcesStart) {
			if (equivalent.equals(startNode))
				continue;
			sourcesStartnoNode.add(equivalent);
		}

		if (!sourcesStartnoNode.isEmpty())
			result.add(new EquivalenceClass<Description>(sourcesStartnoNode));

		// iterate over the subsequent nodes, they are all descendant of desc
		while (iterator.hasNext()) {
			Description child = iterator.next();

			// add the node and its equivalent nodes
			EquivalenceClass<Description> sources = getEquivalences(child);
			if (!sources.isEmpty())
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

		Description node = dag.getRepresentativeFor(desc);

		AbstractGraphIterator<Description, DefaultEdge>  iterator = 
				new BreadthFirstIterator<Description, DefaultEdge>(dag.getDag(), node);

		// I don't want to consider the current node
		iterator.next();

		Description startNode = desc;
		EquivalenceClass<Description> sourcesStart = getEquivalences(startNode);
		Set<Description> sourcesStartnoNode = new HashSet<Description>();
		for (Description equivalent : sourcesStart) {
			if (equivalent.equals(startNode))
				continue;
			sourcesStartnoNode.add(equivalent);
		}

		if (!sourcesStartnoNode.isEmpty())
			result.add(new EquivalenceClass<Description>(sourcesStartnoNode));

		// iterate over the subsequent nodes, they are all ancestor of desc
		while (iterator.hasNext()) {
			Description parent = iterator.next();

			// add the node and its equivalent nodes
			EquivalenceClass<Description> sources = getEquivalences(parent);
			if (!sources.isEmpty())
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
		return dag.getEquivalenceClass(desc);
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

		for (Description vertex : dag.getDag().vertexSet()) 
				result.add(getEquivalences(vertex));

		return result;
	}


	public static TBoxReasonerImpl getChainReasoner2(Ontology onto) {
		DefaultDirectedGraph<Description,DefaultEdge> graph = DAGBuilder.getGraph(onto, true);
		
		DAGImpl dag = DAGBuilder.getDAG(graph);
		
		return new TBoxReasonerImpl(dag);		
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

			{
			SimpleDirectedGraph <Description,DefaultEdge> dag = tbox.dag.getDag();

			// clone all the vertex and edges from dag
			for (Description v : dag.vertexSet()) {
				modifiedGraph.addVertex(v);

			}
			for (DefaultEdge e : dag.edgeSet()) {
				Description s = dag.getEdgeSource(e);
				Description t = dag.getEdgeTarget(e);
				modifiedGraph.addEdge(s, t, e);
			}
			}
			Collection<Description> nodes = new HashSet<Description>(tbox.dag.getDag().vertexSet());
			OntologyFactory fac = OntologyFactoryImpl.getInstance();
			HashSet<Description> processedNodes = new HashSet<Description>();
			for (Description node : nodes) {
				if (!(node instanceof PropertySomeRestriction)
						|| processedNodes.contains(node)) {
					continue;
				}

				/*
				 * Adding a cycle between exists R and exists R- for each R.
				 */

				PropertySomeRestriction existsR = (PropertySomeRestriction) node;
				PropertySomeRestriction existsRin = fac
						.createPropertySomeRestriction(existsR.getPredicate(),
								!existsR.isInverse());
				Description existsNode = node;
				Description existsInvNode = tbox.dag.getNode(existsRin);
				Set<EquivalenceClass<Description>> childrenExist 
						= new HashSet<EquivalenceClass<Description>>(
									tbox.getDirectChildren(existsNode));
				Set<EquivalenceClass<Description>> childrenExistInv 
						= new HashSet<EquivalenceClass<Description>>(
									tbox.getDirectChildren(existsInvNode));

				for (EquivalenceClass<Description> children : childrenExist) {
					// for(Description child:children){
					// DAGOperations.addParentEdge(child, existsInvNode);
					Description firstChild = children.iterator().next();
					Description child = tbox.dag.getRepresentativeFor(firstChild);
					if (!child.equals(existsInvNode))
						modifiedGraph.addEdge(child, existsInvNode);

					// }
				}
				for (EquivalenceClass<Description> children : childrenExistInv) {
					// for(Description child:children){
					// DAGOperations.addParentEdge(child, existsNode);
					Description firstChild = children.iterator().next();
					Description child = tbox.dag.getRepresentativeFor(firstChild);
					if (!child.equals(existsNode))
						modifiedGraph.addEdge(child, existsNode);

					// }
				}

				Set<EquivalenceClass<Description>> parentExist 
						= new HashSet<EquivalenceClass<Description>>(
								tbox.getDirectParents(existsNode));
				Set<EquivalenceClass<Description>> parentsExistInv 
						= new HashSet<EquivalenceClass<Description>>(
								tbox.getDirectParents(existsInvNode));

				for (EquivalenceClass<Description> parents : parentExist) {
					Description firstParent = parents.iterator().next();
					Description parent = tbox.dag.getRepresentativeFor(firstParent);
					if (!parent.equals(existsInvNode))
						modifiedGraph.addEdge(existsInvNode, parent);

					// }
				}

				for (EquivalenceClass<Description> parents : parentsExistInv) {
					Description firstParent = parents.iterator().next();
					Description parent = tbox.dag.getRepresentativeFor(firstParent);
					if (!parent.equals(existsInvNode))
						modifiedGraph.addEdge(existsNode, parent);

					// }
				}

				processedNodes.add(existsInvNode);
				processedNodes.add(existsNode);
			}

			/* Collapsing the cycles */
			return new TBoxReasonerImpl(DAGBuilder.getDAG(modifiedGraph));
	}

}
