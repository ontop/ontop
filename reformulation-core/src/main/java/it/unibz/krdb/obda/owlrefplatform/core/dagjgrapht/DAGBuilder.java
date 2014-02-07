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

import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
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
	public static SimpleDirectedGraph <Equivalences<Description>,DefaultEdge> getDAG(DefaultDirectedGraph<Description,DefaultEdge> graph, 
			Map<Description, Equivalences<Description>> equivalencesMap) {

		DefaultDirectedGraph<Description,DefaultEdge> copy = 
				new DefaultDirectedGraph<Description,DefaultEdge>(DefaultEdge.class);

		for (Description v : graph.vertexSet()) 
			copy.addVertex(v);

		for (DefaultEdge e : graph.edgeSet()) {
			Description s = graph.getEdgeSource(e);
			Description t = graph.getEdgeTarget(e);

			copy.addEdge(s, t, e);
		}
	
		DefaultDirectedGraph<Equivalences<Description>,DefaultEdge> dag0 = eliminateCycles(copy, equivalencesMap);

		SimpleDirectedGraph <Equivalences<Description>,DefaultEdge> dag1 = getWithoutRedundantEdges(dag0);
						
		return dag1;
	}

	/**
	 */
	private static <T> SimpleDirectedGraph <T,DefaultEdge> getWithoutRedundantEdges(DefaultDirectedGraph<T,DefaultEdge> graph) {

		SimpleDirectedGraph <T,DefaultEdge> dag = new SimpleDirectedGraph <T,DefaultEdge> (DefaultEdge.class);

		for (T v : graph.vertexSet())
			dag.addVertex(v);
		
		for (DefaultEdge edge : graph.edgeSet()) {
			T v1 = graph.getEdgeSource(edge);
			T v2 = graph.getEdgeTarget(edge);
			boolean redundant = false;

			if (graph.outDegreeOf(v1) > 1) {
				// an edge is redundant if 
				//  its source has an edge going to a vertex 
				//         from which the target is reachable (in one step) 
				for (DefaultEdge e2 : graph.outgoingEdgesOf(v1)) 
					if (graph.containsEdge(graph.getEdgeTarget(e2), v2)) {
						redundant = true;
						break;
					}
			}
			if (!redundant)
				dag.addEdge(v1, v2);
		}
		return dag;
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

	private static DefaultDirectedGraph<Equivalences<Description>,DefaultEdge> eliminateCycles(DefaultDirectedGraph<Description,DefaultEdge> graph, 
			Map<Description, Equivalences<Description>> equivalencesMap) {

		DefaultDirectedGraph<Equivalences<Description>,DefaultEdge> dag = 
				new DefaultDirectedGraph<Equivalences<Description>,DefaultEdge>(DefaultEdge.class);

		GabowSCC<Description, DefaultEdge> inspector = new GabowSCC<Description, DefaultEdge>(graph);

		// each set contains vertices which together form a strongly connected
		// component within the given graph
		List<Equivalences<Description>> equivalenceSets = inspector.stronglyConnectedSets();

		for (Equivalences<Description> equivalenceSet : equivalenceSets)  {
			for (Description node : equivalenceSet) 
				equivalencesMap.put(node, equivalenceSet);
			
			dag.addVertex(equivalenceSet);
		}

		for (Equivalences<Description> equivalenceSet : equivalenceSets)  {
			for (Description e : equivalenceSet) {			
				for (DefaultEdge edge : graph.outgoingEdgesOf(e)) {
					Description t = graph.getEdgeTarget(edge);
					if (!equivalenceSet.contains(t))
						dag.addEdge(equivalenceSet, equivalencesMap.get(t));
				}
				for (DefaultEdge edge : graph.incomingEdgesOf(e)) {
					Description s = graph.getEdgeSource(edge);
					if (!equivalenceSet.contains(s))
						dag.addEdge(equivalencesMap.get(s), equivalenceSet);
				}
			}
		}

		// each set contains vertices which together form a strongly connected
		// component within the given graph
		List<Equivalences<Description>> propertyEquivalenceClasses = new LinkedList<Equivalences<Description>>();
		
		for (Equivalences<Description> equivalenceSet : equivalenceSets)  			
			if (equivalenceSet.iterator().next() instanceof Property) {
				if (equivalenceSet.size() > 1) 
					propertyEquivalenceClasses.add(equivalenceSet);
				else {
					Property p = (Property)equivalenceSet.iterator().next();
					equivalenceSet.setRepresentative(p);
					if (!p.isInverse())
						equivalenceSet.setIndexed();
				}
			}
		
		
		OntologyFactory fac = OntologyFactoryImpl.getInstance();

		/*
		 * A set with all the nodes that have been processed as participating in
		 * an equivalence cycle. If a component contains any of these nodes, the
		 * component should be ignored, since a cycle involving the same nodes
		 * or nodes for inverse descriptions has already been processed.
		 */
		Set<Equivalences<Description>> processedPropertyClasses = new HashSet<Equivalences<Description>>();
		Set<Equivalences<Description>> chosenPropertyClasses = new HashSet<Equivalences<Description>>();

		// PROCESS ONLY PROPERTIES
		
		for (Equivalences<Description> equivalenceSet : propertyEquivalenceClasses) {

			//System.out.println("PROPERTY: " + equivalenceSet);
			if (processedPropertyClasses.contains(equivalenceSet))
				continue;
			
			/*
			 * We consider first the elements that are connected with other
			 * named roles, checking if between their parents there is an
			 * already assigned named role or an inverse
			 */

			// THIS DOES NOT ALWAYS WORK
			// THE CODE BELOW IS BASED ON THE ASSUMPTION THAT THE DAG IS TRAVERSED FROM THE TOP 
			// AND NONE OF THE NODES HAS TWO PARENTS
			
			boolean ignore = false;

			for (Description e : equivalenceSet) {
				for (DefaultEdge outEdge : graph.outgoingEdgesOf(e)) {
					Property target = (Property) graph.getEdgeTarget(outEdge);
					Equivalences<Description> targetClass = equivalencesMap.get(target); 

					if (chosenPropertyClasses.contains(targetClass) || 
							(processedPropertyClasses.contains(targetClass) && target.isInverse())) {
						
						chosenPropertyClasses.add(equivalenceSet); 
						ignore = true;
						break;
					}
				}
				if (ignore) 
					break;
			}
			if (ignore) {
				//System.out.println("CHOSEN: " + chosenPropertyClasses);
				continue;
			}

			/* Assign the representative role with its inverse, domain and range
			 * 
			 */
			
			// find a representative 
			Property prop = getNamedRepresentative(equivalenceSet);
			if (prop == null) 	// equivalence class contains inverses only 
				continue;		// they are processed when we consider their properties

			Property inverse = fac.createProperty(prop.getPredicate(), !prop.isInverse());

			// remove all the equivalent node
			for (Description e : equivalenceSet) {			
				Property eProp = (Property) e;

				// remove if not the representative
				if (e != prop) 
					removeNodeAndRedirectEdges(graph, e, prop);
						
				Property eInverse = fac.createProperty(eProp.getPredicate(), !eProp.isInverse());
				
				// if the inverse is not equivalent to the representative 
				// then we remove the inverse
				if ((e != prop) && !eInverse.equals(prop))  
					removeNodeAndRedirectEdges(graph, eInverse, inverse);	
			}
			
			equivalenceSet.setRepresentative(prop);
			equivalenceSet.setIndexed();
			Equivalences<Description> inverseEquivalenceSet = equivalencesMap.get(inverse);
			inverseEquivalenceSet.setRepresentative(inverse);			
			
			processedPropertyClasses.add(equivalenceSet);
			processedPropertyClasses.add(inverseEquivalenceSet);
		}

		for (Equivalences<Description> equivalenceClass : dag.vertexSet()) {
			if (equivalenceClass.iterator().next() instanceof Property)
				if (equivalenceClass.getRepresentative() == null)
					System.out.println("NULL REP FOR: " + equivalenceClass);
		}
		
		//System.out.println("RESULT: " + graph);
		//System.out.println("MAP: " + equivalencesMap);
		
		/*
		 * PROCESS CLASSES ONLY
		 */

		for (Equivalences<Description> equivalenceClassSet : dag.vertexSet()) {

			if (!(equivalenceClassSet.iterator().next() instanceof BasicClassDescription))
				continue;

			BasicClassDescription representative = null;
			
			if (equivalenceClassSet.size() <= 1) {
				representative = (BasicClassDescription)equivalenceClassSet.iterator().next();
			}
			else {
				// find a named class as a representative 
				for (Description e : equivalenceClassSet) 
					if (e instanceof OClass) {
						representative = (BasicClassDescription)e;
						break;
					}
				
				if (representative == null) {
					PropertySomeRestriction first = (PropertySomeRestriction)equivalenceClassSet.iterator().next();
					Property prop = fac.createProperty(first.getPredicate(), first.isInverse());
					Property propRep = (Property) equivalencesMap.get(prop).getRepresentative();
					representative = fac.createPropertySomeRestriction(propRep.getPredicate(), propRep.isInverse());
				}
			}

			equivalenceClassSet.setRepresentative(representative);
			if (representative instanceof OClass)
				equivalenceClassSet.setIndexed();
		}
		return dag;
	}
	
	private static void removeNodeAndRedirectEdges(DefaultDirectedGraph<Description,DefaultEdge> graph, 
			Description eliminatedNode, Description representative) {
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
	}
	

	private static Property getNamedRepresentative(Equivalences<Description> properties) {
		Property representative = null;
		// find representative
		for (Description rep : properties) 
			if (!((Property) rep).isInverse()) {
				representative = (Property)rep;
				break;
			}
		return representative;
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
