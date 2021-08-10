package it.unibz.inf.ontop.spec.mapping.parser.impl;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.rdf4j.RDF4J;

/**
 * Vocabulary definitions for Ontop's unofficial RDF-star extension to R2RML
 *
 * @author Lukas Sundqvist
 */
public class RDFStarVocabulary {

    public static final RDF4J rdf4j = new RDF4J();

    public static final IRI subject = rdf4j.createIRI("http://ontop-vkg.org/2021/rdfstar#subject");
    public static final IRI predicate = rdf4j.createIRI("http://ontop-vkg.org/2021/rdfstar#predicate");
    public static final IRI object = rdf4j.createIRI("http://ontop-vkg.org/2021/rdfstar#object");

    public static final IRI rdfStarTermType = rdf4j.createIRI("http://ontop-vkg.org/2021/rdfstar#RDFStarTermType");
}
