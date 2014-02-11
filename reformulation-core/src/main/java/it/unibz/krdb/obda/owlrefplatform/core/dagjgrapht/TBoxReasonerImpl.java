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
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 * Allows to reason over the TBox using  DAG or graph
 * 
 */

public class TBoxReasonerImpl implements TBoxReasoner {

	private final DefaultDirectedGraph<Property,DefaultEdge> propertyGraph; // test only
	private final DefaultDirectedGraph<BasicClassDescription,DefaultEdge> classGraph; // test only

	private final EquivalencesDAG<Property> propertyDAG;
	private final EquivalencesDAG<BasicClassDescription> classDAG;
	private final EquivalencesDAG<Description> dag;
	
	private Set<OClass> classNames;
	private Set<Property> propertyNames;
	

	

	public TBoxReasonerImpl(Ontology onto) {
		propertyGraph = OntologyGraph.getPropertyGraph(onto);
		propertyDAG = new EquivalencesDAG<Property>(propertyGraph);
		
		classGraph = OntologyGraph.getClassGraph(onto, propertyGraph, false);
		classDAG = new EquivalencesDAG<BasicClassDescription>(classGraph);

		DefaultDirectedGraph<Description,DefaultEdge> graph = new DefaultDirectedGraph<Description,DefaultEdge>(DefaultEdge.class);
		Graphs.addGraph(graph, propertyGraph);
		Graphs.addGraph(graph, classGraph);

		dag = new EquivalencesDAG<Description>(graph);
		
		setup();
	}

	private TBoxReasonerImpl(DefaultDirectedGraph<Property,DefaultEdge> propertyGraph, 
					DefaultDirectedGraph<BasicClassDescription,DefaultEdge> classGraph) {
		this.propertyGraph = propertyGraph;
		propertyDAG = new EquivalencesDAG<Property>(propertyGraph);
		
		this.classGraph = classGraph;
		classDAG = new EquivalencesDAG<BasicClassDescription>(classGraph);
		
		DefaultDirectedGraph<Description,DefaultEdge> graph = new DefaultDirectedGraph<Description,DefaultEdge>(DefaultEdge.class);
		Graphs.addGraph(graph, propertyGraph);
		Graphs.addGraph(graph, classGraph);

		dag = new EquivalencesDAG<Description>(graph);
		
		setup();
	}

	
	private void setup() {
		DAGBuilder.choosePropertyRepresentatives(dag);
		DAGBuilder.chooseClassRepresentatives(dag);

		for (Equivalences<BasicClassDescription> e : classDAG.vertexSet()) {
			Equivalences<Description> de = dag.getVertex(e.iterator().next());
			e.setRepresentative((BasicClassDescription)de.getRepresentative());
			if (de.isIndexed())
				e.setIndexed();
		}
		for (Equivalences<Property> e : propertyDAG.vertexSet()) {
			Equivalences<Description> de = dag.getVertex(e.iterator().next());
			e.setRepresentative((Property)de.getRepresentative());
			if (de.isIndexed())
				e.setIndexed();
		}		
	}



	@Override
	public String toString() {
		return propertyDAG.toString() + "\n" + classDAG.toString();
	}
	
	
	public Description getRepresentativeFor(Description v) {
		if (v instanceof Property) {
			Equivalences<Property> e = propertyDAG.getVertex((Property)v);
			if (e != null)
				return e.getRepresentative();
		}
		else {
			Equivalences<BasicClassDescription> e = classDAG.getVertex((BasicClassDescription)v);
			if (e != null)
				return e.getRepresentative();			
		}
		return null;
	}
	
	
	public boolean isCanonicalRepresentative(Property v) {
		//return (replacements.get(v) == null);
		Equivalences<Property> e = propertyDAG.getVertex(v);
		return e.getRepresentative().equals(v);
	}
	public boolean isCanonicalRepresentative(BasicClassDescription v) {
		//return (replacements.get(v) == null);
		Equivalences<BasicClassDescription> e = classDAG.getVertex(v);
		return e.getRepresentative().equals(v);
	}

	
	

	/**
	 * Allows to have all named roles in the DAG even the equivalent named roles
	 * @return  set of all property (not inverse) in the DAG
	 */
	public Set<Property> getPropertyNames() {
		if (propertyNames == null) {
			propertyNames = new LinkedHashSet<Property> ();
			for (Equivalences<Property> v: propertyDAG.vertexSet()) 
				for (Property r : v) 
					if (!r.isInverse())
						propertyNames.add(r);
		}
		return propertyNames;
	}

	/**
	 * Allows to have all named classes in the DAG even the equivalent named classes
	 * @return  set of all named concepts in the DAG
	 */
	
