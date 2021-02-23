package it.unibz.inf.ontop.model.vocabulary;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.simple.SimpleRDF;

public class UOM {
    public static final String PREFIX = "http://www.opengis.net/def/uom/OGC/1.0/";
    public static final IRI METRE;
    public static final IRI DEGREE;
    public static final IRI RADIAN;

    static {
        org.apache.commons.rdf.api.RDF factory = new SimpleRDF();
        METRE = factory.createIRI(UOM.PREFIX + "metre");
        DEGREE = factory.createIRI(UOM.PREFIX + "degree");
        RADIAN = factory.createIRI(UOM.PREFIX + "radian");
    }

}
