package it.unibz.inf.ontop.spec.ontology;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public interface OntologyTBox {

    OntologyVocabularyCategory<OClass> classes();

    OntologyVocabularyCategory<ObjectPropertyExpression> objectProperties();

    /**
     * return all auxiliary object properties
     * (auxiliary properties result from ontology normalization)
     *
     * @return
     */

    ImmutableSet<ObjectPropertyExpression> getAuxiliaryObjectProperties();

    OntologyVocabularyCategory<DataPropertyExpression> dataProperties();


    // SUBCLASS/PROPERTY

    ImmutableList<BinaryAxiom<ClassExpression>> getSubClassAxioms();

    ImmutableList<BinaryAxiom<DataRangeExpression>> getSubDataRangeAxioms();

    ImmutableList<BinaryAxiom<ObjectPropertyExpression>> getSubObjectPropertyAxioms();

    ImmutableList<BinaryAxiom<DataPropertyExpression>> getSubDataPropertyAxioms();


    // DISJOINTNESS

    ImmutableList<NaryAxiom<ClassExpression>> getDisjointClassesAxioms();

    ImmutableList<NaryAxiom<ObjectPropertyExpression>> getDisjointObjectPropertiesAxioms();

    ImmutableList<NaryAxiom<DataPropertyExpression>> getDisjointDataPropertiesAxioms();


    // REFLEXIVITY / IRREFLEXIVITY

    ImmutableSet<ObjectPropertyExpression> getReflexiveObjectPropertyAxioms();

    ImmutableSet<ObjectPropertyExpression> getIrreflexiveObjectPropertyAxioms();

    // FUNCTIONALITY

    ImmutableSet<ObjectPropertyExpression> getFunctionalObjectProperties();

    ImmutableSet<DataPropertyExpression> getFunctionalDataProperties();

}