	public Set<OClass> getClassNames() {
		if (classNames == null) {
			 classNames = new LinkedHashSet<OClass> ();
			 for (Equivalences<BasicClassDescription> v: classDAG.vertexSet())
				for (BasicClassDescription e : v)
					if (e instanceof OClass)
						classNames.add((OClass)e);
		}
		return classNames;
	}

	
	public boolean isNamed(Property node) {
		return propertyDAG.getVertex(node).isIndexed();
	}
	public boolean isNamed(BasicClassDescription node) {
		return classDAG.getVertex(node).isIndexed();
	}

//	public boolean isNamed0(Description node) {
//		return getClassNames().contains(node) || getPropertyNames().contains(node);
//	}

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
		return propertyDAG.getDirectChildren(propertyDAG.getVertex(desc));
	}
	@Override
	public Set<Equivalences<BasicClassDescription>> getDirectSubClasses(BasicClassDescription desc) {
		return classDAG.getDirectChildren(classDAG.getVertex(desc));
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
		return propertyDAG.getDirectParents(propertyDAG.getVertex(desc));
	}
	@Override
	public Set<Equivalences<BasicClassDescription>> getDirectSuperClasses(BasicClassDescription desc) {
		return classDAG.getDirectParents(classDAG.getVertex(desc));
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
	public Set<Equivalences<Property>> getSubProperties(Property desc) {
		return propertyDAG.getDescendants(propertyDAG.getVertex(desc));
	}
	@Override
	public Set<Equivalences<BasicClassDescription>> getSubClasses(BasicClassDescription desc) {
		return classDAG.getDescendants(classDAG.getVertex(desc));
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
	public Set<Equivalences<Property>> getSuperProperties(Property desc) {
		return propertyDAG.getAncestors(propertyDAG.getVertex(desc));
	}
	@Override
	public Set<Equivalences<BasicClassDescription>> getSuperClasses(BasicClassDescription desc) {
		return classDAG.getAncestors(classDAG.getVertex(desc));
	}

	
	/**
	 * Return the equivalences starting from the given node of the dag
	 * 
	 * @param desc node we want to know the ancestors
	 *            
	 * @return we return a set of description with equivalent nodes 
	 */

	@Override
	public Equivalences<Property> getEquivalences(Property desc) {
		return propertyDAG.getVertex(desc);
	}
	@Override
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

	@Deprecated // TESTS ONLY
	public Set<Equivalences<Description>> getNodes() {
		return dag.vertexSet();
	}
	
	@Override
	public Set<Equivalences<BasicClassDescription>> getClasses() {
		return classDAG.vertexSet();
	}

	public Set<Equivalences<Property>> getProperties() {
		return propertyDAG.vertexSet();
	}
	
	
	// INTERNAL DETAILS
	

	
	
	@Deprecated // test only
	public DefaultDirectedGraph<BasicClassDescription,DefaultEdge> getClassGraph() {
		return classGraph;
	}
	
	@Deprecated // test only
	public DefaultDirectedGraph<Property,DefaultEdge> getPropertyGraph() {
		return propertyGraph;
	}
	
	@Deprecated // test only
	public int edgeSetSize() {
		return propertyDAG.edgeSetSize() + classDAG.edgeSetSize();
	}
	
	@Deprecated // test only
	public int vertexSetSize() {
		return propertyDAG.vertexSet().size() + classDAG.vertexSet().size();
	}
	
	
	
	
	

//	public static TBoxReasonerImpl getChainReasoner2(Ontology onto) {
//		
//		return new TBoxReasonerImpl((OntologyGraph.getGraph(onto, true)));		
//	}
	
	/***
	 * Modifies the DAG so that \exists R = \exists R-, so that the reachability
	 * relation of the original DAG gets extended to the reachability relation
	 * of T and Sigma chains.
	 * 
	 * 
	 */
	
	public static TBoxReasonerImpl getChainReasoner(Ontology onto) {
		TBoxReasonerImpl tbox = new TBoxReasonerImpl(onto);
		
		
		// move everything to a graph that admits cycles
		DefaultDirectedGraph<BasicClassDescription,DefaultEdge> modifiedGraph = 
				new  DefaultDirectedGraph<BasicClassDescription,DefaultEdge>(DefaultEdge.class);

		// clone all the vertex and edges from dag
		for (Equivalences<BasicClassDescription> v : tbox.getClasses()) 
			modifiedGraph.addVertex(v.getRepresentative());
		
		for (Equivalences<BasicClassDescription> v : tbox.getClasses()) {
			BasicClassDescription s = v.getRepresentative();
			for (Equivalences<BasicClassDescription> vp : tbox.getDirectSuperClasses(s))
				modifiedGraph.addEdge(s, vp.getRepresentative());
		}

		OntologyFactory fac = OntologyFactoryImpl.getInstance();
		HashSet<Description> processedNodes = new HashSet<Description>();
		
		for (Equivalences<BasicClassDescription> n : tbox.getClasses()) {
			BasicClassDescription node = n.getRepresentative();
			
			if (!(node instanceof PropertySomeRestriction) || processedNodes.contains(node)) 
				continue;

			/*
			 * Adding a cycle between exists R and exists R- for each R.
			 */

			PropertySomeRestriction existsNode = (PropertySomeRestriction) node;
			BasicClassDescription existsInvNode = (BasicClassDescription)tbox.getRepresentativeFor(
						fac.createPropertySomeRestriction(existsNode.getPredicate(), !existsNode.isInverse()));
			
			for (Equivalences<BasicClassDescription> children : tbox.getDirectSubClasses(existsNode)) {
				BasicClassDescription child = children.getRepresentative(); 
				if (!child.equals(existsInvNode))
					modifiedGraph.addEdge(child, existsInvNode);
			}
			for (Equivalences<BasicClassDescription> children : tbox.getDirectSubClasses(existsInvNode)) {
				BasicClassDescription child = children.getRepresentative(); 
				if (!child.equals(existsNode))
					modifiedGraph.addEdge(child, existsNode);
			}

			for (Equivalences<BasicClassDescription> parents : tbox.getDirectSuperClasses(existsNode)) {
				BasicClassDescription parent = parents.getRepresentative(); 
				if (!parent.equals(existsInvNode))
					modifiedGraph.addEdge(existsInvNode, parent);
			}

			for (Equivalences<BasicClassDescription> parents : tbox.getDirectSuperClasses(existsInvNode)) {
				BasicClassDescription parent = parents.getRepresentative(); 
				if (!parent.equals(existsInvNode))
					modifiedGraph.addEdge(existsNode, parent);
			}

			processedNodes.add(existsNode);
			processedNodes.add(existsInvNode);
		}

		/* Collapsing the cycles */
		return new TBoxReasonerImpl(tbox.propertyGraph, modifiedGraph);
	}

}
