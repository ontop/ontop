package it.unibz.inf.ontop.model.vocabulary;


import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.simple.SimpleRDF;

public class RDF {

    public static final String PREFIX = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    public final static IRI TYPE;

    public final static IRI PROPERTY;

    public final static IRI PLAINLITERAL;

    public final static IRI XMLLITERAL;

    public static final IRI LANGSTRING;

    static {
        org.apache.commons.rdf.api.RDF factory = new SimpleRDF();
        TYPE = factory.createIRI(PREFIX + "type");
        PROPERTY = factory.createIRI(PREFIX + "Property");
        XMLLITERAL = factory.createIRI(PREFIX + "XMLLiteral");
        PLAINLITERAL = factory.createIRI(PREFIX + "PlainLiteral");
        LANGSTRING = factory.createIRI(PREFIX + "langString");
    }
}
