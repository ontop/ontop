/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */

package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.Axiom;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.ClassDescription;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.ontology.impl.SubClassAxiomImpl;
import it.unibz.krdb.obda.ontology.impl.SubPropertyAxiomImpl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

/**
 * Starting from a graph build a DAG. 
 * Considering  equivalences and redundancies, it eliminates cycles executes
 * transitive reduction.
	// contains the representative node with the set of equivalent mappings.
 */

/***
 * A map to keep the relationship between 'eliminated' and 'remaining'
 * nodes, created while computing equivalences by eliminating cycles in the
 * graph.
 */

public class DAGBuilder {


	/**
	 * *Construct a DAG starting from a given graph with already known
	 * equivalent nodes and representative nodes
	 * 
	 * @param graph needs a graph with or without cycles
	 * @param equivalents a map between the node and its equivalent nodes
	 * @param representatives a map between the node and its representative node
	 */
	public static SimpleDirectedGraph <Description,DefaultEdge> getDAG(DefaultDirectedGraph<Description,DefaultEdge> graph, 
			Map<Description, EquivalenceClass<Description>> equivalencesMap) {

		DefaultDirectedGraph<Description,DefaultEdge> copy = 
				new DefaultDirectedGraph<Description,DefaultEdge>(DefaultEdge.class);

		for (Description v : graph.vertexSet()) 
			copy.addVertex(v);

		for (DefaultEdge e : graph.edgeSet()) {
			Description s = graph.getEdgeSource(e);
			Description t = graph.getEdgeTarget(e);

			copy.addEdge(s, t, e);
		}
	
		Map<Description, Description> replacements = new HashMap<Description, Description>();
		eliminateCycles(copy, equivalencesMap, replacements);
		eliminateRedundantEdges(copy);
		
		for (Description d : replacements.keySet()) {
			EquivalenceClass<Description> cl = equivalencesMap.get(d);
			Description rep = replacements.get(d);
			cl.setRepresentative(rep);
		}

		SimpleDirectedGraph <Description,DefaultEdge> dag = new SimpleDirectedGraph <Description,DefaultEdge> (DefaultEdge.class);
		Graphs.addGraph(dag, copy);
		
		return dag;
	}

	/***
	 * Eliminates redundant edges to ensure that the remaining DAG is the
	 * transitive reduction of the original DAG.
	 * 
	 * <p>
	 * This is done with an ad-hoc algorithm that functions as follows:
	 * 
	 * <p>
	 * Compute the set of all nodes with more than 2 outgoing edges (these have
	 * candidate redundant edges.) <br>
	 */
	private static void eliminateRedundantEdges(DefaultDirectedGraph<Description,DefaultEdge> graph) {
		/* Compute the candidate nodes */
		List<Description> candidates = new LinkedList<Description>();
		Set<Description> vertexes = graph.vertexSet();
		for (Description vertex : vertexes) {
			int outdegree = graph.outDegreeOf(vertex);
			if (outdegree > 1) {
				candidates.add(vertex);
			}
		}

		/*
		 * for each candidate x and each outgoing edge x -> y, we will check if
		 * y appears in the set of redundant edges
		 */

		for (Description candidate : candidates) {

			Set<DefaultEdge> possiblyRedundantEdges = new LinkedHashSet<DefaultEdge>();

			possiblyRedundantEdges.addAll(graph.outgoingEdgesOf(candidate));

			Set<DefaultEdge> eliminatedEdges = new HashSet<DefaultEdge>();

			// registering the target of the possible redundant targets for this
			// node
			Set<Description> targets = new HashSet<Description>();

			Map<Description, DefaultEdge> targetEdgeMap = new HashMap<Description, DefaultEdge>();

			for (DefaultEdge edge : possiblyRedundantEdges) {
				Description target = graph.getEdgeTarget(edge);
				targets.add(target);
				targetEdgeMap.put(target, edge);
			}

			for (DefaultEdge currentPathEdge : possiblyRedundantEdges) {
				Description currentTarget = graph.getEdgeTarget(currentPathEdge);
				if (eliminatedEdges.contains(currentPathEdge))
					continue;
				eliminateRedundantEdge(graph, currentPathEdge, targets, targetEdgeMap,
						currentTarget, eliminatedEdges);
			}

		}

	}

