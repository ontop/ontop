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

public class Test_NamedTBoxReasonerImpl implements TBoxReasoner {

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
	
	@Override
	public Set<Equivalences<Property>> getDirectSubProperties(Property desc) {		
		LinkedHashSet<Equivalences<Property>> result = new LinkedHashSet<Equivalences<Property>>();

		for (Equivalences<Property> e : reasoner.getDirectSubProperties(desc)) {
			Property child = e.getRepresentative();
			
			// get the child node and its equivalent nodes
			Equivalences<Property> namedEquivalences = getEquivalences(child);
			if (!namedEquivalences.isEmpty())
				result.add(namedEquivalences);
			else 
				result.addAll(getDirectSubProperties(child)); // recursive call if the child is not empty
		}

		return result;
	}

	@Override
	public Set<Equivalences<BasicClassDescription>> getDirectSubClasses(BasicClassDescription desc) {
		LinkedHashSet<Equivalences<BasicClassDescription>> result = new LinkedHashSet<Equivalences<BasicClassDescription>>();

		for (Equivalences<BasicClassDescription> e : reasoner.getDirectSubClasses(desc)) {
			BasicClassDescription child = e.getRepresentative();
			
			// get the child node and its equivalent nodes
			Equivalences<BasicClassDescription> namedEquivalences = getEquivalences(child);
			if (!namedEquivalences.isEmpty())
				result.add(namedEquivalences);
			else 
				result.addAll(getDirectSubClasses(child)); // recursive call if the child is not empty
		}
		return result;
	}

	
	public Set<Equivalences<Description>> getDirectChildren(Description desc) {
		LinkedHashSet<Equivalences<Description>> result = new LinkedHashSet<Equivalences<Description>>();

		if (desc instanceof Property) {
			for (Equivalences<Property> e : reasoner.getDirectSubProperties((Property)desc)) {
				Description child = e.getRepresentative();
				
				// get the child node and its equivalent nodes
				Equivalences<Description> namedEquivalences = getEquivalences(child);
				if (!namedEquivalences.isEmpty())
					result.add(namedEquivalences);
				else 
					result.addAll(getDirectChildren(child)); // recursive call if the child is not empty
			}
		}
		else {
			for (Equivalences<BasicClassDescription> e : reasoner.getDirectSubClasses((BasicClassDescription)desc)) {
				Description child = e.getRepresentative();
				
				// get the child node and its equivalent nodes
				Equivalences<Description> namedEquivalences = getEquivalences(child);
				if (!namedEquivalences.isEmpty())
					result.add(namedEquivalences);
				else 
					result.addAll(getDirectChildren(child)); // recursive call if the child is not empty
			}			
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

	@Override
	public Set<Equivalences<Property>> getDirectSuperProperties(Property desc) {
		LinkedHashSet<Equivalences<Property>> result = new LinkedHashSet<Equivalences<Property>>();

		for (Equivalences<Property> e : reasoner.getDirectSuperProperties((Property)desc)) {
			Property parent = e.getRepresentative();
			
			// get the child node and its equivalent nodes
			Equivalences<Property> namedEquivalences = getEquivalences(parent);
			if (!namedEquivalences.isEmpty())
				result.add(namedEquivalences);
			else 
				result.addAll(getDirectSuperProperties(parent)); // recursive call if the parent is not named
		}
		return result;
	}

	@Override
	public Set<Equivalences<BasicClassDescription>> getDirectSuperClasses(BasicClassDescription desc) {
		LinkedHashSet<Equivalences<BasicClassDescription>> result = new LinkedHashSet<Equivalences<BasicClassDescription>>();
		
		for (Equivalences<BasicClassDescription> e : reasoner.getDirectSuperClasses(desc)) {
			BasicClassDescription parent = e.getRepresentative();
			
			// get the child node and its equivalent nodes
			Equivalences<BasicClassDescription> namedEquivalences = getEquivalences(parent);
			if (!namedEquivalences.isEmpty())
				result.add(namedEquivalences);
			else 
				result.addAll(getDirectSuperClasses(parent)); // recursive call if the parent is not named
		}
		return null;
	}


	public Set<Equivalences<Description>> getDirectParents(Description desc) {

		LinkedHashSet<Equivalences<Description>> result = new LinkedHashSet<Equivalences<Description>>();

		if (desc instanceof Property) {
			for (Equivalences<Property> e : reasoner.getDirectSuperProperties((Property)desc)) {
				Description parent = e.getRepresentative();
				
				// get the child node and its equivalent nodes
				Equivalences<Description> namedEquivalences = getEquivalences(parent);
				if (!namedEquivalences.isEmpty())
					result.add(namedEquivalences);
				else 
					result.addAll(getDirectParents(parent)); // recursive call if the parent is not named
			}
		}
		else {
			for (Equivalences<BasicClassDescription> e : reasoner.getDirectSuperClasses((BasicClassDescription)desc)) {
				Description parent = e.getRepresentative();
				
				// get the child node and its equivalent nodes
				Equivalences<Description> namedEquivalences = getEquivalences(parent);
				if (!namedEquivalences.isEmpty())
					result.add(namedEquivalences);
				else 
					result.addAll(getDirectParents(parent)); // recursive call if the parent is not named
			}			
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
	@Override
	public Set<Equivalences<Property>> getSubProperties(Property desc) {
		LinkedHashSet<Equivalences<Property>> result = new LinkedHashSet<Equivalences<Property>>();
		
		for (Equivalences<Property> e : reasoner.getSubProperties(desc)) {
			Equivalences<Property> nodes = getEquivalences(e.getRepresentative());
			if (!nodes.isEmpty())
				result.add(nodes);			
		}
		return result;
	}

	@Override
	public Set<Equivalences<BasicClassDescription>> getSubClasses(BasicClassDescription desc) {
		LinkedHashSet<Equivalences<BasicClassDescription>> result = new LinkedHashSet<Equivalences<BasicClassDescription>>();
		
		for (Equivalences<BasicClassDescription> e : reasoner.getSubClasses(desc)) {
			Equivalences<BasicClassDescription> nodes = getEquivalences(e.getRepresentative());
			if (!nodes.isEmpty())
				result.add(nodes);			
		}
		return result;
	}

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
	@Override
	public Set<Equivalences<Property>> getSuperProperties(Property desc) {
		LinkedHashSet<Equivalences<Property>> result = new LinkedHashSet<Equivalences<Property>>();
		
		for (Equivalences<Property> e : reasoner.getSuperProperties(desc)) {
			Equivalences<Property> nodes = getEquivalences(e.getRepresentative());
			if (!nodes.isEmpty())
				result.add(nodes);			
		}

		return result;
	}

	@Override
	public Set<Equivalences<BasicClassDescription>> getSuperClasses(BasicClassDescription desc) {
		LinkedHashSet<Equivalences<BasicClassDescription>> result = new LinkedHashSet<Equivalences<BasicClassDescription>>();

		for (Equivalences<BasicClassDescription> e : reasoner.getSuperClasses(desc)) {
			Equivalences<BasicClassDescription> nodes = getEquivalences(e.getRepresentative());
			if (!nodes.isEmpty())
				result.add(nodes);			
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

		for (Equivalences<Property> e : reasoner.getProperties()) {
			Equivalences<Description> nodes = getEquivalences((Description)e.getRepresentative());
			if (!nodes.isEmpty())
				result.add(nodes);			
		}
		for (Equivalences<BasicClassDescription> e : reasoner.getClasses()) {
			Equivalences<Description> nodes = getEquivalences((Description)e.getRepresentative());
			if (!nodes.isEmpty())
				result.add(nodes);			
		}

		return result;
	}


	@Override
	public Set<Equivalences<Property>> getProperties() {
		LinkedHashSet<Equivalences<Property>> result = new LinkedHashSet<Equivalences<Property>>();
		
		for (Equivalences<Property> e : reasoner.getProperties()) {
			Equivalences<Property> nodes = getEquivalences(e.getRepresentative());
			if (!nodes.isEmpty())
				result.add(nodes);			
		}
		return result;
	}

	@Override
	public Set<Equivalences<BasicClassDescription>> getClasses() {
		LinkedHashSet<Equivalences<BasicClassDescription>> result = new LinkedHashSet<Equivalences<BasicClassDescription>>();
		
		for (Equivalences<BasicClassDescription> e : reasoner.getClasses()) {
			Equivalences<BasicClassDescription> nodes = getEquivalences(e.getRepresentative());
			if (!nodes.isEmpty())
				result.add(nodes);			
		}
		return result;
	}

}
