package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

/**
 * Allows to reason over the TBox using  DAG or graph
 * 
 */

public class TBoxReasonerImpl implements TBoxReasoner {

	private static final OntologyFactory fac = OntologyFactoryImpl.getInstance();
	
	private final EquivalencesDAGImpl<Property> propertyDAG;
	private final EquivalencesDAGImpl<BasicClassDescription> classDAG;

	/**
	 * the EquivalenceMap maps predicates to the representatives of their equivalence class (in TBox)
	 * 
	 * it contains 
	 * 		- an entry for each property name other than the representative of an equivalence class 
	 * 				(or its inverse)
	 * 		- an entry for each class name other than the representative of its equivalence class
	 */
	
	private Map<Predicate, OClass> classEquivalenceMap;
	private Map<Predicate, Property> propertyEquivalenceMap;	

	/**
	 * constructs from a raw ontology
	 * @param onto: ontology
	 */
	public TBoxReasonerImpl(Ontology onto) {
		propertyDAG = EquivalencesDAGImpl.getEquivalencesDAG(OntologyGraph.getPropertyGraph(onto));	
		classDAG = EquivalencesDAGImpl.getEquivalencesDAG(OntologyGraph.getClassGraph(onto, propertyDAG.getGraph(), false));

		choosePropertyRepresentatives(propertyDAG);
		chooseClassRepresentatives(classDAG, propertyDAG);
		
		this.classEquivalenceMap = new HashMap<Predicate, OClass>();
		this.propertyEquivalenceMap = new  HashMap<Predicate, Property>();		
	}

	/**
	 * constructs from DAGs and equivalence maps (for the equivalence simplification) 
	 * @param classDAG
	 * @param propertyDAG
	 * @param classEquivalenceMap
	 * @param propertyEquivalenceMap
	 */
	private TBoxReasonerImpl(EquivalencesDAGImpl<BasicClassDescription> classDAG, EquivalencesDAGImpl<Property> propertyDAG, 
						Map<Predicate, OClass> classEquivalenceMap, Map<Predicate, Property> propertyEquivalenceMap) {
		this.propertyDAG = propertyDAG;		
		this.classDAG = classDAG;

		this.classEquivalenceMap = classEquivalenceMap;
		this.propertyEquivalenceMap = propertyEquivalenceMap;		
	}
	
