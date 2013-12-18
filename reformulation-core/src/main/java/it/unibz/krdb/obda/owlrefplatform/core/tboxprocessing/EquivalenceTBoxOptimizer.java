/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.Axiom;
import it.unibz.krdb.obda.ontology.ClassDescription;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.DAGBuilder;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.DAGImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.EquivalenceClass;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/***
 * An optimizer that will eliminate equivalences implied by the ontology,
 * simplifying the vocabulary of the ontology. This allows to reduce the number
 * of inferences implied by the onotlogy and eliminating redundancy. The output
 * is two components: a "equivalence map" that functional mapping from class
 * (property) expression to class (property) that can be used to retrieve the
 * class (property) of the optimized ontology that should be used instead of one
 * class (property) that has been removed; a TBox T' that has a simpler
 * vocabulary.
 * 
 * 
 * 
 * @author Mariano Rodriguez Muro
 * 
 */
public class EquivalenceTBoxOptimizer {

	private Ontology optimalTBox = null;
	private Map<Predicate, Description> equivalenceMap;
	private Ontology tbox;

	private static final OntologyFactory ofac = OntologyFactoryImpl.getInstance();

	public EquivalenceTBoxOptimizer(Ontology tbox) {
		this.tbox = tbox;
		equivalenceMap = new HashMap<Predicate, Description>();
	}

