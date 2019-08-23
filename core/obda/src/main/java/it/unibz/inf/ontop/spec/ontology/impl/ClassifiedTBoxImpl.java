package it.unibz.inf.ontop.spec.ontology.impl;

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


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.spec.ontology.*;

import java.util.Collections;
import java.util.Comparator;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import com.google.common.collect.ImmutableSet;

/**
 * ClassifiedTBoxImpl
 *
 *    a DAG-based TBox reasoner
 *
 * @author Roman Kontchakov
 *
 */

public class ClassifiedTBoxImpl implements ClassifiedTBox {

	private final OntologyVocabularyCategory<ObjectPropertyExpression> objectProperties;
    private final OntologyVocabularyCategory<DataPropertyExpression> dataProperties;
    private final OntologyVocabularyCategory<OClass> classes;
    private final OntologyVocabularyCategory<AnnotationProperty> annotationProperties;

    private final EquivalencesDAGImpl<ClassExpression> classDAG;
    private final EquivalencesDAGImpl<ObjectPropertyExpression> objectPropertyDAG;
    private final EquivalencesDAGImpl<DataPropertyExpression> dataPropertyDAG;
    private final EquivalencesDAGImpl<DataRangeExpression> dataRangeDAG;

    private final ImmutableList<NaryAxiom<ClassExpression>> classDisjointness;
    private final ImmutableList<NaryAxiom<ObjectPropertyExpression>> objectPropertyDisjointness;
    private final ImmutableList<NaryAxiom<DataPropertyExpression>> dataPropertyDisjointness;

    private final ImmutableSet<ObjectPropertyExpression> reflexiveObjectProperties;
    private final ImmutableSet<ObjectPropertyExpression> irreflexiveObjectProperties;

    private final ImmutableSet<ObjectPropertyExpression> functionalObjectProperties;
    private final ImmutableSet<DataPropertyExpression> functionalDataProperties;


    /**
	 * constructs a TBox reasoner from an ontology
	 * @param onto: ontology
	 */

	static ClassifiedTBox classify(OntologyImpl.UnclassifiedOntologyTBox onto) {

		DefaultDirectedGraph<ObjectPropertyExpression, DefaultEdge> objectPropertyGraph =
				getObjectPropertyGraph(onto);
		EquivalencesDAGImpl<ObjectPropertyExpression> objectPropertyDAG =
				EquivalencesDAGImpl.getEquivalencesDAG(objectPropertyGraph);

		DefaultDirectedGraph<DataPropertyExpression, DefaultEdge> dataPropertyGraph =
				getDataPropertyGraph(onto);
		EquivalencesDAGImpl<DataPropertyExpression> dataPropertyDAG =
				EquivalencesDAGImpl.getEquivalencesDAG(dataPropertyGraph);

		EquivalencesDAGImpl<ClassExpression> classDAG =
				EquivalencesDAGImpl.getEquivalencesDAG(getClassGraph(onto, objectPropertyGraph, dataPropertyGraph));

		EquivalencesDAGImpl<DataRangeExpression> dataRangeDAG =
				EquivalencesDAGImpl.getEquivalencesDAG(getDataRangeGraph(onto, dataPropertyGraph));

		chooseObjectPropertyRepresentatives(objectPropertyDAG);
		chooseDataPropertyRepresentatives(dataPropertyDAG);
		chooseClassRepresentatives(classDAG, objectPropertyDAG, dataPropertyDAG);
		chooseDataRangeRepresentatives(dataRangeDAG, dataPropertyDAG);

		ClassifiedTBoxImpl r = new ClassifiedTBoxImpl(
                onto.classes(),
                onto.objectProperties(),
                onto.dataProperties(),
                onto.annotationProperties(),
                classDAG,
                objectPropertyDAG,
                dataPropertyDAG,
                dataRangeDAG,
                onto.getDisjointClassesAxioms(),
                onto.getDisjointObjectPropertiesAxioms(),
                onto.getDisjointDataPropertiesAxioms(),
                onto.getReflexiveObjectPropertyAxioms(),
                onto.getIrreflexiveObjectPropertyAxioms(),
                onto.getFunctionalObjectProperties(),
                onto.getFunctionalDataProperties());
		return r;
	}

