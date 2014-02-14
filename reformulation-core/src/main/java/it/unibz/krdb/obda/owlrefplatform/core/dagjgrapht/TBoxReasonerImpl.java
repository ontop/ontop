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
import it.unibz.krdb.obda.ontology.Ontology;
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
 * Allows to reason over the TBox using  DAG or graph
 * 
 */

public class TBoxReasonerImpl implements TBoxReasoner {

	private static final OntologyFactory fac = OntologyFactoryImpl.getInstance();
	
	private final DefaultDirectedGraph<Property,DefaultEdge> propertyGraph; // test only
	private final DefaultDirectedGraph<BasicClassDescription,DefaultEdge> classGraph; // test only

	private final EquivalencesDAGImpl<Property> propertyDAG;
	private final EquivalencesDAGImpl<BasicClassDescription> classDAG;
	
	
	public TBoxReasonerImpl(Ontology onto) {
		propertyGraph = OntologyGraph.getPropertyGraph(onto);
		propertyDAG = new EquivalencesDAGImpl<Property>(propertyGraph);
		
		classGraph = OntologyGraph.getClassGraph(onto, propertyGraph, false);
		classDAG = new EquivalencesDAGImpl<BasicClassDescription>(classGraph);

		choosePropertyRepresentatives(propertyDAG);
		chooseClassRepresentatives(classDAG, propertyDAG);
	}

	private TBoxReasonerImpl(DefaultDirectedGraph<Property,DefaultEdge> propertyGraph, 
					DefaultDirectedGraph<BasicClassDescription,DefaultEdge> classGraph) {
		this.propertyGraph = propertyGraph;
		propertyDAG = new EquivalencesDAGImpl<Property>(propertyGraph);
		
		this.classGraph = classGraph;
		classDAG = new EquivalencesDAGImpl<BasicClassDescription>(classGraph);

		choosePropertyRepresentatives(propertyDAG);
		chooseClassRepresentatives(classDAG, propertyDAG);
	}

	


	@Override
	public String toString() {
		return propertyDAG.toString() + "\n" + classDAG.toString();
	}
	
	
		
	
	/**
	 * Return the DAG of classes
	 * 
	 * @return DAG 
	 */


	@Override
	public EquivalencesDAG<BasicClassDescription> getClasses() {
		return classDAG;
	}

	/**
	 * Return the DAG of properties
	 * 
	 * @return DAG 
	 */

	public EquivalencesDAG<Property> getProperties() {
		return propertyDAG;
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
	
	
	
	
	

	public static void choosePropertyRepresentatives(EquivalencesDAGImpl<Property> dag) {
		
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
				if (component.containsAll(dag.getDirectSub(equivalences))) {
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
			for (Equivalences<Property> t : dag.getDirectSub(eq)) {
				if (t.getRepresentative() == null && !set.contains(t)) {
					set.add(t);
					queue.add(t);
				}
			}
			for (Equivalences<Property> s : dag.getDirectSuper(eq)) {
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

	

	
	
	
	
	public static void chooseClassRepresentatives(EquivalencesDAGImpl<BasicClassDescription> dag, EquivalencesDAG<Property> propertyDAG) {

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
		
		
		EquivalencesDAG<BasicClassDescription> classes = tbox.getClasses();
		
		// move everything to a graph that admits cycles
		DefaultDirectedGraph<BasicClassDescription,DefaultEdge> modifiedGraph = 
				new  DefaultDirectedGraph<BasicClassDescription,DefaultEdge>(DefaultEdge.class);

		// clone all the vertex and edges from dag
		for (Equivalences<BasicClassDescription> v : classes) 
			modifiedGraph.addVertex(v.getRepresentative());
		
		for (Equivalences<BasicClassDescription> v : classes) {
			BasicClassDescription s = v.getRepresentative();
			for (Equivalences<BasicClassDescription> vp : classes.getDirectSuper(v))
				modifiedGraph.addEdge(s, vp.getRepresentative());
		}

		OntologyFactory fac = OntologyFactoryImpl.getInstance();
		HashSet<BasicClassDescription> processedNodes = new HashSet<BasicClassDescription>();
		
		for (Equivalences<BasicClassDescription> existsNode : classes) {
			BasicClassDescription node = existsNode.getRepresentative();
			
			if (!(node instanceof PropertySomeRestriction) || processedNodes.contains(node)) 
				continue;

			/*
			 * Adding a cycle between exists R and exists R- for each R.
			 */

			PropertySomeRestriction exists = (PropertySomeRestriction) node;
			Equivalences<BasicClassDescription> existsInvNode = classes.getVertex(
						fac.createPropertySomeRestriction(exists.getPredicate(), !exists.isInverse()));
			
			for (Equivalences<BasicClassDescription> children : classes.getDirectSub(existsNode)) {
				BasicClassDescription child = children.getRepresentative(); 
				if (!child.equals(existsInvNode))
					modifiedGraph.addEdge(child, existsInvNode.getRepresentative());
			}
			for (Equivalences<BasicClassDescription> children : classes.getDirectSub(existsInvNode)) {
				BasicClassDescription child = children.getRepresentative(); 
				if (!child.equals(existsNode))
					modifiedGraph.addEdge(child, existsNode.getRepresentative());
			}

			for (Equivalences<BasicClassDescription> parents : classes.getDirectSuper(existsNode)) {
				BasicClassDescription parent = parents.getRepresentative(); 
				if (!parent.equals(existsInvNode))
					modifiedGraph.addEdge(existsInvNode.getRepresentative(), parent);
			}

			for (Equivalences<BasicClassDescription> parents : classes.getDirectSuper(existsInvNode)) {
				BasicClassDescription parent = parents.getRepresentative(); 
				if (!parent.equals(existsInvNode))
					modifiedGraph.addEdge(existsNode.getRepresentative(), parent);
			}

			processedNodes.add(existsNode.getRepresentative());
			processedNodes.add(existsInvNode.getRepresentative());
		}

		/* Collapsing the cycles */
		return new TBoxReasonerImpl(tbox.propertyGraph, modifiedGraph);
	}

}
