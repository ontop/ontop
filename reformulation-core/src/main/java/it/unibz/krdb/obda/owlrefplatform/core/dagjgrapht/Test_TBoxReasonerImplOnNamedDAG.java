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
import it.unibz.krdb.obda.ontology.Property;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.BreadthFirstIterator;

/**
 * Allows to reason over the TBox using Named DAG
 * TEST ONLY CLASS
 */

public class Test_TBoxReasonerImplOnNamedDAG implements TBoxReasoner {

	private NamedDAG dag;
	private TBoxReasonerImpl reasoner;

	/**
	 * Constructor using a DAG or a named DAG
	 * @param dag DAG to be used for reasoning
	 */
	public Test_TBoxReasonerImplOnNamedDAG(TBoxReasonerImpl reasoner) {
		this.reasoner = reasoner;
		this.dag = NamedDAG.getNamedDAG(reasoner);
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
	public Set<Equivalences<Property>> getDirectSubProperties(Property desc) {
		
		LinkedHashSet<Equivalences<Property>> result = new LinkedHashSet<Equivalences<Property>>();

		// take the representative node
		Description node = reasoner.getRepresentativeFor(desc);
		for (Description source: dag.getPredecessors(node)) {

			// get the child node and its equivalent nodes
			Equivalences<Property> namedEquivalences = getEquivalences((Property)source);
			if (!namedEquivalences.isEmpty())
				result.add(namedEquivalences);
			else 
				result.addAll(getDirectSubProperties((Property)source));
		}

		return result;
	}
	public Set<Equivalences<BasicClassDescription>> getDirectSubClasses(BasicClassDescription desc) {
		
		LinkedHashSet<Equivalences<BasicClassDescription>> result = new LinkedHashSet<Equivalences<BasicClassDescription>>();

		// take the representative node
		Description node = reasoner.getRepresentativeFor(desc);
		for (Description source: dag.getPredecessors(node)) {

			// get the child node and its equivalent nodes
			Equivalences<BasicClassDescription> namedEquivalences = getEquivalences((BasicClassDescription)source);
			if (!namedEquivalences.isEmpty())
				result.add(namedEquivalences);
			else 
				result.addAll(getDirectSubClasses((BasicClassDescription)source));
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
	public Set<Equivalences<Property>> getDirectSuperProperties(Property desc) {

		LinkedHashSet<Equivalences<Property>> result = new LinkedHashSet<Equivalences<Property>>();
		
		// take the representative node
		Description node = reasoner.getRepresentativeFor(desc);
		for (Description target: dag.getSuccessors(node)) {

			// get the child node and its equivalent nodes
			Equivalences<Property> namedEquivalences = getEquivalences((Property)target);
			if (!namedEquivalences.isEmpty())
				result.add(namedEquivalences);
			else 
				result.addAll(getDirectSuperProperties((Property)target));
		}

		return result;
	}
	public Set<Equivalences<BasicClassDescription>> getDirectSuperClasses(BasicClassDescription desc) {

		LinkedHashSet<Equivalences<BasicClassDescription>> result = new LinkedHashSet<Equivalences<BasicClassDescription>>();
		
		// take the representative node
		Description node = reasoner.getRepresentativeFor(desc);
		for (Description target: dag.getSuccessors(node)) {

			// get the child node and its equivalent nodes
			Equivalences<BasicClassDescription> namedEquivalences = getEquivalences((BasicClassDescription)target);
			if (!namedEquivalences.isEmpty())
				result.add(namedEquivalences);
			else 
				result.addAll(getDirectSuperClasses((BasicClassDescription)target));
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
	public Set<Equivalences<Property>> getSubProperties(Property desc) {

		LinkedHashSet<Equivalences<Property>> result = new LinkedHashSet<Equivalences<Property>>();
		Description node = reasoner.getRepresentativeFor(desc);
		
		// reverse the dag
		DirectedGraph<Description, DefaultEdge> reversed = dag.getReversedDag();
		BreadthFirstIterator<Description, DefaultEdge>  iterator = 
					new BreadthFirstIterator<Description, DefaultEdge>(reversed, node);

		while (iterator.hasNext()) {
			Description child = iterator.next();

			// add the node and its equivalent nodes
			Equivalences<Property> sources = getEquivalences((Property)child);
			if (!sources.isEmpty())
				result.add(sources);
		}

		return result;
	}
	public Set<Equivalences<BasicClassDescription>> getSubClasses(BasicClassDescription desc) {

		LinkedHashSet<Equivalences<BasicClassDescription>> result = new LinkedHashSet<Equivalences<BasicClassDescription>>();
		Description node = reasoner.getRepresentativeFor(desc);
		
		// reverse the dag
		DirectedGraph<Description, DefaultEdge> reversed = dag.getReversedDag();
		BreadthFirstIterator<Description, DefaultEdge>  iterator = 
					new BreadthFirstIterator<Description, DefaultEdge>(reversed, node);

		while (iterator.hasNext()) {
			Description child = iterator.next();

			// add the node and its equivalent nodes
			Equivalences<BasicClassDescription> sources = getEquivalences((BasicClassDescription)child);
			if (!sources.isEmpty())
				result.add(sources);
		}
		
		return result;
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

	public Set<Equivalences<Property>> getSuperProperties(Property desc) {

		LinkedHashSet<Equivalences<Property>> result = new LinkedHashSet<Equivalences<Property>>();

		Description node = reasoner.getRepresentativeFor(desc);

		BreadthFirstIterator<Description, DefaultEdge>  iterator = 
				new BreadthFirstIterator<Description, DefaultEdge>(dag.getDag(), node);

		while (iterator.hasNext()) {
			Description parent = iterator.next();

			// add the node and its equivalent nodes
			Equivalences<Property> sources = getEquivalences((Property)parent);
			if (!sources.isEmpty())
				result.add(sources);
		}

		return result;
	}
	
	public Set<Equivalences<BasicClassDescription>> getSuperClasses(BasicClassDescription desc) {

		LinkedHashSet<Equivalences<BasicClassDescription>> result = new LinkedHashSet<Equivalences<BasicClassDescription>>();

		Description node = reasoner.getRepresentativeFor(desc);

		BreadthFirstIterator<Description, DefaultEdge>  iterator = 
				new BreadthFirstIterator<Description, DefaultEdge>(dag.getDag(), node);

		while (iterator.hasNext()) {
			Description parent = iterator.next();

			// add the node and its equivalent nodes
			Equivalences<BasicClassDescription> sources = getEquivalences((BasicClassDescription)parent);
			if (!sources.isEmpty())
				result.add(sources);
		}

		return result;
	}


	/**
	 * Return the equivalences starting from the given node of the dag
	 * 
	 * @param desc node we want to know the ancestors
	 *            
	 * @return we return a set of description with equivalent nodes 
	 */

	public Equivalences<BasicClassDescription> getEquivalences(BasicClassDescription desc) {
		
		Set<BasicClassDescription> equivalences = new LinkedHashSet<BasicClassDescription>();
			for (BasicClassDescription vertex : reasoner.getEquivalences(desc)) {
				if (reasoner.isNamed(vertex)) 
						equivalences.add(vertex);
			}
			if (!equivalences.isEmpty())
				return new Equivalences<BasicClassDescription>(equivalences, reasoner.getEquivalences(desc).getRepresentative());
			
			return new Equivalences<BasicClassDescription>(equivalences);
	}
	public Equivalences<Property> getEquivalences(Property desc) {
		
		Set<Property> equivalences = new LinkedHashSet<Property>();
			for (Property vertex : reasoner.getEquivalences(desc)) {
				if (reasoner.isNamed(vertex)) 
						equivalences.add(vertex);
			}
			if (!equivalences.isEmpty())
				return new Equivalences<Property>(equivalences, reasoner.getEquivalences(desc).getRepresentative());
			
			return new Equivalences<Property>(equivalences);
	}
	
	/**
	 * Return all the nodes in the DAG or graph
	 * 
	 * @return we return a set of set of description to distinguish between
	 *         different nodes and equivalent nodes. equivalent nodes will be in
	 *         the same set of description
	 */
	
	public Set<Equivalences<Property>> getProperties() {

		LinkedHashSet<Equivalences<Property>> result = new LinkedHashSet<Equivalences<Property>>();

		for (Description vertex : dag.getDag().vertexSet()) 
			if (vertex instanceof Property)
				result.add(getEquivalences((Property)vertex));

		return result;
	}

	public Set<Equivalences<BasicClassDescription>> getClasses() {

		LinkedHashSet<Equivalences<BasicClassDescription>> result = new LinkedHashSet<Equivalences<BasicClassDescription>>();

		for (Description vertex : dag.getDag().vertexSet()) 
			if (vertex instanceof BasicClassDescription)
				result.add(getEquivalences((BasicClassDescription)vertex));

		return result;
	}
	
}
