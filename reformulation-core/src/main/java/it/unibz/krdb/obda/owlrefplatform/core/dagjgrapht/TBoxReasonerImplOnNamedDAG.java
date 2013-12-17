/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import it.unibz.krdb.obda.ontology.Axiom;
import it.unibz.krdb.obda.ontology.ClassDescription;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.ontology.impl.SubClassAxiomImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.AbstractGraphIterator;
import org.jgrapht.traverse.BreadthFirstIterator;

/**
 * Allows to reason over the TBox using  DAG or graph
 * 
 */

public class TBoxReasonerImplOnNamedDAG implements TBoxReasoner {

	private DAGImpl dag;

	private Set<OClass> namedClasses;
	private Set<Property> property;

	/**
	 * Constructor using a DAG or a named DAG
	 * @param dag DAG to be used for reasoning
	 */
	public TBoxReasonerImplOnNamedDAG(DAGImpl dag) {
		this.dag = dag;
		namedClasses = dag.getClasses();
		property = dag.getRoles();
	}
	
	@Override
	public DAGImpl getDAG() {
		return dag;
	}
	
	/**
	 * return the direct children starting from the given node of the dag
	 * 
	 * @param desc node that we want to know the direct children
	 * @param named
	 *            when it's true only the children that correspond to named
	 *            classes and property are returned
	 * @return we return a set of set of description to distinguish between
	 *         different nodes and equivalent nodes. equivalent nodes will be in
	 *         the same set of description
	 */
	@Override
	public Set<Set<Description>> getDirectChildren(Description desc) {
		
		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();

		// take the representative node
		Description node = dag.getReplacementFor(desc);
		if (node == null)
			node = desc;

		for (DefaultEdge edge : dag.incomingEdgesOf(node)) {
			
			Description source = dag.getEdgeSource(edge);

			// get the child node and its equivalent nodes
			Set<Description> equivalences = getEquivalences(source);

				Set<Description> namedEquivalences = getEquivalences(source);

				if (!namedEquivalences.isEmpty())
					result.add(namedEquivalences);
				else {
					result.addAll(getNamedChildren(source));
				}
		}

		return Collections.unmodifiableSet(result);
	}

	/*
	 *  Private method that searches for the first named children
	 */

	private Set<Set<Description>> getNamedChildren(Description desc) {

		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();

			// get equivalences of the current node
			Set<Description> equivalenceSet = getEquivalences(desc);
			// I want to consider also the children of the equivalent nodes
			if (!dag.containsVertex(desc)) {
				System.out.println(desc);
				System.out.println(equivalenceSet);
			}
			Set<DefaultEdge> edges = dag.incomingEdgesOf(desc);
			for (DefaultEdge edge : edges) {
				Description source = dag.getEdgeSource(edge);

				// I don't want to consider as children the equivalent node of
				// the current node desc
				if (equivalenceSet.contains(source)) {
					continue;
				}
//				Set<Description> equivalences = getEquivalences(source, false);

				Set<Description> namedEquivalences = getEquivalences(source);

				if (!namedEquivalences.isEmpty())
					result.add(namedEquivalences);
				else {
					result.addAll(getNamedChildren(source));
					// for (Description node: equivalences){
					// //I search for the first named description
					// if(!namedEquivalences.contains(node) ){
					//
					// result.addAll( getNamedChildren(node));
					// }
					// }
				}
			}
			
			return result;
	}

