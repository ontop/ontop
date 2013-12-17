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

public class TBoxReasonerImpl implements TBoxReasoner {

	private DAGImpl dag;

	/**
	 * Constructor using a DAG or a named DAG
	 * @param dag DAG to be used for reasoning
	 */
	public TBoxReasonerImpl(DAGImpl dag) {
		this.dag = dag;
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
			if (!equivalences.isEmpty())
				result.add(equivalences);
		}

		return Collections.unmodifiableSet(result);
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

			if (!equivalences.isEmpty())
				result.add(equivalences);
		}

		return Collections.unmodifiableSet(result);
	}


	/**
	 * Traverse the graph return the descendants starting from the given node of
	 * the dag
	 * 
	 * @param desc node we want to know the descendants
	 *
	 * @return we return a set of set of description to distinguish between
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
	 *
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
	 *            
	 * @return we return a set of description with equivalent nodes 
	 */

	@Override
	public Set<Description> getEquivalences(Description desc) {

		EquivalenceClass<Description> equivalents = dag.getEquivalenceClass(desc);

		// if there are no equivalent nodes return the node or nothing
		if (equivalents == null) 
			return Collections.unmodifiableSet(Collections.singleton(desc));
				
		return Collections.unmodifiableSet(equivalents.getMembers());
	}
	
	/**
	 * Return all the nodes in the DAG or graph
	 * 
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

	
	/***
	 * Modifies the DAG so that \exists R = \exists R-, so that the reachability
	 * relation of the original DAG gets extended to the reachability relation
	 * of T and Sigma chains.
	 * 
	 * 
	 */
	
	public void convertIntoChainDAG() {

		// move everything to a graph that admits cycles
			DefaultDirectedGraph<Description,DefaultEdge> modifiedGraph = 
					new  DefaultDirectedGraph<Description,DefaultEdge>(DefaultEdge.class);

			// clone all the vertex and edges from dag

			for (Description v : dag.vertexSet()) {
				modifiedGraph.addVertex(v);

			}
			for (DefaultEdge e : dag.edgeSet()) {
				Description s = dag.getEdgeSource(e);
				Description t = dag.getEdgeTarget(e);

				modifiedGraph.addEdge(s, t, e);
			}

			Collection<Description> nodes = new HashSet<Description>(
					dag.vertexSet());
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
				Description existsInvNode = dag.getNode(existsRin);
				Set<Set<Description>> childrenExist = new HashSet<Set<Description>>(
						getDirectChildren(existsNode));
				Set<Set<Description>> childrenExistInv = new HashSet<Set<Description>>(
						getDirectChildren(existsInvNode));

				for (Set<Description> children : childrenExist) {
					// for(Description child:children){
					// DAGOperations.addParentEdge(child, existsInvNode);
					Description firstChild = children.iterator().next();
					Description child = dag.getReplacementFor(firstChild);
					if (child == null)
						child = firstChild;
					if (!child.equals(existsInvNode))
						modifiedGraph.addEdge(child, existsInvNode);

					// }
				}
				for (Set<Description> children : childrenExistInv) {
					// for(Description child:children){
					// DAGOperations.addParentEdge(child, existsNode);
					Description firstChild = children.iterator().next();
					Description child = dag.getReplacementFor(firstChild);
					if (child == null)
						child = firstChild;
					if (!child.equals(existsNode))
						modifiedGraph.addEdge(child, existsNode);

					// }
				}

				Set<Set<Description>> parentExist = new HashSet<Set<Description>>(
						getDirectParents(existsNode));
				Set<Set<Description>> parentsExistInv = new HashSet<Set<Description>>(
						getDirectParents(existsInvNode));

				for (Set<Description> parents : parentExist) {
					Description firstParent = parents.iterator().next();
					Description parent = dag.getReplacementFor(firstParent);
					if (parent == null)
						parent = firstParent;
					if (!parent.equals(existsInvNode))
						modifiedGraph.addEdge(existsInvNode, parent);

					// }
				}

				for (Set<Description> parents : parentsExistInv) {
					Description firstParent = parents.iterator().next();
					Description parent = dag.getReplacementFor(firstParent);
					if (parent == null)
						parent = firstParent;
					if (!parent.equals(existsInvNode))
						modifiedGraph.addEdge(existsNode, parent);

					// }
				}

				processedNodes.add(existsInvNode);
				processedNodes.add(existsNode);
			}

			/* Collapsing the cycles */
			dag = DAGBuilder.getDAG(modifiedGraph,
					dag.getMapEquivalences(), dag.getReplacements());

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

	public static Ontology getSigma(Ontology ontology) {
		OntologyFactory descFactory = new OntologyFactoryImpl();
		Ontology sigma = descFactory.createOntology("sigma");
		sigma.addConcepts(ontology.getConcepts());
		sigma.addRoles(ontology.getRoles());
		for (Axiom assertion : ontology.getAssertions()) {

			if (assertion instanceof SubClassAxiomImpl) {
				SubClassAxiomImpl inclusion = (SubClassAxiomImpl) assertion;
				Description parent = inclusion.getSuper();

				if (parent instanceof PropertySomeRestriction) {
					continue;
				}
			}

			sigma.addAssertion(assertion);
		}

		sigma.saturate();
		return sigma;
	}
}
