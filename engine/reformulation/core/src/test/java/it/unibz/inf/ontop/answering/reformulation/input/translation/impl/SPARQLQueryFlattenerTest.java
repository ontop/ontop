package it.unibz.inf.ontop.answering.reformulation.input.translation.impl;

import it.unibz.inf.ontop.datalog.InternalSparqlQuery;
import it.unibz.inf.ontop.exception.OntopInvalidInputQueryException;
import it.unibz.inf.ontop.exception.OntopUnsupportedInputQueryException;
import it.unibz.inf.ontop.utils.UriTemplateMatcher;
import org.junit.Test;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.QueryParser;
import org.eclipse.rdf4j.query.parser.QueryParserUtil;

import java.util.stream.Stream;

import static it.unibz.inf.ontop.utils.ReformulationTestingTools.*;


/**
 * Created by Roman Kontchakov on 12/03/2016.
 */
public class SPARQLQueryFlattenerTest {

    @Test
    public void testBind0() throws MalformedQueryException, OntopUnsupportedInputQueryException, OntopInvalidInputQueryException {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title (UCASE(?title) AS ?v) (CONCAT(?title, \" \", ?v) AS ?w) WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "}";

        QueryParser parser = QueryParserUtil.createParser(QueryLanguage.SPARQL);
        ParsedQuery pq = parser.parseQuery(queryBind, null);

        SparqlAlgebraToDatalogTranslator translator = new SparqlAlgebraToDatalogTranslator(
                UriTemplateMatcher.create(Stream.of(), TERM_FACTORY), null, ATOM_FACTORY, TERM_FACTORY,
                TYPE_FACTORY, DATALOG_FACTORY, IMMUTABILITY_TOOLS, RDF_FACTORY);

        InternalSparqlQuery program = translator.translate(pq);
        System.out.println(program);
    }

    @Test
    public void testBind() throws MalformedQueryException, OntopUnsupportedInputQueryException, OntopInvalidInputQueryException {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (UCASE(?title) AS ?v)\n"
                + "   BIND (CONCAT(?title, \" \", ?v) AS ?w)\n"
                + "}";

        QueryParser parser = QueryParserUtil.createParser(QueryLanguage.SPARQL);
        ParsedQuery pq = parser.parseQuery(queryBind, null);

        SparqlAlgebraToDatalogTranslator translator = new SparqlAlgebraToDatalogTranslator(
                UriTemplateMatcher.create(Stream.of(), TERM_FACTORY), null, ATOM_FACTORY, TERM_FACTORY,
                TYPE_FACTORY, DATALOG_FACTORY, IMMUTABILITY_TOOLS, RDF_FACTORY);
        InternalSparqlQuery program = translator.translate(pq);
        System.out.println(program);
    }


    @Test
    public void testUnion() throws MalformedQueryException, OntopUnsupportedInputQueryException, OntopInvalidInputQueryException {

        String query6 = "PREFIX : <http://www.example.org/test#> "
                + "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
                + "SELECT * "
                + "WHERE {"
                + " ?p a :Person .  { ?p :name ?name .  "
                + " ?p a [ a owl:Restriction; owl:onProperty :q; owl:someValuesFrom owl:Thing ] . } "
                + " OPTIONAL {"
                + "   ?p :nick1 ?nick1 "
                + "   OPTIONAL {"
                + "     {?p :nick2 ?nick2 } UNION {?p :nick22 ?nick22} } } }";

        QueryParser parser = QueryParserUtil.createParser(QueryLanguage.SPARQL);
        ParsedQuery pq = parser.parseQuery(query6, null);

        SparqlAlgebraToDatalogTranslator translator = new SparqlAlgebraToDatalogTranslator(
                UriTemplateMatcher.create(Stream.of(), TERM_FACTORY), null, ATOM_FACTORY, TERM_FACTORY,
                TYPE_FACTORY, DATALOG_FACTORY, IMMUTABILITY_TOOLS, RDF_FACTORY);
        InternalSparqlQuery program = translator.translate(pq);
        System.out.println(program);
    }

    @Test
    public void testUnionJoin() throws MalformedQueryException, OntopUnsupportedInputQueryException, OntopInvalidInputQueryException {

        String query6 = "PREFIX : <http://www.example.org/test#> "
                + "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
                + "SELECT * "
                + "WHERE {"
                + " { ?x :q ?z } UNION { ?x :p ?y. ?u :q ?v FILTER (?x > 3) FILTER (?v = 2) }  "
                + " { ?y :q ?v } UNION { ?z :p ?w } }";

        QueryParser parser = QueryParserUtil.createParser(QueryLanguage.SPARQL);
        ParsedQuery pq = parser.parseQuery(query6, null);

        SparqlAlgebraToDatalogTranslator translator = new SparqlAlgebraToDatalogTranslator(
                UriTemplateMatcher.create(Stream.of(), TERM_FACTORY), null, ATOM_FACTORY, TERM_FACTORY,
                TYPE_FACTORY, DATALOG_FACTORY, IMMUTABILITY_TOOLS, RDF_FACTORY);
        InternalSparqlQuery program = translator.translate(pq);
        System.out.println(program);
    }

}
