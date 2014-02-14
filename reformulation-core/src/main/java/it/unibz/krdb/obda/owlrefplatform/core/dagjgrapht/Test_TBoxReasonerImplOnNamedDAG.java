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
 * Allows to reason over the TBox using Named DAG
 * TEST ONLY CLASS
 */

public class Test_TBoxReasonerImplOnNamedDAG implements TBoxReasoner {

	private NamedDAG dag;
	
	private EquivalencesDAGImplOnNamedDAG<Property> propertyDAG;
	private EquivalencesDAGImplOnNamedDAG<BasicClassDescription> classDAG;

	/**
	 * Constructor using a DAG or a named DAG
	 * @param dag DAG to be used for reasoning
	 */
	public Test_TBoxReasonerImplOnNamedDAG(TBoxReasonerImpl reasoner) {
		this.dag = new NamedDAG(reasoner);
		
		this.propertyDAG = new EquivalencesDAGImplOnNamedDAG<Property>(dag.getPropertyDag(), reasoner.getProperties());
		this.classDAG = new EquivalencesDAGImplOnNamedDAG<BasicClassDescription>(dag.getClassDag(), reasoner.getClasses());		
	}

	public int getEdgesSize() {
		return dag.getPropertyDag().edgeSet().size() + dag.getClassDag().edgeSet().size();
	}
	

	
	/**
	 * Return all the nodes in the DAG or graph
	 * 
	 * @return we return a set of set of description to distinguish between
	 *         different nodes and equivalent nodes. equivalent nodes will be in
	 *         the same set of description
	 */
	
	public EquivalencesDAG<Property> getProperties() {
		return propertyDAG;
	}

	public EquivalencesDAG<BasicClassDescription> getClasses() {
		return classDAG;
	}

	public static final class EquivalencesDAGImplOnNamedDAG<T> implements EquivalencesDAG<T> {

		private SimpleDirectedGraph <T,DefaultEdge> dag;
		private EquivalencesDAG<T> reasonerDAG;
		
		public EquivalencesDAGImplOnNamedDAG(SimpleDirectedGraph<T, DefaultEdge> dag, EquivalencesDAG<T> reasonerDAG) {
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
			
			Set<T> equivalences = new LinkedHashSet<T>();
			for (T vertex : reasonerDAG.getVertex(v)) {
				if (reasonerDAG.isIndexed(reasonerDAG.getVertex(v))) 
						equivalences.add(vertex);
			}
			if (!equivalences.isEmpty())
				return new Equivalences<T>(equivalences, reasonerDAG.getVertex(v).getRepresentative());
			
			return new Equivalences<T>(equivalences);
		}

		@Override
		public Set<Equivalences<T>> getDirectSub(Equivalences<T> v) {
			return getDirectSub(v.getRepresentative());
		}
		public Set<Equivalences<T>> getDirectSub(T node) {
			LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<Equivalences<T>>();

			for (T source: Graphs.predecessorListOf(dag, node)) {

				// get the child node and its equivalent nodes
				Equivalences<T> namedEquivalences = getVertex(source);
				if (!namedEquivalences.isEmpty())
					result.add(namedEquivalences);
				else 
					result.addAll(getDirectSub(source));
			}

			return result;
		}

		@Override
		public Set<Equivalences<T>> getSub(Equivalences<T> v) {
			LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<Equivalences<T>>();
			
			T node = v.getRepresentative();
			
			// reverse the dag
			DirectedGraph<T, DefaultEdge> reversed = 
					new EdgeReversedGraph<T, DefaultEdge>(dag);
			
			BreadthFirstIterator<T, DefaultEdge>  iterator = 
						new BreadthFirstIterator<T, DefaultEdge>(reversed, node);

			while (iterator.hasNext()) {
				T child = iterator.next();

				// add the node and its equivalent nodes
				Equivalences<T> sources = getVertex(child);
				if (!sources.isEmpty())
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
			
			for (T target: Graphs.successorListOf(dag, node)) {

				// get the child node and its equivalent nodes
				Equivalences<T> namedEquivalences = getVertex(target);
				if (!namedEquivalences.isEmpty())
					result.add(namedEquivalences);
				else 
					result.addAll(getDirectSuper(target));
			}

			return result;
		}

		@Override
		public Set<Equivalences<T>> getSuper(Equivalences<T> v) {
			LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<Equivalences<T>>();

			T node = v.getRepresentative();

			BreadthFirstIterator<T, DefaultEdge>  iterator = 
					new BreadthFirstIterator<T, DefaultEdge>(dag, node);

			while (iterator.hasNext()) {
				T parent = iterator.next();

				// add the node and its equivalent nodes
				Equivalences<T> sources = getVertex(parent);
				if (!sources.isEmpty())
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
