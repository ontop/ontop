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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.AbstractGraphIterator;
import org.jgrapht.traverse.BreadthFirstIterator;

/**
 * Allows to reason over the TBox using Named DAG
 * TEST ONLY CLASS
 */

public class TestTBoxReasonerImplOnNamedDAG  {

	private NamedDAG dag;
	private TBoxReasonerImpl reasoner;

	/**
	 * Constructor using a DAG or a named DAG
	 * @param dag DAG to be used for reasoning
	 */
	public TestTBoxReasonerImplOnNamedDAG(TBoxReasonerImpl reasoner) {
		this.reasoner = reasoner;
		this.dag = NamedDAG.getNamedDAG(reasoner);
	}

	public Set<Description> vertexSet() {
		return dag.getDag().vertexSet();
	}
	
	public TBoxReasonerImpl reasoner() {
		return reasoner;
	}
	
	public int getEdgesSize() {
		return dag.getDag().edgeSet().size();
	}
	
	/**
	 * return the direct children starting from the given node of the dag
	 * 
	 * @param desc node that we want to know the direct children
	 * @return we return a set of set of description to distinguish between
	 *         different nodes and equivalent nodes. equivalent nodes will be in
	 *         the same set of description
	 */
	public Set<Equivalences<Description>> getDirectChildren(Description desc) {
		
		LinkedHashSet<Equivalences<Description>> result = new LinkedHashSet<Equivalences<Description>>();

		// take the representative node
		Description node = reasoner.getRepresentativeFor(desc);

//		for (DefaultEdge edge : dag.getDag().incomingEdgesOf(node)) {			
//			Description source = dag.getDag().getEdgeSource(edge);
		for (Description source: dag.getPredecessors(node)) {

			// get the child node and its equivalent nodes
			Equivalences<Description> namedEquivalences = getEquivalences(source);
			if (!namedEquivalences.isEmpty())
				result.add(namedEquivalences);
			else 
				result.addAll(getNamedChildren(source));
		}

		return Collections.unmodifiableSet(result);
	}

	/*
	 *  Private method that searches for the first named children
	 */

	private Set<Equivalences<Description>> getNamedChildren(Description desc) {

		LinkedHashSet<Equivalences<Description>> result = new LinkedHashSet<Equivalences<Description>>();

		// get equivalences of the current node
		Equivalences<Description> equivalenceSet = getEquivalences(desc);
		// I want to consider also the children of the equivalent nodes
		//Set<DefaultEdge> edges = dag.getDag().incomingEdgesOf(desc);
		//for (DefaultEdge edge : edges) {
		//	Description source = dag.getDag().getEdgeSource(edge);
		for (Description source: dag.getPredecessors(desc)) {

			// I don't want to consider as children the equivalent node of
			// the current node desc
			if (equivalenceSet.contains(source)) 
				continue;

			Equivalences<Description> namedEquivalences = getEquivalences(source);
			if (!namedEquivalences.isEmpty())
				result.add(namedEquivalences);
			else 
				result.addAll(getNamedChildren(source));
		}
			
		return result;
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
	public Set<Equivalences<Description>> getDirectParents(Description desc) {

		LinkedHashSet<Equivalences<Description>> result = new LinkedHashSet<Equivalences<Description>>();
		
		// take the representative node
		Description node = reasoner.getRepresentativeFor(desc);

//		for (DefaultEdge edge : dag.getDag().outgoingEdgesOf(node)) {
//			Description target = dag.getDag().getEdgeTarget(edge);
		for (Description target: dag.getSuccessors(node)) {

			// get the child node and its equivalent nodes
			Equivalences<Description> namedEquivalences = getEquivalences(target);
			if (!namedEquivalences.isEmpty())
				result.add(namedEquivalences);
			else {
				result.addAll(getNamedParents(target));
			}
		}

		return Collections.unmodifiableSet(result);
	}

	/*
	 *  private method that search for the first named parents
	 */
	
	private Set<Equivalences<Description>> getNamedParents(Description desc) {

		LinkedHashSet<Equivalences<Description>> result = new LinkedHashSet<Equivalences<Description>>();

		// get equivalences of the current node
		Equivalences<Description> equivalenceSet = getEquivalences(desc);
		// I want to consider also the parents of the equivalent nodes

//		Set<DefaultEdge> edges = dag.getDag().outgoingEdgesOf(desc);
//		for (DefaultEdge edge : edges) {
//		Description target = dag.getDag().getEdgeTarget(edge);
		for (Description target: dag.getSuccessors(desc)) {

			// I don't want to consider as parents the equivalent node of
			// the current node desc
			if (equivalenceSet.contains(target)) 
				continue;

			Equivalences<Description> namedEquivalences = getEquivalences(target);
			if (!namedEquivalences.isEmpty())
				result.add(namedEquivalences);
			else 
				result.addAll(getNamedParents(target));
		}
		return result;
	}

	/**
	 * Traverse the graph return the descendants starting from the given node of
	 * the dag
	 * 
	 * @param desc node we want to know the descendants
	 *
	 *@return we return a set of set of description to distinguish between
	 *         different nodes and equivalent nodes. equivalent nodes will be in
	 *         the same set of description
	 */
	public Set<Equivalences<Description>> getDescendants(Description desc) {

		LinkedHashSet<Equivalences<Description>> result = new LinkedHashSet<Equivalences<Description>>();

		Description node = reasoner.getRepresentativeFor(desc);
		
		// reverse the dag
		DirectedGraph<Description, DefaultEdge> reversed = dag.getReversedDag();

		AbstractGraphIterator<Description, DefaultEdge>  iterator = 
					new BreadthFirstIterator<Description, DefaultEdge>(reversed, node);

		while (iterator.hasNext()) {
			Description child = iterator.next();

			// add the node and its equivalent nodes
			Equivalences<Description> sources = getEquivalences(child);
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
	 * @return we return a set of set of description to distinguish between
	 *         different nodes and equivalent nodes. equivalent nodes will be in
	 *         the same set of description
	 */

	public Set<Equivalences<Description>> getAncestors(Description desc) {

		LinkedHashSet<Equivalences<Description>> result = new LinkedHashSet<Equivalences<Description>>();

		Description node = reasoner.getRepresentativeFor(desc);

		AbstractGraphIterator<Description, DefaultEdge>  iterator = 
				new BreadthFirstIterator<Description, DefaultEdge>(dag.getDag(), node);

		while (iterator.hasNext()) {
			Description parent = iterator.next();

			// add the node and its equivalent nodes
			Equivalences<Description> sources = getEquivalences(parent);
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

	public Equivalences<Description> getEquivalences(Description desc) {
		
		Set<Description> equivalences = new LinkedHashSet<Description>();
			for (Description vertex : reasoner.getEquivalences(desc)) {
				if (reasoner.isNamed(vertex)) {
						equivalences.add(vertex);
				}
			}
		return new Equivalences<Description>(equivalences);
	}
	
	/**
	 * Return all the nodes in the DAG or graph
	 * 
	 * @return we return a set of set of description to distinguish between
	 *         different nodes and equivalent nodes. equivalent nodes will be in
	 *         the same set of description
	 */

	public Set<Equivalences<Description>> getNodes() {

		LinkedHashSet<Equivalences<Description>> result = new LinkedHashSet<Equivalences<Description>>();

		for (Description vertex : vertexSet()) 
				result.add(getEquivalences(vertex));

		return result;
	}

}
