package it.unibz.inf.ontop.model.vocabulary;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.simple.SimpleRDF;

public class OntopInternal {

    private static final String PREFIX = "urn:it:unibz:inf:ontop:internal:";


    public static final IRI NUMERIC;
    public static final IRI DATE_OR_DATETIME;

    /**
     * TODO: remove it!
     */
    public static final IRI UNSUPPORTED;

    public static final String PREFIX_XSD;
    public static final String PREFIX_RDF;
    public static final String PREFIX_RDFS;
    public static final String PREFIX_OWL;
    public static final String PREFIX_OBDA;

    static {
        RDF rdfFactory = new SimpleRDF();

        NUMERIC = rdfFactory.createIRI(PREFIX + "numeric");
        DATE_OR_DATETIME = rdfFactory.createIRI(PREFIX + "dateOrDateTime");
        UNSUPPORTED = rdfFactory.createIRI(PREFIX + "unsupported");
        PREFIX_XSD = "xsd:";
        PREFIX_RDF = "rdf:";
        PREFIX_RDFS = "rdfs:";
        PREFIX_OWL = "owl:";
        PREFIX_OBDA = "obda:";

    }
}
