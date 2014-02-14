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

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

/**
 * Representation of the named part of the property and class DAGs  
 *     based on the NamedDAG abstraction
 * 
 * WARNING: THIS CLASS IS FOR TESTING ONLY 
 */


public class Test_TBoxReasonerImplOnNamedDAG implements TBoxReasoner {

	private EquivalencesDAG<Property> propertyDAG;
	private EquivalencesDAG<BasicClassDescription> classDAG;

	/**
	 * Constructor using a DAG or a named DAG
	 * @param dag DAG to be used for reasoning
	 */
	public Test_TBoxReasonerImplOnNamedDAG(TBoxReasonerImpl reasoner) {
		NamedDAG dag = new NamedDAG(reasoner);
		
		this.propertyDAG = new EquivalencesDAGImpl<Property>(dag.getPropertyDag(), reasoner.getProperties());
		this.classDAG = new EquivalencesDAGImpl<BasicClassDescription>(dag.getClassDag(), reasoner.getClasses());		
	}

	
	/**
	 * Return the DAG of properties
	 * 
	 * @return DAG 
	 */

	public EquivalencesDAG<Property> getProperties() {
		return propertyDAG;
	}

	/**
	 * Return the DAG of classes
	 * 
	 * @return DAG 
	 */

	public EquivalencesDAG<BasicClassDescription> getClasses() {
		return classDAG;
	}

	/**
	 * Reconstruction of the Named DAG (as EquivalencesDAG) from a NamedDAG
	 *
	 * @param <T> Property or BasicClassDescription
	 */
	
	public static final class EquivalencesDAGImpl<T> implements EquivalencesDAG<T> {

		private SimpleDirectedGraph <T,DefaultEdge> dag;
		private EquivalencesDAG<T> reasonerDAG;
		
		public EquivalencesDAGImpl(SimpleDirectedGraph<T, DefaultEdge> dag, EquivalencesDAG<T> reasonerDAG) {
			this.dag = dag;
			this.reasonerDAG = reasonerDAG;
		}

		@Override
		public Iterator<Equivalences<T>> iterator() {
			LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<Equivalences<T>>();

			for (T vertex : dag.vertexSet()) 
				result.add(getVertex(vertex));
			
			return result.iterator();
		}

		@Override
		public Equivalences<T> getVertex(T v) {		
			// either all or none
			Equivalences<T> vertex = reasonerDAG.getVertex(v);
			if (dag.containsVertex(vertex.getRepresentative()))
				return vertex;
			else
				return null;
		}

		@Override
		public Set<Equivalences<T>> getDirectSub(Equivalences<T> v) {
			return getDirectSub(v.getRepresentative());
		}
		public Set<Equivalences<T>> getDirectSub(T node) {
			LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<Equivalences<T>>();

			for (T source : Graphs.predecessorListOf(dag, node)) {

				// get the child node and its equivalent nodes
				Equivalences<T> namedEquivalences = getVertex(source);
				if (namedEquivalences != null)
					result.add(namedEquivalences);
				else 
					result.addAll(getDirectSub(source));
			}

			return result;
		}

		@Override
		public Set<Equivalences<T>> getSub(Equivalences<T> v) {
			
			T node = v.getRepresentative();
			
			LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<Equivalences<T>>();
			// reverse the dag
			DirectedGraph<T, DefaultEdge> reversed = new EdgeReversedGraph<T, DefaultEdge>(dag);
			BreadthFirstIterator<T, DefaultEdge>  iterator = new BreadthFirstIterator<T, DefaultEdge>(reversed, node);

			while (iterator.hasNext()) {
				T child = iterator.next();

				// add the node and its equivalent nodes
				Equivalences<T> sources = getVertex(child);
				if (sources != null)
					result.add(sources);
			}
			
			return result;
		}

		@Override
		public Set<Equivalences<T>> getDirectSuper(Equivalences<T> v) {
			return getDirectSuper(v.getRepresentative());
		}
		
		public Set<Equivalences<T>> getDirectSuper(T node) {
			LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<Equivalences<T>>();
			
			for (T target : Graphs.successorListOf(dag, node)) {

				// get the child node and its equivalent nodes
				Equivalences<T> namedEquivalences = getVertex(target);
				if (namedEquivalences != null)
					result.add(namedEquivalences);
				else 
					result.addAll(getDirectSuper(target));
			}

			return result;
		}

		@Override
		public Set<Equivalences<T>> getSuper(Equivalences<T> v) {

			T node = v.getRepresentative();
			
			LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<Equivalences<T>>();
			BreadthFirstIterator<T, DefaultEdge>  iterator = new BreadthFirstIterator<T, DefaultEdge>(dag, node);

			while (iterator.hasNext()) {
				T parent = iterator.next();

				// add the node and its equivalent nodes
				Equivalences<T> sources = getVertex(parent);
				if (sources != null)
					result.add(sources);
			}

			return result;
		}

		@Override
		public boolean isIndexed(Equivalences<T> v) {
			return reasonerDAG.getVertex(v.getRepresentative()).isIndexed();
		}	
	}
}
