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

    public static final IRI CLASS = RDF_FACTORY.createIRI(PREFIX + "Class");
    public static final IRI DISJOINT_WITH = RDF_FACTORY.createIRI(PREFIX + "disjointWith");

    public static final IRI RESTRICTION = RDF_FACTORY.createIRI(PREFIX + "Restriction");
    public static final IRI ON_PROPERTY = RDF_FACTORY.createIRI(PREFIX + "onProperty");
    public static final IRI SOME_VALUES_FROM = RDF_FACTORY.createIRI(PREFIX + "someValuesFrom");

    public static final IRI OBJECT_PROPERTY = RDF_FACTORY.createIRI(PREFIX + "ObjectProperty");
    public static final IRI DATATYPE_PROPERTY = RDF_FACTORY.createIRI(PREFIX + "DatatypeProperty");
    public static final IRI INVERSE_OF = RDF_FACTORY.createIRI(PREFIX + "inverseOf");
}