	/**
	 * constructs from DAGs
	 * @param classDAG
	 * @param dataRangeDAG
	 * @param objectPropertyDAG
	 * @param objectPropertyDAG
	 */
	private ClassifiedTBoxImpl(OntologyVocabularyCategory<OClass> classes,
							   OntologyVocabularyCategory<ObjectPropertyExpression> objectProperties,
							   OntologyVocabularyCategory<DataPropertyExpression> dataProperties,
							   OntologyVocabularyCategory<AnnotationProperty> annotationProperties,
                               EquivalencesDAGImpl<ClassExpression> classDAG,
                               EquivalencesDAGImpl<ObjectPropertyExpression> objectPropertyDAG,
                               EquivalencesDAGImpl<DataPropertyExpression> dataPropertyDAG,
							   EquivalencesDAGImpl<DataRangeExpression> dataRangeDAG,
                               ImmutableList<NaryAxiom<ClassExpression>> classDisjointness,
                               ImmutableList<NaryAxiom<ObjectPropertyExpression>> objectPropertyDisjointness,
                               ImmutableList<NaryAxiom<DataPropertyExpression>> dataPropertyDisjointness,
                               ImmutableSet<ObjectPropertyExpression> reflexiveObjectProperties,
                               ImmutableSet<ObjectPropertyExpression> irreflexiveObjectProperties,
                               ImmutableSet<ObjectPropertyExpression> functionalObjectProperties,
                               ImmutableSet<DataPropertyExpression> functionalDataProperties) {
        this.classes = classes;
		this.objectProperties = objectProperties;
		this.dataProperties = dataProperties;
		this.annotationProperties = annotationProperties;
		this.classDAG = classDAG;
		this.objectPropertyDAG = objectPropertyDAG;
		this.dataPropertyDAG = dataPropertyDAG;
        this.dataRangeDAG = dataRangeDAG;
        this.classDisjointness = classDisjointness;
        this.objectPropertyDisjointness = objectPropertyDisjointness;
        this.dataPropertyDisjointness = dataPropertyDisjointness;
        this.reflexiveObjectProperties = reflexiveObjectProperties;
        this.irreflexiveObjectProperties = irreflexiveObjectProperties;
        this.functionalObjectProperties = functionalObjectProperties;
        this.functionalDataProperties = functionalDataProperties;
	}

	@Override
	public String toString() {
		return objectPropertyDAG.toString() + "\n" + dataPropertyDAG.toString() + "\n" + classDAG.toString();
	}


    @Override
    public OntologyVocabularyCategory<ObjectPropertyExpression> objectProperties() { return objectProperties; }

    @Override
    public EquivalencesDAG<ObjectPropertyExpression> objectPropertiesDAG() { return objectPropertyDAG; }

    @Override
    public OntologyVocabularyCategory<DataPropertyExpression> dataProperties() { return dataProperties; }

    @Override
    public EquivalencesDAG<DataPropertyExpression> dataPropertiesDAG() { return dataPropertyDAG; }

    @Override
	public OntologyVocabularyCategory<OClass> classes() { return classes; }

    @Override
    public EquivalencesDAG<ClassExpression> classesDAG() { return classDAG; }

    @Override
    public EquivalencesDAG<DataRangeExpression> dataRangesDAG() { return dataRangeDAG; }




    @Override
    public ImmutableSet<ObjectPropertyExpression> functionalObjectProperties() { return functionalObjectProperties; }

    @Override
    public ImmutableSet<DataPropertyExpression> functionalDataProperties() { return functionalDataProperties; }

    @Override
    public ImmutableList<NaryAxiom<ClassExpression>> disjointClasses() { return classDisjointness; }

    @Override
    public ImmutableList<NaryAxiom<ObjectPropertyExpression>> disjointObjectProperties() { return objectPropertyDisjointness; }

    @Override
    public ImmutableList<NaryAxiom<DataPropertyExpression>> disjointDataProperties() { return dataPropertyDisjointness; }

    @Override
    public ImmutableSet<ObjectPropertyExpression> reflexiveObjectProperties() { return reflexiveObjectProperties; }

    @Override
    public ImmutableSet<ObjectPropertyExpression> irreflexiveObjectProperties() { return irreflexiveObjectProperties; }



    // INTERNAL DETAILS




	@Deprecated // test only
	public DefaultDirectedGraph<ClassExpression,DefaultEdge> getClassGraph() { return classDAG.getGraph(); }

