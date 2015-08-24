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
import it.unibz.krdb.obda.ontology.ClassExpression;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.DataPropertyRangeExpression;
import it.unibz.krdb.obda.ontology.DataRangeExpression;
import it.unibz.krdb.obda.ontology.DataSomeValuesFrom;
import it.unibz.krdb.obda.ontology.Datatype;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.ObjectSomeValuesFrom;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.impl.DatatypeImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

/**
 * Allows to reason over the TBox using  DAG or graph
 * 
 */

public class TBoxReasonerImpl implements TBoxReasoner {

	private final EquivalencesDAGImpl<ObjectPropertyExpression> objectPropertyDAG;
	private final EquivalencesDAGImpl<DataPropertyExpression> dataPropertyDAG;
	private final EquivalencesDAGImpl<ClassExpression> classDAG;
	private final EquivalencesDAGImpl<DataRangeExpression> dataRangeDAG;

	/**
	 * the EquivalenceMap maps predicates to the representatives of their equivalence class (in TBox)
	 * (these maps are only used for the equivalence-reduced TBoxes)
	 * 
	 * it contains 
	 * 		- an entry for each property name other than the representative of an equivalence class 
	 * 				(or its inverse)
	 * 		- an entry for each class name other than the representative of its equivalence class
	 */
	
	private final Map<Predicate, OClass> classEquivalenceMap;
	private final Map<Predicate, ObjectPropertyExpression> objectPropertyEquivalenceMap;	
	private final Map<Predicate, DataPropertyExpression> dataPropertyEquivalenceMap;	

	/**
	 * constructs from a raw ontology
	 * @param onto: ontology
	 */

	public static TBoxReasoner create(Ontology onto) {
		final DefaultDirectedGraph<ObjectPropertyExpression, DefaultEdge> objectPropertyGraph = 
				OntologyGraph.getObjectPropertyGraph(onto);
		final DefaultDirectedGraph<DataPropertyExpression, DefaultEdge> dataPropertyGraph = 
				OntologyGraph.getDataPropertyGraph(onto);
		final DefaultDirectedGraph<ClassExpression, DefaultEdge> classGraph = 
				OntologyGraph.getClassGraph(onto, objectPropertyGraph, dataPropertyGraph, false);
		final DefaultDirectedGraph<DataRangeExpression, DefaultEdge> dataRangeGraph = 
				OntologyGraph.getDataRangeGraph(onto, dataPropertyGraph);

		return new TBoxReasonerImpl(objectPropertyGraph, dataPropertyGraph, classGraph, dataRangeGraph);
	}
	
	/**
	 * constructs from DAGs and equivalence maps (for the equivalence simplification) 
	 * @param classDAG
	 * @param propertyDAG
	 * @param classEquivalenceMap
	 * @param propertyEquivalenceMap
	 */
	private TBoxReasonerImpl(EquivalencesDAGImpl<ClassExpression> classDAG,
						EquivalencesDAGImpl<DataRangeExpression> dataRangeDAG,
						EquivalencesDAGImpl<ObjectPropertyExpression> objectPropertyDAG, 
						EquivalencesDAGImpl<DataPropertyExpression> dataPropertyDAG, 
						Map<Predicate, OClass> classEquivalenceMap, 
						Map<Predicate, ObjectPropertyExpression> objectPropertyEquivalenceMap,
						Map<Predicate, DataPropertyExpression> dataPropertyEquivalenceMap) {
		this.objectPropertyDAG = objectPropertyDAG;		
		this.dataPropertyDAG = dataPropertyDAG;		
		this.classDAG = classDAG;
		this.dataRangeDAG = dataRangeDAG;

		this.classEquivalenceMap = classEquivalenceMap;
		this.objectPropertyEquivalenceMap = objectPropertyEquivalenceMap;		
		this.dataPropertyEquivalenceMap = dataPropertyEquivalenceMap;		
	}
	
