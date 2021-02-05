package it.unibz.inf.ontop.spec.ontology;

import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ObjectConstant;
import it.unibz.inf.ontop.model.term.RDFConstant;
import it.unibz.inf.ontop.model.term.RDFLiteralConstant;
import org.apache.commons.rdf.api.IRI;

public interface OntologyBuilder {


    OntologyVocabularyCategory<OClass> classes();

    OntologyVocabularyCategory<ObjectPropertyExpression> objectProperties();

    OntologyVocabularyCategory<DataPropertyExpression> dataProperties();

    OntologyVocabularyCategory<AnnotationProperty> annotationProperties();

    Datatype getDatatype(String uri);

    /**
     * declare an entity
     *
     * @param iri
     * @return entity object
     */

    OClass declareClass(IRI iri);

    ObjectPropertyExpression declareObjectProperty(IRI iri);

    DataPropertyExpression declareDataProperty(IRI iri);

    AnnotationProperty declareAnnotationProperty(IRI iri);




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

    /**
     * inserts a class assertion
     *    (does nothing if ce is the top class, cf. rule [C4])
     *
     * @param ce
     * @param o
     * @throws InconsistentOntologyException if ce is the bottom class ([C4])
     */

    void addClassAssertion(OClass ce, ObjectConstant o) throws InconsistentOntologyException;

    /**
     * inserts an object property assertion
     * (ensures that the property is not inverse by swapping arguments if necessary)
     *    (does nothing if ope is the top property, cf. rule [O4])
     *
     * @param ope
     * @param o1
     * @param o2
     * @throws InconsistentOntologyException if ope is the bottom property ([O4])
     */

    void addObjectPropertyAssertion(ObjectPropertyExpression ope, ObjectConstant o1, ObjectConstant o2) throws InconsistentOntologyException;

    /**
     * inserts a data property assertion
     *    (does nothing if dpe is the top property, cf. rule [O4])
     *
     * @param dpe
     * @param o
     * @param v
     * @throws InconsistentOntologyException if dpe is the bottom property ([D4])
     */

    void addDataPropertyAssertion(DataPropertyExpression dpe, ObjectConstant o, RDFLiteralConstant v) throws InconsistentOntologyException;

    /**
     * inserts an annotation property assertion
     *
     * @param ap
     * @param o
     * @param c
     */

    void addAnnotationAssertion(AnnotationProperty ap, ObjectConstant o, RDFConstant c);


    // build

    Ontology build();

}
