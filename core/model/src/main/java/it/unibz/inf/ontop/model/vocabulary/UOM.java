package it.unibz.inf.ontop.model.vocabulary;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.simple.SimpleRDF;

public class UOM {
    public static final String PREFIX = "http://www.opengis.net/def/uom/OGC/1.0/";
    public static final IRI METRE;
    public static final IRI DEGREE;
    public static final IRI RADIAN;

    public static final String METRE_STRING = UOM.PREFIX + "metre";
    public static final String DEGREE_STRING = UOM.PREFIX + "degree";
    public static final String RADIAN_STRING = UOM.PREFIX + "radian";

    static {
        org.apache.commons.rdf.api.RDF factory = new SimpleRDF();
        METRE = factory.createIRI(METRE_STRING);
        DEGREE = factory.createIRI(DEGREE_STRING);
        RADIAN = factory.createIRI(RADIAN_STRING);
    }

}
