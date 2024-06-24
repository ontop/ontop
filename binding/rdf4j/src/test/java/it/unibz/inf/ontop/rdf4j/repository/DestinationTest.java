package it.unibz.inf.ontop.rdf4j.repository;

import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DestinationTest extends AbstractRDF4JTest {

    private static final String OBDA_FILE = "/destination/dest.obda"; //contiene i mapping
    private static final String SQL_SCRIPT = "/destination/schema.sql"; //sql script per generare le tabelle del database
    private static final String ONTOLOGY_FILE = "/destination/dest.owl"; //contiene l'ontologia
    private static final String PROPERTIES_FILE = "/destination/dest.properties"; //permette di specificare le chiavi di configurazioni. Guarda: https://ontop-vkg.org/guide/advanced/configuration.html

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE, ONTOLOGY_FILE, PROPERTIES_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testQuery() {
        int count = runQueryAndCount("PREFIX schema: <http://schema.org/>\n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX : <http://noi.example.org/ontology/odh#>\n" +
                "\n" +
                "SELECT ?h ?posLabel ?posColor\n" +
                "WHERE {\n" +
                "  ?h a schema:LodgingBusiness ;\n" +
                "     schema:name ?posLabel .\n" +
                "  #?h schema:containedInPlace/schema:name \"Bozen\"@de . # Uncomment for restricting to a municipality\n" +
                "  FILTER (lang(?posLabel) = 'de')\n" +
                "  \n" +
                "    # Colors\n" +
                "  OPTIONAL {\n" +
                "    ?h a schema:Campground .\n" +
                "    BIND(\"chlorophyll,0.5\" AS ?posColor) # Green\n" +
                "  }\n" +
                "    OPTIONAL {\n" +
                "    ?h a schema:BedAndBreakfast .\n" +
                "    BIND(\"viridis,0.1\" AS ?posColor) # Purple\n" +
                "  }\n" +
                "  OPTIONAL {\n" +
                "    ?h a schema:Hotel . \n" +
                "    BIND(\"jet,0.3\" AS ?posColor) # Light blue\n" +
                "  }\n" +
                "  OPTIONAL {\n" +
                "    ?h a schema:Hostel .\n" +
                "    BIND(\"jet,0.8\" AS ?posColor) # Red\n" +
                "  }\n" +
                "\n" +
                "}\n" +
                "LIMIT 500\n");
        assertEquals(1, count);
    }

    @Test
    public void testSubQueryOrderByNonProjectedVariable() {
        int count = runQueryAndCount("PREFIX schema: <http://schema.org/>\n" +
                "\n" +
                "SELECT * WHERE {\n" +
                "  { SELECT DISTINCT ?h ?nStr WHERE {\n" +
                "      ?h a schema:LodgingBusiness ;\n" +
                "         schema:name ?n .\n" +
                "      BIND(str(?n) AS ?nStr)\n" +
                "    }\n" +
                "    ORDER BY DESC(CONCAT(?nStr, ?nStr))\n" +
                "    LIMIT 2\n" +
                "  }\n" +
                "  ?h schema:name ?name\n" +
                "}");

        assertEquals(6, count);
    }

    @Test
    public void testSPO() {
        runQueryAndCount(
                "SELECT * WHERE {\n" +
                        "  ?s ?p ?o \n" +
                        "  VALUES ?p {\n" +
                        "<http://qudt.org/schema/qudt#conversionOffset>\n" +
                        "<http://www.linkedmodel.org/schema/vaem#namespace>\n" +
                        "  }" +
                        "}\n" +
                        "LIMIT 10");
    }

    /**
     * Reproducing https://github.com/ontop/ontop/issues/417 (re-opened issue)
     */
    @Test
    public void testDistinctSubQuery() {
        int count = runQueryAndCount("PREFIX schema: <http://schema.org/>\n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX : <http://noi.example.org/ontology/odh#>\n" +
                "\n" +
                "SELECT ?o\n" +
                "WHERE {\n" +
                "  ?h a schema:LodgingBusiness ;\n" +
                "     schema:name ?o .\n" +
                "  { SELECT DISTINCT ?h {\n" +
                "    ?h a schema:Campground ;\n" +
                "      schema:name ?o " +
                "  }}\n" +
                "}\n" +
                "LIMIT 1\n");
        assertEquals(1, count);
    }

    @Test
    public void testAllProperties() {
        String sparql = "SELECT DISTINCT ?p\n" +
                "WHERE {\n" +
                        "?s ?p ?o" +
                "}";

        int count = runQueryAndCount(sparql);
        assertEquals(92, count);

        String sql = reformulateIntoNativeQuery(sparql);
        assertEquals(113, StringUtils.countMatches(sql, "LIMIT 1"));
        assertTrue(StringUtils.countMatches(sql.toUpperCase(), "DISTINCT") <= 1);
    }

    @Test
    public void testAllPropertiesWithOrder() {
        String sparql = "SELECT DISTINCT ?p\n" +
                "WHERE {\n" +
                "?s ?p ?o" +
                "}\n" +
                "ORDER BY ?p";

        int count = runQueryAndCount(sparql);
        assertEquals(92, count);

        String sql = reformulateIntoNativeQuery(sparql);
        assertEquals(113, StringUtils.countMatches(sql, "LIMIT 1"));
        assertTrue(StringUtils.countMatches(sql.toUpperCase(), "DISTINCT") <= 1);
    }

    @Test
    public void testAllClasses() {
        String sparql = "SELECT DISTINCT ?c\n" +
                "WHERE {\n" +
                "?s a ?c" +
                "}";

        int count = runQueryAndCount(sparql);
        assertEquals(274, count);

        String sql = reformulateIntoNativeQuery(sparql);
        assertEquals(54, StringUtils.countMatches(sql, "LIMIT 1"));
        assertEquals(0, StringUtils.countMatches(sql.toUpperCase(), "DISTINCT"));
    }

    @Test
    public void testDataPropertyLodgingBusiness() {
        String sparql = "    SELECT DISTINCT ?pred {\n" +
                "        ?subject a     <http://schema.org/LodgingBusiness>;\n" +
                "                 ?pred ?object.\n" +
                "        FILTER(!isBlank(?object) && isLiteral(?object))\n" +
                "    }\n" +
                "    GROUP BY ?pred\n";

        int count = runQueryAndCount(sparql);
        // Due to null values
        assertEquals(1, count);
    }

    @Test
    public void testOneLJElimination() {
        String sparql = "PREFIX schema: <http://schema.org/>\n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX : <http://noi.example.org/ontology/odh#>\n" +
                "\n" +
                "SELECT *\n" +
                "WHERE {\n" +
                "  ?r a schema:Accommodation .\n" +
                "  OPTIONAL { \n" +
                "   ?r schema:containedInPlace ?h .\n" +
                "   OPTIONAL { \n" +
                "     ?h schema:name ?n . \n" +
                "     FILTER (lang(?n) = 'en')\n" +
                "    }\n" +
                "  }\n" +
                "}\n";

        String sql = reformulateIntoNativeQuery(sparql);
        // The non-simplifiable LJs are those between the accommodations and the lodging businesses (2 sources)
        // due to the absence of FKs
        assertEquals(2, StringUtils.countMatches(sql, "LEFT OUTER JOIN"));
    }

    @Test
    public void testMergeLJs() {
        String sparql = "PREFIX schema: <http://schema.org/>\n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX : <http://noi.example.org/ontology/odh#>\n" +
                "\n" +
                "SELECT *\n" +
                "WHERE {\n" +
                "  ?r a schema:Accommodation ;\n" +
                "       schema:containedInPlace ?h .\n" +
                "  OPTIONAL { \n" +
                "    ?h schema:name ?en . \n" +
                "    FILTER (lang(?en) = 'en')\n" +
                "  }\n" +
                "  OPTIONAL { \n" +
                "    ?h schema:name ?it . \n" +
                "    FILTER (lang(?it) = 'it')\n" +
                "  }\n" +
                "}\n";

        String sql = reformulateIntoNativeQuery(sparql);
        // The non-simplifiable LJs are those between the accommodations and the lodging businesses (2 sources)
        // due to the absence of FKs
        assertEquals(2, StringUtils.countMatches(sql, "LEFT OUTER JOIN"));
    }

    @Test
    public void testGroupByWithCount() {
        String sparql = "PREFIX schema: <http://schema.org/>\n" +
                "    SELECT DISTINCT ?subject {\n" +
                "        ?subject a     <http://schema.org/LodgingBusiness>;\n" +
                "                 ?pred ?object.\n" +
                "        FILTER(!isBlank(?object) && isLiteral(?object))" +
                "        { SELECT (COUNT(*) as ?cnt) { ?s a <http://schema.org/LodgingBusiness>; ?p ?o. FILTER(!isBlank(?o) && isLiteral(?o)). } GROUP BY ?p LIMIT 10 }" +
                "       \n" +
                "    }\n";

        int count = runQueryAndCount(sparql);
        // Due to null values
        assertEquals(1, count);
    }

    @Test
    public void testValuesOnIRI1() {
        String sparql = "PREFIX schema: <http://schema.org/>\n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX : <http://noi.example.org/ontology/odh#>\n" +
                "PREFIX data: <http://destination.example.org/data/>\n" +
                "\n" +
                "SELECT (?h AS ?v)\n" +
                "WHERE {\n" +
                "  ?h a schema:LodgingBusiness .\n" +
                "  OPTIONAL { \n" +
                "    ?h schema:name ?en . \n" +
                "    FILTER (lang(?en) = 'en')\n" +
                "  }\n" +
                "  OPTIONAL { \n" +
                "    ?h schema:name ?it . \n" +
                "    FILTER (lang(?it) = 'it')\n" +
                "  }\n" +
                "  VALUES ?h { \n" +
                "   <http://destination.example.org/data/source1/hospitality/aaa> \n " +
                "   <http://destination.example.org/data/source1/hospitality/bbb> \n" +
                "  } \n" +
                "}\n";

        int count = runQueryAndCount(sparql);
        assertEquals(1, count);

        String sql = reformulateIntoNativeQuery(sparql);
        assertEquals(0, StringUtils.countMatches(sql, "LEFT OUTER JOIN"));
        assertEquals(0, StringUtils.countMatches(sql, "REPLACE"));
    }

    @Test
    public void testValuesOnIRI2() {
        String sparql = "PREFIX schema: <http://schema.org/>\n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX : <http://destination.example.org/ontology/dest#>\n" +
                "\n" +
                "SELECT (?h AS ?v)\n" +
                "WHERE {\n" +
                "  ?h a :Municipality .\n" +
                "  OPTIONAL { \n" +
                "    ?h schema:name ?en . \n" +
                "    FILTER (lang(?en) = 'en')\n" +
                "  }\n" +
                "  OPTIONAL { \n" +
                "    ?h schema:name ?it . \n" +
                "    FILTER (lang(?it) = 'it')\n" +
                "  }\n" +
                "  VALUES ?h { \n" +
                "   <http://destination.example.org/data/municipality/ESTSTE> \n " +
                "   <http://destination.example.org/data/municipality/EEPST> \n" +
                "  } \n" +
                "}\n";

        int count = runQueryAndCount(sparql);
        assertEquals(1, count);

        String sql = reformulateIntoNativeQuery(sparql);
        assertEquals(0, StringUtils.countMatches(sql, "REPLACE"));
    }

    @Ignore("to be enabled when homomorphisms modulo IRI unification is implemented")
    @Test
    public void testObservableProperties() {
        String sparql = "PREFIX sosa: <http://www.w3.org/ns/sosa/>\n" +
                "SELECT * WHERE {\n" +
                " ?sub a sosa:ObservableProperty .\n" +
                "}\n";

        String sql = reformulateIntoNativeQuery(sparql);
        assertEquals(0, StringUtils.countMatches(sql, "DISTINCT"));
        assertEquals(0, StringUtils.countMatches(sql, "UNION"));
    }

    @Test
    public void andreaTest1(){
        String sparql = "SELECT DISTINCT ?p\n" +
                "WHERE\n" +
                "{\n" +
                "  ?s ?p ?o .\n" +
                "}\n" +
                "LIMIT 100";
        runQuery(sparql);
    }

    @Test
    public void andreaTest2(){
        String sparql = "PREFIX schema: <http://schema.org/>\n" +
                "SELECT ?hotel\n" +
                "WHERE {\n" +
                "  ?hotel a schema:Hotel .\n" +
                "}";
        runQuery(sparql);
    }

    @Test
    public void andreaTest3(){
        String sparql = "PREFIX : <http://schema.org/>\n" +
                "SELECT ?s\n" +
                "WHERE\n" +
                "{\n" +
                "  ?x :elevation ?n .\n" +
                "  ?x :latitude ?m .\n" +
                "  Bind((?n + ?m) AS ?s)\n" +
                "  FILTER(bound(?s))\n" +
                "}";
        runQuery(sparql);
    }
    @Test
    public void andreaTest4(){
        String sparql = "PREFIX schema: <http://schema.org/>\n" +
                "PREFIX : <http://destination.example.org/ontology/dest#>\n" +
                "SELECT ?name\n" +
                "WHERE\n" +
                "{\n" +
                "\t?mun a :Municipality .\n" +
                "\t?mun schema:name ?name .\n" +
                "}\n";
        runQuery(sparql);
    }

    @Test
    public void andreaTest5(){
        String sparql = "PREFIX : <http://destination.example.org/ontology/dest#>\n" +
                "PREFIX schema: <http://schema.org/>\n" +
                "SELECT ?s ?o\n" +
                "WHERE\n" +
                "{\n" +
                "  ?s a :Municipality.\n" +
                "  ?s schema:geo ?o .\n" +
                "}\n" +
                "LIMIT 100";
        runQuery(sparql);
    }

    @Test
    public void andreaTest6(){
        String sparql = "PREFIX schema: <http://schema.org/>\n" +
                "PREFIX : <http://destination.example.org/ontology/dest#>\n" +
                "SELECT ?municipality ?name\n" +
                "WHERE\n" +
                "{\n" +
                "\t?municipality a :Municipality .\n" +
                "\t?municipality schema:name ?name .\n" +
                "    FILTER (lang(?name) = \"it\")\n" +
                "}\n";
        runQuery(sparql);
    }

    @Test
    public void andreaTest7(){
        String sparql = "PREFIX : <http://destination.example.org/ontology/dest#>\n" +
                "PREFIX schema: <http://schema.org/>\n" +
                "SELECT ?s ?o ?name\n" +
                "WHERE\n" +
                "{\n" +
                "  ?s a :Municipality.\n" +
                "  ?s schema:geo ?o .\n" +
                "  ?s schema:name ?name .\n" +
                "}\n" +
                "LIMIT 100";
        runQuery(sparql);
    }

    @Test
    public void andreaTest8(){
        String sparql = "PREFIX : <http://destination.example.org/ontology/dest#>\n" +
                "PREFIX schema: <http://schema.org/>\n" +
                "SELECT ?s ?o ?name\n" +
                "WHERE\n" +
                "{\n" +
                "  ?s a :Municipality.\n" +
                "  ?s schema:geo ?o .\n" +
                "  OPTIONAL{ ?s schema:name ?name . }\n" +
                "  FILTER (lang(?name) = \"it\")\n" +
                "}\n" +
                "LIMIT 100";
        runQuery(sparql);
    }

    @Test
    public void andreaTest9(){
        String sparql = "SELECT ?o {\n" +
                "  <http://destination.example.org/data/weather/observation/202268> ?p ?o\n" +
                "    }\n" +
                "    LIMIT 500";
        runQuery(sparql);
    }

    @Test
    public void andreaTest10(){
        String sparql = "SELECT ?subject {\n" +
                "  ?subject ?p <http://destination.example.org/data/weather/observation/202268> \n" +
                "}\n" +
                "LIMIT 500";
        runQuery(sparql);
    }

    @Test
    public void andreaTest11(){
        String sparql = "SELECT DISTINCT ?subject ?class {\n" +
                "          ?subject a ?class .\n" +
                "          { ?subject ?p <http://destination.example.org/data/source1/hospitality/60E1EE8CE9647B98CF711E8B78F09955> }\n" +
                "          UNION\n" +
                "          { <http://destination.example.org/data/source1/hospitality/60E1EE8CE9647B98CF711E8B78F09955> ?p ?subject }\n" +
                "        }";
        runQuery(sparql);
    }

    @Test
    public void andreaTest12(){
        String sparql = "SELECT DISTINCT ?subject ?class {\n" +
                "          ?subject a ?class .\n" +
                "        { ?subject ?p <http://destination.example.org/data/weather/observation/202268> }\n" +
                "                UNION\n" +
                "            { <http://destination.example.org/data/weather/observation/202268> ?p ?subject }\n" +
                "            }\n" +
                "            LIMIT 500";
        runQuery(sparql);
    }

    @Test
    public void andreaTest13(){
        String sparql = "PREFIX schema: <http://schema.org/>\n" +
                "\n" +
                "    SELECT DISTINCT ?c WHERE {\n" +
                "  ?c a schema:Thing .\n" +
                "                ?s a ?c .\n" +
                "    }\n" +
                "    LIMIT 10";
        int count = runQueryAndCount(sparql);
        assertEquals(10, count);
    }

    @Test
    public void andreaTest14(){
        String sparql = "PREFIX schema: <http://schema.org/>\n" +
                "SELECT ?name ?email\n" +
                "WHERE {\n" +
                "  ?hotel a schema:Hotel .\n" +
                "  ?hotel schema:name ?name .\n" +
                "  ?hotel schema:email ?email .\n" +
                "  VALUES (?name ?email) {\n" +
                "    (\"Residence Bozen\"@it  \"residencbozen@gmail.com\")\n" +
                "    (\"Residence Bozen\"@en  \"residencbozen@gmail.com\")\n" +
                "  }\n" +
                "}";
        runQuery(sparql);
    }

    @Test
    public void andreaTest15(){
        String sparql = "SELECT ?class (COUNT(?subject) AS ?count) {\n" +
                "  ?subject a ?class .\n" +
                "  { ?subject ?p <http://destination.example.org/data/weather/observation/202268> }\n" +
                "  UNION\n" +
                "  { <http://destination.example.org/data/weather/observation/202268> ?p ?subject }\n" +
                "}\n" +
                "GROUP BY ?class\n" +
                "LIMIT 500\n";
        runQuery(sparql);
    }

    @Test
    public void andreaTest16(){
        String sparql = "SELECT * WHERE {\n" +
                "  <http://destination.example.org/data/source1/hospitality/60E1EE8CE9647B98CF711E8B78F09955> ?pred ?obj .\n" +
                "    }\n" +
                "    LIMIT 10";
        runQuery(sparql);
    }

    @Test
    public void andreaTest17(){
        String sparql = "PREFIX schema: <http://schema.org/>\n" +
                "SELECT ?hotel ?o\n" +
                "WHERE {\n" +
                "  ?hotel a schema:Hotel .\n" +
                "  ?hotel ?p ?o .\n" +
                "}\n";
        runQuery(sparql);
    }

    @Test
    public void andreaTest18(){
        String sparql = "PREFIX schema: <http://schema.org/>\n" +
                "PREFIX : <http://destination.example.org/ontology/dest#>\n" +
                "SELECT ?hotel ?p1 ?o1 ?mun ?p2 ?o2\n" +
                "WHERE {\n" +
                "  ?mun a :Municipality .\n" +
                "  ?hotel a schema:Hotel .\n" +
                "  ?hotel ?p1 ?o1 .\n" +
                "  ?mun ?p2 ?o2 .\n" +
                "}\n" +
                "LIMIT 10\n";
        runQuery(sparql);
    }

    @Test
    public void andreaTest19(){
        String sparql = "PREFIX : <http://destination.example.org/ontology/dest#>\n" +
                "PREFIX schema: <http://schema.org/>\n" +
                "SELECT ?s ?p ?o\n" +
                "WHERE\n" +
                "{\n" +
                "  ?s a :Municipality.\n" +
                "  ?s ?p ?o\n" +
                "}\n" +
                "LIMIT 100";
        runQuery(sparql);
    }

    @Test
    public void andreaTest20(){
        String sparql = "PREFIX : <http://destination.example.org/ontology/dest#>\n" +
                "PREFIX schema: <http://schema.org/>\n" +
                "SELECT ?s ?p ?o\n" +
                "WHERE\n" +
                "{\n" +
                "  ?s ?p ?o .\n" +
                "  ?s a :Municipality .\n" +
                "}\n" +
                "LIMIT 100";
        runQuery(sparql);
    }

    @Test
    public void andreaTest21(){
        String sparql = "PREFIX schema: <http://schema.org/>\n" +
                "PREFIX : <http://destination.example.org/ontology/dest#>\n" +
                "SELECT ?s ?p1 ?o1 ?p2\n" +
                "WHERE {\n" +
                "  {\n" +
                "    ?s a :Municipality .\n" +
                "    ?s ?p1 ?o1 .\n" +
                "  }\n" +
                "  UNION\n" +
                "  {\n" +
                "    ?s a schema:Hotel .\n" +
                "    ?s ?p2 ?o1 .\n" +
                "  }\n" +
                "}\n";
        runQuery(sparql);
    }

    @Test
    public void andreaTest22(){
        String sparql = "PREFIX schema: <http://schema.org/>\n" +
                "PREFIX : <http://destination.example.org/ontology/dest#>\n" +
                "\n" +
                "SELECT ?s ?p1 ?o1 ?p2\n" +
                "WHERE {\n" +
                "  {\n" +
                "    ?s a :Municipality .\n" +
                "    ?s ?p1 ?o1 .\n" +
                "  }\n" +
                "  UNION\n" +
                "  {\n" +
                "    ?s ?p2 ?o1 .\n" +
                "  }\n" +
                "}\n";
        runQuery(sparql);
    }

    @Test
    public void andreaTest23(){
        String sparql = "PREFIX sc: <http://purl.org/science/owl/sciencecommons/>\n" +
                "PREFIX schema: <http://schema.org/>\n" +
                "PREFIX dest: <http://destination.example.org/ontology/dest#>\n" +
                "\n" +
                "SELECT ?s ?p1 ?o1 ?p2\n" +
                "WHERE {\n" +
                "  ?s a schema:Hotel .\n" +
                "  {\n" +
                "    ?s a dest:Municipality ;\n" +
                "       ?p1 ?o1 .\n" +
                "  }\n" +
                "  UNION\n" +
                "  {\n" +
                "    ?s ?p2 ?o1 .\n" +
                "  }\n" +
                "}";
        runQuery(sparql);
    }

    @Test
    public void andreaTest24(){
        String sparql = "PREFIX schema: <http://schema.org/>\n" +
                "\n" +
                "SELECT ?s ?place ?p ?o\n" +
                "WHERE {\n" +
                "  ?s a schema:Hotel .\n" +
                "  ?s schema:containedInPlace ?place .\n" +
                "  OPTIONAL {\n" +
                "    ?place ?p ?o .\n" +
                "  }\n" +
                "}\n" +
                "LIMIT 100\n";
        runQuery(sparql);
    }

    @Test
    public void andreaTest25(){
        String sparql = "PREFIX schema: <http://schema.org/>\n" +
                "SELECT ?s ?place ?p ?o\n" +
                "WHERE {\n" +
                "  ?s a schema:Hotel .\n" +
                "  ?s schema:containedInPlace ?place .\n" +
                "  OPTIONAL {\n" +
                "    ?place a schema:City .\n" +
                "    ?place ?p ?o .\n" +
                "  }\n" +
                "}\n" +
                "LIMIT 100\n";
        runQuery(sparql);
    }

    @Test
    public void andreaTest26(){
        String sparql = "PREFIX schema: <http://schema.org/>\n" +
                "SELECT ?s {\n" +
                "  ?s schema:containedInPlace ?h .\n" +
                "  ?h a schema:Hotel .\n" +
                "  ?h ?p2 ?o2 .\n" +
                "}\n" +
                "LIMIT 100";
        runQuery(sparql);
    }

    @Test
    public void andreaTest27(){
        String sparql = "PREFIX sc: <http://purl.org/science/owl/sciencecommons/>\n" +
                "PREFIX schema: <http://schema.org/>\n" +
                "\n" +
                "SELECT ?s (COUNT(*) AS ?count) {\n" +
                "  ?s schema:containedInPlace ?h .\n" +
                "  ?h a schema:Hotel .\n" +
                "  ?h ?p2 ?o2 .\n" +
                "} \n" +
                "GROUP BY ?s\n" +
                "LIMIT 100";
        runQuery(sparql);
    }

    @Test
    public void andreaTest28(){
        String sparql = "PREFIX schema: <http://schema.org/>\n" +
                "SELECT ?s2 ?o\n" +
                "WHERE {\n" +
                "  ?s1 a ?c .\n" +
                "  ?s2 ?o ?s1 .\n" +
                "}\n" +
                "LIMIT 100\n";
        runQuery(sparql);
    }

    @Test
    public void andreaTest29(){
        String sparql = "PREFIX schema: <http://schema.org/>\n" +
                "SELECT ?s1 ?c\n" +
                "WHERE {\n" +
                "  ?s1 a ?c .\n" +
                "}\n" +
                "LIMIT 100\n";
        runQuery(sparql);
    }



    /*@Test
    public void graphexplorer_critical_query_1(){
        String sparql = "SELECT ?class (COUNT(?class) AS ?count) {\n" +
                "      ?subject a ?class {\n" +
                "        SELECT DISTINCT ?subject ?class {\n" +
                "          ?subject a ?class .\n" +
                "          { ?subject ?p <http://destination.example.org/data/weather/observation/202268> }\n" +
                "          UNION\n" +
                "          { <http://destination.example.org/data/weather/observation/202268> ?p ?subject }\n" +
                "        }\n" +
                "        LIMIT 500\n" +
                "      }\n" +
                "    }\n" +
                "    GROUP BY ?class";
        runQuery(sparql);
    }

    @Test
    public void graphexplorer_critical_query_2(){
        String sparql = "SELECT ?class (COUNT(?class) AS ?count) {\n" +
                "      ?subject a ?class {\n" +
                "        SELECT DISTINCT ?subject ?class {\n" +
                "          ?subject a ?class .\n" +
                "          { ?subject ?p <http://destination.example.org/data/source1/hospitality/11893AC7C0CD11D2AE71004095429799> }\n" +
                "          UNION\n" +
                "          { <http://destination.example.org/data/source1/hospitality/11893AC7C0CD11D2AE71004095429799> ?p ?subject }\n" +
                "        }\n" +
                "        \n" +
                "      }\n" +
                "    }\n" +
                "    GROUP BY ?class";
        runQuery(sparql);
    }

    @Test
    public void graphexplorer_critical_query_3(){
        String sparql = "SELECT ?class (COUNT(?class) AS ?count) {\n" +
                "      ?subject a ?class {\n" +
                "        SELECT DISTINCT ?subject ?class {\n" +
                "          ?subject a ?class .\n" +
                "          { ?subject ?p <http://destination.example.org/data/source1/hospitality/A1B9B1850E0B035D21D93D3FCD3AA257> }\n" +
                "          UNION\n" +
                "          { <http://destination.example.org/data/source1/hospitality/A1B9B1850E0B035D21D93D3FCD3AA257> ?p ?subject }\n" +
                "        }\n" +
                "        \n" +
                "      }\n" +
                "    }\n" +
                "    GROUP BY ?class";
        runQuery(sparql);
    }

    @Test
    public void graphexplorer_critical_query_4(){
        String sparql = "SELECT ?subject ?pred ?value ?subjectClass ?pToSubject ?pFromSubject {\n" +
                "      ?subject a     ?subjectClass;\n" +
                "               ?pred ?value {\n" +
                "        SELECT DISTINCT ?subject ?pToSubject ?pFromSubject {\n" +
                "          BIND(<http://destination.example.org/data/municipality/021114> AS ?argument)\n" +
                "          VALUES ?subjectClass { <http://schema.org/BedAndBreakfast> }\n" +
                "          {\n" +
                "            ?argument ?pToSubject ?subject.\n" +
                "            ?subject a         ?subjectClass;\n" +
                "                     ?sPred    ?sValue .\n" +
                "            \n" +
                "          }\n" +
                "          UNION\n" +
                "          {\n" +
                "            ?subject ?pFromSubject ?argument;\n" +
                "                     a         ?subjectClass;\n" +
                "                     ?sPred    ?sValue .\n" +
                "           \n" +
                "          }\n" +
                "        }\n" +
                "        LIMIT 67 OFFSET 0\n" +
                "      }\n" +
                "      FILTER(isLiteral(?value))\n" +
                "    }";
        runQuery(sparql);
    }

    @Test
    public void graphexplorer_critical_query_5(){
        String sparql = "SELECT ?subject ?pred ?value ?subjectClass ?pToSubject ?pFromSubject {\n" +
                "      ?subject a     ?subjectClass;\n" +
                "               ?pred ?value {\n" +
                "        SELECT DISTINCT ?subject ?pToSubject ?pFromSubject {\n" +
                "          BIND(<http://destination.example.org/data/municipality/021051> AS ?argument)\n" +
                "          VALUES ?subjectClass { <http://schema.org/BedAndBreakfast> }\n" +
                "          {\n" +
                "            ?argument ?pToSubject ?subject.\n" +
                "            ?subject a         ?subjectClass;\n" +
                "                     ?sPred    ?sValue .\n" +
                "            \n" +
                "          }\n" +
                "          UNION\n" +
                "          {\n" +
                "            ?subject ?pFromSubject ?argument;\n" +
                "                     a         ?subjectClass;\n" +
                "                     ?sPred    ?sValue .\n" +
                "           \n" +
                "          }\n" +
                "        }\n" +
                "        LIMIT 504 OFFSET 0\n" +
                "      }\n" +
                "      FILTER(isLiteral(?value))\n" +
                "    }";
        runQuery(sparql);
    }


    @Test
    public void graphexplorer_critical_query_6(){
        String sparql = "SELECT ?subject ?pred ?value ?subjectClass ?pToSubject ?pFromSubject {\n" +
                "      ?subject a     ?subjectClass;\n" +
                "               ?pred ?value {\n" +
                "        SELECT DISTINCT ?subject ?pToSubject ?pFromSubject {\n" +
                "          BIND(<http://destination.example.org/data/municipality/021056> AS ?argument)\n" +
                "          VALUES ?subjectClass { <http://schema.org/BedAndBreakfast> }\n" +
                "          {\n" +
                "            ?argument ?pToSubject ?subject.\n" +
                "            ?subject a         ?subjectClass;\n" +
                "                     ?sPred    ?sValue .\n" +
                "            \n" +
                "          }\n" +
                "          UNION\n" +
                "          {\n" +
                "            ?subject ?pFromSubject ?argument;\n" +
                "                     a         ?subjectClass;\n" +
                "                     ?sPred    ?sValue .\n" +
                "           \n" +
                "          }\n" +
                "        }\n" +
                "        LIMIT 172 OFFSET 0\n" +
                "      }\n" +
                "      FILTER(isLiteral(?value))\n" +
                "    }";
        runQuery(sparql);
    }

    @Test
    public void graphexplorer_critical_query_7(){
        String sparql = "SELECT ?subject ?pred ?value ?class {\n" +
                "      ?subject ?pred ?value {\n" +
                "        SELECT DISTINCT ?subject ?class {\n" +
                "            ?subject a          ?class ;\n" +
                "                     ?predicate ?value .\n" +
                "            FILTER (?predicate IN (<http://schema.org/name>, <http://schema.org/description>))\n" +
                "            FILTER (?class IN (<http://schema.org/Accommodation>))\n" +
                "            \n" +
                "        }\n" +
                "        LIMIT 10 OFFSET 0\n" +
                "      }\n" +
                "      FILTER(isLiteral(?value))\n" +
                "    }";
        runQuery(sparql);
    }

    @Test
    public void graphexplorer_critical_query_8(){
        String sparql = "SELECT ?subject ?pred ?value ?class {\n" +
                "      ?subject ?pred ?value {\n" +
                "        SELECT DISTINCT ?subject ?class {\n" +
                "            ?subject a          ?class ;\n" +
                "                     ?predicate ?value .\n" +
                "            FILTER (?predicate IN (<http://schema.org/name>, <http://schema.org/description>))\n" +
                "            FILTER (?class IN (<http://schema.org/Apartment>))\n" +
                "            \n" +
                "        }\n" +
                "        LIMIT 10 OFFSET 0\n" +
                "      }\n" +
                "      FILTER(isLiteral(?value))\n" +
                "    }";
        runQuery(sparql);
    }

    @Test
    public void graphexplorer_critical_query_9(){
        String sparql = "SELECT ?subject ?subjectClass ?predToSubject ?predFromSubject {\n" +
                "      BIND(<http://destination.example.org/data/weather/observation/203021> AS ?argument)\n" +
                "      \n" +
                "      { \n" +
                "        ?argument ?predToSubject ?subject.\n" +
                "        ?subject a ?subjectClass.\n" +
                "      }\n" +
                "      UNION\n" +
                "      { \n" +
                "        ?subject ?predFromSubject ?argument .\n" +
                "        ?subject a ?subjectClass .\n" +
                "      }\n" +
                "    }";
        runQuery(sparql);
    }

    @Test
    public void graphexplorer_critical_query_10(){
        String sparql = "SELECT ?subject ?pred ?value ?class {\n" +
                "      ?subject ?pred ?value {\n" +
                "        SELECT DISTINCT ?subject ?class {\n" +
                "            ?subject a          ?class ;\n" +
                "                     ?predicate ?value .\n" +
                "            FILTER (?predicate IN (<http://schema.org/name>))\n" +
                "            FILTER (?class IN (<http://www.w3.org/ns/sosa/Platform>))\n" +
                "            \n" +
                "        }\n" +
                "        LIMIT 10 OFFSET 0\n" +
                "      }\n" +
                "      FILTER(isLiteral(?value))\n" +
                "    }";
        runQuery(sparql);
    }*/
}