	@Deprecated // test only
	public DefaultDirectedGraph<DataRangeExpression,DefaultEdge> getDataRangeGraph() { return dataRangeDAG.getGraph(); }

	@Deprecated // test only
	public DefaultDirectedGraph<ObjectPropertyExpression,DefaultEdge> getObjectPropertyGraph() { return objectPropertyDAG.getGraph();
	}

	@Deprecated // test only
	public DefaultDirectedGraph<DataPropertyExpression,DefaultEdge> getDataPropertyGraph() { return dataPropertyDAG.getGraph(); }

	@Deprecated // test only
	public int edgeSetSize() {
		return objectPropertyDAG.edgeSetSize() + dataPropertyDAG.edgeSetSize() + classDAG.edgeSetSize();
	}

	@Deprecated // test only
	public int vertexSetSize() {
		return objectPropertyDAG.vertexSetSize() + dataPropertyDAG.vertexSetSize() + classDAG.vertexSetSize();
	}


	// ---------------------------------------------------------------------------------


	// lexicographical comparison of property names (a property is before its inverse)
	private static final Comparator<DataPropertyExpression> dataPropertyComparator = Comparator.comparing(o -> o.getIRI().getIRIString());

	// lexicographical comparison of property names (a property is before its inverse)
	private static final Comparator<ObjectPropertyExpression> objectPropertyComparator = (o1, o2) -> {
		int compared = o1.getIRI().getIRIString().compareTo(o2.getIRI().getIRIString());
		if (compared == 0) {
			if (o1.isInverse() == o2.isInverse())
				return 0;
			else if (o2.isInverse())
				return -1;
			else
				return 1;
		}
		return compared;
	};