	/***
	 * Optimize will compute the implied hierarchy of the given ontology and 
	 * remove any cycles (compute equivalence
	 * classes). Then for each equivalent set (of classes/roles) it will keep
	 * only one representative and replace reference to any other node in the
	 * equivalence set with reference to the representative. The equivalences
	 * will be kept in an equivalence map, that relates classes/properties with
	 * its equivalent. Note that the equivalent of a class can only be another
	 * class, an the equivalent of a property can be another property, or the
	 * inverse of a property.
	 * 
	 *
	 */
	public void optimize() {
		DAGImpl dag = DAGBuilder.getDAG(tbox);
		TBoxReasonerImpl reasoner= new TBoxReasonerImpl(dag);

		/*
		 * Processing all properties
		 */

		Collection<Description> props = new HashSet<Description>(dag.vertexSet());
		HashSet<Property> removedNodes = new HashSet<Property>();

		for (Description desc : props) {
			if (!(desc instanceof Property))
				continue;

			if (removedNodes.contains(desc))
				continue;

			Property prop = (Property) desc;	
			
			/*
			 * Clearing the equivalences of the domain and ranges of the cycle's
			 * head
			 */
			
			Description propNodeDomain =ofac.createPropertySomeRestriction(prop.getPredicate(), prop.isInverse());
			Description propNodeRange = ofac.createPropertySomeRestriction(prop.getPredicate(), !prop.isInverse());
		
			if (dag.containsVertex(propNodeDomain) & dag.getEquivalenceClass0(propNodeDomain)!=null )
				dag.getMapEquivalences().remove(propNodeDomain);
			if (dag.containsVertex(propNodeRange) & dag.getEquivalenceClass0(propNodeRange)!=null )
				dag.getMapEquivalences().remove(propNodeRange);
		

			Collection<Description> equivalents = reasoner.getEquivalences(prop);
			if (equivalents.size() > 1)
				for (Description equivalent : equivalents) {
					if (removedNodes.contains(equivalent) || equivalent.equals(prop)) 
						continue;

					/*
					 * each of the equivalents is redundant, we need to deal with
					 * them and with their inverses!
					 */
					Property equiProp = (Property) equivalent;
					Property inverseProp = ofac.createProperty(prop.getPredicate(), !prop.isInverse());
					if (equiProp.equals(inverseProp))
						continue;

					if (equiProp.isInverse()) {
						/*
						 * We need to invert the equivalent and the good property
						 */			
						equivalenceMap.put(equiProp.getPredicate(), inverseProp);
					} 
					else {
						equivalenceMap.put(equiProp.getPredicate(), prop);
					}

					
					/*
					 * Dealing with the inverses
					 * 
					 * We need to get the inverse node of the redundant one and
					 * remove it, replacing pointers to the node of the inverse
					 * current non redundant predicate
					 */

					Description nonredundandPropNodeInv = ofac.createProperty(prop.getPredicate(), !prop.isInverse());
					Description redundandEquivPropNodeInv = ofac.createProperty(equiProp.getPredicate(), !equiProp.isInverse());
				

					if (dag.containsVertex(redundandEquivPropNodeInv)) {
						dag.addVertex(nonredundandPropNodeInv); //choose the representative inverse
					
						for (Set<Description> children : reasoner.getDirectChildren(redundandEquivPropNodeInv)) {
							Description firstChild=children.iterator().next();
							Description child = dag.getRepresentativeFor(firstChild);
							dag.removeAllEdges(child, redundandEquivPropNodeInv);
							dag.addEdge(child, nonredundandPropNodeInv);
						}

						for (Set<Description> parents : reasoner.getDirectParents(redundandEquivPropNodeInv)) {
							Description firstParent=parents.iterator().next();
							Description parent = dag.getRepresentativeFor(firstParent);
							dag.removeAllEdges(parent, redundandEquivPropNodeInv);
							dag.addEdge(nonredundandPropNodeInv, parent);	
						}
						
						dag.removeVertex(redundandEquivPropNodeInv);
						removedNodes.add((Property) redundandEquivPropNodeInv);
					
						//assign the new representative to the equivalent nodes
						for (Description equivInverse: dag.getEquivalenceClass0(nonredundandPropNodeInv)){
							if (equivInverse.equals(nonredundandPropNodeInv)){
								dag.removeReplacementFor(nonredundandPropNodeInv);
								continue;
							}
						dag.setReplacementFor(equivInverse, nonredundandPropNodeInv);
					}
					
					dag.getMapEquivalences().remove(redundandEquivPropNodeInv);
				}

				removedNodes.add((Property) equivalent);

				dag.removeVertex(equivalent);
			}
			// We clear all equivalents!
			
			dag.getMapEquivalences().remove(prop);
		}

		/*
		 * Replacing any \exists R for \exists S, in case R was replaced by S
		 * due to an equivalence
		 */

		for (Property prop : removedNodes) {
			if (prop.isInverse())
				continue;
			Predicate redundantProp = prop.getPredicate();
			Property equivalent = (Property) equivalenceMap.get(redundantProp);

			/* first for the domain */
			Description directRedundantNode = ofac.getPropertySomeRestriction(redundantProp, false);
			boolean containsNodeDomain=  dag.containsVertex(directRedundantNode);
			if (containsNodeDomain) {
				Description equivalentNode = ofac.getPropertySomeRestriction(equivalent.getPredicate(),
						equivalent.isInverse());
				dag.addVertex(equivalentNode); 
				if(dag.getEquivalenceClass0(equivalentNode)!= null)
				{
					EquivalenceClass<Description> equivalences = dag.getEquivalenceClass0(equivalentNode);
					equivalences.getMembers().remove(directRedundantNode);
					
					dag.getMapEquivalences().put(equivalentNode, equivalences);
				}
				for (Set<Description> children : reasoner.getDirectChildren(directRedundantNode)) {
					Description firstChild=children.iterator().next();
					Description child= dag.getRepresentativeFor(firstChild);
					if (!reasoner.getDirectChildren(equivalentNode).contains(child)) {
						dag.addEdge(child, equivalentNode);
					}
		
				}
				for (Set<Description> parents : reasoner.getDirectParents(directRedundantNode)) {
					Description firstParent=parents.iterator().next();
					Description parent= dag.getRepresentativeFor(firstParent);
					if (!reasoner.getDirectParents(equivalentNode).contains(parent)) {
						dag.addEdge(equivalentNode, parent);
					}
					
				}
				for(Description equivInverse: dag.getEquivalenceClass0(directRedundantNode)){
					if(equivInverse.equals(equivalentNode)){
							dag.removeReplacementFor(equivalentNode);
						continue;
					}
					dag.setReplacementFor(equivInverse,equivalentNode);
					
				}
				dag.removeReplacementFor(directRedundantNode);
				dag.removeVertex(directRedundantNode);
			}

			/* Now for the range */

			directRedundantNode = ofac.getPropertySomeRestriction(redundantProp, true);
			boolean  containsNodeRange = dag.containsVertex(directRedundantNode);
			if (containsNodeRange) {
				Description equivalentNode = ofac.getPropertySomeRestriction(equivalent.getPredicate(),
						!equivalent.isInverse());
				dag.addVertex(equivalentNode);
				
				if(dag.getEquivalenceClass0(equivalentNode)!= null){
					EquivalenceClass<Description> equivalences = dag.getEquivalenceClass0(equivalentNode);
					equivalences.getMembers().remove(directRedundantNode);
					dag.getMapEquivalences().put(equivalentNode, equivalences);
				}
				
				for (Set<Description> children : reasoner.getDirectChildren(directRedundantNode)) {
					Description firstChild=children.iterator().next();
					Description child = dag.getRepresentativeFor(firstChild);
					if (!reasoner.getDirectChildren(equivalentNode).contains(child)) {
						dag.addEdge(child, equivalentNode);
					}
					
				}

				for (Set<Description> parents : reasoner.getDirectParents(directRedundantNode)) {
					Description firstParent=parents.iterator().next();
					Description parent = dag.getRepresentativeFor(firstParent);
					if (!reasoner.getDirectParents(equivalentNode).contains(parent)) {
						dag.addEdge(equivalentNode, parent);
					}
					
				}
				
				for(Description equivInverse: dag.getEquivalenceClass0(directRedundantNode)){
					if(equivInverse.equals(equivalentNode)) {
						dag.removeReplacementFor(equivalentNode);
						continue;
					}
					dag.setReplacementFor(equivInverse, equivalentNode);
				}
				dag.removeReplacementFor(directRedundantNode);
				dag.removeVertex(directRedundantNode);
//				equivalentNode.getDescendants().remove(directRedundantNode);
			}
		}
		
	
		/*
		 * Processing all classes
		 */
		Collection<OClass> classNodes = dag.getClassNames();
		for (OClass classNode : classNodes) {
			if(dag.hasReplacementFor(classNode))
				continue;
			
			OClass classDescription = (OClass) classNode;

			Collection<Description> redundantClasses = new LinkedList<Description>();
			Collection<Description> replacements = new HashSet<Description>();

			for (Description equivalentNode : reasoner.getEquivalences(classNode)) {
				if(equivalentNode.equals(classNode))
					continue;
				Description descEquivalent = equivalentNode;
				if (descEquivalent instanceof OClass) {
					/*
					 * It's a named class, we need to remove it
					 */
					OClass equiClass = (OClass) descEquivalent;
					equivalenceMap.put(equiClass.getPredicate(), classDescription);
					redundantClasses.add(equivalentNode);
				} else {

					/*
					 * It's an \exists R, we need to make sure that it references
					 * to the proper vocabulary
					 */
					PropertySomeRestriction existsR = (PropertySomeRestriction) descEquivalent;
					Property prop = ofac.createProperty(existsR.getPredicate());
					Property equiProp = (Property) equivalenceMap.get(prop);
					if (equiProp == null)
						continue;

					/*
					 * This \exists R indeed refers to a property that was
					 * removed in the previous step and replace for some other
					 * property S. we need to eliminate the \exists R, and add
					 * an \exists S
					 */
					PropertySomeRestriction replacement = ofac.createPropertySomeRestriction(equiProp.getPredicate(), equiProp.isInverse());
					redundantClasses.add(equivalentNode);

					replacements.add(replacement);
				}
			}

			if(dag.getEquivalenceClass0(classNode)!=null)
			{
				EquivalenceClass<Description> equivalences = dag.getEquivalenceClass0(classNode);
				equivalences.getMembers().removeAll(redundantClasses);
				dag.getMapEquivalences().put(classNode, equivalences);
			}
			

			
			for (Description replacement : replacements){
				
				EquivalenceClass<Description> equivalences = dag.getEquivalenceClass0(classNode);
				equivalences.getMembers().add(replacement);
				dag.getMapEquivalences().put(classNode, equivalences);
				dag.setReplacementFor(replacement, classNode);
			}

		}

		/*
		 * Done with the simplificatino of the vocabulary, now we create the
		 * optimized ontology
		 */

		optimalTBox = ofac.createOntology();
		
		for(Description node:dag.vertexSet()){
			for (Set<Description> descendants : reasoner.getDescendants(node)) {
				Description firstDescendant = descendants.iterator().next();
				Description descendant = dag.getRepresentativeFor(firstDescendant);

				if (!descendant.equals(node)) 
					addToTBox(optimalTBox, descendant, node);					
			}
			for (Description equivalent : reasoner.getEquivalences(node)) {
				if (!equivalent.equals(node)) {
					addToTBox(optimalTBox, node, equivalent);					
					addToTBox(optimalTBox, equivalent, node);
				}
			}
		}

		//
		/*
		 * Last, we add references to all the vocabulary of the previous TBox
		 */
		Set<Predicate> extraVocabulary = new HashSet<Predicate>();
		extraVocabulary.addAll(tbox.getVocabulary());
		extraVocabulary.removeAll(equivalenceMap.keySet());
		optimalTBox.addEntities(extraVocabulary);
	}

	private static void addToTBox(Ontology o, Description s, Description t) {
		Axiom axiom;
		if (s instanceof ClassDescription) {
			axiom = ofac.createSubClassAxiom((ClassDescription) s, (ClassDescription) t);
		} 
		else{
			axiom = ofac.createSubPropertyAxiom((Property) s, (Property) t);
		}
		o.addEntities(axiom.getReferencedEntities());
		o.addAssertion(axiom);		
	}
	
	public Ontology getOptimalTBox() {
		return this.optimalTBox;
	}

	public Map<Predicate, Description> getEquivalenceMap() {
		return this.equivalenceMap;
	}
}
