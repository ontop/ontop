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


import it.unibz.krdb.obda.ontology.BinaryAxiom;
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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import com.google.common.collect.ImmutableSet;

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
	 * constructs from a raw ontology
	 * @param onto: ontology
	 */

	public static TBoxReasoner create(Ontology onto) {
		final DefaultDirectedGraph<ObjectPropertyExpression, DefaultEdge> objectPropertyGraph = 
				getObjectPropertyGraph(onto);
		final EquivalencesDAGImpl<ObjectPropertyExpression> objectPropertyDAG = 
				EquivalencesDAGImpl.getEquivalencesDAG(objectPropertyGraph);
		
		final DefaultDirectedGraph<DataPropertyExpression, DefaultEdge> dataPropertyGraph = 
				getDataPropertyGraph(onto);
		final EquivalencesDAGImpl<DataPropertyExpression> dataPropertyDAG = 
				EquivalencesDAGImpl.getEquivalencesDAG(dataPropertyGraph);	
		
		final EquivalencesDAGImpl<ClassExpression> classDAG = 
				 EquivalencesDAGImpl.getEquivalencesDAG(getClassGraph(onto, objectPropertyGraph, dataPropertyGraph));
		
		final EquivalencesDAGImpl<DataRangeExpression> dataRangeDAG = 
				 EquivalencesDAGImpl.getEquivalencesDAG(getDataRangeGraph(onto, dataPropertyGraph));

		chooseObjectPropertyRepresentatives(objectPropertyDAG);
		chooseDataPropertyRepresentatives(dataPropertyDAG);
		chooseClassRepresentatives(classDAG, objectPropertyDAG, dataPropertyDAG);
		chooseDataRangeRepresentatives(dataRangeDAG, dataPropertyDAG);
		
		return new TBoxReasonerImpl(classDAG, dataRangeDAG, objectPropertyDAG, dataPropertyDAG);
	}
	
	/**
	 * constructs from DAGs
	 * @param classDAG
	 * @param propertyDAG
	 */
	private TBoxReasonerImpl(EquivalencesDAGImpl<ClassExpression> classDAG,
						EquivalencesDAGImpl<DataRangeExpression> dataRangeDAG,
						EquivalencesDAGImpl<ObjectPropertyExpression> objectPropertyDAG, 
						EquivalencesDAGImpl<DataPropertyExpression> dataPropertyDAG) {
		this.objectPropertyDAG = objectPropertyDAG;		
		this.dataPropertyDAG = dataPropertyDAG;		
		this.classDAG = classDAG;
		this.dataRangeDAG = dataRangeDAG;
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
		Equivalences<ClassExpression> ces = classDAG.equivalenceIndex.get(p);
		if (ces == null)
			return null;
		
		return (OClass)ces.getRepresentative();
	}

	@Override
	public ObjectPropertyExpression getObjectPropertyRepresentative(ObjectPropertyExpression p) {
		Equivalences<ObjectPropertyExpression> opes = objectPropertyDAG.equivalenceIndex.get(p);
		if (opes == null)
			return null;
		
		return opes.getRepresentative();
	}
		
	@Override
	public DataPropertyExpression getDataPropertyRepresentative(DataPropertyExpression p) {
		Equivalences<DataPropertyExpression> dpes = dataPropertyDAG.equivalenceIndex.get(p);
		if (dpes == null)
			return null;
		
		return dpes.getRepresentative();
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
			int compared = o1.getName().compareTo(o2.getName()); 
			return compared;
		}
	}; 

	// lexicographical comparison of property names (a property is before its inverse) 
	private static final Comparator<ObjectPropertyExpression> objectPropertyComparator = new Comparator<ObjectPropertyExpression>() {
		@Override
		public int compare(ObjectPropertyExpression o1, ObjectPropertyExpression o2) {
			int compared = o1.getName().compareTo(o2.getName()); 
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
	

	private static void chooseObjectPropertyRepresentatives(EquivalencesDAGImpl<ObjectPropertyExpression> dag) {
		
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
	
	private static void chooseDataPropertyRepresentatives(EquivalencesDAGImpl<DataPropertyExpression> dag) {
		
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
			return o1.getName().compareTo(o2.getName());
		}
	}; 
	
	// lexicographical comparison of class names 
	private static final Comparator<Datatype> datatypeComparator = new Comparator<Datatype>() {
		@Override
		public int compare(Datatype o1, Datatype o2) {
			return o1.getPredicate().getName().compareTo(o2.getPredicate().getName());
		}
	}; 
	
	
	private static void chooseClassRepresentatives(EquivalencesDAGImpl<ClassExpression> dag, 
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
	
	private static void chooseDataRangeRepresentatives(EquivalencesDAGImpl<DataRangeExpression> dag, 
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
		SimpleDirectedGraph<Equivalences<ObjectPropertyExpression>, DefaultEdge> objectProperties 
					= new SimpleDirectedGraph<>(DefaultEdge.class);

		// create vertices for properties 
		for(Equivalences<ObjectPropertyExpression> node : reasoner.getObjectPropertyDAG()) {
			ObjectPropertyExpression rep = node.getRepresentative();
			ObjectPropertyExpression repInv = rep.getInverse();
			
			Equivalences<ObjectPropertyExpression> reducedNode;
			if (!node.contains(repInv))
				reducedNode = new Equivalences<>(ImmutableSet.of(rep), rep, node.isIndexed());
			else
				// the object property is equivalent to its inverse
				reducedNode = new Equivalences<>(ImmutableSet.of(rep, repInv), rep, node.isIndexed());	
				
			objectProperties.addVertex(reducedNode);
		}
		
		EquivalencesDAGImpl<ObjectPropertyExpression> objectPropertyDAG 
					= factorize(reasoner.getObjectPropertyDAG(), objectProperties);

		// DATA PROPERTIES
		// 		
		SimpleDirectedGraph<Equivalences<DataPropertyExpression>, DefaultEdge> dataProperties 
					= new SimpleDirectedGraph<>(DefaultEdge.class);
		
		// create vertices for properties 
		for(Equivalences<DataPropertyExpression> node : reasoner.getDataPropertyDAG()) {
			DataPropertyExpression rep = node.getRepresentative();
			
			Equivalences<DataPropertyExpression> reducedNode = new Equivalences<>(ImmutableSet.of(rep), rep, node.isIndexed());
			dataProperties.addVertex(reducedNode);
		}
		
		EquivalencesDAGImpl<DataPropertyExpression> dataPropertyDAG = 
							factorize(reasoner.getDataPropertyDAG(), dataProperties);
				
		// CLASSES
		// 
		SimpleDirectedGraph<Equivalences<ClassExpression>, DefaultEdge> classes = new SimpleDirectedGraph<>(DefaultEdge.class);
	
		// create vertices for classes
		for(Equivalences<ClassExpression> node : reasoner.getClassDAG()) {
			ClassExpression rep = node.getRepresentative();
			
			ImmutableSet.Builder<ClassExpression> reduced = new ImmutableSet.Builder<>();	
			for (ClassExpression equi : node) {	
				if (equi.equals(rep)) {
					reduced.add(equi);
				}
				else if (equi instanceof OClass) {
					// an entry is created for a named class
					//OClass equiClass = (OClass) equi;
					//classEquivalenceMap.put(equiClass.getName(), (OClass)rep);
				}
				else if (equi instanceof ObjectSomeValuesFrom) {
					// the property of the existential is a representative of its equivalence class
					if (objectPropertyDAG.getVertex(((ObjectSomeValuesFrom) equi).getProperty()) != null)
						reduced.add(equi);
				}
				else  {
					// the property of the existential is a representative of its equivalence class
					if (dataPropertyDAG.getVertex(((DataSomeValuesFrom) equi).getProperty()) != null)
						reduced.add(equi);
				}
			}
			
			Equivalences<ClassExpression> reducedNode = new Equivalences<>(reduced.build(), rep, node.isIndexed());			
			classes.addVertex(reducedNode);
		}			

		EquivalencesDAGImpl<ClassExpression> classDAG = factorize(reasoner.getClassDAG(), classes);
		
		// DATA RANGES
		// 
		// TODO: a proper implementation is in order here
		
		return new TBoxReasonerImpl(classDAG, (EquivalencesDAGImpl<DataRangeExpression>)reasoner.getDataRangeDAG(), 
				objectPropertyDAG, dataPropertyDAG);
	}
	
	
	private static <T> EquivalencesDAGImpl<T> factorize(EquivalencesDAG<T> source, SimpleDirectedGraph <Equivalences<T>,DefaultEdge> target) {
		
		Map<T, Equivalences<T>> map = new HashMap<>();
		Map<T, Equivalences<T>> equivalences = new HashMap<>();
		for (Equivalences<T> v : target.vertexSet()) {
			for (T equi : v.getMembers()) 
				equivalences.put(equi, v);
			
			for (T s : source.getVertex(v.getRepresentative()))
				if (!v.contains(s)) 		
					map.put(s, v);	
		}
			
		// create induced edges in the target graph		
		for (Equivalences<T> node : source) {
			T rep = node.getRepresentative();
			Equivalences<T> reducedNode = equivalences.get(rep);
			
			for (Equivalences<T> subNode : source.getDirectSub(node)) {
				T subRep = subNode.getRepresentative();
				Equivalences<T> subReducedNode = equivalences.get(subRep);
				target.addEdge(subReducedNode, reducedNode);
			}
		}		
		
		return new EquivalencesDAGImpl<>(null, target, equivalences, map);
	}
	
	
	
	
	
	
	/**
	 *  graph representation of object property inclusions in the ontology
	 *  
	 *  adds inclusions between the inverses of R and S if
	 *         R is declared a sub-property of S in the ontology
	 * 
	 * @param an ontology 
	 * @return the graph of the property inclusions 
	 */
	
	private static DefaultDirectedGraph<ObjectPropertyExpression,DefaultEdge> getObjectPropertyGraph(Ontology ontology) {
		
		DefaultDirectedGraph<ObjectPropertyExpression,DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
				
		for (ObjectPropertyExpression role : ontology.getVocabulary().getObjectProperties()) {
			if (!role.isBottom() && !role.isTop()) {
				graph.addVertex(role);
				graph.addVertex(role.getInverse());
			}
		}
		
		for (ObjectPropertyExpression role : ontology.getAuxiliaryObjectProperties()) {
			graph.addVertex(role);
			graph.addVertex(role.getInverse());
		}
		
		// property inclusions
		for (BinaryAxiom<ObjectPropertyExpression> roleIncl : ontology.getSubObjectPropertyAxioms()) {
			// adds the direct edge and the inverse (e.g., R ISA S and R- ISA S-)
			graph.addEdge(roleIncl.getSub(), roleIncl.getSuper());			
			graph.addEdge(roleIncl.getSub().getInverse(), roleIncl.getSuper().getInverse());
		}
		
		return graph;
	}
	
	/**
	 *  graph representation of data property inclusions in the ontology
	 *  
	 *  adds inclusions between the inverses of R and S if
	 *         R is declared a sub-property of S in the ontology
	 * 
	 * @param an ontology 
	 * @return the graph of the property inclusions 
	 */
	
	private static DefaultDirectedGraph<DataPropertyExpression,DefaultEdge> getDataPropertyGraph(Ontology ontology) {
		
		DefaultDirectedGraph<DataPropertyExpression,DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
				
		for (DataPropertyExpression role : ontology.getVocabulary().getDataProperties()) 
			if (!role.isBottom() && !role.isTop()) 
				graph.addVertex(role);
		
		for (DataPropertyExpression role : ontology.getAuxiliaryDataProperties()) 
			graph.addVertex(role);

		for (BinaryAxiom<DataPropertyExpression> roleIncl : ontology.getSubDataPropertyAxioms()) 
			graph.addEdge(roleIncl.getSub(), roleIncl.getSuper());
		
		return graph;
	}
	
	/**
	 * graph representation of the class inclusions in the ontology
	 * 
	 * adds inclusions of the domain of R in the domain of S if
	 *           the provided property graph has an edge from R to S
	 *           (given the getPropertyGraph algorithm, this also 
	 *           implies inclusions of the range of R in the range of S
	 * 
	 * @param ontology
	 * @param objectPropertyGraph obtained by getObjectPropertyGraph
	 * @param dataPropertyGraph obtained by getDataPropertyGraph
	 * @return the graph of the concept inclusions
	 */
	
	private static DefaultDirectedGraph<ClassExpression,DefaultEdge> getClassGraph (Ontology ontology, 
													DefaultDirectedGraph<ObjectPropertyExpression,DefaultEdge> objectPropertyGraph, 
													DefaultDirectedGraph<DataPropertyExpression,DefaultEdge> dataPropertyGraph) {
		
		DefaultDirectedGraph<ClassExpression,DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
		
		for (OClass concept : ontology.getVocabulary().getClasses()) 
			if (!concept.isBottom() && !concept.isTop()) 
				graph.addVertex(concept);
	
		// domains and ranges of roles
		for (ObjectPropertyExpression role : objectPropertyGraph.vertexSet()) 
			graph.addVertex(role.getDomain());			
		
		// edges between the domains and ranges for sub-properties
		for (DefaultEdge edge : objectPropertyGraph.edgeSet()) {
			ObjectPropertyExpression child = objectPropertyGraph.getEdgeSource(edge);
			ObjectPropertyExpression parent = objectPropertyGraph.getEdgeTarget(edge);
			graph.addEdge(child.getDomain(), parent.getDomain());		
		}
		
		// domains and ranges of roles
		for (DataPropertyExpression role : dataPropertyGraph.vertexSet()) 
			graph.addVertex(role.getDomainRestriction(DatatypeImpl.rdfsLiteral));			
		
		// edges between the domains and ranges for sub-properties
		for (DefaultEdge edge : dataPropertyGraph.edgeSet()) {
			DataPropertyExpression child = dataPropertyGraph.getEdgeSource(edge);
			DataPropertyExpression parent = dataPropertyGraph.getEdgeTarget(edge);
			graph.addEdge(child.getDomainRestriction(DatatypeImpl.rdfsLiteral), parent.getDomainRestriction(DatatypeImpl.rdfsLiteral));		
		}

		
		// class inclusions from the ontology
		for (BinaryAxiom<ClassExpression> clsIncl : ontology.getSubClassAxioms()) 
			graph.addEdge(clsIncl.getSub(), clsIncl.getSuper());
		 
		return graph;
	}

	private static DefaultDirectedGraph<DataRangeExpression,DefaultEdge> getDataRangeGraph (Ontology ontology, 
							DefaultDirectedGraph<DataPropertyExpression,DefaultEdge> dataPropertyGraph) {

		DefaultDirectedGraph<DataRangeExpression,DefaultEdge> dataRangeGraph 
					= new  DefaultDirectedGraph<DataRangeExpression,DefaultEdge>(DefaultEdge.class);

		// ranges of roles
		for (DataPropertyExpression role : dataPropertyGraph.vertexSet()) 
			dataRangeGraph.addVertex(role.getRange());			
		
		// edges between the ranges for sub-properties
		for (DefaultEdge edge : dataPropertyGraph.edgeSet()) {
			DataPropertyExpression child = dataPropertyGraph.getEdgeSource(edge);
			DataPropertyExpression parent = dataPropertyGraph.getEdgeTarget(edge);
			dataRangeGraph.addEdge(child.getRange(), parent.getRange());		
		}

		// data range inclusions from the ontology
		for (BinaryAxiom<DataRangeExpression> clsIncl : ontology.getSubDataRangeAxioms()) {
			dataRangeGraph.addVertex(clsIncl.getSuper()); // Datatype is not among the vertices from the start
			dataRangeGraph.addEdge(clsIncl.getSub(), clsIncl.getSuper());
		}

		return dataRangeGraph;
	}
	
}