	private static void eliminateRedundantEdge(DefaultDirectedGraph<Description,DefaultEdge> graph, 
			DefaultEdge safeEdge,
			Set<Description> targets,
			Map<Description, DefaultEdge> targetEdgeMap,
			Description currentTarget, Set<DefaultEdge> eliminatedEdges) {
		
		if (targets.contains(currentTarget)) {
			DefaultEdge edge = targetEdgeMap.get(currentTarget);
			if (!edge.equals(safeEdge)) {
				/*
				 * This is a redundant edge, removing it.
				 */
				graph.removeEdge(edge);
				eliminatedEdges.add(edge);
			}
		}

		// continue traversing the dag up
		Set<DefaultEdge> edgesInPath = graph.outgoingEdgesOf(currentTarget);
		for (DefaultEdge outEdge : edgesInPath) {
			Description target = graph.getEdgeTarget(outEdge);
			// System.out.println("target "+target+" "+outEdge);
			eliminateRedundantEdge(graph, safeEdge, targets, targetEdgeMap, target, eliminatedEdges);
		}

	}

	/***
	 * Eliminates all cycles in the graph by computing all strongly connected
	 * components and eliminating all but one node in each of the components
	 * from the graph. The result of this transformation is that the graph
	 * becomes a DAG.
	 * 
	 * 
	 * <p>
	 * In the process two objects are generated, an 'Equivalence map' and a
	 * 'replacementMap'. The first can be used to get the implied equivalences
	 * of the TBox. The second can be used to locate the node that is
	 * representative of an eliminated node.
	 * 
	 * <p>
	 * Computation of the strongly connected components is done using Gabow SCC
	 * algorithm.
	 * 
	 */

