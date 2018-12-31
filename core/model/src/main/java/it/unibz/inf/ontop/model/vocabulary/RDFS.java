package it.unibz.inf.ontop.model.vocabulary;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.simple.SimpleRDF;

public class RDFS {

    public static final String PREFIX = "http://www.w3.org/2000/01/rdf-schema#";

    public final static IRI RESOURCE;

    public final static IRI LITERAL;

    public final static IRI CLASS;

    public final static IRI SUBCLASSOF;

    public final static IRI SUBPROPERTYOF;

    public final static IRI DOMAIN;

    public final static IRI RANGE;

    public final static IRI COMMENT;

    public final static IRI LABEL;

    public final static IRI DATATYPE;

    public final static IRI CONTAINER;

    public final static IRI MEMBER;

    public final static IRI ISDEFINEDBY;

    public final static IRI SEEALSO;

    public final static IRI CONTAINERMEMBERSHIPPROPERTY;

    static {
        org.apache.commons.rdf.api.RDF factory = new SimpleRDF();
        RESOURCE = factory.createIRI(PREFIX +  "Resource");
        LITERAL = factory.createIRI(PREFIX +  "Literal");
        CLASS = factory.createIRI(PREFIX +  "Class");
        SUBCLASSOF = factory.createIRI(PREFIX +  "subClassOf");
        SUBPROPERTYOF = factory.createIRI(PREFIX +  "subPropertyOf");
        DOMAIN = factory.createIRI(PREFIX +  "domain");
        RANGE = factory.createIRI(PREFIX +  "range");
        COMMENT = factory.createIRI(PREFIX +  "comment");
        LABEL = factory.createIRI(PREFIX +  "label");
        DATATYPE = factory.createIRI(PREFIX +  "Datatype");
        CONTAINER = factory.createIRI(PREFIX +  "Container");
        MEMBER = factory.createIRI(PREFIX +  "member");
        ISDEFINEDBY = factory.createIRI(PREFIX +  "isDefinedBy");
        SEEALSO = factory.createIRI(PREFIX +  "seeAlso");
        CONTAINERMEMBERSHIPPROPERTY = factory.createIRI(PREFIX +  "ContainerMembershipProperty");
    }
}
