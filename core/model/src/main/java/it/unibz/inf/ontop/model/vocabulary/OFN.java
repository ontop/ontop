package it.unibz.inf.ontop.model.vocabulary;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.simple.SimpleRDF;

public class OFN {

    // Adapted from https://graphdb.ontotext.com/free/devhub/time-functions.html
    public static final String PREFIX = "http://www.ontotext.com/sparql/functions/";

    /*
     * Time extension - duration arithmetic
     */
    public static final IRI WEEKS_BETWEEN;

    public static final IRI DAYS_BETWEEN;

    public static final IRI HOURS_BETWEEN;

    public static final IRI MINUTES_BETWEEN;

    public static final IRI SECONDS_BETWEEN;

    public static final IRI MILLIS_BETWEEN;

    static {
        org.apache.commons.rdf.api.RDF factory = new SimpleRDF();

        /*
         * Time extension - duration arithmetic
         */
        WEEKS_BETWEEN = factory.createIRI(PREFIX + "weeksBetween");

        DAYS_BETWEEN = factory.createIRI(PREFIX + "daysBetween");

        HOURS_BETWEEN = factory.createIRI(PREFIX + "hoursBetween");

        MINUTES_BETWEEN = factory.createIRI(PREFIX + "minutesBetween");

        SECONDS_BETWEEN = factory.createIRI(PREFIX + "secondsBetween");

        MILLIS_BETWEEN = factory.createIRI(PREFIX + "millisBetween");
    }
}