	private static void eliminateCycles(DefaultDirectedGraph<Description,DefaultEdge> graph, 
			Map<Description, EquivalenceClass<Description>> equivalencesMap, 
			Map<Description, Description> replacements) {

		GabowSCC<Description, DefaultEdge> inspector = new GabowSCC<Description, DefaultEdge>(graph);

		// each set contains vertices which together form a strongly connected
		// component within the given graph
		List<EquivalenceClass<Description>> equivalenceSets = inspector
				.stronglyConnectedSets();

		for (EquivalenceClass<Description> equivalenceSet : equivalenceSets) {
			if (equivalenceSet.size() > 1)
				for (Description node : equivalenceSet) 
					equivalencesMap.put(node, equivalenceSet);
		}
		
		
		OntologyFactory fac = OntologyFactoryImpl.getInstance();

		/*
		 * A set with all the nodes that have been processed as participating in
		 * an equivalence cycle. If a component contains any of these nodes, the
		 * component should be ignored, since a cycle involving the same nodes
		 * or nodes for inverse descriptions has already been processed.
		 */
		Set<Property> processedProperties = new HashSet<Property>();
		Set<Property> chosenProperties = new HashSet<Property>();

		// PROCESSING ONLY PROPERTIES FIRST
		
		for (EquivalenceClass<Description> equivalenceSet : equivalenceSets) {

			if (equivalenceSet.size() < 2)
				continue;

			boolean ignore = false;

			for (Description e : equivalenceSet) 
				if (!(e instanceof Property) || processedProperties.contains(e)) { 
					ignore = true;
					break;
				}
			if (ignore)
				continue;
			
			/*
			 * We consider first the elements that are connected with other
			 * named roles, checking if between their parents there is an
			 * already assigned named role or an inverse
			 */

			for (Description e : equivalenceSet) {
				for (DefaultEdge outEdge : graph.outgoingEdgesOf(e)) {
					Property target = (Property) graph.getEdgeTarget(outEdge);

					if (chosenProperties.contains(target) || 
							(processedProperties.contains(target) && target.isInverse())) {
						ignore = true;
						break;
					}
				}
				if (ignore) 
					break;
			}
			if (ignore) {
				for (Description e : equivalenceSet) 
					chosenProperties.add((Property)e);
				
				continue;				
			}

			/* Assign the representative role with its inverse, domain and range
			 * 
			 */
			
			// find a representative 
			Property prop = null;
			for (Description representative : equivalenceSet) 
				if (!((Property) representative).isInverse()) {
					prop = (Property)representative;
					break;
				}
			
			if (prop == null) 	// equivalence class contains inverses only 
				continue;		// they are processed when we consider their properties

			Property inverse = fac.createProperty(prop.getPredicate(), !prop.isInverse());
			PropertySomeRestriction domain = fac.createPropertySomeRestriction(prop.getPredicate(), prop.isInverse());
			PropertySomeRestriction range = fac.createPropertySomeRestriction(prop.getPredicate(), !prop.isInverse());

			// remove all the equivalent node
			for (Description e : equivalenceSet) {			
				Property eProp = (Property) e;

				// remove everything but the representative
				if (e != prop) {
					removeNodeAndRedirectEdges(graph, e, prop, replacements);
					
					PropertySomeRestriction eDomain = fac.createPropertySomeRestriction(eProp.getPredicate(), eProp.isInverse());
					replacements.put(eDomain, domain); // set the representative
				}		
				processedProperties.add(eProp);

				Property eInverse = fac.createProperty(eProp.getPredicate(), !eProp.isInverse());
				
				if ((e != prop) && !eInverse.equals(prop))  {
					// if the inverse is not equivalent to the representative 
					// then we remove the inverse (with its domain)
					// but keep the representative for the inverse
					removeNodeAndRedirectEdges(graph, eInverse, inverse, replacements);	

					PropertySomeRestriction eRange = fac.createPropertySomeRestriction(eProp.getPredicate(), !eProp.isInverse());
					replacements.put(eRange, range); // set the representative
				}
				processedProperties.add(eInverse);
			}
		}

		/*
		 * PROCESS CLASSES ONLY
		 */

		for (EquivalenceClass<Description> equivalenceClassSet : equivalenceSets) {

			Description first = equivalenceClassSet.iterator().next();
			if (!(first instanceof BasicClassDescription))
				continue;
			
			BasicClassDescription representative = null;
			
			// find a named class as a representative 
			for (Description e : equivalenceClassSet) 
				if (e instanceof OClass) {
					representative = (BasicClassDescription)e;
					break;
				}
			if (representative == null) {
				BasicClassDescription nodeReplacement = (BasicClassDescription)replacements.get(first);	
				representative = (nodeReplacement == null) ? (BasicClassDescription)first : nodeReplacement;
			}

			for (Description e : equivalenceClassSet) {
				// careful -- proper equality check is required because the replacement is "generated"
				if (!e.equals(representative))  
					removeNodeAndRedirectEdges(graph, e, representative, replacements);
			}
		}
	}
	
	private static void removeNodeAndRedirectEdges(DefaultDirectedGraph<Description,DefaultEdge> graph, 
			Description eliminatedNode, Description representative, Map<Description, Description> replacements) {
		/*
		 * Re-pointing all links to and from the eliminated node to
		 * the representative node
		 */

		for (DefaultEdge incEdge : graph.incomingEdgesOf(eliminatedNode)) {
			Description source = graph.getEdgeSource(incEdge);

			if (!source.equals(representative))
				graph.addEdge(source, representative);
		}

		for (DefaultEdge outEdge : graph.outgoingEdgesOf(eliminatedNode)) {
			Description target = graph.getEdgeTarget(outEdge);

			if (!target.equals(representative))
				graph.addEdge(representative, target);
		}

		graph.removeVertex(eliminatedNode);		// removes all edges as well
		
		replacements.put(eliminatedNode, representative);	
	}
	


	
	