    /**
     * constructs from graphs (for the chain reasoner)
     * @param propertyGraph
     * @param classGraph
     */
	private TBoxReasonerImpl(DefaultDirectedGraph<ObjectPropertyExpression,DefaultEdge> objectPropertyGraph, 
					DefaultDirectedGraph<DataPropertyExpression,DefaultEdge> dataPropertyGraph, 
					DefaultDirectedGraph<ClassExpression,DefaultEdge> classGraph,
					DefaultDirectedGraph<DataRangeExpression,DefaultEdge> dataRangeGraph) {
		
		objectPropertyDAG = EquivalencesDAGImpl.getEquivalencesDAG(objectPropertyGraph);		
		dataPropertyDAG = EquivalencesDAGImpl.getEquivalencesDAG(dataPropertyGraph);		
		classDAG = EquivalencesDAGImpl.getEquivalencesDAG(classGraph);
		dataRangeDAG = EquivalencesDAGImpl.getEquivalencesDAG(dataRangeGraph);

		chooseObjectPropertyRepresentatives(objectPropertyDAG);
		chooseDataPropertyRepresentatives(dataPropertyDAG);
		chooseClassRepresentatives(classDAG, objectPropertyDAG, dataPropertyDAG);
		chooseDataRangeRepresentatives(dataRangeDAG, dataPropertyDAG);
		
		this.classEquivalenceMap = new HashMap<>();
		this.objectPropertyEquivalenceMap = new  HashMap<>();				
		this.dataPropertyEquivalenceMap = new  HashMap<>();				
	}

	
	@Override
	public String toString() {
		return objectPropertyDAG.toString() + "\n" + dataPropertyDAG.toString() + "\n" + classDAG.toString();
	}
	
	
		
	
	/**
	 * Return the DAG of classes
	 * 
	 * @return DAG 
	 */


	@Override
	public EquivalencesDAG<ClassExpression> getClassDAG() {
		return classDAG;
	}

	/**
	 * Return the DAG of datatypes and data property ranges
	 * 
	 * @return DAG 
	 */


	@Override
	public EquivalencesDAG<DataRangeExpression> getDataRangeDAG() {
		return dataRangeDAG;
	}
	
	/**
	 * Return the DAG of properties
	 * 
	 * @return DAG 
	 */

	public EquivalencesDAG<ObjectPropertyExpression> getObjectPropertyDAG() {
		return objectPropertyDAG;
	}
	
	public EquivalencesDAG<DataPropertyExpression> getDataPropertyDAG() {
		return dataPropertyDAG;
	}
	
	@Override
	public OClass getClassRepresentative(OClass p) {
		return classEquivalenceMap.get(p.getPredicate());
	}

	@Override
	public ObjectPropertyExpression getObjectPropertyRepresentative(ObjectPropertyExpression p) {
		return objectPropertyEquivalenceMap.get(p.getPredicate());
	}
		
	@Override
	public DataPropertyExpression getDataPropertyRepresentative(DataPropertyExpression p) {
		return dataPropertyEquivalenceMap.get(p.getPredicate());
	}
	
	// INTERNAL DETAILS
	

	
	
	@Deprecated // test only
	public DefaultDirectedGraph<ClassExpression,DefaultEdge> getClassGraph() {
		return classDAG.getGraph();
	}
	@Deprecated // test only
	public DefaultDirectedGraph<DataRangeExpression,DefaultEdge> getDataRangeGraph() {
		return dataRangeDAG.getGraph();
	}
	
	@Deprecated // test only
	public DefaultDirectedGraph<ObjectPropertyExpression,DefaultEdge> getObjectPropertyGraph() {
		return objectPropertyDAG.getGraph();
	}
	@Deprecated // test only
	public DefaultDirectedGraph<DataPropertyExpression,DefaultEdge> getDataPropertyGraph() {
		return dataPropertyDAG.getGraph();
	}
	
	
	@Deprecated // test only
	public int edgeSetSize() {
		return objectPropertyDAG.edgeSetSize() + dataPropertyDAG.edgeSetSize() + classDAG.edgeSetSize();
	}
	
	@Deprecated // test only
	public int vertexSetSize() {
		return objectPropertyDAG.vertexSetSize() + dataPropertyDAG.vertexSetSize() +  classDAG.vertexSetSize();
	}
	
	
	// ---------------------------------------------------------------------------------
	
	
	// lexicographical comparison of property names (a property is before its inverse) 
	private static final Comparator<DataPropertyExpression> dataPropertyComparator = new Comparator<DataPropertyExpression>() {
		@Override
		public int compare(DataPropertyExpression o1, DataPropertyExpression o2) {
			int compared = o1.getPredicate().getName().compareTo(o2.getPredicate().getName()); 
			return compared;
		}
	}; 

