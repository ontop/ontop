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
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.traverse.AbstractGraphIterator;
import org.jgrapht.traverse.BreadthFirstIterator;

/**
 * Allows to reason over the TBox using a TBox graph
 * 
 * WARNING: THIS CLASS IS ONLY FOR TESTING
 * 
 */

public class TestTBoxReasonerImplOnGraph {

	private DefaultDirectedGraph<Description,DefaultEdge> graph;
	AbstractGraphIterator<Description, DefaultEdge> iterator;

	private Set<OClass> namedClasses;
	private Set<Property> property;
	
	/**
	 * Constructor using a graph (cycles admitted)
	 * @param dag DAG to be used for reasoning
	 */
	public TestTBoxReasonerImplOnGraph(DefaultDirectedGraph<Description,DefaultEdge> graph) {
		this.graph = graph;
		namedClasses = new HashSet<OClass>();
		for (Description c: graph.vertexSet()) {
			if (c instanceof OClass) 
				namedClasses.add((OClass)c);
		}
		property = new HashSet<Property>();
		for (Description r: graph.vertexSet()) {
			if (r instanceof Property) {
				if (!((Property) r).isInverse())
					property.add((Property)r);
			}
		}
	}

	//return all roles in the graph
	public Set<Property> getRoles() {
		return property;
	}

	/**
	 * Allows to have all named classes in the graph
	 * @return  set of all named concepts in the graph
	 */

