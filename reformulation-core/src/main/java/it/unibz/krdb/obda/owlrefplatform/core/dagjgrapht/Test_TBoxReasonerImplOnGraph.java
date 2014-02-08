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
import org.jgrapht.traverse.BreadthFirstIterator;

/**
 * Allows to reason over the TBox using a TBox graph
 * 
 * WARNING: THIS CLASS IS ONLY FOR TESTING
 * 
 */

public class Test_TBoxReasonerImplOnGraph implements TBoxReasoner {

	private DefaultDirectedGraph<Property,DefaultEdge> propertyGraph;
	private DefaultDirectedGraph<BasicClassDescription,DefaultEdge> classGraph;
	

	public Test_TBoxReasonerImplOnGraph(TBoxReasonerImpl reasoner) {	
		this.propertyGraph = reasoner.getPropertyGraph();
		this.classGraph = reasoner.getClassGraph();
	}
	
//	public boolean isNamed0(Description vertex) {
//		return property.contains(vertex) || namedClasses.contains(vertex);
//	}
	
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
	private static <T> Set<Equivalences<T>> getDirectSub(T desc, DefaultDirectedGraph<T,DefaultEdge> graph) {
		
		LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<Equivalences<T>>();

			// get equivalences of the current node
		Equivalences<T> equivalenceSet = getEquivalences(desc, graph);
		// I want to consider also the children of the equivalent nodes
		for (T n : equivalenceSet) {
			Set<DefaultEdge> edges = graph.incomingEdgesOf(n);
			for (DefaultEdge edge : edges) {
				T source = graph.getEdgeSource(edge);

				// I don't want to consider as children the equivalent node
				// of the current node desc
				if (equivalenceSet.contains(source)) 
					continue;
				
				Equivalences<T> equivalences = getEquivalences(source, graph);
					/* 
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
					} */

				if (!equivalences.isEmpty())
					result.add(equivalences);
			}
		}
	
		return Collections.unmodifiableSet(result);
	}

	public Set<Equivalences<Property>> getDirectSubProperties(Property desc) {
		return getDirectSub(desc, propertyGraph);
	}
	
	public Set<Equivalences<BasicClassDescription>> getDirectSubClasses(BasicClassDescription desc) {
		return getDirectSub(desc, classGraph);
	}
	
	/*
	 *  Private method that searches for the first named children
	 */

	/*
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
	*/

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
	private static <T> Set<Equivalences<T>> getDirectSuper(T desc, DefaultDirectedGraph<T,DefaultEdge> graph) {
			
		LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<Equivalences<T>>();

		// get equivalences of the current node
		Equivalences<T> equivalenceSet = getEquivalences(desc, graph);

		// I want to consider also the parents of the equivalent nodes
		for (T n : equivalenceSet) {
			Set<DefaultEdge> edges = graph.outgoingEdgesOf(n);
			for (DefaultEdge edge : edges) {
				T target = graph.getEdgeTarget(edge);

				// I don't want to consider as parents the equivalent node
				// of the current node desc
				if (equivalenceSet.contains(target)) 
					continue;
				
				Equivalences<T> equivalences = getEquivalences(target, graph);

					/* if (named) { // if true I search only for the named nodes

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

					} */
				if (!equivalences.isEmpty())
					result.add(equivalences);
			}
		}

		return Collections.unmodifiableSet(result);
	}

	public Set<Equivalences<Property>> getDirectSuperProperties(Property desc) {
		return getDirectSuper(desc, propertyGraph);
	}
	
	public Set<Equivalences<BasicClassDescription>> getDirectSuperClasses(BasicClassDescription desc) {
		return getDirectSuper(desc, classGraph);
	}
	
	/*
	 *  private method that search for the first named parents
	 */
	/*
	private Set<Equivalences<Description>> getNamedParents(Description desc) {

		LinkedHashSet<Equivalences<Description>> result = new LinkedHashSet<Equivalences<Description>>();

			// get equivalences of the current node
		Equivalences<Description> equivalenceSet = getEquivalences(desc);
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
	*/

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
	public static <T> Set<Equivalences<T>> getSup(T desc, DirectedGraph<T, DefaultEdge> graph) {
		
		LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<Equivalences<T>>();
		BreadthFirstIterator<T, DefaultEdge> iterator = new BreadthFirstIterator<T, DefaultEdge>(graph, desc);

		while (iterator.hasNext()) {
			T node = iterator.next();

				/* if (named) { // add only the named classes and property
					if (namedClasses.contains(node) | property.contains(node)) {
						Set<Description> sources = new HashSet<Description>();
						sources.add(node);

						result.add(new Equivalences<Description>(sources));
					}
				} */
			Set<T> sources = new HashSet<T>();
			sources.add(node);
			result.add(new Equivalences<T>(sources));
		}
		// add each of them to the result
		return Collections.unmodifiableSet(result);
	}
	
	public Set<Equivalences<Property>> getSubProperties(Property desc) {
		return getSup(desc, new EdgeReversedGraph<Property, DefaultEdge>(propertyGraph));
	}
	
	public Set<Equivalences<BasicClassDescription>> getSubClasses(BasicClassDescription desc) {
		return getSup(desc, new EdgeReversedGraph<BasicClassDescription, DefaultEdge>(classGraph));
	}

	public Set<Equivalences<Property>> getSuperProperties(Property desc) {
		return getSup(desc, propertyGraph);
	}

