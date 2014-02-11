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
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

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

	public static void choosePropertyRepresentatives(EquivalencesDAG<Property> dag) {
		
		Deque<Equivalences<Property>> asymmetric = new LinkedList<Equivalences<Property>>();
		
		for (Equivalences<Property> equivalences : dag.vertexSet()) {

			// RULE 0: IF IT HAS BEEN SET, DO NOTHING
			if (equivalences.getRepresentative() != null)
				continue;
			
			Property first = equivalences.iterator().next();
						
			// RULE 1: IF THE EQUIVALENCES IS OF SIZE 1, 
			//            SET THE REPRESENTATIVE AND MAKE THE NAMED ONE INDEXED 
			if (equivalences.size() == 1) {
				equivalences.setRepresentative(first);
				if (!first.isInverse())
					equivalences.setIndexed();
			}
			else {
			// RULE 2: IF THE EQUIVALENCES DOES NOT CONTAIN A SYMMETRIC PROPERTY, DEAL LATER	
				Equivalences<Property> inverseEquivalenceSet = getInverse(dag, equivalences);
				if (!inverseEquivalenceSet.contains(first)) {
					assert (equivalences != inverseEquivalenceSet);
					asymmetric.add(equivalences);
				}
				// RULE 3: OTHERWISE, IT IS ALL SYMMETIC PROPERTIES ONLY, 
				//  CHOOSE ONE AS A REP AND MAKE IT INDEXED
				else {
					assert (equivalences == inverseEquivalenceSet);
					equivalences.setRepresentative(getNamedRepresentative(equivalences));
					equivalences.setIndexed();
				}							
			}
		}
	
		while (!asymmetric.isEmpty()) {
			Equivalences<Property> c = asymmetric.pollFirst();
			if (c.getRepresentative() != null) 
				continue;
						
			boolean invertedRepRequired = false;
			Set<Equivalences<Property>> component = getRoleComponent(dag, c);
			// find a maximal property (non-deterministic!!)
			for (Equivalences<Property> equivalences : component) 
				if (component.containsAll(dag.getDirectChildren(equivalences))) {
					Property p = getNamedRepresentative(equivalences);
					if (p == null) {
						invertedRepRequired = true;
						break;
					}
					Property invRep = getNamedRepresentative(getInverse(dag, equivalences));
					if (invRep != null && invRep.getPredicate().getName().compareTo(p.getPredicate().getName()) < 0) {
						//System.out.println(invRep.getPredicate().getName() + " COMPARED TO " + p.getPredicate().getName());
						invertedRepRequired = true;
						break;
					}
				}
			
			if (invertedRepRequired) 
				component = getRoleComponent(dag, getInverse(dag, c));		
			
			for (Equivalences<Property> equivalences : component) {
				Property rep = getNamedRepresentative(equivalences);
				if (rep == null) 
					rep = equivalences.iterator().next(); // again, non-deterministic
				
				equivalences.setRepresentative(rep);
				equivalences.setIndexed();

				Property inverse = fac.createProperty(rep.getPredicate(), !rep.isInverse());
				Equivalences<Property> inverseEquivalences = dag.getVertex(inverse);
				inverseEquivalences.setRepresentative(inverse);
			}
		}
		
		
	/*	
		for (Equivalences<Property> equivalenceClass : dag.vertexSet()) {
				System.out.println(" " + equivalenceClass);
				if (equivalenceClass.getRepresentative() == null)
					System.out.println("NULL REP FOR: " + equivalenceClass);
				if (!equivalenceClass.isIndexed()) {
					Property representative = equivalenceClass.getRepresentative();
					Property inverse = fac.createProperty(representative.getPredicate(), !representative.isInverse());
					Equivalences<Property> inverseEquivalenceSet = dag.getVertex(inverse);
					if (!inverseEquivalenceSet.isIndexed())
						System.out.println("NOT INDEXED: " + equivalenceClass + " AND " + inverseEquivalenceSet);
				}
		}
		
		System.out.println("RESULT: " + dag);
	*/
	}	
	
	
	private static Equivalences<Property> getInverse(EquivalencesDAG<Property> dag, Equivalences<Property> node) {
		Property property = node.iterator().next();
		Property inverse = fac.createProperty(property.getPredicate(), !property.isInverse());
		return dag.getVertex(inverse);		
	}
	
	/**
	 * get the connected component for a given equivalence set
	 * 		the vertex traversal does not go trough vertices with already selected representatives
	 *      (in particular, through sets of symmetric properties)
	 *   
	 * @param dag
	 * @param node
	 * @param symmetric
	 * @return
	 */
	
	private static Set<Equivalences<Property>> getRoleComponent(EquivalencesDAG<Property> dag, Equivalences<Property> node)		{
		
		Set<Equivalences<Property>> set = new HashSet<Equivalences<Property>>();
		
		Deque<Equivalences<Property>> queue = new LinkedList<Equivalences<Property>>();
		queue.add(node);
		set.add(node);

		while (!queue.isEmpty()) {
			Equivalences<Property> eq = queue.pollFirst();
			for (Equivalences<Property> t : dag.getDirectChildren(eq)) {
				if (t.getRepresentative() == null && !set.contains(t)) {
					set.add(t);
					queue.add(t);
				}
			}
			for (Equivalences<Property> s : dag.getDirectParents(eq)) {
				if (s.getRepresentative() == null && !set.contains(s)) {
					set.add(s);
					queue.add(s);
				}
			}
		}	
		return set;
	}
	

	
	/**
	 * get the first (in lexicographical order) named property in the equivalence class 
	 * 
	 * @param properties
	 * @return
	 */

	private static Property getNamedRepresentative(Equivalences<Property> properties) {
		Property representative = null;
		for (Property rep : properties) 
			if (!rep.isInverse()) {
				if (representative == null || rep.getPredicate().getName().compareTo(representative.getPredicate().getName()) < 0)
					representative = rep;
			}
		return representative;
	}

	

	
	
	
	
	public static void chooseClassRepresentatives(EquivalencesDAG<BasicClassDescription> dag, EquivalencesDAG<Property> propertyDAG) {

		for (Equivalences<BasicClassDescription> equivalenceSet : dag.vertexSet()) {

			BasicClassDescription representative = null;			
			if (equivalenceSet.size() <= 1) {
				representative = equivalenceSet.iterator().next();
			}
			else {
				// find a named class as a representative 
				// TODO: find the minimal rep for the lexicographical order -- otherwise not unique 
				for (BasicClassDescription e : equivalenceSet) 
					if (e instanceof OClass) {
						representative = e;
						break;
					}
				
				if (representative == null) {
					PropertySomeRestriction first = (PropertySomeRestriction)equivalenceSet.iterator().next();
					Property prop = fac.createProperty(first.getPredicate(), first.isInverse());
					Property propRep = (Property) propertyDAG.getVertex(prop).getRepresentative();
					representative = fac.createPropertySomeRestriction(propRep.getPredicate(), propRep.isInverse());
				}
			}

			equivalenceSet.setRepresentative(representative);
			if (representative instanceof OClass)
				equivalenceSet.setIndexed();
		}
	}
	
	
}
