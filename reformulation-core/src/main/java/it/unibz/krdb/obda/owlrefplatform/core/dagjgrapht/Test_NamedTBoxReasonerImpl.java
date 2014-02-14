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
import it.unibz.krdb.obda.ontology.Property;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Representation of the named part of the property and class DAGs  
 *     based on the DAGs provided by a TBoxReasonerImpl
 * 
 * WARNING: THIS CLASS IS FOR TESTING ONLY 
 */

public class Test_NamedTBoxReasonerImpl implements TBoxReasoner {

	private EquivalencesDAG<Property> propertyDAG;
	private EquivalencesDAG<BasicClassDescription> classDAG;

	public Test_NamedTBoxReasonerImpl(TBoxReasonerImpl reasoner) {
		this.propertyDAG = new EquivalencesDAGImpl<Property>(reasoner.getProperties());
		this.classDAG = new EquivalencesDAGImpl<BasicClassDescription>(reasoner.getClasses());
	}


	/**
	 * Return the DAG of properties
	 * 
	 * @return DAG 
	 */

	@Override
	public EquivalencesDAG<Property> getProperties() {
		return propertyDAG;
	}
	
	/**
	 * Return the DAG of classes
	 * 
	 * @return DAG 
	 */

	@Override
	public EquivalencesDAG<BasicClassDescription> getClasses() {
		return classDAG;
	}

	/**
	 * Reconstruction of the Named DAG (as EquivalncesDAG) from a DAG
	 *
	 * @param <T> Property or BasicClassDescription
	 */
	
	public static final class EquivalencesDAGImpl<T> implements EquivalencesDAG<T> {

		private EquivalencesDAG<T> reasonerDAG;
		
		EquivalencesDAGImpl(EquivalencesDAG<T> reasonerDAG) {
			this.reasonerDAG = reasonerDAG;
		}
		
		@Override
		public Iterator<Equivalences<T>> iterator() {
			LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<Equivalences<T>>();
			
			for (Equivalences<T> e : reasonerDAG) {
				Equivalences<T> nodes = getVertex(e.getRepresentative());
				if (nodes != null)
					result.add(nodes);			
			}
			return result.iterator();
		}

		@Override
		public Equivalences<T> getVertex(T desc) {

			// either all elements of the equivalence set are there or none!
			Equivalences<T> vertex = reasonerDAG.getVertex(desc);
			if (vertex.isIndexed())
				return vertex;
			else
				return null;
		}

		
		@Override
		public Set<Equivalences<T>> getDirectSub(Equivalences<T> v) {
			LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<Equivalences<T>>();

			for (Equivalences<T> e : reasonerDAG.getDirectSub(v)) {
				T child = e.getRepresentative();
				
				// get the child node and its equivalent nodes
				Equivalences<T> namedEquivalences = getVertex(child);
				if (namedEquivalences != null)
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
				if (nodes != null)
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
				if (namedEquivalences != null)
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
				if (nodes != null)
					result.add(nodes);			
			}
			
			return result;
		}		
	}
}
