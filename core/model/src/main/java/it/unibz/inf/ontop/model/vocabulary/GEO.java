package it.unibz.inf.ontop.model.vocabulary;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.simple.SimpleRDF;

public class GEO {
    public static final String PREFIX = "http://www.opengis.net/ont/geosparql#";
    public static final IRI GEO_WKT_LITERAL;

    static {
        org.apache.commons.rdf.api.RDF factory = new SimpleRDF();
        GEO_WKT_LITERAL = factory.createIRI(GEO.PREFIX + "wktLiteral");
    }
}
