package it.unibz.inf.ontop.model.vocabulary;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.simple.SimpleRDF;

public class GEOF {

    public static final String PREFIX = "http://www.opengis.net/def/function/geosparql/";

    public static final IRI SF_WITHIN;

    public static final IRI DISTANCE;

    static {
        org.apache.commons.rdf.api.RDF factory = new SimpleRDF();

        SF_WITHIN = factory.createIRI(PREFIX + "sfWithin");

        DISTANCE = factory.createIRI(PREFIX + "distance");

    }
}

