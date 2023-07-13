package it.unibz.inf.ontop.model.vocabulary;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.simple.SimpleRDF;

public class Ontop {

    private static final RDF rdfFactory = new SimpleRDF();

    public static final String PREFIX = "https://w3id.org/obda/vocabulary#";
    public static final String FUNCTION_PREFIX = "https://w3id.org/obda/functions#";
    public static final IRI CANONICAL_IRI = rdfFactory.createIRI(PREFIX + "isCanonicalIRIOf");
    public static final IRI WEEK_FROM_DATETIME;
    public static final IRI QUARTER_FROM_DATETIME;
    public static final IRI DECADE_FROM_DATETIME;
    public static final IRI CENTURY_FROM_DATETIME;
    public static final IRI MILLENNIUM_FROM_DATETIME;
    public static final IRI MILLISECONDS_FROM_DATETIME;
    public static final IRI MICROSECONDS_FROM_DATETIME;
    public static final IRI DATE_TRUNC;

    static {
        org.apache.commons.rdf.api.RDF factory = new SimpleRDF();

        WEEK_FROM_DATETIME = factory.createIRI(FUNCTION_PREFIX + "week-from-dateTime");
        QUARTER_FROM_DATETIME = factory.createIRI(FUNCTION_PREFIX + "quarter-from-dateTime");
        DECADE_FROM_DATETIME = factory.createIRI(FUNCTION_PREFIX + "decade-from-dateTime");
        CENTURY_FROM_DATETIME = factory.createIRI(FUNCTION_PREFIX + "century-from-dateTime");
        MILLENNIUM_FROM_DATETIME = factory.createIRI(FUNCTION_PREFIX + "millennium-from-dateTime");
        MILLISECONDS_FROM_DATETIME = factory.createIRI(FUNCTION_PREFIX + "milliseconds-from-dateTime");
        MICROSECONDS_FROM_DATETIME = factory.createIRI(FUNCTION_PREFIX + "microseconds-from-dateTime");
        DATE_TRUNC = factory.createIRI(FUNCTION_PREFIX + "dateTrunc");
    }
}
