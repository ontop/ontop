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
		return dag.getPropertyDag().edgeSet().size() + dag.getClassDag().edgeSet().size();
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
		Property node = (Property) reasoner.getRepresentativeFor(desc);
		for (Property source: dag.getPredecessors(node)) {

			// get the child node and its equivalent nodes
			Equivalences<Property> namedEquivalences = getEquivalences(source);
			if (!namedEquivalences.isEmpty())
				result.add(namedEquivalences);
			else 
				result.addAll(getDirectSubProperties(source));
		}

		return result;
	}
	public Set<Equivalences<BasicClassDescription>> getDirectSubClasses(BasicClassDescription desc) {
		
		LinkedHashSet<Equivalences<BasicClassDescription>> result = new LinkedHashSet<Equivalences<BasicClassDescription>>();

		// take the representative node
		BasicClassDescription node = (BasicClassDescription)reasoner.getRepresentativeFor(desc);
		for (BasicClassDescription source: dag.getPredecessors(node)) {

			// get the child node and its equivalent nodes
			Equivalences<BasicClassDescription> namedEquivalences = getEquivalences(source);
			if (!namedEquivalences.isEmpty())
				result.add(namedEquivalences);
			else 
				result.addAll(getDirectSubClasses(source));
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
		Property node = (Property)reasoner.getRepresentativeFor(desc);
		for (Property target: dag.getSuccessors(node)) {

			// get the child node and its equivalent nodes
			Equivalences<Property> namedEquivalences = getEquivalences(target);
			if (!namedEquivalences.isEmpty())
				result.add(namedEquivalences);
			else 
				result.addAll(getDirectSuperProperties(target));
		}

		return result;
	}
	public Set<Equivalences<BasicClassDescription>> getDirectSuperClasses(BasicClassDescription desc) {

		LinkedHashSet<Equivalences<BasicClassDescription>> result = new LinkedHashSet<Equivalences<BasicClassDescription>>();
		
		// take the representative node
		BasicClassDescription node = (BasicClassDescription)reasoner.getRepresentativeFor(desc);
		for (BasicClassDescription target: dag.getSuccessors(node)) {

			// get the child node and its equivalent nodes
			Equivalences<BasicClassDescription> namedEquivalences = getEquivalences(target);
			if (!namedEquivalences.isEmpty())
				result.add(namedEquivalences);
			else 
				result.addAll(getDirectSuperClasses(target));
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
		Property node = (Property)reasoner.getRepresentativeFor(desc);
		
		// reverse the dag
		DirectedGraph<Property, DefaultEdge> reversed = dag.getReversedPropertyDag();
		BreadthFirstIterator<Property, DefaultEdge>  iterator = 
					new BreadthFirstIterator<Property, DefaultEdge>(reversed, node);

		while (iterator.hasNext()) {
			Property child = iterator.next();

			// add the node and its equivalent nodes
			Equivalences<Property> sources = getEquivalences(child);
			if (!sources.isEmpty())
				result.add(sources);
		}

		return result;
	}
	public Set<Equivalences<BasicClassDescription>> getSubClasses(BasicClassDescription desc) {

		LinkedHashSet<Equivalences<BasicClassDescription>> result = new LinkedHashSet<Equivalences<BasicClassDescription>>();
		BasicClassDescription node = (BasicClassDescription)reasoner.getRepresentativeFor(desc);
		
		// reverse the dag
		DirectedGraph<BasicClassDescription, DefaultEdge> reversed = dag.getReversedClassDag();
		BreadthFirstIterator<BasicClassDescription, DefaultEdge>  iterator = 
					new BreadthFirstIterator<BasicClassDescription, DefaultEdge>(reversed, node);

		while (iterator.hasNext()) {
			BasicClassDescription child = iterator.next();

			// add the node and its equivalent nodes
			Equivalences<BasicClassDescription> sources = getEquivalences(child);
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

		Property node = (Property)reasoner.getRepresentativeFor(desc);

		BreadthFirstIterator<Property, DefaultEdge>  iterator = 
				new BreadthFirstIterator<Property, DefaultEdge>(dag.getPropertyDag(), node);

		while (iterator.hasNext()) {
			Property parent = iterator.next();

			// add the node and its equivalent nodes
			Equivalences<Property> sources = getEquivalences(parent);
			if (!sources.isEmpty())
				result.add(sources);
		}

		return result;
	}
	
	public Set<Equivalences<BasicClassDescription>> getSuperClasses(BasicClassDescription desc) {

		LinkedHashSet<Equivalences<BasicClassDescription>> result = new LinkedHashSet<Equivalences<BasicClassDescription>>();

		BasicClassDescription node = (BasicClassDescription)reasoner.getRepresentativeFor(desc);

		BreadthFirstIterator<BasicClassDescription, DefaultEdge>  iterator = 
				new BreadthFirstIterator<BasicClassDescription, DefaultEdge>(dag.getClassDag(), node);

		while (iterator.hasNext()) {
			BasicClassDescription parent = iterator.next();

			// add the node and its equivalent nodes
			Equivalences<BasicClassDescription> sources = getEquivalences(parent);
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

		for (Property vertex : dag.getPropertyDag().vertexSet()) 
			result.add(getEquivalences((Property)vertex));

		return result;
	}

	public Set<Equivalences<BasicClassDescription>> getClasses() {

		LinkedHashSet<Equivalences<BasicClassDescription>> result = new LinkedHashSet<Equivalences<BasicClassDescription>>();

		for (BasicClassDescription vertex : dag.getClassDag().vertexSet()) 
			result.add(getEquivalences(vertex));

		return result;
	}
	
}
