package it.unibz.inf.ontop.spec.ontology;

import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ObjectConstant;
import it.unibz.inf.ontop.model.term.ValueConstant;

public interface OntologyBuilder {


    OntologyVocabularyCategory<OClass> classes();

    OntologyVocabularyCategory<ObjectPropertyExpression> objectProperties();

    OntologyVocabularyCategory<DataPropertyExpression> dataProperties();

    OntologyVocabularyCategory<AnnotationProperty> annotationProperties();

    Datatype getDatatype(String uri);

    /**
     * declare an entity
     *
     * @param uri
     * @return entity object
     */

    OClass declareClass(String uri);

    ObjectPropertyExpression declareObjectProperty(String uri);

    DataPropertyExpression declareDataProperty(String uri);

    AnnotationProperty declareAnnotationProperty(String uri);



    /**
     * Creates a class assertion
     *    (implements rule [C4])
     *
     * @param ce
     * @param o
     * @return null if ce is the top class ([C4])
     * @throws InconsistentOntologyException if ce is the bottom class ([C4])
     */

    ClassAssertion createClassAssertion(OClass ce, ObjectConstant o) throws InconsistentOntologyException;


    /**
     * Creates an object property assertion
     * (ensures that the property is not inverse by swapping arguments if necessary)
     *    (implements rule [O4])
     *
     * @param ope
     * @param o1
     * @param o2
     * @return null if ope is the top property ([O4])
     * @throws InconsistentOntologyException if ope is the bottom property ([O4])
     */

    ObjectPropertyAssertion createObjectPropertyAssertion(ObjectPropertyExpression ope, ObjectConstant o1, ObjectConstant o2) throws InconsistentOntologyException;

    /**
     * Creates a data property assertion
     *    (implements rule [D4])
     *
     * @param dpe
     * @param o
     * @param v
     * @return null if dpe is the top property ([D4])
     * @throws InconsistentOntologyException if dpe is the bottom property ([D4])
     */

    DataPropertyAssertion createDataPropertyAssertion(DataPropertyExpression dpe, ObjectConstant o, ValueConstant v) throws InconsistentOntologyException;

    /**
     * Creates an annotation property assertion
     *
     */

    AnnotationAssertion createAnnotationAssertion(AnnotationProperty ap, ObjectConstant o, Constant c);



    // SUBCLASS/PROPERTY

    void addSubClassOfAxiom(ClassExpression concept1, ClassExpression concept2) throws InconsistentOntologyException;

    void addDataPropertyRangeAxiom(DataPropertyRangeExpression range, Datatype datatype) throws InconsistentOntologyException;

    void addSubPropertyOfAxiom(ObjectPropertyExpression included, ObjectPropertyExpression including) throws InconsistentOntologyException;

    void addSubPropertyOfAxiom(DataPropertyExpression included, DataPropertyExpression including) throws InconsistentOntologyException;

    // DISJOINTNESS

    void addDisjointClassesAxiom(ClassExpression... classes) throws InconsistentOntologyException;

    void addDisjointObjectPropertiesAxiom(ObjectPropertyExpression... properties) throws InconsistentOntologyException;

    void addDisjointDataPropertiesAxiom(DataPropertyExpression... properties) throws InconsistentOntologyException;

    // REFLEXIVITY / IRREFLEXIVITY

    void addReflexiveObjectPropertyAxiom(ObjectPropertyExpression ope) throws InconsistentOntologyException;

    void addIrreflexiveObjectPropertyAxiom(ObjectPropertyExpression ope) throws InconsistentOntologyException;

    // FUNCTIONALITY

    void addFunctionalObjectPropertyAxiom(ObjectPropertyExpression prop);

    void addFunctionalDataPropertyAxiom(DataPropertyExpression prop);

    /**
     * create an auxiliary object property
     * (auxiliary properties result from ontology normalization)
     */

    ObjectPropertyExpression createAuxiliaryObjectProperty();

    // ASSERTIONS

    void addClassAssertion(OClass ce, ObjectConstant o) throws InconsistentOntologyException;

    void addObjectPropertyAssertion(ObjectPropertyExpression ope, ObjectConstant o1, ObjectConstant o) throws InconsistentOntologyException;

    void addDataPropertyAssertion(DataPropertyExpression dpe, ObjectConstant o, ValueConstant v) throws InconsistentOntologyException;

    void addAnnotationAssertion(AnnotationProperty ap, ObjectConstant o, Constant c);


    // build

    Ontology build();
}
