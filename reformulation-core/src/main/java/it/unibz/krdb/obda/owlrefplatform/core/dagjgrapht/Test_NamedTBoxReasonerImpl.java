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

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Simulates the NamedDAG over TBoxReasonerImpl 
 * 
 * THIS CLASS IS FOR TESTS ONLY
 * 
 */

public class Test_NamedTBoxReasonerImpl {

	private TBoxReasonerImpl reasoner;

	/**
	 * Constructor using a DAG or a named DAG
	 * @param dag DAG to be used for reasoning
	 */
	public Test_NamedTBoxReasonerImpl(TBoxReasonerImpl reasoner) {
		this.reasoner = reasoner;
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

		for (Equivalences<Description> e : reasoner.getDirectChildren(desc)) {
			Description child = e.getRepresentative();
			
			// get the child node and its equivalent nodes
			Equivalences<Description> namedEquivalences = getEquivalences(child);
			if (!namedEquivalences.isEmpty())
				result.add(namedEquivalences);
			else 
				result.addAll(getDirectChildren(child)); // recursive call if the child is not empty
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

		for (Equivalences<Description> e : reasoner.getDirectParents(desc)) {
			Description parent = e.getRepresentative();
			
			// get the child node and its equivalent nodes
			Equivalences<Description> namedEquivalences = getEquivalences(parent);
			if (!namedEquivalences.isEmpty())
				result.add(namedEquivalences);
			else 
				result.addAll(getDirectParents(parent)); // recursive call if the parent is not named
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

		if (desc instanceof Property) {
			for (Equivalences<Property> e : reasoner.getSubProperties((Property)desc)) {
				Equivalences<Description> nodes = getEquivalences((Description)e.getRepresentative());
				if (!nodes.isEmpty())
					result.add(nodes);			
			}
		}
		else {
			for (Equivalences<BasicClassDescription> e : reasoner.getSubClasses((BasicClassDescription)desc)) {
				Equivalences<Description> nodes = getEquivalences((Description)e.getRepresentative());
				if (!nodes.isEmpty())
					result.add(nodes);			
			}
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

	public Set<Equivalences<Description>> getAncestors(Description desc) {

		LinkedHashSet<Equivalences<Description>> result = new LinkedHashSet<Equivalences<Description>>();

		if (desc instanceof Property) {
			for (Equivalences<Property> e : reasoner.getSuperProperties((Property)desc)) {
				Equivalences<Description> nodes = getEquivalences((Description)e.getRepresentative());
				if (!nodes.isEmpty())
					result.add(nodes);			
			}
		}
		else {
			for (Equivalences<BasicClassDescription> e : reasoner.getSuperClasses((BasicClassDescription)desc)) {
				Equivalences<Description> nodes = getEquivalences((Description)e.getRepresentative());
				if (!nodes.isEmpty())
					result.add(nodes);			
			}
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

	public Equivalences<Property> getEquivalences(Property desc) {

		Set<Property> equivalences = new LinkedHashSet<Property>();
			for (Property vertex : reasoner.getEquivalences(desc)) {
				if (isNamed(vertex)) 
					equivalences.add(vertex);
			}
		if (!equivalences.isEmpty())
			return new Equivalences<Property>(equivalences, reasoner.getEquivalences(desc).getRepresentative());
		
		return new Equivalences<Property>(equivalences);
	}
	public Equivalences<BasicClassDescription> getEquivalences(BasicClassDescription desc) {

		Set<BasicClassDescription> equivalences = new LinkedHashSet<BasicClassDescription>();
			for (BasicClassDescription vertex : reasoner.getEquivalences(desc)) {
				if (isNamed(vertex)) 
					equivalences.add(vertex);
			}
		if (!equivalences.isEmpty())
			return new Equivalences<BasicClassDescription>(equivalences, reasoner.getEquivalences(desc).getRepresentative());
		
		return new Equivalences<BasicClassDescription>(equivalences);
	}
	
	public Equivalences<Description> getEquivalences(Description desc) {

		Set<Description> equivalences = new LinkedHashSet<Description>();
			for (Description vertex : reasoner.getEquivalences(desc)) {
				if (isNamed(vertex)) 
					equivalences.add(vertex);
			}
		if (!equivalences.isEmpty())
			return new Equivalences<Description>(equivalences, reasoner.getEquivalences(desc).getRepresentative());
		
		return new Equivalences<Description>(equivalences);
	}
	
	public boolean isNamed(Description vertex) {
		return reasoner.isNamed(vertex);
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

		for (Equivalences<Description> e : reasoner.getNodes()) {
			Equivalences<Description> nodes = getEquivalences(e.getRepresentative());
			if (!nodes.isEmpty())
				result.add(nodes);			
		}

		return result;
	}

}