	/**
	 * Build the graph from the TBox axioms of the ontology
	 * 
	 * @param ontology TBox containing the axioms
	 * @param chain 
	 * Modifies the DAG so that \exists R = \exists R-, so that the reachability
	 * relation of the original DAG gets extended to the reachability relation
	 * of T and Sigma chains.
	 */
	public static DefaultDirectedGraph<Description,DefaultEdge> getGraph (Ontology ontology, boolean chain) {
		
		DefaultDirectedGraph<Description,DefaultEdge> graph = new  DefaultDirectedGraph<Description,DefaultEdge>(DefaultEdge.class);
		OntologyFactory descFactory = OntologyFactoryImpl.getInstance();
		
		
		for (Predicate conceptp : ontology.getConcepts()) {
			ClassDescription concept = descFactory.createClass(conceptp);
			graph.addVertex(concept);
		}

		/*
		 * For each role we add nodes for its inverse, its domain and its range
		 */
		for (Predicate rolep : ontology.getRoles()) {
			Property role = descFactory.createProperty(rolep);
			Property roleInv = descFactory.createProperty(role.getPredicate(), !role.isInverse());
			PropertySomeRestriction existsRole = descFactory.getPropertySomeRestriction(role.getPredicate(), role.isInverse());
			PropertySomeRestriction existsRoleInv = descFactory.getPropertySomeRestriction(role.getPredicate(), !role.isInverse());
			graph.addVertex(role);
			graph.addVertex(roleInv);
			graph.addVertex(existsRole);
			graph.addVertex(existsRoleInv);
			if (chain) {
				graph.addEdge(existsRoleInv, existsRole);				
				graph.addEdge(existsRole, existsRoleInv);				
			}
		}

		for (Axiom assertion : ontology.getAssertions()) {

			if (assertion instanceof SubClassAxiomImpl) {
				SubClassAxiomImpl clsIncl = (SubClassAxiomImpl) assertion;
				ClassDescription parent = clsIncl.getSuper();
				ClassDescription child = clsIncl.getSub();
				graph.addVertex(child);
				graph.addVertex(parent);
				graph.addEdge(child, parent);
			} 
			else if (assertion instanceof SubPropertyAxiomImpl) {
				SubPropertyAxiomImpl roleIncl = (SubPropertyAxiomImpl) assertion;
				Property parent = roleIncl.getSuper();
				Property child = roleIncl.getSub();
				Property parentInv = descFactory.createProperty(parent.getPredicate(), !parent.isInverse());
				Property childInv = descFactory.createProperty(child.getPredicate(), !child.isInverse());

				// This adds the direct edge and the inverse, e.g., R ISA S and
				// R- ISA S-,
				// R- ISA S and R ISA S-
				graph.addVertex(child);
				graph.addVertex(parent);
				graph.addVertex(childInv);
				graph.addVertex(parentInv);
				graph.addEdge(child, parent);
				graph.addEdge(childInv, parentInv);
				
				//add also edges between the existential
				ClassDescription existsParent = descFactory.getPropertySomeRestriction(parent.getPredicate(), parent.isInverse());
				ClassDescription existChild = descFactory.getPropertySomeRestriction(child.getPredicate(), child.isInverse());
				ClassDescription existsParentInv = descFactory.getPropertySomeRestriction(parent.getPredicate(), !parent.isInverse());
				ClassDescription existChildInv = descFactory.getPropertySomeRestriction(child.getPredicate(), !child.isInverse());
				graph.addVertex(existChild);
				graph.addVertex(existsParent);
				graph.addEdge(existChild, existsParent);
				
				graph.addVertex(existChildInv);
				graph.addVertex(existsParentInv);
				graph.addEdge(existChildInv, existsParentInv);
				
			}
		}
		return graph;
	}
	
}
