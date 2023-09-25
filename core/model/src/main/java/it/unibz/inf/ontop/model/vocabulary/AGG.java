package it.unibz.inf.ontop.model.vocabulary;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.simple.SimpleRDF;

public class AGG {
    public static final String PREFIX = "http://jena.apache.org/ARQ/function/aggregate#";

    public static final IRI STDEV;
    public static final IRI STDEV_SAMP;
    public static final IRI STDEV_POP;
    public static final IRI VARIANCE;
    public static final IRI VAR_SAMP;
    public static final IRI VAR_POP;

    static {
        org.apache.commons.rdf.api.RDF factory = new SimpleRDF();
        STDEV = factory.createIRI(AGG.PREFIX + "stdev");
        STDEV_SAMP = factory.createIRI(AGG.PREFIX + "stdev_samp");
        STDEV_POP = factory.createIRI(AGG.PREFIX + "stdev_pop");
        VARIANCE = factory.createIRI(AGG.PREFIX + "variance");
        VAR_SAMP = factory.createIRI(AGG.PREFIX + "var_samp");
        VAR_POP = factory.createIRI(AGG.PREFIX + "var_pop");
    }
}
