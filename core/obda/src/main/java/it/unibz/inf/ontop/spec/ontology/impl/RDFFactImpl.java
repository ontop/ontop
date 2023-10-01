package it.unibz.inf.ontop.spec.ontology.impl;

import it.unibz.inf.ontop.model.term.IRIConstant;
import it.unibz.inf.ontop.model.term.ObjectConstant;
import it.unibz.inf.ontop.model.term.RDFConstant;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.ontology.RDFFact;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public class RDFFactImpl implements RDFFact {

    private final ObjectConstant subject;
    private final IRIConstant property;
    private final RDFConstant object;

    @Nullable
    private final ObjectConstant graph;
    private final ObjectConstant classOrProperty;

    private RDFFactImpl(ObjectConstant subject, IRIConstant property, RDFConstant object, @Nullable ObjectConstant graph) {
        this.subject = Objects.requireNonNull(subject);
        this.property = Objects.requireNonNull(property);
        this.object = Objects.requireNonNull(object);
        this.graph = graph;
        this.classOrProperty = extractClassOrProperty(property, object);
    }

    public static RDFFact createQuadFact(ObjectConstant subject, IRIConstant property, RDFConstant object, ObjectConstant graph) {
        return new RDFFactImpl(subject, property, object, graph);
    }

    public static RDFFact createTripleFact(ObjectConstant subject, IRIConstant property, RDFConstant object) {
        return new RDFFactImpl(subject, property, object, null);
    }


    private static ObjectConstant extractClassOrProperty(IRIConstant property, RDFConstant object) {
        if (property.getIRI().equals(RDF.TYPE)) {
            if (object instanceof ObjectConstant)
                return (ObjectConstant) object;
            else
                throw new IllegalArgumentException("The class must be an IRI or a b-node");
        }
        else
            return property;
    }

    @Override
    public ObjectConstant getSubject() {
        return subject;
    }

    @Override
    public IRIConstant getProperty() {
        return property;
    }

    @Override
    public RDFConstant getObject() {
        return object;
    }

    @Override
    public Optional<ObjectConstant> getGraph() {
        return Optional.ofNullable(graph);
    }

    @Override
    public ObjectConstant getClassOrProperty() {
        return classOrProperty;
    }

    @Override
    public boolean isClassAssertion() {
        return !getClassOrProperty().equals(property);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof RDFFactImpl) {
            RDFFactImpl rdfFact = (RDFFactImpl) o;
            return subject.equals(rdfFact.subject) &&
                    property.equals(rdfFact.property) &&
                    object.equals(rdfFact.object) &&
                    Objects.equals(graph, rdfFact.graph);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(subject, property, object, graph);
    }

    @Override
    public String toString() {
        return "RDFFact{" +
                "subject=" + subject +
                ", property=" + property +
                ", object=" + object +
                ", graph=" + graph +
                ", classOrProperty=" + classOrProperty +
                '}';
    }
}