	private static void chooseObjectPropertyRepresentatives(EquivalencesDAG<ObjectPropertyExpression> dag) {

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
			if (!set.contains(repInv)) {
				// if not symmetric
				// (each set either consists of symmetric properties
				//        or none of the properties in the set is symmetric)
				setInv.setRepresentative(repInv);
			}
		}
	}

	private static void chooseDataPropertyRepresentatives(EquivalencesDAG<DataPropertyExpression> dag) {

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
	private static final Comparator<OClass> classComparator = Comparator.comparing(o -> o.getIRI().getIRIString());

	// lexicographical comparison of class names
	private static final Comparator<Datatype> datatypeComparator = Comparator.comparing(o -> o.getIRI().getIRIString());


	private static void chooseClassRepresentatives(EquivalencesDAG<ClassExpression> dag,
												   EquivalencesDAG<ObjectPropertyExpression> objectPropertyDAG,
												   EquivalencesDAG<DataPropertyExpression> dataPropertyDAG) {

		for (Equivalences<ClassExpression> equivalenceSet : dag) {

			final ClassExpression representative;
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

	private static void chooseDataRangeRepresentatives(EquivalencesDAG<DataRangeExpression> dag,
													   EquivalencesDAG<DataPropertyExpression> dataPropertyDAG) {

		for (Equivalences<DataRangeExpression> equivalenceSet : dag) {

			final DataRangeExpression representative;
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
	 *  graph representation of object property inclusions in the ontology
	 *
	 *  adds inclusions between the inverses of R and S if
	 *         R is declared a sub-property of S in the ontology
	 *
	 * @param ontology
	 * @return the graph of the property inclusions
	 */

	private static DefaultDirectedGraph<ObjectPropertyExpression,DefaultEdge> getObjectPropertyGraph(OntologyImpl.UnclassifiedOntologyTBox ontology) {

		DefaultDirectedGraph<ObjectPropertyExpression,DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

		for (ObjectPropertyExpression role : ontology.objectProperties()) {
			if (!role.isBottom() && !role.isTop()) {
				graph.addVertex(role);
				graph.addVertex(role.getInverse());
			}
		}

		for (ObjectPropertyExpression role : ontology.getAuxiliaryObjectProperties()) {
			graph.addVertex(role);
			graph.addVertex(role.getInverse());
		}

		ObjectPropertyExpression top = null;
		// property inclusions
		for (BinaryAxiom<ObjectPropertyExpression> roleIncl : ontology.getSubObjectPropertyAxioms()) {
		    if (roleIncl.getSub().isBottom() || roleIncl.getSuper().isTop())
		        continue;
            if (roleIncl.getSuper().isBottom()) {
                throw new RuntimeException("BOT cannot occur on the LHS - replaced by DISJ");
            }
            if (roleIncl.getSub().isTop()) {
                top = roleIncl.getSub();
                graph.addVertex(top);
            }

            // adds the direct edge and the inverse (e.g., R ISA S and R- ISA S-)
			graph.addEdge(roleIncl.getSub(), roleIncl.getSuper());
			graph.addEdge(roleIncl.getSub().getInverse(), roleIncl.getSuper().getInverse());
		}

		if (top != null) {
		    for (ObjectPropertyExpression ope : graph.vertexSet())
		        graph.addEdge(ope, top);
        }

		return graph;
	}

	/**
	 *  graph representation of data property inclusions in the ontology
	 *
	 *  adds inclusions between the inverses of R and S if
	 *         R is declared a sub-property of S in the ontology
	 *
	 * @param ontology
	 * @return the graph of the property inclusions
	 */

	private static DefaultDirectedGraph<DataPropertyExpression,DefaultEdge> getDataPropertyGraph(OntologyImpl.UnclassifiedOntologyTBox ontology) {

		DefaultDirectedGraph<DataPropertyExpression,DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

		for (DataPropertyExpression role : ontology.dataProperties())
			if (!role.isBottom() && !role.isTop())
				graph.addVertex(role);

        DataPropertyExpression top = null;
		for (BinaryAxiom<DataPropertyExpression> roleIncl : ontology.getSubDataPropertyAxioms()) {
		    if (roleIncl.getSub().isBottom() || roleIncl.getSuper().isTop())
		        continue;
		    if (roleIncl.getSuper().isBottom()) {
                throw new RuntimeException("BOT cannot occur on the LHS - replaced by DISJ");
            }
            if (roleIncl.getSub().isTop()) {
                top = roleIncl.getSub();
                graph.addVertex(top);
            }
            graph.addEdge(roleIncl.getSub(), roleIncl.getSuper());
        }

        if (top != null) {
            for (DataPropertyExpression dpe : graph.vertexSet())
                graph.addEdge(dpe, top);
        }

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

	private static DefaultDirectedGraph<ClassExpression,DefaultEdge> getClassGraph (OntologyImpl.UnclassifiedOntologyTBox ontology,
																					DefaultDirectedGraph<ObjectPropertyExpression,DefaultEdge> objectPropertyGraph,
																					DefaultDirectedGraph<DataPropertyExpression,DefaultEdge> dataPropertyGraph) {

		DefaultDirectedGraph<ClassExpression,DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

		for (OClass concept : ontology.classes())
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
			for (DataSomeValuesFrom dom : role.getAllDomainRestrictions())
				graph.addVertex(dom);

		// edges between the domains and ranges for sub-properties
		for (DefaultEdge edge : dataPropertyGraph.edgeSet()) {
			DataPropertyExpression child = dataPropertyGraph.getEdgeSource(edge);
			DataPropertyExpression parent = dataPropertyGraph.getEdgeTarget(edge);
			graph.addEdge(child.getDomainRestriction(DatatypeImpl.rdfsLiteral), parent.getDomainRestriction(DatatypeImpl.rdfsLiteral));
		}

		ClassExpression top = null;
		// class inclusions from the ontology
		for (BinaryAxiom<ClassExpression> clsIncl : ontology.getSubClassAxioms()) {
			if (clsIncl.getSub().isBottom() || clsIncl.getSuper().isTop())
				continue;
			if (clsIncl.getSuper().isBottom()) {
			    throw new RuntimeException("BOT cannot occur on the LHS - replaced by DISJ");
			}
			if (clsIncl.getSub().isTop()) {
				top = clsIncl.getSub();
				graph.addVertex(top);
			}
			graph.addEdge(clsIncl.getSub(), clsIncl.getSuper());
		}

        if (top != null) {
            for (ClassExpression c : graph.vertexSet())
                graph.addEdge(c, top);
        }

		return graph;
	}

	private static DefaultDirectedGraph<DataRangeExpression,DefaultEdge> getDataRangeGraph (OntologyImpl.UnclassifiedOntologyTBox ontology,
																							DefaultDirectedGraph<DataPropertyExpression,DefaultEdge> dataPropertyGraph) {

		DefaultDirectedGraph<DataRangeExpression,DefaultEdge> dataRangeGraph
				= new DefaultDirectedGraph<>(DefaultEdge.class);

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