	public Set<OClass> getClasses(){
		return namedClasses;
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
	public Set<Equivalences<Description>> getDirectChildren(Description desc) {
		return getDirectChildren(desc,false);
	}
	
	public Set<Equivalences<Description>> getDirectChildren(Description desc,
			boolean named) {
		LinkedHashSet<Equivalences<Description>> result = new LinkedHashSet<Equivalences<Description>>();

			// get equivalences of the current node
		Equivalences<Description> equivalenceSet = getEquivalences(desc);
			// I want to consider also the children of the equivalent nodes
			for (Description n : equivalenceSet) {
				Set<DefaultEdge> edges = graph.incomingEdgesOf(n);
				for (DefaultEdge edge : edges) {
					Description source = graph.getEdgeSource(edge);

					// I don't want to consider as children the equivalent node
					// of the current node desc
					if (equivalenceSet.contains(source)) {
						continue;
					}
					Equivalences<Description> equivalences = getEquivalences(source);
					if (named) { // if true I search only for the named nodes

						Equivalences<Description> namedEquivalences = getEquivalences(source, true);

						if (!namedEquivalences.isEmpty())
							result.add(namedEquivalences);
						else {
							for (Description node : equivalences) {
								// I search for the first named description
								if (!namedEquivalences.contains(node)) {

									result.addAll(getNamedChildren(node));
								}
							}
						}
					}

					else {

						if (!equivalences.isEmpty())
							result.add(equivalences);
					}
				}
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

			Set<DefaultEdge> edges = graph.incomingEdgesOf(desc);
			for (DefaultEdge edge : edges) {
				Description source = graph.getEdgeSource(edge);

				// I don't want to consider as children the equivalent node of
				// the current node desc
				if (equivalenceSet.contains(source)) {
					continue;
				}
				Equivalences<Description> equivalences = getEquivalences(source);

				Equivalences<Description> namedEquivalences = getEquivalences(source, true);

				if (!namedEquivalences.isEmpty())
					result.add(namedEquivalences);
				else {
					for (Description node : equivalences) {
						// I search for the first named description
						if (!namedEquivalences.contains(node)) {

							result.addAll(getNamedChildren(node));
						}
					}
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
	public Set<Equivalences<Description>> getDirectParents(Description desc) {
		return getDirectParents(desc,false);
	}

	public Set<Equivalences<Description>> getDirectParents(Description desc, boolean named) {

		LinkedHashSet<Equivalences<Description>> result = new LinkedHashSet<Equivalences<Description>>();

		// get equivalences of the current node
		Equivalences<Description> equivalenceSet = getEquivalences(desc);

			// I want to consider also the parents of the equivalent nodes
			for (Description n : equivalenceSet) {
				Set<DefaultEdge> edges = graph.outgoingEdgesOf(n);
				for (DefaultEdge edge : edges) {
					Description target = graph.getEdgeTarget(edge);

					// I don't want to consider as parents the equivalent node
					// of the current node desc
					if (equivalenceSet.contains(target)) {
						continue;
					}
					Equivalences<Description> equivalences = getEquivalences(target);

					if (named) { // if true I search only for the named nodes

						Equivalences<Description> namedEquivalences = getEquivalences(target, true);
						if (!namedEquivalences.isEmpty())
							result.add(namedEquivalences);
						else {
							for (Description node : equivalences) {
								// I search for the first named description
								if (!namedEquivalences.contains(node)) {

									result.addAll(getNamedParents(node));
								}
							}
						}

					} else {

						if (!equivalences.isEmpty())
							result.add(equivalences);
					}
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
		Equivalences<Description> equivalenceSet = getEquivalences(desc, false);
			// I want to consider also the parents of the equivalent nodes

			Set<DefaultEdge> edges = graph.outgoingEdgesOf(desc);
			for (DefaultEdge edge : edges) {
				Description target = graph.getEdgeTarget(edge);

				// I don't want to consider as parents the equivalent node of
				// the current node desc
				if (equivalenceSet.contains(target)) {
					continue;
				}
				Equivalences<Description> equivalences = getEquivalences(target);

				Equivalences<Description> namedEquivalences = getEquivalences(target, true);

				if (!namedEquivalences.isEmpty())
					result.add(namedEquivalences);
				else {
					for (Description node : equivalences) {
						// I search for the first named description
						if (!namedEquivalences.contains(node)) {

							result.addAll(getNamedParents(node));
						}
					}
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
	public Set<Equivalences<Description>> getDescendants(Description desc) {
		return getDescendants(desc, false);
	}
	
	public Set<Equivalences<Description>> getDescendants(Description desc, boolean named) {
		LinkedHashSet<Equivalences<Description>> result = new LinkedHashSet<Equivalences<Description>>();

		// reverse the graph
			DirectedGraph<Description, DefaultEdge> reversed = new EdgeReversedGraph<Description, DefaultEdge>(
					graph);

			iterator = new BreadthFirstIterator<Description, DefaultEdge>(
					reversed, desc);

			while (iterator.hasNext()) {
				Description node = iterator.next();

				if (named) { // add only the named classes and property
					if (namedClasses.contains(node) | property.contains(node)) {
						Set<Description> sources = new HashSet<Description>();
						sources.add(node);

						result.add(new Equivalences<Description>(sources));
					}
				} else {
					Set<Description> sources = new HashSet<Description>();
					sources.add(node);

					result.add(new Equivalences<Description>(sources));
				}

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

	public Set<Equivalences<Description>> getAncestors(Description desc) {
		return getAncestors(desc, false);
	}
	
	public Set<Equivalences<Description>> getAncestors(Description desc, boolean named) {
		LinkedHashSet<Equivalences<Description>> result = new LinkedHashSet<Equivalences<Description>>();

		
			iterator = new BreadthFirstIterator<Description, DefaultEdge>(
					graph, desc);

			while (iterator.hasNext()) {
				Description node = iterator.next();

				if (named) { // add only the named classes and property
					if (namedClasses.contains(node) | property.contains(node)) {
						Set<Description> sources = new HashSet<Description>();
						sources.add(node);

						result.add(new Equivalences<Description>(sources));
					}
				} else {
					Set<Description> sources = new HashSet<Description>();
					sources.add(node);

					result.add(new Equivalences<Description>(sources));
				}

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

	public Equivalences<Description> getEquivalences(Description desc) {
		return getEquivalences(desc, false);
	}
	
	public Equivalences<Description> getEquivalences(Description desc, boolean named) {

			// search for cycles
			StrongConnectivityInspector<Description, DefaultEdge> inspector = 
					new StrongConnectivityInspector<Description, DefaultEdge>(graph);

			// each set contains vertices which together form a strongly
			// connected component within the given graph
			List<Set<Description>> equivalenceSets = inspector
					.stronglyConnectedSets();

			Set<Description> equivalences = new LinkedHashSet<Description>();
			// I want to find the equivalent node of desc
			for (Set<Description> equivalenceSet : equivalenceSets) {
				if (equivalenceSet.size() >= 2) {
					if (equivalenceSet.contains(desc)) {
						if (named) {
							for (Description vertex : equivalenceSet) {
								if (namedClasses.contains(vertex)
										| property.contains(vertex)) {
									equivalences.add(vertex);
								}
							}
							return new Equivalences<Description>(equivalences);
						}

						return new Equivalences<Description>(equivalenceSet);
					}

				}

			}

			// if there are not equivalent node return the node or nothing
			if (named) {
				if (namedClasses.contains(desc) | property.contains(desc)) {
					return new Equivalences<Description>(Collections
							.singleton(desc));
				} else { // return empty set if the node we are considering
							// (desc) is not a named class or propertu
					equivalences = Collections.emptySet();
					return new Equivalences<Description>(equivalences);
				}
			}
			return new Equivalences<Description>(Collections.singleton(desc));
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

	public Set<Equivalences<Description>> getNodes(boolean named) {
		LinkedHashSet<Equivalences<Description>> result = new LinkedHashSet<Equivalences<Description>>();

		for (Description vertex : graph.vertexSet()) {
				result.add(getEquivalences(vertex, named));
			}

		return result;
	}

	/***
	 * Modifies the DAG so that \exists R = \exists R-, so that the reachability
	 * relation of the original DAG gets extended to the reachability relation
	 * of T and Sigma chains.
	 * 
	 */
	
	public void convertIntoChainDAG() {

		Collection<Description> nodes = new HashSet<Description>(
					graph.vertexSet());
			OntologyFactory fac = OntologyFactoryImpl.getInstance();
			HashSet<Description> processedNodes = new HashSet<Description>();
			for (Description node : nodes) {
				if (!(node instanceof PropertySomeRestriction)
						|| processedNodes.contains(node)) {
					continue;
				}

				/*
				 * Adding a cycle between exists R and exists R- for each R.
				 */

				PropertySomeRestriction existsR = (PropertySomeRestriction) node;
				PropertySomeRestriction existsRin = fac
						.createPropertySomeRestriction(existsR.getPredicate(),
								!existsR.isInverse());
				Description existsNode = node;
				Description existsInvNode = existsRin;
				Set<Equivalences<Description>> childrenExist = new HashSet<Equivalences<Description>>(
						getDirectChildren(existsNode));
				Set<Equivalences<Description>> childrenExistInv = new HashSet<Equivalences<Description>>(
						getDirectChildren(existsInvNode));

				for (Equivalences<Description> children : childrenExist) {
					for (Description child : children) {
						// DAGOperations.addParentEdge(child, existsInvNode);
						graph.addEdge(child, existsInvNode);

					}
				}
				for (Equivalences<Description> children : childrenExistInv) {
					for (Description child : children) {
						// DAGOperations.addParentEdge(child, existsNode);
						graph.addEdge(child, existsNode);

					}
				}

				Set<Equivalences<Description>> parentExist = new HashSet<Equivalences<Description>>(
						getDirectParents(existsNode));
				Set<Equivalences<Description>> parentsExistInv = new HashSet<Equivalences<Description>>(
						getDirectParents(existsInvNode));

				for (Equivalences<Description> parents : parentExist) {
					for (Description parent : parents) {
						// DAGOperations.addParentEdge(existsInvNode, parent);
						graph.addEdge(existsInvNode, parent);

					}
				}

				for (Equivalences<Description> parents : parentsExistInv) {
					for (Description parent : parents) {
						// DAGOperations.addParentEdge(existsNode,parent);
						graph.addEdge(existsNode, parent);

					}
				}

				processedNodes.add(existsInvNode);
				processedNodes.add(existsNode);
			}
	}
	
}
