package it.unibz.inf.ontop.model.vocabulary;


import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.simple.SimpleRDF;

public class OWL {

    private static final RDF RDF_FACTORY = new SimpleRDF();
    public static final String PREFIX = "http://www.w3.org/2002/07/owl#";

    public static final IRI REAL =  RDF_FACTORY.createIRI(PREFIX + "real");
    public static final IRI RATIONAL =  RDF_FACTORY.createIRI(PREFIX + "rational");

    public static final IRI THING = RDF_FACTORY.createIRI(PREFIX + "Thing");
    public static final IRI NOTHING  = RDF_FACTORY.createIRI(PREFIX + "Nothing");

    public static final IRI TOP_OBJECT_PROPERTY =  RDF_FACTORY.createIRI(PREFIX + "topObjectProperty");
    public static final IRI BOTTOM_OBJECT_PROPERTY = RDF_FACTORY.createIRI(PREFIX +"bottomObjectProperty");

    public static final IRI TOP_DATA_PROPERTY = RDF_FACTORY.createIRI(PREFIX + "topDataProperty");
    public static final IRI BOTTOM_DATA_PROPERTY  = RDF_FACTORY.createIRI(PREFIX + "bottomDataProperty");

    public static final IRI SAME_AS = RDF_FACTORY.createIRI(PREFIX + "sameAs");
}
