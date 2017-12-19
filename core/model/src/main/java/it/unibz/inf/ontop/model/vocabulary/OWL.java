package it.unibz.inf.ontop.model.vocabulary;


import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.simple.SimpleRDF;

public class OWL {

    private static final RDF RDF_FACTORY = new SimpleRDF();
    private static final String PREFIX = "http://www.w3.org/2002/07/owl#";

    public static final IRI REAL =  RDF_FACTORY.createIRI(PREFIX + "real");
    public static final IRI RATIONAL =  RDF_FACTORY.createIRI(PREFIX + "rational");
}
