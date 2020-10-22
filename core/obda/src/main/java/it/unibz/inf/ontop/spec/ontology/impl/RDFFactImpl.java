package it.unibz.inf.ontop.spec.ontology.impl;

import com.google.common.base.Objects;
import it.unibz.inf.ontop.model.term.IRIConstant;
import it.unibz.inf.ontop.model.term.ObjectConstant;
import it.unibz.inf.ontop.model.term.RDFConstant;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.ontology.RDFFact;

import javax.annotation.Nullable;
import java.util.Optional;

public class RDFFactImpl implements RDFFact {

    private final ObjectConstant subject;
    private final IRIConstant property;
    private final RDFConstant object;

    @Nullable
    private final ObjectConstant graph;
    private final ObjectConstant classOrProperty;

    /**
     * Quad
     */
    private RDFFactImpl(ObjectConstant subject, IRIConstant property, RDFConstant object, ObjectConstant graph) {
        this.subject = subject;
        this.property = property;
        this.object = object;
        this.graph = graph;
        this.classOrProperty = extractClassOrProperty(property, object);
    }

    /**
     * Triple
     */
    private RDFFactImpl(ObjectConstant subject, IRIConstant property, RDFConstant object) {
        this.subject = subject;
        this.property = property;
        this.object = object;
        this.graph = null;
        this.classOrProperty = extractClassOrProperty(property, object);
    }

    public static RDFFact createQuadFact(ObjectConstant subject, IRIConstant property, RDFConstant object, ObjectConstant graph) {
        return new RDFFactImpl(subject, property, object, graph);
    }

    public static RDFFact createTripleFact(ObjectConstant subject, IRIConstant property, RDFConstant object) {
        return new RDFFactImpl(subject, property, object);
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
        if (!(o instanceof RDFFact)) return false;
        RDFFact rdfFact = (RDFFact) o;
        return Objects.equal(getSubject(), rdfFact.getSubject()) &&
                Objects.equal(getProperty(), rdfFact.getProperty()) &&
                Objects.equal(getObject(), rdfFact.getObject()) &&
                Objects.equal(getGraph(), rdfFact.getGraph()) &&
                Objects.equal(getClassOrProperty(), rdfFact.getClassOrProperty());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getSubject(), getProperty(), getObject(), getGraph(), getClassOrProperty());
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
