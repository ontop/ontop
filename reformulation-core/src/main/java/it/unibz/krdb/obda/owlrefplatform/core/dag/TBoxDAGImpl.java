/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.core.dag;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.Axiom;
import it.unibz.krdb.obda.ontology.ClassDescription;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.ontology.impl.SubClassAxiomImpl;
import it.unibz.krdb.obda.ontology.impl.SubPropertyAxiomImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class TBoxDAGImpl implements TBoxDAG {

	private static final OntologyFactory descFactory = new OntologyFactoryImpl();

	DefaultDirectedGraph<Description, DefaultEdge> dag = new DefaultDirectedGraph<Description, DefaultEdge>(DefaultEdge.class);

	private Map<Description, Set<Description>> equivalencesMap = new HashMap<Description, Set<Description>>();

	/***
	 * A map to keep the relationship between 'eliminated' and 'remaining'
	 * nodes, created while computing equivalences by eliminating cycles in the
	 * graph.
	 */
	private Map<Description, Description> replacements = new HashMap<Description, Description>();

	public DefaultDirectedGraph<Description, DefaultEdge> getDag() {
		return dag;
	}

	public TBoxDAGImpl(Ontology ontology) {

		for (Predicate conceptp : ontology.getConcepts()) {
			ClassDescription concept = descFactory.createClass(conceptp);
			dag.addVertex(concept);
		}

		/*
		 * For each role we add nodes for its inverse, its domain and its range
		 */
		for (Predicate rolep : ontology.getRoles()) {
			Property role = descFactory.createProperty(rolep);
			Property roleInv = descFactory.createProperty(role.getPredicate(), !role.isInverse());
			PropertySomeRestriction existsRole = descFactory.getPropertySomeRestriction(role.getPredicate(), role.isInverse());
			PropertySomeRestriction existsRoleInv = descFactory.getPropertySomeRestriction(role.getPredicate(), !role.isInverse());
			dag.addVertex(role);
			dag.addVertex(roleInv);
			dag.addVertex(existsRole);
			dag.addVertex(existsRoleInv);
		}

		for (Axiom assertion : ontology.getAssertions()) {

			if (assertion instanceof SubClassAxiomImpl) {
				SubClassAxiomImpl clsIncl = (SubClassAxiomImpl) assertion;
				ClassDescription parent = clsIncl.getSuper();
				ClassDescription child = clsIncl.getSub();
				dag.addVertex(child);
				dag.addVertex(parent);
				dag.addEdge(child, parent);
			} else if (assertion instanceof SubPropertyAxiomImpl) {
				SubPropertyAxiomImpl roleIncl = (SubPropertyAxiomImpl) assertion;
				Property parent = roleIncl.getSuper();
				Property child = roleIncl.getSub();
				Property parentInv = descFactory.createProperty(parent.getPredicate(), !parent.isInverse());
				Property childInv = descFactory.createProperty(child.getPredicate(), !child.isInverse());

				// This adds the direct edge and the inverse, e.g., R ISA S and
				// R- ISA S-,
				// R- ISA S and R ISA S-
				dag.addVertex(child);
				dag.addVertex(parent);
				dag.addVertex(childInv);
				dag.addVertex(parentInv);
				dag.addEdge(child, parent);
				dag.addEdge(childInv, parentInv);
			}
		}

	//	System.out.println(dag);
		eliminateCycles();
	//	System.out.println(equivalencesMap);
		eliminateRedundantEdges();
	//	System.out.println(dag);
	}

	/***
	 * Eliminiates redundant edges to ensure that the remaining DAG is the
	 * transitive reduct of the original DAG.
	 * 
	 * <p>
	 * This is done with an ad-hoc algorithm that functions as follows:
	 * 
	 * <p>
	 * Compute the set of all nodes with more than 2 outgoing edges (these have
	 * candidate redundant edges. <br>
	 */
	private void eliminateRedundantEdges() {
		/* Compute the candidate nodes */
		List<Description> candidates = new LinkedList<Description>();
		Set<Description> vertexes = dag.vertexSet();
		for (Description vertex : vertexes) {
			int outdegree = dag.outDegreeOf(vertex);
			if (outdegree > 1) {
				candidates.add(vertex);
			}
		}

		/*
		 * for each candidate x and each outgoing edge x -> y, we will check if
		 * y appears in the
		 */

		for (Description candidate : candidates) {
			Set<DefaultEdge> possiblyRedundantEdges = new LinkedHashSet<DefaultEdge>();
			possiblyRedundantEdges.addAll(dag.outgoingEdgesOf(candidate));
			Set<DefaultEdge> eliminatedEdges = new HashSet<DefaultEdge>();
			// registring the target of the possible redundant targets for this
			// node
			Set<Description> targets = new HashSet<Description>();
			Map<Description, DefaultEdge> targetEdgeMap = new HashMap<Description, DefaultEdge>();
			for (DefaultEdge edge : possiblyRedundantEdges) {
				Description target = dag.getEdgeTarget(edge);
				targets.add(target);
				targetEdgeMap.put(target, edge);
			}

			for (DefaultEdge currentPathEdge : possiblyRedundantEdges) {
				Description currentTarget = dag.getEdgeTarget(currentPathEdge);
				if (eliminatedEdges.contains(currentPathEdge))
					continue;
				eliminateRedundantEdge(currentPathEdge, targets, targetEdgeMap, currentTarget, eliminatedEdges);
			}

		}

	}

	private void eliminateRedundantEdge(DefaultEdge safeEdge, Set<Description> targets, Map<Description, DefaultEdge> targetEdgeMap,
			Description currentTarget, Set<DefaultEdge> eliminatedEdges) {
		if (targets.contains(currentTarget)) {
			DefaultEdge edge = targetEdgeMap.get(currentTarget);
			if (!edge.equals(safeEdge)) {
				/*
				 * This is a redundant edge, removing it.
				 */
				dag.removeEdge(edge);
				eliminatedEdges.add(edge);
			}
		}

		// continue traversing the dag up
		Set<DefaultEdge> edgesInPath = dag.outgoingEdgesOf(currentTarget);
		for (DefaultEdge outEdge : edgesInPath) {
			Description target = dag.getEdgeTarget(outEdge);
			eliminateRedundantEdge(safeEdge, targets, targetEdgeMap, target, eliminatedEdges);
		}

	}

	/***
	 * Eliminates all cycles in the graph by computing all strongly connected
	 * components and eliminating all but one node in each of the components
	 * from the graph. The result of this transformation is that the graph
	 * becomes a DAG.
	 * 
	 * <p>
	 * In the process two objects are generated, an 'Equivalence map' and a
	 * 'replacementMap'. The first can be used to get the implied equivalences
	 * of the TBox. The second can be used to locate the node that is
	 * representative of an eliminated node.
	 * 
	 * <p>
	 * Computation of the strongly connected components is done using the
	 * StrongConnectivityInspector from JGraphT.
	 * 
	 */
	private void eliminateCycles() {
		StrongConnectivityInspector<Description, DefaultEdge> inspector = new StrongConnectivityInspector<Description, DefaultEdge>(dag);
		List<Set<Description>> equivalenceSets = inspector.stronglyConnectedSets();

		for (Set<Description> equivalenceSet : equivalenceSets) {
			if (equivalenceSet.size() < 2)
				continue;
			Iterator<Description> iterator = equivalenceSet.iterator();
			Description representative = iterator.next();
			equivalencesMap.put(representative, equivalenceSet);

			while (iterator.hasNext()) {
				Description eliminatedNode = iterator.next();
				replacements.put(eliminatedNode, representative);
				equivalencesMap.put(eliminatedNode, equivalenceSet);

				/*
				 * Re-pointing all links to and from the eliminated node to the
				 * representative node
				 */

				Set<DefaultEdge> edges = new HashSet<DefaultEdge>(dag.incomingEdgesOf(eliminatedNode));
				for (DefaultEdge incEdge : edges) {
					Description source = dag.getEdgeSource(incEdge);
					dag.removeAllEdges(source, eliminatedNode);
					
					if (source.equals(representative))
						continue;
					
					dag.addEdge(source, representative);
				}

				edges = new HashSet<DefaultEdge>(dag.outgoingEdgesOf(eliminatedNode));
				for (DefaultEdge outEdge : edges) {
					Description target = dag.getEdgeTarget(outEdge);
					dag.removeAllEdges(eliminatedNode, target);
					
					if (target.equals(representative))
						continue;
					dag.addEdge(representative, target);
				}
				
				dag.removeVertex(eliminatedNode);

			}
		}

	}

	/**
	 * Returns the set of equivalent descriptions for this description. Note,
	 * this includes the description itself.
	 * 
	 * @param description
	 * @return
	 */
	@Override
	public Set<Description> getEquiavlences(Description description) {
		Set<Description> equivalents = equivalencesMap.get(description);
		if (equivalents == null)
			return Collections.unmodifiableSet(Collections.singleton(description));
		return Collections.unmodifiableSet(equivalents);
	}

	@Override
	public Set<Set<Description>> getDirectChildren(Description desc) {

		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();
		Description node = replacements.get(desc);
		if (node == null)
			node = desc;
		Set<DefaultEdge> edges = dag.incomingEdgesOf(node);
		for (DefaultEdge edge : edges) {
			Description source = dag.getEdgeSource(edge);
			Set<Description> equivalences = getEquiavlences(source);
			result.add(equivalences);
		}
		return Collections.unmodifiableSet(result);

	}

	@Override
	public Set<Set<Description>> getDescendants(Description desc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void getNode() {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<Set<Description>> getDirectParents(Description desc) {
		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();
		Description node = replacements.get(desc);
		if (node == null)
			node = desc;
		Set<DefaultEdge> edges = dag.outgoingEdgesOf(node);
		for (DefaultEdge edge : edges) {
			Description source = dag.getEdgeSource(edge);
			Set<Description> equivalences = getEquiavlences(source);
			result.add(equivalences);
		}
		return Collections.unmodifiableSet(result);
	}

	@Override
	public Set<Set<Description>> getAncestors(Description desc) {
		// TODO Auto-generated method stub
		return null;
	}

}
