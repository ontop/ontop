package it.unibz.inf.ontop.model.vocabulary;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.simple.SimpleRDF;

public class XPathFunction {

    public static final String PREFIX = "http://www.w3.org/2005/xpath-functions#";

    public final static IRI CONCAT;
    public final static IRI UPPER_CASE;
    public final static IRI LOWER_CASE;
    public final static IRI STARTS_WITH;
    public static final IRI ENDS_WITH;

    static {
        org.apache.commons.rdf.api.RDF factory = new SimpleRDF();
        CONCAT = factory.createIRI(PREFIX + "concat");
        UPPER_CASE = factory.createIRI(PREFIX + "upper-case");
        LOWER_CASE = factory.createIRI(PREFIX + "lower-case");
        STARTS_WITH = factory.createIRI(PREFIX + "starts-with");
        ENDS_WITH = factory.createIRI(PREFIX + "ends-with");

    }
}
