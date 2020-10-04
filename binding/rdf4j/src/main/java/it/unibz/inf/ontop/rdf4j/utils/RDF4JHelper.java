package it.unibz.inf.ontop.rdf4j.utils;

import it.unibz.inf.ontop.model.term.BNode;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.spec.ontology.*;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.Objects;

public class RDF4JHelper {

    private static final ValueFactory fact = SimpleValueFactory.getInstance();

    public static Resource getResource(ObjectConstant obj) {
        if (obj instanceof BNode)
            return fact.createBNode(((BNode) obj).getName());
        else if (obj instanceof IRIConstant)
            return fact.createIRI(((IRIConstant) obj).getIRI().getIRIString());
        else
            return null;
    }

    /**
     * TODO: could we have a RDF sub-class of ValueConstant?
     */
    public static Literal getLiteral(RDFLiteralConstant literal) {
        Objects.requireNonNull(literal);
        RDFDatatype type = literal.getType();
        if (type == null)
            // TODO: throw a proper exception
            throw new IllegalStateException("A ValueConstant given to OWLAPI must have a RDF datatype");

        return type.getLanguageTag()
                .map(lang -> fact.createLiteral(literal.getValue(), lang.getFullString()))
                .orElseGet(() -> fact.createLiteral(literal.getValue(),
                        fact.createIRI(type.getIRI().getIRIString())));
    }

    public static Value getValue(RDFConstant c) {
        if (c == null)
            return null;

        Value value = null;
        if (c instanceof RDFLiteralConstant) {
            value = RDF4JHelper.getLiteral((RDFLiteralConstant) c);
        } else if (c instanceof ObjectConstant) {
            value = RDF4JHelper.getResource((ObjectConstant) c);
        }
        return value;
    }

    private static IRI createURI(String uri) {
        return fact.createIRI(uri);
    }

    public static Statement createStatement(RDFFact assertion) {

        return assertion.getGraph()
                .map(g -> fact.createStatement(
                        getResource(assertion.getSubject()),
                        createURI(assertion.getProperty().getIRI().getIRIString()),
                        getValue(assertion.getObject()),
                        getResource(g)))
                .orElseGet(() -> fact.createStatement(
                        getResource(assertion.getSubject()),
                        createURI(assertion.getProperty().getIRI().getIRIString()),
                        getValue(assertion.getObject())));
    }
}