	public Set<Equivalences<BasicClassDescription>> getSuperClasses(BasicClassDescription desc) {
		return getSup(desc, classGraph);
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
	
	public static <T> Equivalences<T> getEquivalences(T desc, DefaultDirectedGraph<T,DefaultEdge> graph) {

		// search for cycles
		StrongConnectivityInspector<T, DefaultEdge> inspector = new StrongConnectivityInspector<T, DefaultEdge>(graph);

		// each set contains vertices which together form a strongly
		// connected component within the given graph
		List<Set<T>> equivalenceSets = inspector.stronglyConnectedSets();

		// I want to find the equivalent node of desc
		for (Set<T> equivalenceSet : equivalenceSets) {
			if (equivalenceSet.size() >= 2) {
				if (equivalenceSet.contains(desc)) {
					/* if (named) {
							Set<Description> equivalences = new LinkedHashSet<Description>();
							for (Description vertex : equivalenceSet) {
								if (namedClasses.contains(vertex)
										| property.contains(vertex)) {
									equivalences.add(vertex);
								}
							}
							return new Equivalences<Description>(equivalences);
						}
					*/
					return new Equivalences<T>(equivalenceSet, equivalenceSet.iterator().next());
				}
			}
		}

		// if there are not equivalent node return the node or nothing
		/* if (named) {
			if (namedClasses.contains(desc) | property.contains(desc)) {
					return new Equivalences<Description>(Collections
							.singleton(desc));
				} else { // return empty set if the node we are considering
							// (desc) is not a named class or propertu
					equivalences = Collections.emptySet();
					return new Equivalences<Description>(equivalences);
				}
		}*/
		return new Equivalences<T>(Collections.singleton(desc), desc);
	}

	public Equivalences<Property> getEquivalences(Property desc) {
		return getEquivalences(desc, propertyGraph);
	}
	
	public Equivalences<BasicClassDescription> getEquivalences(BasicClassDescription desc) {
		return getEquivalences(desc, classGraph);
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

	public Set<Equivalences<Property>> getProperties() {
		LinkedHashSet<Equivalences<Property>> result = new LinkedHashSet<Equivalences<Property>>();

		for (Property vertex : propertyGraph.vertexSet()) {
				result.add(getEquivalences(vertex));
			}

		return result;
	}
	public Set<Equivalences<BasicClassDescription>> getClasses() {
		LinkedHashSet<Equivalences<BasicClassDescription>> result = new LinkedHashSet<Equivalences<BasicClassDescription>>();

		for (BasicClassDescription vertex : classGraph.vertexSet()) {
				result.add(getEquivalences(vertex));
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

		Collection<BasicClassDescription> nodes = new HashSet<BasicClassDescription>(classGraph.vertexSet());
		OntologyFactory fac = OntologyFactoryImpl.getInstance();
		HashSet<BasicClassDescription> processedNodes = new HashSet<BasicClassDescription>();
		for (BasicClassDescription node : nodes) {
			if (!(node instanceof PropertySomeRestriction)
					|| processedNodes.contains(node)) {
				continue;
			}

			/*
			 * Adding a cycle between exists R and exists R- for each R.
			 */

			PropertySomeRestriction existsR = (PropertySomeRestriction) node;
			PropertySomeRestriction existsRin = fac.createPropertySomeRestriction(existsR.getPredicate(),!existsR.isInverse());
			
			BasicClassDescription existsNode = node;
			BasicClassDescription existsInvNode = existsRin;
			
			Set<Equivalences<BasicClassDescription>> childrenExist 
					= new HashSet<Equivalences<BasicClassDescription>>(getDirectSubClasses(existsNode));
			Set<Equivalences<BasicClassDescription>> childrenExistInv 
					= new HashSet<Equivalences<BasicClassDescription>>(getDirectSubClasses(existsInvNode));

			for (Equivalences<BasicClassDescription> children : childrenExist) {
				for (BasicClassDescription child : children) 
					classGraph.addEdge(child, existsInvNode);
			}
			for (Equivalences<BasicClassDescription> children : childrenExistInv) {
				for (BasicClassDescription child : children) 
					classGraph.addEdge(child, existsNode);
			}

			Set<Equivalences<BasicClassDescription>> parentExist 
					= new HashSet<Equivalences<BasicClassDescription>>(getDirectSuperClasses(existsNode));
			Set<Equivalences<BasicClassDescription>> parentsExistInv 
					= new HashSet<Equivalences<BasicClassDescription>>(getDirectSuperClasses(existsInvNode));

			for (Equivalences<BasicClassDescription> parents : parentExist) {
				for (BasicClassDescription parent : parents) 
					classGraph.addEdge(existsInvNode, parent);
			}

			for (Equivalences<BasicClassDescription> parents : parentsExistInv) {
				for (BasicClassDescription parent : parents) 
					classGraph.addEdge(existsNode, parent);
			}

			processedNodes.add(existsInvNode);
			processedNodes.add(existsNode);
		}
	}

	public int vertexSetSize() {
		return propertyGraph.vertexSet().size() + classGraph.vertexSet().size();
	}

	public int edgeSetSize() {
		return propertyGraph.edgeSet().size() + classGraph.edgeSet().size();
	}

	public DefaultDirectedGraph<Property, DefaultEdge> getPropertyGraph() {
		return propertyGraph;
	}
	public DefaultDirectedGraph<BasicClassDescription, DefaultEdge> getClassGraph() {
		return classGraph;
	}
	
}
