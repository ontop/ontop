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
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.DataSomeValuesFrom;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.ObjectSomeValuesFrom;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.PropertyExpression;
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
	
	private final EquivalencesDAGImpl<ObjectPropertyExpression> objectPropertyDAG;
	private final EquivalencesDAGImpl<DataPropertyExpression> dataPropertyDAG;
	private final EquivalencesDAGImpl<BasicClassDescription> classDAG;

	/**
	 * the EquivalenceMap maps predicates to the representatives of their equivalence class (in TBox)
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
	public TBoxReasonerImpl(Ontology onto) {
		objectPropertyDAG = EquivalencesDAGImpl.getEquivalencesDAG(OntologyGraph.getObjectPropertyGraph(onto));	
		dataPropertyDAG = EquivalencesDAGImpl.getEquivalencesDAG(OntologyGraph.getDataPropertyGraph(onto));	
		classDAG = EquivalencesDAGImpl.getEquivalencesDAG(OntologyGraph.getClassGraph(onto, objectPropertyDAG.getGraph(), dataPropertyDAG.getGraph(), false));

		chooseObjectPropertyRepresentatives(objectPropertyDAG);
		chooseDataPropertyRepresentatives(dataPropertyDAG);
		chooseClassRepresentatives(classDAG, objectPropertyDAG, dataPropertyDAG);
		
		this.classEquivalenceMap = new HashMap<Predicate, OClass>();
		this.objectPropertyEquivalenceMap = new  HashMap<Predicate, ObjectPropertyExpression>();		
		this.dataPropertyEquivalenceMap = new  HashMap<Predicate, DataPropertyExpression>();		
	}

	/**
	 * constructs from DAGs and equivalence maps (for the equivalence simplification) 
	 * @param classDAG
	 * @param propertyDAG
	 * @param classEquivalenceMap
	 * @param propertyEquivalenceMap
	 */
	private TBoxReasonerImpl(EquivalencesDAGImpl<BasicClassDescription> classDAG, 
						EquivalencesDAGImpl<ObjectPropertyExpression> objectPropertyDAG, 
						EquivalencesDAGImpl<DataPropertyExpression> dataPropertyDAG, 
						Map<Predicate, OClass> classEquivalenceMap, 
						Map<Predicate, ObjectPropertyExpression> objectPropertyEquivalenceMap,
						Map<Predicate, DataPropertyExpression> dataPropertyEquivalenceMap) {
		this.objectPropertyDAG = objectPropertyDAG;		
		this.dataPropertyDAG = dataPropertyDAG;		
		this.classDAG = classDAG;

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
					DefaultDirectedGraph<BasicClassDescription,DefaultEdge> classGraph) {
		objectPropertyDAG = EquivalencesDAGImpl.getEquivalencesDAG(objectPropertyGraph);		
		dataPropertyDAG = EquivalencesDAGImpl.getEquivalencesDAG(dataPropertyGraph);		
		classDAG = EquivalencesDAGImpl.getEquivalencesDAG(classGraph);

		chooseObjectPropertyRepresentatives(objectPropertyDAG);
		chooseDataPropertyRepresentatives(dataPropertyDAG);
		chooseClassRepresentatives(classDAG, objectPropertyDAG, dataPropertyDAG);
		
		this.classEquivalenceMap = new HashMap<Predicate, OClass>();
		this.objectPropertyEquivalenceMap = new  HashMap<Predicate, ObjectPropertyExpression>();				
		this.dataPropertyEquivalenceMap = new  HashMap<Predicate, DataPropertyExpression>();				
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
	public EquivalencesDAG<BasicClassDescription> getClasses() {
		return classDAG;
	}

	/**
	 * Return the DAG of properties
	 * 
	 * @return DAG 
	 */

	public EquivalencesDAG<ObjectPropertyExpression> getObjectProperties() {
		return objectPropertyDAG;
	}
	
	public EquivalencesDAG<DataPropertyExpression> getDataProperties() {
		return dataPropertyDAG;
	}
	
	@Override
	public OClass getClassRepresentative(Predicate p) {
		return classEquivalenceMap.get(p);
	}

	@Override
	public ObjectPropertyExpression getObjectPropertyRepresentative(Predicate p) {
		return objectPropertyEquivalenceMap.get(p);
	}
		
	@Override
	public DataPropertyExpression getDataPropertyRepresentative(Predicate p) {
		return dataPropertyEquivalenceMap.get(p);
	}
	
	// INTERNAL DETAILS
	

	
	
	@Deprecated // test only
	public DefaultDirectedGraph<BasicClassDescription,DefaultEdge> getClassGraph() {
		return classDAG.getGraph();
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
	private static final Comparator<PropertyExpression> propertyComparator = new Comparator<PropertyExpression>() {
		@Override
		public int compare(PropertyExpression o1, PropertyExpression o2) {
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
				
			ObjectPropertyExpression rep = Collections.min(set.getMembers(), propertyComparator);	
			ObjectPropertyExpression repInv = rep.getInverse();
			
			Equivalences<ObjectPropertyExpression> setInv = dag.getVertex(repInv);
			
			if (rep.isInverse()) {
				repInv = Collections.min(setInv.getMembers(), propertyComparator);	
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
				
			DataPropertyExpression rep = Collections.min(set.getMembers(), propertyComparator);	

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
	
	
	
	public static void chooseClassRepresentatives(EquivalencesDAGImpl<BasicClassDescription> dag, 
				EquivalencesDAG<ObjectPropertyExpression> objectPropertyDAG, 
				EquivalencesDAG<DataPropertyExpression> dataPropertyDAG) {

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
					BasicClassDescription first = equivalenceSet.iterator().next();
					if (first instanceof ObjectSomeValuesFrom) {
						ObjectSomeValuesFrom firstp = (ObjectSomeValuesFrom)first;
						ObjectPropertyExpression prop = firstp.getProperty();
						ObjectPropertyExpression propRep = objectPropertyDAG.getVertex(prop).getRepresentative();
						representative = fac.createPropertySomeRestriction(propRep);
					}
					else {
						assert (first instanceof DataSomeValuesFrom); 
						DataSomeValuesFrom firstp = (DataSomeValuesFrom)first;
						DataPropertyExpression prop = firstp.getProperty();
						DataPropertyExpression propRep = dataPropertyDAG.getVertex(prop).getRepresentative();
						representative = fac.createPropertySomeRestriction(propRep);
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
	
	
	
	public static TBoxReasonerImpl getEquivalenceSimplifiedReasoner(TBoxReasoner reasoner) {

		Map<Predicate, ObjectPropertyExpression> objectPropertyEquivalenceMap = new HashMap<Predicate, ObjectPropertyExpression>();
		SimpleDirectedGraph <Equivalences<ObjectPropertyExpression>,DefaultEdge> objectProperties 
					= new SimpleDirectedGraph<Equivalences<ObjectPropertyExpression>,DefaultEdge>(DefaultEdge.class);
		Map<ObjectPropertyExpression, Equivalences<ObjectPropertyExpression>> objectPropertyEquivalences 
					= new HashMap<ObjectPropertyExpression, Equivalences<ObjectPropertyExpression>>();


		// create vertices for properties 
		
		for(Equivalences<ObjectPropertyExpression> node : reasoner.getObjectProperties()) {
			ObjectPropertyExpression prop = node.getRepresentative();
			ObjectPropertyExpression inverseProp = prop.getInverse();
			
			Set<ObjectPropertyExpression> reduced = new HashSet<ObjectPropertyExpression>();
			Equivalences<ObjectPropertyExpression> reducedNode = new Equivalences<ObjectPropertyExpression>(reduced, prop);
			
			for (ObjectPropertyExpression equi : node) {
				// no map entry if the property coincides with its inverse
				if (equi.equals(prop) || equi.equals(inverseProp)) {
					objectPropertyEquivalences.put(equi, reducedNode);
					reduced.add(equi);
					continue;
				}

				// if the property is different from its inverse, an entry is created 
				// (taking the inverses into account)
				if (equi.isInverse()) 
					objectPropertyEquivalenceMap.put(equi.getPredicate(), inverseProp);
				else 
					objectPropertyEquivalenceMap.put(equi.getPredicate(), prop);
			}
			if (node.isIndexed())
				reducedNode.setIndexed();
			objectProperties.addVertex(reducedNode);
		}
		
		// create edges for the properties graph
		copyEdges(reasoner.getObjectProperties(), objectPropertyEquivalences, objectProperties);
		EquivalencesDAGImpl<ObjectPropertyExpression> objectPropertyDAG = new EquivalencesDAGImpl<ObjectPropertyExpression>(null, objectProperties, objectPropertyEquivalences);

		
		Map<Predicate, DataPropertyExpression> dataPropertyEquivalenceMap = new HashMap<Predicate, DataPropertyExpression>();
		SimpleDirectedGraph <Equivalences<DataPropertyExpression>,DefaultEdge> dataProperties 
					= new SimpleDirectedGraph<Equivalences<DataPropertyExpression>,DefaultEdge>(DefaultEdge.class);
		Map<DataPropertyExpression, Equivalences<DataPropertyExpression>> dataPropertyEquivalences 
					= new HashMap<DataPropertyExpression, Equivalences<DataPropertyExpression>>();
		
		
		// create vertices for properties 
		
		for(Equivalences<DataPropertyExpression> node : reasoner.getDataProperties()) {
			DataPropertyExpression prop = node.getRepresentative();
			DataPropertyExpression inverseProp = prop.getInverse();
			
			Set<DataPropertyExpression> reduced = new HashSet<DataPropertyExpression>();
			Equivalences<DataPropertyExpression> reducedNode = new Equivalences<DataPropertyExpression>(reduced, prop);
			
			for (DataPropertyExpression equi : node) {
				// no map entry if the property coincides with its inverse
				if (equi.equals(prop) || equi.equals(inverseProp)) {
					dataPropertyEquivalences.put(equi, reducedNode);
					reduced.add(equi);
					continue;
				}

				// if the property is different from its inverse, an entry is created 
				// (taking the inverses into account)
				if (equi.isInverse()) 
					dataPropertyEquivalenceMap.put(equi.getPredicate(), inverseProp);
				else 
					dataPropertyEquivalenceMap.put(equi.getPredicate(), prop);
			}
			if (node.isIndexed())
				reducedNode.setIndexed();
			dataProperties.addVertex(reducedNode);
		}
		
		// create edges for the properties graph
		copyEdges(reasoner.getDataProperties(), dataPropertyEquivalences, dataProperties);
		EquivalencesDAGImpl<DataPropertyExpression> dataPropertyDAG = new EquivalencesDAGImpl<DataPropertyExpression>(null, dataProperties, dataPropertyEquivalences);
		
		
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
				else if (equi instanceof ObjectSomeValuesFrom) {
					Predicate pred = ((ObjectSomeValuesFrom)equi).getProperty().getPredicate();
					// the property of the existential is a representative of its equivalence class
					if (objectPropertyEquivalenceMap.get(pred) == null) {
						classEquivalences.put(equi, reducedNode);
						reduced.add(equi);
					}
				}
				else if (equi instanceof DataSomeValuesFrom) {
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
		copyEdges(reasoner.getClasses(), classEquivalences, classes);
		EquivalencesDAGImpl<BasicClassDescription> classDAG = new EquivalencesDAGImpl<BasicClassDescription>(null, classes, classEquivalences);
		
		
		return new TBoxReasonerImpl(classDAG, objectPropertyDAG, dataPropertyDAG, classEquivalenceMap, objectPropertyEquivalenceMap, dataPropertyEquivalenceMap);
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
			
			if ((!(node instanceof ObjectSomeValuesFrom) && !(node instanceof DataSomeValuesFrom)) || processedNodes.contains(node)) 
				continue;

			/*
			 * Adding a cycle between exists R and exists R- for each R.
			 */
			BasicClassDescription invNode;

			if (node instanceof ObjectSomeValuesFrom) {
				ObjectPropertyExpression exists = ((ObjectSomeValuesFrom) node).getProperty();
				invNode = fac.createPropertySomeRestriction(exists.getInverse());				
			}
			else {
				DataPropertyExpression exists = ((DataSomeValuesFrom) node).getProperty();
				invNode = fac.createPropertySomeRestriction(exists.getInverse());
			// TODO: fix DataRange
//				invNode = fac.createDataPropertyRange((DataPropertyExpression)exists);		
			}
				
			Equivalences<BasicClassDescription> existsInvNode = classes.getVertex(invNode);
			
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
		return new TBoxReasonerImpl(tbox.objectPropertyDAG.getGraph(), tbox.dataPropertyDAG.getGraph(), modifiedGraph);
	}


}
