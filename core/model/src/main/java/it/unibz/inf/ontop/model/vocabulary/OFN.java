package it.unibz.inf.ontop.model.vocabulary;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.simple.SimpleRDF;

public class OFN {

    // Adapted from https://graphdb.ontotext.com/free/devhub/time-functions.html
    public static final String PREFIX = "http://www.ontotext.com/sparql/functions/";

    /*
     * Time extension - duration arithmetic
     */
    public static final IRI WEEKSBETWEEN;

    public static final IRI DAYSBETWEEN;

    public static final IRI HOURSBETWEEN;

    public static final IRI MINUTESBETWEEN;

    public static final IRI SECONDSBETWEEN;

    public static final IRI MILLISBETWEEN;

    static {
        org.apache.commons.rdf.api.RDF factory = new SimpleRDF();

        /*
         * Time extension - duration arithmetic
         */
        WEEKSBETWEEN = factory.createIRI(PREFIX + "weeksBetween");

        DAYSBETWEEN = factory.createIRI(PREFIX + "daysBetween");

        HOURSBETWEEN = factory.createIRI(PREFIX + "hoursBetween");

        MINUTESBETWEEN = factory.createIRI(PREFIX + "minutesBetween");

        SECONDSBETWEEN = factory.createIRI(PREFIX + "secondsBetween");

        MILLISBETWEEN = factory.createIRI(PREFIX + "millisBetween");
    }
}

