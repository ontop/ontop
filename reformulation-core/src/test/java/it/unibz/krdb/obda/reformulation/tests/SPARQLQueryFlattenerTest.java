package it.unibz.krdb.obda.reformulation.tests;

import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.owlrefplatform.core.abox.SemanticIndexURIMap;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.UriTemplateMatcher;
import it.unibz.krdb.obda.owlrefplatform.core.translator.SparqlAlgebraToDatalogTranslator;
import it.unibz.krdb.obda.owlrefplatform.core.translator.SPARQLQueryFlattener;
import it.unibz.krdb.obda.owlrefplatform.core.translator.SparqlQuery;
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
    public void testUnion() throws MalformedQueryException {

        String query6 = "PREFIX : <http://www.example.org/test#> "
                + "SELECT * "
                + "WHERE {"
                + " ?p a :Person . ?p :name ?name ."
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
}
