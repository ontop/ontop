package it.unibz.krdb.obda.reformulation.tests;

import it.unibz.inf.ontop.model.UriTemplateMatcher;
import it.unibz.inf.ontop.owlrefplatform.core.abox.SemanticIndexURIMap;
import it.unibz.inf.ontop.owlrefplatform.core.translator.SparqlAlgebraToDatalogTranslator;
import it.unibz.inf.ontop.owlrefplatform.core.translator.SparqlQuery;
import org.junit.Test;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.QueryParser;
import org.openrdf.query.parser.QueryParserUtil;


/**
 * Created by Roman Kontchakov on 12/03/2016.
 */
public class SPARQLQueryFlattenerTest {

    @Test
    public void testBind0() throws MalformedQueryException {

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
                new UriTemplateMatcher(), new SemanticIndexURIMap());
        SparqlQuery program = translator.translate(pq);
        System.out.println(program);
    }

    @Test
    public void testBind() throws MalformedQueryException {

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
                new UriTemplateMatcher(), new SemanticIndexURIMap());
        SparqlQuery program = translator.translate(pq);
        System.out.println(program);
    }


    @Test
    public void testUnion() throws MalformedQueryException {

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
                new UriTemplateMatcher(), new SemanticIndexURIMap());
        SparqlQuery program = translator.translate(pq);
        System.out.println(program);
    }

    @Test
    public void testUnionJoin() throws MalformedQueryException {

        String query6 = "PREFIX : <http://www.example.org/test#> "
                + "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
                + "SELECT * "
                + "WHERE {"
                + " { ?x :q ?z } UNION { ?x :p ?y. ?u :q ?v FILTER (?x > 3) FILTER (?v = 2) }  "
                + " { ?y :q ?v } UNION { ?z :p ?w } }";

        QueryParser parser = QueryParserUtil.createParser(QueryLanguage.SPARQL);
        ParsedQuery pq = parser.parseQuery(query6, null);

        SparqlAlgebraToDatalogTranslator translator = new SparqlAlgebraToDatalogTranslator(
                new UriTemplateMatcher(), new SemanticIndexURIMap());
        SparqlQuery program = translator.translate(pq);
        System.out.println(program);
    }

}