	/**
	 * return the direct parents starting from the given node of the dag
	 * 
	 * @param desc node from which we want to know the direct parents
	 * @param named
	 *            when it's true only the parents that correspond to named
	 *            classes or property are returned
	 *            
	 * @return we return a set of set of description to distinguish between
	 *         different nodes and equivalent nodes. equivalent nodes will be in
	 *         the same set of description
	 * */
	@Override
	public Set<Set<Description>> getDirectParents(Description desc) {

		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();
		
		// take the representative node
		Description node = dag.getReplacementFor(desc);
		if (node == null)
			node = desc;

		for (DefaultEdge edge : dag.outgoingEdgesOf(node)) {
			Description target = dag.getEdgeTarget(edge);

			// get the child node and its equivalent nodes
			Set<Description> equivalences = getEquivalences(target);

				Set<Description> namedEquivalences = getEquivalences(target);
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
	
	private Set<Set<Description>> getNamedParents(Description desc) {

		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();

			// get equivalences of the current node
			Set<Description> equivalenceSet = getEquivalences(desc);
			// I want to consider also the parents of the equivalent nodes

			Set<DefaultEdge> edges = dag.outgoingEdgesOf(desc);
			for (DefaultEdge edge : edges) {
				Description target = dag.getEdgeTarget(edge);

				// I don't want to consider as parents the equivalent node of
				// the current node desc
				if (equivalenceSet.contains(target)) {
					continue;
				}

				Set<Description> namedEquivalences = getEquivalences(target);

				if (!namedEquivalences.isEmpty())
					result.add(namedEquivalences);
				else {
					result.addAll(getNamedParents(target));
					// for (Description node: equivalences){
					// //I search for the first named description
					// if(!namedEquivalences.contains(node) ){
					//
					// result.addAll(getNamedParents(node));
					// }
					// }
				}
			}
			return result;
	}

	/**
	 * Traverse the graph return the descendants starting from the given node of
	 * the dag
	 * 
	 * @param desc node we want to know the descendants
	 * @param named
	 *            when it's true only the descendants that are named classes or
	 *            property are returned
	 *@return we return a set of set of description to distinguish between
	 *         different nodes and equivalent nodes. equivalent nodes will be in
	 *         the same set of description
	 */
	@Override
	public Set<Set<Description>> getDescendants(Description desc) {

		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();

		Description node = dag.getReplacementFor(desc);
		if (node == null)
			node = desc;
		
		// reverse the dag
		DirectedGraph<Description, DefaultEdge> reversed = dag.getReversedDag();

		AbstractGraphIterator<Description, DefaultEdge>  iterator = 
					new BreadthFirstIterator<Description, DefaultEdge>(reversed, node);

		// I don't want to consider the current node
		iterator.next();

		Description startNode = desc;
		Set<Description> sourcesStart = getEquivalences(startNode);
		Set<Description> sourcesStartnoNode = new HashSet<Description>();
		for (Description equivalent : sourcesStart) {
			if (equivalent.equals(startNode))
				continue;
			sourcesStartnoNode.add(equivalent);

		}

		if (!sourcesStartnoNode.isEmpty())
			result.add(sourcesStartnoNode);

		// iterate over the subsequent nodes, they are all descendant of desc
		while (iterator.hasNext()) {
			Description child = iterator.next();

			// add the node and its equivalent nodes
			Set<Description> sources = getEquivalences(child);

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
	 * @param named
	 *            when it's true only the ancestors that are named classes or
	 *            property are returned
	 * @return we return a set of set of description to distinguish between
	 *         different nodes and equivalent nodes. equivalent nodes will be in
	 *         the same set of description
	 */

	@Override
	public Set<Set<Description>> getAncestors(Description desc) {

		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();

		Description node = dag.getReplacementFor(desc);
		if (node == null)
			node = desc;

		AbstractGraphIterator<Description, DefaultEdge>  iterator = 
				new BreadthFirstIterator<Description, DefaultEdge>(dag.getDag(), node);

		// I don't want to consider the current node
		iterator.next();

		Description startNode = desc;
		Set<Description> sourcesStart = getEquivalences(startNode);
		Set<Description> sourcesStartnoNode = new HashSet<Description>();
		for (Description equivalent : sourcesStart) {
			if (equivalent.equals(startNode))
				continue;
			sourcesStartnoNode.add(equivalent);

		}

		if (!sourcesStartnoNode.isEmpty())
			result.add(sourcesStartnoNode);

		// iterate over the subsequent nodes, they are all ancestor of desc
		while (iterator.hasNext()) {
			Description parent = iterator.next();

			// add the node and its equivalent nodes
			Set<Description> sources = getEquivalences(parent);

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

	 * @param named
	 *            when it's <code> true </code> only the equivalences that are named classes or
	 *            property are returned
	 *            
	 * @return we return a set of description with equivalent nodes 
	 */

	@Override
	public Set<Description> getEquivalences(Description desc) {

		Set<Description> equivalents = dag.getMapEquivalences().get(desc);

		// if there are no equivalent nodes return the node or nothing
		if (equivalents == null) {
			
				if (namedClasses.contains(desc) | property.contains(desc)) {
					return Collections.unmodifiableSet(Collections.singleton(desc));
				} 
				else { // return empty set if the node we are considering
						// (desc) is not a named class or property
					return Collections.emptySet();
				}
		}
		
		Set<Description> equivalences = new LinkedHashSet<Description>();
			for (Description vertex : equivalents) {
				if (namedClasses.contains(vertex) | property.contains(vertex)) {
						equivalences.add(vertex);
				}
			}
		return Collections.unmodifiableSet(equivalences);
	}
	
	/**
	 * Return all the nodes in the DAG or graph
	 * 
	 * @param named when it's <code> true </code> only the named classes or
	 *            property are returned 
	 * @return we return a set of set of description to distinguish between
	 *         different nodes and equivalent nodes. equivalent nodes will be in
	 *         the same set of description
	 */

	@Override
	public Set<Set<Description>> getNodes() {

		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();

		for (Description vertex : dag.vertexSet()) 
				result.add(getEquivalences(vertex));

		return result;
	}


	public Ontology getSigmaOntology() {
		OntologyFactory descFactory = new OntologyFactoryImpl();

		Ontology sigma = descFactory.createOntology("sigma");

		// DAGEdgeIterator edgeiterator = new DAGEdgeIterator(dag);
		OntologyFactory fac = OntologyFactoryImpl.getInstance();
		// for(DefaultEdge edge: dag.edgeSet()){
		// while (edgeiterator.hasNext()) {
		// Edge edge = edgeiterator.next();
		for (Description node : dag.vertexSet()) {
			for (Set<Description> descendants : getDescendants(node)) {
				Description firstDescendant = descendants.iterator().next();
				Description descendant = dag.getReplacementFor(firstDescendant);
				if (descendant == null)
					descendant = firstDescendant;
//				Axiom axiom = null;
				/*
				 * Creating subClassOf or subPropertyOf axioms
				 */
				if (!descendant.equals(node)) {
					if (descendant instanceof ClassDescription) {
						ClassDescription sub = (ClassDescription) descendant;
						ClassDescription superp = (ClassDescription) node;
						if (superp instanceof PropertySomeRestriction)
							continue;

						Axiom ax = fac.createSubClassAxiom(sub, superp);
						sigma.addEntities(ax.getReferencedEntities());
						sigma.addAssertion(ax);
					} else {
						Property sub = (Property) descendant;
						Property superp = (Property) node;

						Axiom ax = fac.createSubPropertyAxiom(sub, superp);
						sigma.addEntities(ax.getReferencedEntities());

						sigma.addAssertion(ax);
					}

				}
			}
			for (Description equivalent : getEquivalences(node)) {
				if (!equivalent.equals(node)) {
					Axiom ax = null;
					if (node instanceof ClassDescription) {
						ClassDescription sub = (ClassDescription) node;
						ClassDescription superp = (ClassDescription) equivalent;
						if (!(superp instanceof PropertySomeRestriction)) {
							ax = fac.createSubClassAxiom(sub, superp);
							sigma.addEntities(ax.getReferencedEntities());
							sigma.addAssertion(ax);
						}

					} else {
						Property sub = (Property) node;
						Property superp = (Property) equivalent;

						ax = fac.createSubPropertyAxiom(sub, superp);
						sigma.addEntities(ax.getReferencedEntities());
						sigma.addAssertion(ax);

					}

					if (equivalent instanceof ClassDescription) {
						ClassDescription sub = (ClassDescription) equivalent;
						ClassDescription superp = (ClassDescription) node;
						if (!(superp instanceof PropertySomeRestriction)) {
							ax = fac.createSubClassAxiom(sub, superp);
							sigma.addEntities(ax.getReferencedEntities());
							sigma.addAssertion(ax);
						}

					} else {
						Property sub = (Property) equivalent;
						Property superp = (Property) node;

						ax = fac.createSubPropertyAxiom(sub, superp);
						sigma.addEntities(ax.getReferencedEntities());
						sigma.addAssertion(ax);

					}

				}

			}
		}
		// }

		return sigma;
	}
}
