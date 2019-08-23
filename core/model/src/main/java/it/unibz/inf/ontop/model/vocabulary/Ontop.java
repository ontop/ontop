package it.unibz.inf.ontop.model.vocabulary;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.simple.SimpleRDF;

public class Ontop {

    private static final RDF rdfFactory = new SimpleRDF();

    public static final String PREFIX = "https://w3id.org/obda/vocabulary#";
    public static final IRI CANONICAL_IRI = rdfFactory.createIRI(PREFIX + "isCanonicalIRIOf");
}