    /**
     * constructs from graphs (for the chain reasoner)
     * @param propertyGraph
     * @param classGraph
     */
	private TBoxReasonerImpl(DefaultDirectedGraph<Property,DefaultEdge> propertyGraph, 
					DefaultDirectedGraph<BasicClassDescription,DefaultEdge> classGraph) {
		propertyDAG = EquivalencesDAGImpl.getEquivalencesDAG(propertyGraph);		
		classDAG = EquivalencesDAGImpl.getEquivalencesDAG(classGraph);

		choosePropertyRepresentatives(propertyDAG);
		chooseClassRepresentatives(classDAG, propertyDAG);
		
		this.classEquivalenceMap = new HashMap<Predicate, OClass>();
		this.propertyEquivalenceMap = new  HashMap<Predicate, Property>();				
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
	
	@Override
	public OClass getClassRepresentative(Predicate p) {
		return classEquivalenceMap.get(p);
	}

	@Override
	public Property getPropertyRepresentative(Predicate p) {
		return propertyEquivalenceMap.get(p);
	}
		
	
	// INTERNAL DETAILS
	

	
	
	@Deprecated // test only
	public DefaultDirectedGraph<BasicClassDescription,DefaultEdge> getClassGraph() {
		return classDAG.getGraph();
	}
	
	@Deprecated // test only
	public DefaultDirectedGraph<Property,DefaultEdge> getPropertyGraph() {
		return propertyDAG.getGraph();
	}
	
	@Deprecated // test only
	public int edgeSetSize() {
		return propertyDAG.edgeSetSize() + classDAG.edgeSetSize();
	}
	
	@Deprecated // test only
	public int vertexSetSize() {
		return propertyDAG.vertexSetSize() + classDAG.vertexSetSize();
	}
	
	
	// ---------------------------------------------------------------------------------
	
	
	// lexicographical comparison of property names (a property is before its inverse) 
	private static final Comparator<Property> propertyComparator = new Comparator<Property>() {
		@Override
		public int compare(Property o1, Property o2) {
			int compared = o1.getPredicate().getName().compareTo(o2.getPredicate().getName()); 
			if (compared == 0) {
				if (o1.isInverse() == o2.isInverse())
					return 0;
				else if (o2.isInverse())
					return -1;
				else 
					return 1;
			}
			return compared;
		}
	}; 
	

	public static void choosePropertyRepresentatives(EquivalencesDAGImpl<Property> dag) {
		
		
		for (Equivalences<Property> set : dag) {
			
			// ski if has already been done 
			if (set.getRepresentative() != null)
				continue;
				
			Property rep = Collections.min(set.getMembers(), propertyComparator);	
			Property repInv = fac.createProperty(rep.getPredicate(), !rep.isInverse());
			
			Equivalences<Property> setInv = dag.getVertex(repInv);
			
			if (rep.isInverse()) {
				repInv = Collections.min(setInv.getMembers(), propertyComparator);	
				rep = fac.createProperty(repInv.getPredicate(), !repInv.isInverse());	
				
				setInv.setIndexed();							
			}
			else
				set.setIndexed();
			
			set.setRepresentative(rep);
			// System.out.println("SET REP: " + set);
			if (!set.contains(repInv)) {
				// if not symmetric 
				// (each set either consists of symmetric properties 
				//        or none of the properties in the set is symmetric)
				setInv.setRepresentative(repInv);
				//System.out.println("SET REP: " + setInv);
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
	
		
	// lexicographical comparison of class names 
	private static final Comparator<OClass> classComparator = new Comparator<OClass>() {
		@Override
		public int compare(OClass o1, OClass o2) {
			return o1.getPredicate().getName().compareTo(o2.getPredicate().getName());
		}
	}; 
	
	
	
	public static void chooseClassRepresentatives(EquivalencesDAGImpl<BasicClassDescription> dag, EquivalencesDAG<Property> propertyDAG) {

		for (Equivalences<BasicClassDescription> equivalenceSet : dag) {

			BasicClassDescription representative = null;			
			if (equivalenceSet.size() <= 1) {
				representative = equivalenceSet.iterator().next();
			}
			else {
				// find a named class as a representative 
				OClass namedRepresentative = null;
				for (BasicClassDescription e : equivalenceSet) 
					if (e instanceof OClass) {
						if (namedRepresentative == null || classComparator.compare((OClass)e, namedRepresentative) < 0)
							namedRepresentative = (OClass)e;
					}
		
				if (namedRepresentative == null) {
					PropertySomeRestriction first = (PropertySomeRestriction)equivalenceSet.iterator().next();
					Property prop = fac.createProperty(first.getPredicate(), first.isInverse());
					Property propRep = propertyDAG.getVertex(prop).getRepresentative();
					representative = fac.createPropertySomeRestriction(propRep.getPredicate(), propRep.isInverse());
				}
				else
					representative = namedRepresentative;
			}

			equivalenceSet.setRepresentative(representative);
			if (representative instanceof OClass)
				equivalenceSet.setIndexed();
		}
	}
	
	
	
	public static TBoxReasonerImpl getEquivalenceSimplifiedReasoner(TBoxReasoner reasoner) {

		Map<Predicate, Property> propertyEquivalenceMap = new HashMap<Predicate, Property>();
		SimpleDirectedGraph <Equivalences<Property>,DefaultEdge> properties 
					= new SimpleDirectedGraph<Equivalences<Property>,DefaultEdge>(DefaultEdge.class);
		Map<Property, Equivalences<Property>> propertyEquivalences 
					= new HashMap<Property, Equivalences<Property>>();


		// create vertices for properties 
		
		for(Equivalences<Property> node : reasoner.getProperties()) {
			Property prop = node.getRepresentative();
			Property inverseProp = fac.createProperty(prop.getPredicate(), !prop.isInverse());
			
			Set<Property> reduced = new HashSet<Property>();
			Equivalences<Property> reducedNode = new Equivalences<Property>(reduced, prop);
			
			for (Property equi : node) {
				// no map entry if the property coincides with its inverse
				if (equi.equals(prop) || equi.equals(inverseProp)) {
					propertyEquivalences.put(equi, reducedNode);
					reduced.add(equi);
					continue;
				}

				// if the property is different from its inverse, an entry is created 
				// (taking the inverses into account)
				if (equi.isInverse()) 
					propertyEquivalenceMap.put(equi.getPredicate(), inverseProp);
				else 
					propertyEquivalenceMap.put(equi.getPredicate(), prop);
			}
			if (node.isIndexed())
				reducedNode.setIndexed();
			properties.addVertex(reducedNode);
		}
		
		// create edges for the properties graph
		copyEdges(reasoner.getProperties(), propertyEquivalences, properties);
		EquivalencesDAGImpl<Property> propertyDAG = new EquivalencesDAGImpl<Property>(null, properties, propertyEquivalences);

		
		//
		// CLASSES
		// 
		
		
		Map<Predicate, OClass> classEquivalenceMap = new HashMap<Predicate, OClass>();
		SimpleDirectedGraph <Equivalences<BasicClassDescription>,DefaultEdge> classes 
				= new SimpleDirectedGraph<Equivalences<BasicClassDescription>,DefaultEdge>(DefaultEdge.class);
		Map<BasicClassDescription, Equivalences<BasicClassDescription>> classEquivalences 
				= new HashMap<BasicClassDescription, Equivalences<BasicClassDescription>>();
	
		// create vertices for classes
		
		for(Equivalences<BasicClassDescription> node : reasoner.getClasses()) {
			BasicClassDescription rep = node.getRepresentative();
			Set<BasicClassDescription> reduced = new HashSet<BasicClassDescription>();
			Equivalences<BasicClassDescription> reducedNode = new Equivalences<BasicClassDescription>(reduced, rep);
			
			for (BasicClassDescription equi : node) {	
				if (equi.equals(rep)) {
					classEquivalences.put(equi, reducedNode);
					reduced.add(equi);
					continue;
				}
				
				if (equi instanceof OClass) {
					// an entry is created for a named class
					OClass equiClass = (OClass) equi;
					classEquivalenceMap.put(equiClass.getPredicate(), (OClass)rep);
				}
				else if (equi instanceof PropertySomeRestriction) {
					Predicate pred = ((PropertySomeRestriction)equi).getPredicate();
					// the property of the existential is a representative of its equivalence class
					if (propertyEquivalenceMap.get(pred) == null) {
						classEquivalences.put(equi, reducedNode);
						reduced.add(equi);
					}
				}
			}
			if (node.isIndexed())
				reducedNode.setIndexed();
			classes.addVertex(reducedNode);
		}			

		// create edges for the classes graph
		copyEdges(reasoner.getClasses(), classEquivalences, classes);
		EquivalencesDAGImpl<BasicClassDescription> classDAG = new EquivalencesDAGImpl<BasicClassDescription>(null, classes, classEquivalences);
		
		
		return new TBoxReasonerImpl(classDAG, propertyDAG, classEquivalenceMap, propertyEquivalenceMap);
	}
	
	
	private static <T> void copyEdges(EquivalencesDAG<T> source, Map<T, Equivalences<T>> eqMap, SimpleDirectedGraph <Equivalences<T>,DefaultEdge> target) {
		for (Equivalences<T> node : source) {
			T rep = node.getRepresentative();
			Equivalences<T> reducedNode = eqMap.get(rep);
			
			for (Equivalences<T> subNode : source.getDirectSub(node)) {
				T subRep = subNode.getRepresentative();
				Equivalences<T> subReducedNode = eqMap.get(subRep);
				target.addEdge(subReducedNode, reducedNode);
			}
		}		
	}
/*	
	private static <T> DefaultDirectedGraph<T,DefaultEdge> getGraphFromDAG(EquivalencesDAG<T> source) {
		
		DefaultDirectedGraph<T,DefaultEdge> graph = new DefaultDirectedGraph<T,DefaultEdge>(DefaultEdge.class);

		for (Equivalences<T> node : source) {
			for (T v : node) 
				graph.addVertex(v);
			for (T v : node)  {
				graph.addEdge(v, node.getRepresentative());
				graph.addEdge(node.getRepresentative(), v);
			}
		}
		
		for (Equivalences<T> node : source) 
			for (Equivalences<T> subNode : source.getDirectSub(node)) 
				graph.addEdge(subNode.getRepresentative(), node.getRepresentative());
	
		return graph;
	}
*/	
	

	
	
	
	
	
	

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
	
	public static TBoxReasoner getChainReasoner(TBoxReasonerImpl tbox) {		
		
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
		return new TBoxReasonerImpl(tbox.propertyDAG.getGraph(), modifiedGraph);
	}


}
