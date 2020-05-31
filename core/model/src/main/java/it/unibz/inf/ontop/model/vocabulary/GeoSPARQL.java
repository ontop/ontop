package it.unibz.inf.ontop.model.vocabulary;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.simple.SimpleRDF;

public class GeoSPARQL {

    public static final String GEO_PREFIX = "http://www.opengis.net/ont/geosparql#";

    public static final String GEOF_PREFIX = "http://www.opengis.net/def/function/geosparql/";

    public static final IRI GEO_WKT_LITERAL;

    public static final IRI GEOF_SFWITHIN;


    static {
        org.apache.commons.rdf.api.RDF factory = new SimpleRDF();
        GEO_WKT_LITERAL = factory.createIRI(GEO_PREFIX + "wktLiteral");

        GEOF_SFWITHIN = factory.createIRI(GEOF_PREFIX + "sfWithin");
    }
}

