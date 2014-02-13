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

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Simulates the NamedDAG over TBoxReasonerImpl 
 * 
 * THIS CLASS IS FOR TESTS ONLY
 * 
 */

public class Test_NamedTBoxReasonerImpl implements TBoxReasoner {

	private EquivalencesNamedDAGImpl<Property> propertyDAG;
	private EquivalencesNamedDAGImpl<BasicClassDescription> classDAG;

	/**
	 * Constructor using a DAG or a named DAG
	 * @param dag DAG to be used for reasoning
	 */
	public Test_NamedTBoxReasonerImpl(TBoxReasonerImpl reasoner) {
		this.propertyDAG = new EquivalencesNamedDAGImpl<Property>(reasoner.getProperties());
		this.classDAG = new EquivalencesNamedDAGImpl<BasicClassDescription>(reasoner.getClasses());
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
		return propertyDAG.getDirectSub(propertyDAG.getVertex(desc));
	}

	@Override
	public Set<Equivalences<BasicClassDescription>> getDirectSubClasses(BasicClassDescription desc) {
		return classDAG.getDirectSub(classDAG.getVertex(desc));
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
		return propertyDAG.getDirectSuper(propertyDAG.getVertex(desc));
	}

	@Override
	public Set<Equivalences<BasicClassDescription>> getDirectSuperClasses(BasicClassDescription desc) {
		return classDAG.getDirectSuper(classDAG.getVertex(desc));
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
		return propertyDAG.getSub(propertyDAG.getVertex(desc));
	}

	@Override
	public Set<Equivalences<BasicClassDescription>> getSubClasses(BasicClassDescription desc) {
		return classDAG.getSub(classDAG.getVertex(desc));
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
		return propertyDAG.getSuper(propertyDAG.getVertex(desc));
	}

	@Override
	public Set<Equivalences<BasicClassDescription>> getSuperClasses(BasicClassDescription desc) {
		return classDAG.getSuper(classDAG.getVertex(desc));
	}


	/**
	 * Return the equivalences starting from the given node of the dag
	 * 
	 * @param desc node we want to know the ancestors
	 *            
	 * @return we return a set of description with equivalent nodes 
	 */

	public Equivalences<Property> getEquivalences(Property desc) {
		return propertyDAG.getVertex(desc);
	}
	public Equivalences<BasicClassDescription> getEquivalences(BasicClassDescription desc) {
		return classDAG.getVertex(desc);
	}
	

	/**
	 * Return all the nodes in the DAG or graph
	 * 
	 * @return we return a set of set of description to distinguish between
	 *         different nodes and equivalent nodes. equivalent nodes will be in
	 *         the same set of description
	 */


	@Override
	public EquivalencesDAG<Property> getProperties() {
		return propertyDAG;
	}

	@Override
	public EquivalencesDAG<BasicClassDescription> getClasses() {
		return classDAG;
	}

	public static final class EquivalencesNamedDAGImpl<T> implements EquivalencesDAG<T> {

		private EquivalencesDAG<T> reasonerDAG;
		
		EquivalencesNamedDAGImpl(EquivalencesDAG<T> reasonerDAG) {
			this.reasonerDAG = reasonerDAG;
		}
		
		@Override
		public Iterator<Equivalences<T>> iterator() {
			LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<Equivalences<T>>();
			
			for (Equivalences<T> e : reasonerDAG) {
				Equivalences<T> nodes = getVertex(e.getRepresentative());
				if (!nodes.isEmpty())
					result.add(nodes);			
			}
			return result.iterator();
		}

		@Override
		public Equivalences<T> getVertex(T desc) {

			Set<T> equivalences = new LinkedHashSet<T>();
				for (T vertex : reasonerDAG.getVertex(desc)) {
					if (reasonerDAG.isIndexed(vertex)) 
						equivalences.add(vertex);
				}
			if (!equivalences.isEmpty())
				return new Equivalences<T>(equivalences, reasonerDAG.getVertex(desc).getRepresentative());
			
			return new Equivalences<T>(equivalences);
		}

		
		@Override
		public Set<Equivalences<T>> getDirectSub(Equivalences<T> v) {
			LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<Equivalences<T>>();

			for (Equivalences<T> e : reasonerDAG.getDirectSub(v)) {
				T child = e.getRepresentative();
				
				// get the child node and its equivalent nodes
				Equivalences<T> namedEquivalences = getVertex(child);
				if (!namedEquivalences.isEmpty())
					result.add(namedEquivalences);
				else 
					result.addAll(getDirectSub(e)); // recursive call if the child is not empty
			}
			return result;
		}

		@Override
		public Set<Equivalences<T>> getSub(Equivalences<T> v) {
			LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<Equivalences<T>>();
			
			for (Equivalences<T> e : reasonerDAG.getSub(v)) {
				Equivalences<T> nodes = getVertex(e.getRepresentative());
				if (!nodes.isEmpty())
					result.add(nodes);			
			}
			return result;
		}

		@Override
		public Set<Equivalences<T>> getDirectSuper(Equivalences<T> v) {
			LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<Equivalences<T>>();
			
			for (Equivalences<T> e : reasonerDAG.getDirectSuper(v)) {
				T parent = e.getRepresentative();
				
				// get the child node and its equivalent nodes
				Equivalences<T> namedEquivalences = getVertex(parent);
				if (!namedEquivalences.isEmpty())
					result.add(namedEquivalences);
				else 
					result.addAll(getDirectSuper(e)); // recursive call if the parent is not named
			}
			return result;
		}
		
		@Override
		public Set<Equivalences<T>> getSuper(Equivalences<T> v) {
			LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<Equivalences<T>>();

			for (Equivalences<T> e : reasonerDAG.getSuper(v)) {
				Equivalences<T> nodes = getVertex(e.getRepresentative());
				if (!nodes.isEmpty())
					result.add(nodes);			
			}
			
			return result;
		}

		@Override
		public boolean isIndexed(T v) {
			return false;
		}
		
	}
}
