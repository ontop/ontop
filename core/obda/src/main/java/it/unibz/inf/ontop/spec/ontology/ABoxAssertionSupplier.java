package it.unibz.inf.ontop.spec.ontology;

import it.unibz.inf.ontop.model.term.ObjectConstant;
import it.unibz.inf.ontop.model.term.RDFLiteralConstant;

/*
    creates ABox assertions without checking their vocabulary against an ontology
    (used in implementations of iterators)
 */

public interface ABoxAssertionSupplier {

    /**
     * Creates a class assertion
     *    (implements rule [C4])
     *
     * @param c
     * @param o
     * @return null if ce is the top class ([C4])
     * @throws InconsistentOntologyException if ce is the bottom class ([C4])
     */

    ClassAssertion createClassAssertion(String c, ObjectConstant o) throws InconsistentOntologyException;


    /**
     * Creates an object property assertion
     * (ensures that the property is not inverse by swapping arguments if necessary)
     *    (implements rule [O4])
     *
     * @param op
     * @param o1
     * @param o2
     * @return null if ope is the top property ([O4])
     * @throws InconsistentOntologyException if ope is the bottom property ([O4])
     */

    ObjectPropertyAssertion createObjectPropertyAssertion(String op, ObjectConstant o1, ObjectConstant o2) throws InconsistentOntologyException;

    /**
     * Creates a data property assertion
     *    (implements rule [D4])
     *
     * @param dp
     * @param o
     * @param v
     * @return null if dpe is the top property ([D4])
     * @throws InconsistentOntologyException if dpe is the bottom property ([D4])
     */

    DataPropertyAssertion createDataPropertyAssertion(String dp, ObjectConstant o, RDFLiteralConstant v) throws InconsistentOntologyException;
}
