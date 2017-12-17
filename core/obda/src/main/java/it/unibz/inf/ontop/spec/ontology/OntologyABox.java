package it.unibz.inf.ontop.spec.ontology;

import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ObjectConstant;
import it.unibz.inf.ontop.model.term.ValueConstant;

import java.util.List;

public interface OntologyABox {

    OClass getClass(String uri);

    ObjectPropertyExpression getObjectProperty(String uri);

    DataPropertyExpression getDataProperty(String uri);

    AnnotationProperty getAnnotationProperty(String uri);

    // ASSERTIONS

    void addClassAssertion(ClassAssertion assertion);

    void addObjectPropertyAssertion(ObjectPropertyAssertion assertion);

    void addDataPropertyAssertion(DataPropertyAssertion assertion);

    void addAnnotationAssertion(AnnotationAssertion assertion);


    List<ClassAssertion> getClassAssertions();

    List<ObjectPropertyAssertion> getObjectPropertyAssertions();

    List<DataPropertyAssertion> getDataPropertyAssertions();

    List<AnnotationAssertion> getAnnotationAssertions();





    /**
     * Creates a class assertion
     *    (implements rule [C4])
     *
     * @param ce
     * @param o
     * @return
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
}
