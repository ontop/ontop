package it.unibz.inf.ontop.model.vocabulary;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.simple.SimpleRDF;

public class OntopInternal {

    private static final String PREFIX = "urn:it:unibz:inf:ontop:internal:";

    public static final IRI NUMERIC;

    /**
     * TODO: remove it!
     */
    public static final IRI UNSUPPORTED;


    static {
        RDF rdfFactory = new SimpleRDF();

        NUMERIC = rdfFactory.createIRI(PREFIX + "numeric");
        UNSUPPORTED = rdfFactory.createIRI(PREFIX + "unsupported");
    }
}
