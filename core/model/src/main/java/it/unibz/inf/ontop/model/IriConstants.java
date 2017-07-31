package it.unibz.inf.ontop.model;

public interface IriConstants {

    String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

    String SAME_AS = "http://www.w3.org/2002/07/owl#sameAs";

    String CANONICAL_IRI = "https://w3id.org/obda/vocabulary#isCanonicalIRIOf";

	/* Common namespaces and prefixes */

    String NS_XSD = "http://www.w3.org/2001/XMLSchema#";
    String NS_RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    String NS_RDFS = "http://www.w3.org/2000/01/rdf-schema#";
    String NS_OWL = "http://www.w3.org/2002/07/owl#";
    String NS_OBDA = "https://w3id.org/obda/vocabulary#";
    String PREFIX_XSD = "xsd:";
    String PREFIX_RDF = "rdf:";
    String PREFIX_RDFS = "rdfs:";
    String PREFIX_OWL = "owl:";
    String PREFIX_OBDA = "obda:";

    // TODO: to be removed
    String RDFS_LITERAL_URI = "http://www.w3.org/2000/01/rdf-schema#Literal";
}
