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
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

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

	private static OntologyFactory fac = OntologyFactoryImpl.getInstance();


	/**
	 */

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
	 * 
	 */

	public static void choosePropertyRepresentatives(EquivalencesDAG<Description> dag) {
		
		Deque<Equivalences<Description>> asymmetric1 = new LinkedList<Equivalences<Description>>();
		Deque<Equivalences<Description>> asymmetric2 = new LinkedList<Equivalences<Description>>();
		Set<Equivalences<Description>> symmetric = new HashSet<Equivalences<Description>>();
		
		for (Equivalences<Description> equivalenceSet : dag.vertexSet()) {
			if (!(equivalenceSet.iterator().next() instanceof Property))
				continue;
			
			if (equivalenceSet.size() == 1) {
				Property p = (Property)equivalenceSet.iterator().next();
				equivalenceSet.setRepresentative(p);
				if (!p.isInverse())
					equivalenceSet.setIndexed();
			}

			if (equivalenceSet.getRepresentative() != null)
				continue;
			
			Property representative = getNamedRepresentative(equivalenceSet);
			if (representative == null)
				representative = (Property)equivalenceSet.iterator().next();
			
			equivalenceSet.setRepresentative(representative);
			
			Property inverse = fac.createProperty(representative.getPredicate(), !representative.isInverse());
			Equivalences<Description> inverseEquivalenceSet = dag.getVertex(inverse);
			if (!inverseEquivalenceSet.contains(representative)) {
				inverseEquivalenceSet.setRepresentative(inverse);
				assert (equivalenceSet != inverseEquivalenceSet);
				asymmetric1.add(equivalenceSet);
				asymmetric2.add(inverseEquivalenceSet);
			}
			else {
				assert (equivalenceSet == inverseEquivalenceSet);
				symmetric.add(equivalenceSet);				
			}			
		}

		while (!asymmetric1.isEmpty()) {
			Equivalences<Description> c1 = asymmetric1.pollFirst();
			Equivalences<Description> c2 = asymmetric2.pollFirst();
			assert (!c1.isIndexed() || !c2.isIndexed());
			if (c1.isIndexed() || c2.isIndexed()) 
				continue;
						
			Set<Equivalences<Description>> set = getRoleComponent(dag, c1, symmetric);
			boolean swap = false;
			for (Equivalences<Description> eq : set) 
				if (dag.getDirectParents(eq).size() == 0) {
					Property p = (Property)c1.getRepresentative();
					if (p.isInverse()) {
						swap = true;
						break;
					}
				}
			if (swap)
				set = getRoleComponent(dag, c2, symmetric);
			
			for (Equivalences<Description> eq : set) {
				Property rep = (Property)eq.getRepresentative();
				if (rep.isInverse()) {
					Property rep2 = getNamedRepresentative(eq); 
					if (rep2 != null) {
						eq.setRepresentative(rep2);
						Property inverse = fac.createProperty(rep2.getPredicate(), !rep2.isInverse());
						Equivalences<Description> inverseEquivalenceSet = dag.getVertex(inverse);
						inverseEquivalenceSet.setRepresentative(inverse);
					}
				}
				eq.setIndexed();
			}
		}
		
		for (Equivalences<Description> sym : symmetric)
			sym.setIndexed();
		
		/*
		for (Equivalences<Description> equivalenceClass : dag.vertexSet()) {
			if (equivalenceClass.iterator().next() instanceof Property) {
				System.out.println(" " + equivalenceClass);
				if (equivalenceClass.getRepresentative() == null)
					System.out.println("NULL REP FOR: " + equivalenceClass);
				if (!equivalenceClass.isIndexed()) {
					Property representative = (Property) equivalenceClass.getRepresentative();
					Property inverse = fac.createProperty(representative.getPredicate(), !representative.isInverse());
					Equivalences<Description> inverseEquivalenceSet = equivalencesMap.get(inverse);
					if (!inverseEquivalenceSet.isIndexed())
						System.out.println("NOT INDEXED: " + equivalenceClass + " AND " + inverseEquivalenceSet);
				}
			}
		}
		*/
		//System.out.println("RESULT: " + dag);
		//System.out.println("MAP: " + equivalencesMap);

	}	
	
	public static void chooseClassRepresentatives(EquivalencesDAG<Description> dag) {

		for (Equivalences<Description> equivalenceSet : dag.vertexSet()) {

			if (!(equivalenceSet.iterator().next() instanceof BasicClassDescription))
				continue;

			BasicClassDescription representative = null;
			
			if (equivalenceSet.size() <= 1) {
				representative = (BasicClassDescription)equivalenceSet.iterator().next();
			}
			else {
				// find a named class as a representative 
				for (Description e : equivalenceSet) 
					if (e instanceof OClass) {
						representative = (BasicClassDescription)e;
						break;
					}
				
				if (representative == null) {
					PropertySomeRestriction first = (PropertySomeRestriction)equivalenceSet.iterator().next();
					Property prop = fac.createProperty(first.getPredicate(), first.isInverse());
					Property propRep = (Property) dag.getVertex(prop).getRepresentative();
					representative = fac.createPropertySomeRestriction(propRep.getPredicate(), propRep.isInverse());
				}
			}

			equivalenceSet.setRepresentative(representative);
			if (representative instanceof OClass)
				equivalenceSet.setIndexed();
		}
	}
	
	private static Set<Equivalences<Description>> getRoleComponent(EquivalencesDAG<Description> dag, 
																		Equivalences<Description> node, Set<Equivalences<Description>> symmetric)		{
		
		Set<Equivalences<Description>> set = new HashSet<Equivalences<Description>>();
		
		Deque<Equivalences<Description>> queue = new LinkedList<Equivalences<Description>>();
		queue.add(node);
		set.add(node);

		while (!queue.isEmpty()) {
			Equivalences<Description> eq = queue.pollFirst();
			for (Equivalences<Description> t : dag.getDirectChildren(eq)) {
				if (!set.contains(t) && !symmetric.contains(t)) {
					set.add(t);
					queue.add(t);
				}
			}
			for (Equivalences<Description> s : dag.getDirectParents(eq)) {
				if (!set.contains(s) && !symmetric.contains(s)) {
					set.add(s);
					queue.add(s);
				}
			}
		}	
		return set;
	}
	

	

	private static Property getNamedRepresentative(Equivalences<Description> properties) {
		Property representative = null;
		for (Description rep : properties) 
			if (!((Property) rep).isInverse()) {
				representative = (Property)rep;
				break;
			}
		return representative;
	}

	

	
}
