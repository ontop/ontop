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
import it.unibz.krdb.obda.ontology.Ontology;
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

public class TBoxReasonerImplOnGraph implements TBoxReasoner {

	private TBoxGraph graph;
	private DAGImpl dag;
	AbstractGraphIterator<Description, DefaultEdge> iterator;

	private Set<OClass> namedClasses;
	private Set<Property> property;
	
	/**
	 * Constructor using a graph (cycles admitted)
	 * @param dag DAG to be used for reasoning
	 */
	public TBoxReasonerImplOnGraph(TBoxGraph graph) {
		this.graph = graph;
		namedClasses = graph.getClasses();
		property = graph.getRoles();
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
		return getDirectChildren(desc,false);
	}
	
	public Set<Set<Description>> getDirectChildren(Description desc,
			boolean named) {
		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();

			// get equivalences of the current node
			Set<Description> equivalenceSet = getEquivalences(desc);
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
					Set<Description> equivalences = getEquivalences(source);

					if (named) { // if true I search only for the named nodes

						Set<Description> namedEquivalences = getEquivalences(source, true);

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

	private Set<Set<Description>> getNamedChildren(Description desc) {

		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();

			// get equivalences of the current node
			Set<Description> equivalenceSet = getEquivalences(desc);
			// I want to consider also the children of the equivalent nodes

			Set<DefaultEdge> edges = graph.incomingEdgesOf(desc);
			for (DefaultEdge edge : edges) {
				Description source = graph.getEdgeSource(edge);

				// I don't want to consider as children the equivalent node of
				// the current node desc
				if (equivalenceSet.contains(source)) {
					continue;
				}
				Set<Description> equivalences = getEquivalences(source);

				Set<Description> namedEquivalences = getEquivalences(source, true);

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
	@Override
	public Set<Set<Description>> getDirectParents(Description desc) {
		return getDirectParents(desc,false);
	}

	public Set<Set<Description>> getDirectParents(Description desc, boolean named) {

		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();

		// get equivalences of the current node
			Set<Description> equivalenceSet = getEquivalences(desc);

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
					Set<Description> equivalences = getEquivalences(target);

					if (named) { // if true I search only for the named nodes

						Set<Description> namedEquivalences = getEquivalences(target, true);
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
	
	private Set<Set<Description>> getNamedParents(Description desc) {

		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();

			// get equivalences of the current node
			Set<Description> equivalenceSet = getEquivalences(desc, false);
			// I want to consider also the parents of the equivalent nodes

			Set<DefaultEdge> edges = graph.outgoingEdgesOf(desc);
			for (DefaultEdge edge : edges) {
				Description target = graph.getEdgeTarget(edge);

				// I don't want to consider as parents the equivalent node of
				// the current node desc
				if (equivalenceSet.contains(target)) {
					continue;
				}
				Set<Description> equivalences = getEquivalences(target);

				Set<Description> namedEquivalences = getEquivalences(target, true);

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
	@Override
	public Set<Set<Description>> getDescendants(Description desc) {
		return getDescendants(desc, false);
	}
	
	public Set<Set<Description>> getDescendants(Description desc, boolean named) {
		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();

		// reverse the graph
			DirectedGraph<Description, DefaultEdge> reversed = new EdgeReversedGraph<Description, DefaultEdge>(
					graph.getGraph());

			iterator = new BreadthFirstIterator<Description, DefaultEdge>(
					reversed, desc);

			// I don't want to consider the current node
			Description current = iterator.next();

			// get equivalences of the current node
//			Set<Description> equivalenceSet = getEquivalences(current, named);
			// iterate over the subsequent nodes, they are all descendant of
			// desc
			while (iterator.hasNext()) {
				Description node = iterator.next();

				// I don't want to add between the descendants a node equivalent
				// to the starting node
				if (node.equals(current))
					continue;

				if (named) { // add only the named classes and property
					if (namedClasses.contains(node) | property.contains(node)) {
						Set<Description> sources = new HashSet<Description>();
						sources.add(node);

						result.add(sources);
					}
				} else {
					Set<Description> sources = new HashSet<Description>();
					sources.add(node);

					result.add(sources);
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

	@Override
	public Set<Set<Description>> getAncestors(Description desc) {
		return getAncestors(desc, false);
	}
	
	public Set<Set<Description>> getAncestors(Description desc, boolean named) {
		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();

		
			iterator = new BreadthFirstIterator<Description, DefaultEdge>(
					graph.getGraph(), desc);

			// I don't want to consider the current node
			Description current = iterator.next();

			// get equivalences of the current node
//			Set<Description> equivalenceSet = getEquivalences(current, named);
			// iterate over the subsequent nodes, they are all ancestor of desc
			while (iterator.hasNext()) {
				Description node = iterator.next();

				// I don't want to add between the ancestors a node equivalent
				// to the starting node
				if (current.equals(node))
					continue;

				if (named) { // add only the named classes and property
					if (namedClasses.contains(node) | property.contains(node)) {
						Set<Description> sources = new HashSet<Description>();
						sources.add(node);

						result.add(sources);
					}
				} else {
					Set<Description> sources = new HashSet<Description>();
					sources.add(node);

					result.add(sources);
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

	@Override
	public Set<Description> getEquivalences(Description desc) {
		return getEquivalences(desc, false);
	}
	
	public Set<Description> getEquivalences(Description desc, boolean named) {

			// search for cycles
			StrongConnectivityInspector<Description, DefaultEdge> inspector = 
					new StrongConnectivityInspector<Description, DefaultEdge>(graph.getGraph());

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
							return Collections.unmodifiableSet(equivalences);
						}

						return Collections.unmodifiableSet(equivalenceSet);
					}

				}

			}

			// if there are not equivalent node return the node or nothing
			if (named) {
				if (namedClasses.contains(desc) | property.contains(desc)) {
					return Collections.unmodifiableSet(Collections
							.singleton(desc));
				} else { // return empty set if the node we are considering
							// (desc) is not a named class or propertu
					equivalences = Collections.emptySet();
					return equivalences;
				}
			}
			return Collections.unmodifiableSet(Collections.singleton(desc));
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

	public Set<Set<Description>> getNodes(boolean named) {
		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();

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
				Set<Set<Description>> childrenExist = new HashSet<Set<Description>>(
						getDirectChildren(existsNode));
				Set<Set<Description>> childrenExistInv = new HashSet<Set<Description>>(
						getDirectChildren(existsInvNode));

				for (Set<Description> children : childrenExist) {
					for (Description child : children) {
						// DAGOperations.addParentEdge(child, existsInvNode);
						graph.getGraph().addEdge(child, existsInvNode);

					}
				}
				for (Set<Description> children : childrenExistInv) {
					for (Description child : children) {
						// DAGOperations.addParentEdge(child, existsNode);
						graph.getGraph().addEdge(child, existsNode);

					}
				}

				Set<Set<Description>> parentExist = new HashSet<Set<Description>>(
						getDirectParents(existsNode));
				Set<Set<Description>> parentsExistInv = new HashSet<Set<Description>>(
						getDirectParents(existsInvNode));

				for (Set<Description> parents : parentExist) {
					for (Description parent : parents) {
						// DAGOperations.addParentEdge(existsInvNode, parent);
						graph.getGraph().addEdge(existsInvNode, parent);

					}
				}

				for (Set<Description> parents : parentsExistInv) {
					for (Description parent : parents) {
						// DAGOperations.addParentEdge(existsNode,parent);
						graph.getGraph().addEdge(existsNode, parent);

					}
				}

				processedNodes.add(existsInvNode);
				processedNodes.add(existsNode);

			/* Collapsing the cycles */
			dag = DAGBuilder.getDAG(graph);

		}
	}
	
	
	public Ontology getSigmaOntology() {
		throw new IllegalArgumentException("getSigmaOntology is not supported");
	}

	@Override
	public Set<Set<Description>> getNodes() {
		// TODO Auto-generated method stub
		return null;
	}
}