	// lexicographical comparison of property names (a property is before its inverse) 
	private static final Comparator<ObjectPropertyExpression> objectPropertyComparator = new Comparator<ObjectPropertyExpression>() {
		@Override
		public int compare(ObjectPropertyExpression o1, ObjectPropertyExpression o2) {
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
	

	public static void chooseObjectPropertyRepresentatives(EquivalencesDAGImpl<ObjectPropertyExpression> dag) {
		
		for (Equivalences<ObjectPropertyExpression> set : dag) {
			
			// skip if has already been done 
			if (set.getRepresentative() != null)
				continue;
				
			ObjectPropertyExpression rep = Collections.min(set.getMembers(), objectPropertyComparator);	
			ObjectPropertyExpression repInv = rep.getInverse();
			
			Equivalences<ObjectPropertyExpression> setInv = dag.getVertex(repInv);
			
			if (rep.isInverse()) {
				repInv = Collections.min(setInv.getMembers(), objectPropertyComparator);	
				rep = repInv.getInverse();	
				
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
	}	
	
	public static void chooseDataPropertyRepresentatives(EquivalencesDAGImpl<DataPropertyExpression> dag) {
		
		for (Equivalences<DataPropertyExpression> set : dag) {			
			// skip if has already been done 
			if (set.getRepresentative() != null)
				continue;
				
			DataPropertyExpression rep = Collections.min(set.getMembers(), dataPropertyComparator);	

			set.setIndexed();
			
			set.setRepresentative(rep);
		}				
	}	
	
		
	// lexicographical comparison of class names 
	private static final Comparator<OClass> classComparator = new Comparator<OClass>() {
		@Override
		public int compare(OClass o1, OClass o2) {
			return o1.getPredicate().getName().compareTo(o2.getPredicate().getName());
		}
	}; 
	
	// lexicographical comparison of class names 
	private static final Comparator<Datatype> datatypeComparator = new Comparator<Datatype>() {
		@Override
		public int compare(Datatype o1, Datatype o2) {
			return o1.getPredicate().getName().compareTo(o2.getPredicate().getName());
		}
	}; 
	
	
	public static void chooseClassRepresentatives(EquivalencesDAGImpl<ClassExpression> dag, 
				EquivalencesDAG<ObjectPropertyExpression> objectPropertyDAG, 
				EquivalencesDAG<DataPropertyExpression> dataPropertyDAG) {

		for (Equivalences<ClassExpression> equivalenceSet : dag) {

			ClassExpression representative = null;			
			if (equivalenceSet.size() <= 1) {
				representative = equivalenceSet.iterator().next();
			}
			else {
				// find a named class as a representative 
				OClass namedRepresentative = null;
				for (ClassExpression e : equivalenceSet) 
					if (e instanceof OClass) {
						if (namedRepresentative == null || classComparator.compare((OClass)e, namedRepresentative) < 0)
							namedRepresentative = (OClass)e;
					}
		
				if (namedRepresentative == null) {
					ClassExpression first = equivalenceSet.iterator().next();
					if (first instanceof ObjectSomeValuesFrom) {
						ObjectSomeValuesFrom firstp = (ObjectSomeValuesFrom)first;
						ObjectPropertyExpression prop = firstp.getProperty();
						ObjectPropertyExpression propRep = objectPropertyDAG.getVertex(prop).getRepresentative();
						representative = propRep.getDomain();
					}
					else {
						assert (first instanceof DataSomeValuesFrom); 
						DataSomeValuesFrom firstp = (DataSomeValuesFrom)first;
						DataPropertyExpression prop = firstp.getProperty();
						DataPropertyExpression propRep = dataPropertyDAG.getVertex(prop).getRepresentative();
						representative = propRep.getDomainRestriction(DatatypeImpl.rdfsLiteral);
					}
				}
				else
					representative = namedRepresentative;
			}

			equivalenceSet.setRepresentative(representative);
			if (representative instanceof OClass)
				equivalenceSet.setIndexed();
		}
	}
	
	public static void chooseDataRangeRepresentatives(EquivalencesDAGImpl<DataRangeExpression> dag, 
			EquivalencesDAG<DataPropertyExpression> dataPropertyDAG) {

		for (Equivalences<DataRangeExpression> equivalenceSet : dag) {

			DataRangeExpression representative = null;			
			if (equivalenceSet.size() <= 1) {
				representative = equivalenceSet.iterator().next();
			}
			else {
				// find a named class as a representative 
				Datatype namedRepresentative = null;
				for (DataRangeExpression e : equivalenceSet) 
					if (e instanceof Datatype) {
						if (namedRepresentative == null || datatypeComparator.compare((Datatype)e, namedRepresentative) < 0)
							namedRepresentative = (Datatype)e;
					}
		
				if (namedRepresentative == null) {
					DataRangeExpression first = equivalenceSet.iterator().next();
					assert (first instanceof DataPropertyRangeExpression);
					DataPropertyRangeExpression firstp = (DataPropertyRangeExpression)first;
					DataPropertyExpression prop = firstp.getProperty();
	                Equivalences<DataPropertyExpression> vertex = dataPropertyDAG.getVertex(prop);
	                if (vertex == null){
	                    throw new IllegalStateException("Unknown data property: " + prop);
	                }
	                DataPropertyExpression propRep = vertex.getRepresentative();
					representative = propRep.getRange();
				}
				else
					representative = namedRepresentative;
			}

			equivalenceSet.setRepresentative(representative);
			if (representative instanceof OClass)
				equivalenceSet.setIndexed();
		}		
	}
	
	/**
	 * constructs a TBoxReasoner that has a reduced number of classes and properties in each equivalent class
	 *  
	 *   - each object property equivalence class contains one property (representative)
	 *     except when the representative property is equivalent to its inverse, in which 
	 *     case the equivalence class contains both the property and its inverse
	 *     
	 *   - each data property equivalence class contains a single property (representative)
	 *   
	 *   - each class equivalence class contains the representative and all domains / ranges 
	 *     of the representatives of property equivalence classes
	 *     
	 *  in other words, the constructed TBoxReasoner is the restriction to the vocabulary of the representatives
	 *     all other symbols are mapped to the nodes via *Equivalences hash-maps
	 *     
	 *  note that *EquivalenceMap's are required for compatibility with older code
	 * 
	 * @param reasoner
	 * @return reduced reasoner
	 */
	
	
	public static TBoxReasonerImpl getEquivalenceSimplifiedReasoner(TBoxReasoner reasoner) {

		// OBJECT PROPERTIES
		// 		
		Map<Predicate, ObjectPropertyExpression> objectPropertyEquivalenceMap = new HashMap<>();
		SimpleDirectedGraph <Equivalences<ObjectPropertyExpression>,DefaultEdge> objectProperties 
					= new SimpleDirectedGraph<>(DefaultEdge.class);
		Map<ObjectPropertyExpression, Equivalences<ObjectPropertyExpression>> objectPropertyEquivalences = new HashMap<>();

		// create vertices for properties 
		for(Equivalences<ObjectPropertyExpression> node : reasoner.getObjectPropertyDAG()) {
			ObjectPropertyExpression rep = node.getRepresentative();
			ObjectPropertyExpression repInv = rep.getInverse();
			
			Set<ObjectPropertyExpression> reduced = new HashSet<>();
			Equivalences<ObjectPropertyExpression> reducedNode = new Equivalences<>(reduced, rep);
			
			for (ObjectPropertyExpression equi : node) {
				// if the property coincides with the representative or its inverse
				if (equi.equals(rep) || equi.equals(repInv)) {
					objectPropertyEquivalences.put(equi, reducedNode);
					reduced.add(equi);
				}
				else {
					// an entry in the equivalence map is created 
					// (but only for a non-inverse -- the inverse will be treated in the respective node    
					if (!equi.isInverse()) 
						objectPropertyEquivalenceMap.put(equi.getPredicate(), rep);
				}
			}
			if (node.isIndexed())
				reducedNode.setIndexed();
			objectProperties.addVertex(reducedNode);
		}
		
		// create edges for the properties graph
		copyEdges(reasoner.getObjectPropertyDAG(), objectPropertyEquivalences, objectProperties);
		EquivalencesDAGImpl<ObjectPropertyExpression> objectPropertyDAG = new EquivalencesDAGImpl<>(null, objectProperties, objectPropertyEquivalences);

		// DATA PROPERTIES
		// 		
		Map<Predicate, DataPropertyExpression> dataPropertyEquivalenceMap = new HashMap<>();
		SimpleDirectedGraph <Equivalences<DataPropertyExpression>,DefaultEdge> dataProperties 
					= new SimpleDirectedGraph<>(DefaultEdge.class);
		Map<DataPropertyExpression, Equivalences<DataPropertyExpression>> dataPropertyEquivalences = new HashMap<>();
		
		// create vertices for properties 
		for(Equivalences<DataPropertyExpression> node : reasoner.getDataPropertyDAG()) {
			DataPropertyExpression rep = node.getRepresentative();
			
			Set<DataPropertyExpression> reduced = new HashSet<>();
			Equivalences<DataPropertyExpression> reducedNode = new Equivalences<>(reduced, rep);
			
			for (DataPropertyExpression equi : node) {
				// if the property coincides with the representative 
				if (equi.equals(rep)) {
					dataPropertyEquivalences.put(equi, reducedNode);
					reduced.add(equi);
				}
				else
					// an entry in the equivalence map is created 
					dataPropertyEquivalenceMap.put(equi.getPredicate(), rep);
			}
			if (node.isIndexed())
				reducedNode.setIndexed();
			dataProperties.addVertex(reducedNode);
		}
		
		// create edges for the properties graph
		copyEdges(reasoner.getDataPropertyDAG(), dataPropertyEquivalences, dataProperties);
		EquivalencesDAGImpl<DataPropertyExpression> dataPropertyDAG = new EquivalencesDAGImpl<>(null, dataProperties, dataPropertyEquivalences);
				
		// CLASSES
		// 
		Map<Predicate, OClass> classEquivalenceMap = new HashMap<>();
		SimpleDirectedGraph <Equivalences<ClassExpression>,DefaultEdge> classes = new SimpleDirectedGraph<>(DefaultEdge.class);
		Map<ClassExpression, Equivalences<ClassExpression>> classEquivalences = new HashMap<>();
	
		// create vertices for classes
		for(Equivalences<ClassExpression> node : reasoner.getClassDAG()) {
			ClassExpression rep = node.getRepresentative();
			Set<ClassExpression> reduced = new HashSet<>();
			Equivalences<ClassExpression> reducedNode = new Equivalences<>(reduced, rep);
			
			for (ClassExpression equi : node) {	
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
				else if (equi instanceof ObjectSomeValuesFrom) {
					Predicate pred = ((ObjectSomeValuesFrom)equi).getProperty().getPredicate();
					// the property of the existential is a representative of its equivalence class
					if (objectPropertyEquivalenceMap.get(pred) == null) {
						classEquivalences.put(equi, reducedNode);
						reduced.add(equi);
					}
				}
				else  {
					assert (equi instanceof DataSomeValuesFrom);
					Predicate pred = ((DataSomeValuesFrom)equi).getProperty().getPredicate();
					// the property of the existential is a representative of its equivalence class
					if (dataPropertyEquivalenceMap.get(pred) == null) {
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
		copyEdges(reasoner.getClassDAG(), classEquivalences, classes);
		EquivalencesDAGImpl<ClassExpression> classDAG = new EquivalencesDAGImpl<>(null, classes, classEquivalences);
		
		// DATA RANGES
		// 
		// TODO: a proper implementation is in order here
		
		return new TBoxReasonerImpl(classDAG,  
				(EquivalencesDAGImpl<DataRangeExpression>)reasoner.getDataRangeDAG(), objectPropertyDAG, dataPropertyDAG, 
				classEquivalenceMap, objectPropertyEquivalenceMap, dataPropertyEquivalenceMap);
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
	
}
