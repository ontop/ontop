package it.unibz.inf.ontop.spec.ontology;

import java.util.Collection;
import java.util.Set;

public interface OntologyTBox {

    OntologyVocabularyCategory<OClass> classes();

    OntologyVocabularyCategory<ObjectPropertyExpression> objectProperties();

    OntologyVocabularyCategory<DataPropertyExpression> dataProperties();


    // SUBCLASS/PROPERTY

    void addSubClassOfAxiom(ClassExpression concept1, ClassExpression concept2) throws InconsistentOntologyException;

    void addDataPropertyRangeAxiom(DataPropertyRangeExpression range, Datatype datatype) throws InconsistentOntologyException;

    void addSubPropertyOfAxiom(ObjectPropertyExpression included, ObjectPropertyExpression including) throws InconsistentOntologyException;

    void addSubPropertyOfAxiom(DataPropertyExpression included, DataPropertyExpression including) throws InconsistentOntologyException;


    Collection<BinaryAxiom<ClassExpression>> getSubClassAxioms();

    Collection<BinaryAxiom<DataRangeExpression>> getSubDataRangeAxioms();

    Collection<BinaryAxiom<ObjectPropertyExpression>> getSubObjectPropertyAxioms();

    Collection<BinaryAxiom<DataPropertyExpression>> getSubDataPropertyAxioms();


    // DISJOINTNESS

    void addDisjointClassesAxiom(ClassExpression... classes) throws InconsistentOntologyException;

    void addDisjointObjectPropertiesAxiom(ObjectPropertyExpression... properties) throws InconsistentOntologyException;

    void addDisjointDataPropertiesAxiom(DataPropertyExpression... properties) throws InconsistentOntologyException;


    Collection<NaryAxiom<ClassExpression>> getDisjointClassesAxioms();

    Collection<NaryAxiom<ObjectPropertyExpression>> getDisjointObjectPropertiesAxioms();

    Collection<NaryAxiom<DataPropertyExpression>> getDisjointDataPropertiesAxioms();


    // REFLEXIVITY / IRREFLEXIVITY

    void addReflexiveObjectPropertyAxiom(ObjectPropertyExpression ope) throws InconsistentOntologyException;

    void addIrreflexiveObjectPropertyAxiom(ObjectPropertyExpression ope) throws InconsistentOntologyException;

    Collection<ObjectPropertyExpression> getReflexiveObjectPropertyAxioms();

    Collection<ObjectPropertyExpression> getIrreflexiveObjectPropertyAxioms();

    // FUNCTIONALITY


    void addFunctionalObjectPropertyAxiom(ObjectPropertyExpression prop);

    void addFunctionalDataPropertyAxiom(DataPropertyExpression prop);

    Set<ObjectPropertyExpression> getFunctionalObjectProperties();

    Set<DataPropertyExpression> getFunctionalDataProperties();


    /**
     * create an auxiliary object property
     * (auxiliary properties result from ontology normalization)
     *
     *
     */

    ObjectPropertyExpression createAuxiliaryObjectProperty();


    /**
     * return all auxiliary object properties
     * (auxiliary properties result from ontology normalization)
     *
     * @return
     */

    Collection<ObjectPropertyExpression> getAuxiliaryObjectProperties();

}
