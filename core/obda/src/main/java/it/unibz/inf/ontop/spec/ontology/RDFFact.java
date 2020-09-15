package it.unibz.inf.ontop.spec.ontology;

import it.unibz.inf.ontop.model.term.IRIConstant;
import it.unibz.inf.ontop.model.term.ObjectConstant;
import it.unibz.inf.ontop.model.term.RDFConstant;
import it.unibz.inf.ontop.spec.ontology.impl.RDFFactImpl;

import java.util.Optional;

public interface RDFFact {
    ObjectConstant getSubject();
    IRIConstant getProperty();
    RDFConstant getObject();

    Optional<ObjectConstant> getGraph();

    /**
     * If the class or the property (when the latter is not rdf:type)
     */
    ObjectConstant getClassOrProperty();

    boolean isClassAssertion();


    static RDFFact createQuadFact(ObjectConstant subject, IRIConstant property, RDFConstant object, ObjectConstant graph) {
        return RDFFactImpl.createQuadFact(subject, property, object, graph);
    }

    static RDFFact createTripleFact(ObjectConstant subject, IRIConstant property, RDFConstant object) {
        return RDFFactImpl.createTripleFact(subject, property, object);
    }
}
