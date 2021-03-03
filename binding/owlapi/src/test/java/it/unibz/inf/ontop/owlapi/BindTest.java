package it.unibz.inf.ontop.owlapi;

/*
 * #%L
 * ontop-quest-owlapi
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.OntopReformulationException;
import it.unibz.inf.ontop.owlapi.exception.OntopOWLException;
import org.junit.*;

/***
 * A simple test that check if the system is able to handle Mappings for
 * classes/roles and attributes even if there are no URI templates. i.e., the
 * database stores URI's directly.
 * 
 * We are going to create an H2 DB, the .sql file is fixed. We will map directly
 * there and then query on top.
 *
 * Class to test if bind in SPARQL is working properly.
 *
 * It uses the test from http://www.w3.org/TR/sparql11-query/#bind
 */

public class BindTest extends AbstractOWLAPITest {

    @BeforeClass
    public static void setUp() throws Exception {
        initOBDA("/test/bind/sparqlBind-create-h2.sql",
                "/test/bind/sparqlBind.obda",
                "/test/bind/sparqlBind.owl");
	}

    @AfterClass
    public static void tearDown() throws Exception {
        release();
    }

    @Test
    public void testSelectSimple() throws Exception {
        String querySelect = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n" +
                "PREFIX  ns:  <http://example.org/ns#>\n" +
                "SELECT  ?title (17.25 AS ?price)\n" +
                "{ ?x ns:price ?p .\n" +
                "  ?x dc:title ?title . \n" +
                "  ?x ns:discount ?discount . \n" +
                "}";

        checkReturnedValues(querySelect, "price", ImmutableList.of(
                "\"17.25\"^^xsd:decimal",
                "\"17.25\"^^xsd:decimal"));
    }

    @Test
    public void testSelectComplex() throws Exception {
        String querySelect = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n" +
                "PREFIX  ns:  <http://example.org/ns#>\n" +
                "SELECT  ?title (?p*(1-?discount) AS ?price)\n" +
                "{ ?x ns:price ?p .\n" +
                "  ?x dc:title ?title . \n" +
                "  ?x ns:discount ?discount . \n" +
                "}";

        checkReturnedValues(querySelect, "price", ImmutableList.of(
                "\"33.6\"^^xsd:decimal",
                "\"17.25\"^^xsd:decimal"));
    }

    @Test
    public void testBind() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount\n"
                + "   BIND (?p*(1-?discount) AS ?w)\n"
                + "   FILTER(?w < 20)\n"
                + "   ?x dc:title ?title .\n"
                + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"17.25\"^^xsd:decimal"));
    }

    @Test
    public void testBindWithStrlen() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?x ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount\n"
                + "   BIND (?p*(1-?discount) AS ?w)\n"
                + "   FILTER(?w < 20 && strlen(?title) > 0) \n"
                + "   ?x dc:title ?title .\n"
                + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"17.25\"^^xsd:decimal"));
    }

    @Test
    public void testDoubleBind() throws Exception{
        
		// double bind
        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n" +
                "SELECT  ?title  ?w WHERE \n" +
                "{  ?x ns:discount ?discount .\n" +
                "   ?x dc:title ?title .\n" +
                "   ?x ns:price ?p .\n" +
                "   BIND (?p AS ?fullPrice) \n" +
                "   BIND (?fullPrice  - ?discount AS ?w) \n" +
                "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"41.8\"^^xsd:decimal",
                "\"22.75\"^^xsd:decimal"));
    }


    @Test
    public void testBindWithSelect() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n" +
                "SELECT  ?title  (?fullPrice * ?discount AS ?w) WHERE \n" +
                "{  ?x ns:discount ?discount .\n" +
                "   ?x dc:title ?title .\n" +
                "  ?x ns:price ?p .\n" +
                "   BIND (?p AS ?fullPrice) \n" +
                "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"8.4\"^^xsd:decimal",
                "\"5.75\"^^xsd:decimal"));
    }

    @Test
    public void testSelect2()  throws Exception {
        String querySelect = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n" +
                "PREFIX  ns:  <http://example.org/ns#>\n" +
                "SELECT  ?title (?p * (1-?discount) AS ?customerPrice)\n" +
                "{ ?x ns:price ?p .\n" +
                "   ?x dc:title ?title . \n" +
                "   ?x ns:discount ?discount \n" +
                "}";

        checkReturnedValues(querySelect, "customerPrice", ImmutableList.of(
                "\"33.6\"^^xsd:decimal",
                "\"17.25\"^^xsd:decimal"));
    }

    @Test//(expected = OntopOWLException.class)
    public void testFailingSelect1()  throws Exception {

        String querySelect = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n" +
                "PREFIX  ns:  <http://example.org/ns#>\n" +
                "SELECT  ?title (?p -?discount AS ?price)\n" +
                "{ ?x ns:price ?p .\n" +
                "  ?x dc:title ?title . \n" +
                "  ?x ns:discount ?discount . \n" +
                "  FILTER(?price < 20) \n" +
                "}";
        checkReturnedValues(querySelect, "price", ImmutableList.of());
    }

    @Test
    public void testFailingBind() throws Exception {

        String queryBind1 = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n" +
                "SELECT  ?title  (?fullPrice * (1- ?discount) AS ?customerPrice) WHERE \n" +
                "{  ?x ns:discount ?discount .\n" +
                "   ?x ns:price ?p .\n" +
                "   ?x dc:title ?title .\n" +
                "   BIND (?p AS ?fullPrice) \n" +
                "}";

        checkReturnedValues(queryBind1, "customerPrice", ImmutableList.of(
                "\"33.6\"^^xsd:decimal",
                "\"17.25\"^^xsd:decimal"));
    }

    @Test
    public void testBindWithConcat() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (CONCAT(?title, \" title\") AS ?w)\n"
                + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"SPARQL Tutorial title\"^^xsd:string",
                "\"The Semantic Web title\"^^xsd:string"));
    }

    @Test
    public void testBindWithUCase() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (UCASE(?title) AS ?w)\n"
                + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"SPARQL TUTORIAL\"@en",
                "\"THE SEMANTIC WEB\"@en"));
    }

    @Test
    public void testBindWithConcatStrLen() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (STRLEN(CONCAT(?title, \" \")) AS ?v)\n"
                + "   BIND (CONCAT(?title, \" \", str(?v)) AS ?w)\n"
                + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"SPARQL Tutorial 16\"^^xsd:string",
                "\"The Semantic Web 17\"^^xsd:string"));
    }

    @Test
    public void testBindWithConcatLanguage() throws Exception {

		String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (CONCAT(?title, ?title) AS ?w)\n"
                + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"SPARQL TutorialSPARQL Tutorial\"@en",
                "\"The Semantic WebThe Semantic Web\"@en"));
    }

    @Test
    public void testBindConcatUnderUnion() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?w WHERE \n"
                + "{"
                + "   { BIND (\"plop\" AS ?w) }\n"
                + "   UNION\n"
                + "   { \n"
                + "     ?x ns:price ?p .\n" +
                "       ?x ns:discount ?discount .\n"
                + "    ?x dc:title ?title .\n"
                + "    BIND (CONCAT(?title, ?title) AS ?w) }\n"
                + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"plop\"^^xsd:string",
                "\"SPARQL TutorialSPARQL Tutorial\"@en",
                "\"The Semantic WebThe Semantic Web\"@en"));
    }

    @Test
    public void testBindConcatAboveUnion() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?w WHERE \n"
                + "{"
                + "   ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   { BIND (\"plop\" AS ?s) }\n"
                + "   UNION\n"
                + "   { \n"
                + "    ?y dc:title ?s .\n"
                + "   }\n"
                + "   BIND (CONCAT(?title, ?s) AS ?w)"
                + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"SPARQL Tutorialplop\"^^xsd:string",
                "\"SPARQL TutorialSPARQL Tutorial\"@en",
                "\"SPARQL TutorialThe Semantic Web\"@en",
                "\"The Semantic Webplop\"^^xsd:string",
                "\"The Semantic WebSPARQL Tutorial\"@en",
                "\"The Semantic WebThe Semantic Web\"@en"));
    }

    @Test
    public void testBindWithConcatLanguageInDatabase() throws Exception {

		String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?x dc:description ?desc .\n"
                + "   BIND (CONCAT(?desc, ?title) AS ?w)\n"
                + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"goodSPARQL Tutorial\"@en",
                "\"badThe Semantic Web\"@en"));
    }


    //CAST function not supported
    //The builtin function http://www.w3.org/2001/XMLSchema#string is not supported yet!
    @Test(expected = OntopReformulationException.class)
    public void testBindWithCast() throws Throwable {

		String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n"
                + "SELECT  ?title ?price WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount\n"
                + "   BIND (CONCAT(xsd:string(?p*(1-?discount)), xsd:string(?p)) AS ?price)\n"
                + "   ?x dc:title ?title .\n"
                + "}";

        try {
            checkReturnedValues(queryBind, "w", ImmutableList.of());
        }
        catch (OntopOWLException e) {
            throw e.getCause();
        }
    }

    @Test
    public void testBindLangTag() throws Exception {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n" +
                "SELECT ?w\n" +
                "WHERE {\n" +
                "  ?y dc:title ?title .\n" +
                "  BIND( lang(?title) AS ?w ) .\n" +
                "}";

        checkReturnedValues(query, "w", ImmutableList.of(
                "\"en\"^^xsd:string",
                "\"en\"^^xsd:string"));
    }

    @Test
    public void testBindLangTag2() throws Exception {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n" +
                "SELECT ?w\n" +
                "WHERE {\n" +
                "  ?y dc:title ?title .\n" +
                "  BIND (str(?title) AS ?s)\n" +
                "  BIND( lang(?s) AS ?w ) .\n" +
                "}";

        checkReturnedValues(query, "w", ImmutableList.of(
                "\"\"^^xsd:string",
                "\"\"^^xsd:string"));
    }
}
