package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.OntopUnsupportedInputQueryException;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class DestinationTest extends AbstractRDF4JTest {

    private static final String OBDA_FILE = "/destination/dest.obda";
    private static final String SQL_SCRIPT = "/destination/schema.sql";
    private static final String ONTOLOGY_FILE = "/destination/dest.owl";
    private static final String PROPERTIES_FILE = "/destination/dest.properties";

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
    public void testPerf1(){
        String sparql = "SELECT DISTINCT ?p\n" +
                "WHERE\n" +
                "{\n" +
                "  ?s ?p ?o .\n" +
                "}\n" +
                "LIMIT 100";
        int count = runQueryAndCount(sparql);
        assertEquals(92, count);
    }

    @Test
    public void testPerf3(){
        String sparql = "PREFIX : <http://schema.org/>\n" +
                "SELECT ?s\n" +
                "WHERE\n" +
                "{\n" +
                "  ?x :elevation ?n .\n" +
                "  ?x :latitude ?m .\n" +
                "  Bind((?n + ?m) AS ?s)\n" +
                "  FILTER(bound(?s))\n" +
                "}";
        int count = runQueryAndCount(sparql);
        assertEquals(10, count);
    }
    @Test
    public void testPerf4(){
        String sparql = "PREFIX schema: <http://schema.org/>\n" +
                "PREFIX : <http://destination.example.org/ontology/dest#>\n" +
                "SELECT ?name\n" +
                "WHERE\n" +
                "{\n" +
                "\t?mun a :Municipality .\n" +
                "\t?mun schema:name ?name .\n" +
                "}\n";
        int count = runQueryAndCount(sparql);
        assertEquals(33, count);
    }

    @Test
    public void testPerf5(){
        String sparql = "PREFIX : <http://destination.example.org/ontology/dest#>\n" +
                "PREFIX schema: <http://schema.org/>\n" +
                "SELECT ?s ?o\n" +
                "WHERE\n" +
                "{\n" +
                "  ?s a :Municipality.\n" +
                "  ?s schema:geo ?o .\n" +
                "}\n" +
                "LIMIT 100";
        int count = runQueryAndCount(sparql);
        assertEquals(11, count);
    }

    @Test
    public void testPerf6(){
        String sparql = "PREFIX schema: <http://schema.org/>\n" +
                "PREFIX : <http://destination.example.org/ontology/dest#>\n" +
                "SELECT ?municipality ?name\n" +
                "WHERE\n" +
                "{\n" +
                "\t?municipality a :Municipality .\n" +
                "\t?municipality schema:name ?name .\n" +
                "    FILTER (lang(?name) = \"it\")\n" +
                "}\n";
        int count = runQueryAndCount(sparql);
        assertEquals(11, count);
    }

    @Test
    public void testPerf7(){
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
        int count = runQueryAndCount(sparql);
        assertEquals(33, count);
    }

    @Test
    public void testPerf8(){
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
        int count = runQueryAndCount(sparql);
        assertEquals(11, count);
    }

    @Test
    public void testPerf9(){
        String sparql = "SELECT ?o {\n" +
                "  <http://destination.example.org/data/weather/observation/202268> ?p ?o\n" +
                "    }\n" +
                "    LIMIT 500";
        int count = runQueryAndCount(sparql);
        assertEquals(5, count);
    }

    @Test
    public void testPerf10(){
        String sparql = "SELECT ?subject {\n" +
                "  ?subject ?p <http://destination.example.org/data/weather/observation/202268> \n" +
                "}\n" +
                "LIMIT 500";
        int count = runQueryAndCount(sparql);
        assertEquals(2, count);
    }

    @Test
    public void testPerf11(){
        String sparql = "SELECT DISTINCT ?subject ?class {\n" +
                "          ?subject a ?class .\n" +
                "          { ?subject ?p <http://destination.example.org/data/source1/hospitality/60E1EE8CE9647B98CF711E8B78F09955> }\n" +
                "          UNION\n" +
                "          { <http://destination.example.org/data/source1/hospitality/60E1EE8CE9647B98CF711E8B78F09955> ?p ?subject }\n" +
                "        }";
        int count = runQueryAndCount(sparql);
        assertEquals(0, count);
    }

    @Test
    public void testPerf12(){
        String sparql = "SELECT DISTINCT ?subject ?class {\n" +
                "          ?subject a ?class .\n" +
                "        { ?subject ?p <http://destination.example.org/data/weather/observation/202268> }\n" +
                "                UNION\n" +
                "            { <http://destination.example.org/data/weather/observation/202268> ?p ?subject }\n" +
                "            }\n" +
                "            LIMIT 500";
        int count = runQueryAndCount(sparql);
        assertEquals(5, count);
    }

    @Ignore("TODO: enable (too slow)")
    @Test
    public void testPerf13(){
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
    public void testPerf15(){
        String sparql = "SELECT ?class (COUNT(?subject) AS ?count) {\n" +
                "  ?subject a ?class .\n" +
                "  { ?subject ?p <http://destination.example.org/data/weather/observation/202268> }\n" +
                "  UNION\n" +
                "  { <http://destination.example.org/data/weather/observation/202268> ?p ?subject }\n" +
                "}\n" +
                "GROUP BY ?class\n" +
                "LIMIT 500\n";
        int count = runQueryAndCount(sparql);
        assertEquals(5, count);
    }

    @Test
    public void testPerf17(){
        String sparql = "PREFIX schema: <http://schema.org/>\n" +
                "SELECT ?hotel ?o\n" +
                "WHERE {\n" +
                "  ?hotel a schema:Hotel .\n" +
                "  ?hotel ?p ?o .\n" +
                "}\n";
        int count = runQueryAndCount(sparql);
        assertEquals(0, count);
    }

    @Test
    public void testPerf18(){
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
        int count = runQueryAndCount(sparql);
        assertEquals(0, count);
    }

    @Test
    public void testPerf19(){
        String sparql = "PREFIX : <http://destination.example.org/ontology/dest#>\n" +
                "PREFIX schema: <http://schema.org/>\n" +
                "SELECT ?s ?p ?o\n" +
                "WHERE\n" +
                "{\n" +
                "  ?s a :Municipality.\n" +
                "  ?s ?p ?o\n" +
                "}\n" +
                "LIMIT 100";
        int count = runQueryAndCount(sparql);
        assertEquals(55, count);
    }

    @Test
    public void testPerf20(){
        String sparql = "PREFIX : <http://destination.example.org/ontology/dest#>\n" +
                "PREFIX schema: <http://schema.org/>\n" +
                "SELECT ?s ?p ?o\n" +
                "WHERE\n" +
                "{\n" +
                "  ?s ?p ?o .\n" +
                "  ?s a :Municipality .\n" +
                "}\n" +
                "LIMIT 100";
        int count = runQueryAndCount(sparql);
        assertEquals(55, count);
    }

    @Test
    public void testPerf21(){
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
        int count = runQueryAndCount(sparql);
        assertEquals(55, count);
    }

    @Ignore("TODO: enable it (too slow)")
    @Test
    public void testPerf22(){
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
        int count = runQueryAndCount(sparql);
        assertEquals(46635, count);
    }

    @Test
    public void testPerf23(){
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
        int count = runQueryAndCount(sparql);
        assertEquals(0, count);
    }

    @Test
    public void testPerf24(){
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
        int count = runQueryAndCount(sparql);
        assertEquals(0, count);
    }

    @Test
    public void testPerf25(){
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
        int count = runQueryAndCount(sparql);
        assertEquals(0, count);
    }

    @Test
    public void testPerf26(){
        String sparql = "PREFIX schema: <http://schema.org/>\n" +
                "SELECT ?s {\n" +
                "  ?s schema:containedInPlace ?h .\n" +
                "  ?h a schema:Hotel .\n" +
                "  ?h ?p2 ?o2 .\n" +
                "}\n" +
                "LIMIT 100";
        int count = runQueryAndCount(sparql);
        assertEquals(0, count);
    }

    @Test
    public void testPerf27(){
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
        int count = runQueryAndCount(sparql);
        assertEquals(0, count);
    }

    @Test
    public void testPerf29(){
        String sparql =
                "SELECT DISTINCT ?subject ?class {\n" +
                        "?subject a ?class .\n" +
                        "{ ?subject ?p <http://destination.example.org/data/source1/hospitality/A1B9B1850E0B035D21D93D3FCD3AA257> }\n" +
                        "UNION\n" +
                        "{ <http://destination.example.org/data/source1/hospitality/A1B9B1850E0B035D21D93D3FCD3AA257> ?p ?subject }\n" +
                        "}";
        int count = runQueryAndCount(sparql);
        assertEquals(0, count);
    }

    @Ignore("TODO: enable (too slow)")
    @Test
    public void andreaTest30(){
        String sparql =
                "SELECT DISTINCT *\n" +
                        "{ ?s a ?c .}\n";
        int count = runQueryAndCount(sparql);
        assertEquals(14959, count);
    }

    @Test
    public void testPerf31(){
        String sparql =
                "SELECT DISTINCT *\n" +
                        "{\n" +
                        "?s a ?c ." +
                        "<http://destination.example.org/data/source1/hospitality/A1B9B1850E0B035D21D93D3FCD3AA257> ?p ?s .\n" +
                        "}\n";
        int count = runQueryAndCount(sparql);
        assertEquals(0, count);
    }

    @Test
    public void testPerf34(){
        String sparql = "PREFIX schema: <http://schema.org/>\n" +
                "\n" +
                "SELECT DISTINCT ?subject {\n" +
                "  ?subject a <http://schema.org/LodgingBusiness>;\n" +
                "           ?pred ?object.\n" +
                "  {\n" +
                "    SELECT (COUNT(*) as ?cnt) {\n" +
                "      ?s a <http://schema.org/LodgingBusiness>;\n" +
                "         ?p ?o.\n" +
                "    }\n" +
                "    GROUP BY ?p\n" +
                "    LIMIT 10\n" +
                "  }\n" +
                "}\n";
        int count = runQueryAndCount(sparql);
        assertEquals(1, count);
    }

    @Test
    public void andreaTest35(){
        String sparql = "PREFIX schema: <http://schema.org/>\n" +
                "SELECT DISTINCT ?subject {\n" +
                "  ?subject a <http://schema.org/LodgingBusiness>;\n" +
                "           ?pred ?object.\n" +
                "  FILTER(!isBlank(?object) && isLiteral(?object))\n" +
                "  {\n" +
                "    SELECT (COUNT(*) as ?cnt) {\n" +
                "      ?s a <http://schema.org/LodgingBusiness>;\n" +
                "         ?p ?o.\n" +
                "      FILTER(!isBlank(?o) && isLiteral(?o))\n" +
                "    }\n" +
                "    GROUP BY ?p\n" +
                "    ORDER BY ASC(?p)\n" +
                "    LIMIT 10\n" +
                "  }\n" +
                "}\n" +
                "ORDER BY ASC(?p)\n";
        int count = runQueryAndCount(sparql);
        assertEquals(1, count);
    }

    @Test
    public void testPerf36(){
        String sparql = "PREFIX schema: <http://schema.org/>\n" +
                "PREFIX : <http://destination.example.org/ontology/dest#>\n" +
                "SELECT ?subject ?pToSubject\n" +
                "WHERE {\n" +
                "  ?subject ?pFromSubject <http://destination.example.org/data/municipality/021010> .\n" +
                "  ?subject a schema:BedAndBreakfast .\n" +
                "  ?subject ?sPred ?sValue . \n" +
                "}";
        int count = runQueryAndCount(sparql);
        assertEquals(0, count);
    }

    @Test
    public void testPerf37(){
        String sparql = "SELECT DISTINCT ?subject ?pToSubject ?pFromSubject {\n" +
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
                "        }\n";
        int count = runQueryAndCount(sparql);
        assertEquals(0, count);
    }

    @Test
    public void testPerf38(){
        String sparql = "SELECT ?subject ?pred ?value ?subjectClass ?pToSubject ?pFromSubject {\n" +
                "  ?subject a ?subjectClass;\n" +
                "           ?pred ?value {\n" +
                "    SELECT DISTINCT ?subject ?pToSubject ?pFromSubject {\n" +
                "      BIND(<http://destination.example.org/data/municipality/021114> AS ?argument)\n" +
                "      VALUES ?subjectClass { <http://schema.org/BedAndBreakfast> }\n" +
                "      {\n" +
                "        ?argument ?pToSubject ?subject.\n" +
                "        ?subject ?sPred ?sValue .\n" +
                "      }\n" +
                "      UNION\n" +
                "      {\n" +
                "        ?subject ?pFromSubject ?argument;\n" +
                "                 a ?subjectClass;\n" +
                "                 ?sPred ?sValue.\n" +
                "      }\n" +
                "    }\n" +
                "    LIMIT 67 OFFSET 0\n" +
                "  }\n" +
                "  FILTER(isLiteral(?value))\n" +
                "}\n";
        int count = runQueryAndCount(sparql);
        assertEquals(12, count);
    }

    @Test
    public void testPerf39(){
        String sparql = "SELECT ?subject ?pred ?value ?subjectClass ?pToSubject ?pFromSubject {\n" +
                "  ?subject a ?subjectClass;\n" +
                "           ?pred ?value {\n" +
                "    SELECT DISTINCT ?subject ?pToSubject ?pFromSubject {\n" +
                "      BIND(<http://destination.example.org/data/geo/municipality/021010> AS ?argument)\n" +
                "      VALUES ?subjectClass { <http://destination.example.org/ontology/dest#Municipality> }\n" +
                "      {\n" +
                "        ?argument ?pToSubject ?subject.\n" +
                "        ?subject a ?subjectClass;\n" +
                "                 ?sPred ?sValue .\n" +
                "      }\n" +
                "      UNION\n" +
                "      {\n" +
                "        ?subject ?pFromSubject ?argument;\n" +
                "                 a ?subjectClass;\n" +
                "                 ?sPred ?sValue .\n" +
                "      }\n" +
                "    }\n" +
                "    LIMIT 67 OFFSET 0\n" +
                "  }\n" +
                "  FILTER(isLiteral(?value))\n" +
                "}\n";
        runQuery(sparql);
    }

    @Test
    public void testPerf40(){
        String sparql =
                "SELECT * {\n" +
                        "  ?subject a ?subjectClass .\n" +
                        "  ?subject ?pred ?value .\n" +
                        "  {\n" +
                        "    SELECT DISTINCT ?subject {\n" +
                        "      {\n" +
                        "        <http://destination.example.org/data/municipality/021114> ?pToSubject ?subject.\n" +
                        "        ?subject ?sPred ?sValue .\n" +
                        "      }\n" +
                        "    }\n" +
                        "  }\n" +
                        "}\n";
        int count = runQueryAndCount(sparql);
        assertEquals(28, count);
    }

    @Test
    public void testPerf41(){
        String sparql = "PREFIX schema: <http://schema.org/>\n" +
                "SELECT *\n" +
                "WHERE\n" +
                "{\n" +
                "  BIND( schema:Hotel AS ?c )\n" +
                "  ?s a ?c .\n" +
                "  ?s schema:name ?o1 .\n" +
                "}\n" +
                "LIMIT 10";
        runQuery(sparql);
    }

    @Test
    public void graphExplorerCriticalQuery1(){
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
        int count = runQueryAndCount(sparql);
        assertEquals(5, count);
    }

    @Test
    public void graphExplorerCriticalQuery2(){
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
        int count = runQueryAndCount(sparql);
        assertEquals(0, count);
    }

    @Test
    public void graphExplorerCriticalQuery3(){
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
        int count = runQueryAndCount(sparql);
        assertEquals(0, count);
    }

    @Test
    public void graphExplorerCriticalQuery4(){
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
        int count = runQueryAndCount(sparql);
        assertEquals(0, count);
    }

    @Test
    public void graphExplorerCriticalQuery5(){
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
    public void graphExplorerCriticalQuery6(){
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

    @Ignore("TODO: enable (too slow). Needs converting non-strict eq to IRI constant to strict")
    @Test
    public void graphExplorerCriticalQuery7(){
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
        int count = runQueryAndCount(sparql);
        assertEquals(0, count);
    }

    @Test
    public void graphExplorerCriticalQuery7Simplified(){
        String sparql = "SELECT ?subject ?pred ?value ?class {\n" +
                "      ?subject ?pred ?value {\n" +
                "        SELECT DISTINCT ?subject ?class {\n" +
                "            BIND (<http://schema.org/Accommodation> AS ?class)\n" +
                "            ?subject a          ?class ;\n" +
                "                     ?predicate ?value .\n" +
                "            FILTER (?predicate IN (<http://schema.org/name>, <http://schema.org/description>))\n" +
                "            \n" +
                "        }\n" +
                "        LIMIT 10 OFFSET 0\n" +
                "      }\n" +
                "      FILTER(isLiteral(?value))\n" +
                "    }";
        int count = runQueryAndCount(sparql);
        assertEquals(0, count);
    }

    @Ignore("TODO: enable (too slow)")
    @Test
    public void graphExplorerCriticalQuery8(){
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
        int count = runQueryAndCount(sparql);
        assertEquals(0, count);
    }

    @Test
    public void graphExplorerCriticalQuery8CanOptimize(){
        String sparql =
                "SELECT ?subject ?pred ?value ?class {\n" +
                        "  ?subject ?pred ?value {\n" +
                        "    SELECT DISTINCT ?subject ?class {\n" +
                        "      ?subject a <http://schema.org/Apartment> ;\n" +
                        "               ?predicate ?value .\n" +
                        "      FILTER (?predicate IN (<http://schema.org/name>, <http://schema.org/description>))\n" +
                        "    }\n" +
                        "    LIMIT 10 OFFSET 0\n" +
                        "  }\n" +
                        "  FILTER(isLiteral(?value))\n" +
                        "}";
        int count = runQueryAndCount(sparql);
        assertEquals(0, count);
    }

    @Test
    public void graphExplorerCriticalQuery9(){
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
        int count = runQueryAndCount(sparql);
        assertEquals(5, count);
    }

    @Test
    public void graphExplorerCriticalQuery10(){
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
        int count = runQueryAndCount(sparql);
        assertEquals(0, count);
    }

    @Test
    public void testMetaFilterByLiteral1() {
        String sparql = "PREFIX schema: <http://schema.org/>\n" +
                "SELECT ?subject ?pred ?v {\n" +
                "  ?subject ?pred ?v ; \n" +
                "       schema:name \"Glorenza\"@it  \n" +
                "}";
        runQueryAndCompare(sparql, ImmutableSet.of("Glorenza", "Glurns", "Glorenza/Glurns",
                "http://destination.example.org/ontology/dest#Municipality",
                "http://destination.example.org/data/geo/municipality/021036"));
    }

    @Test
    public void testMetaJoinByLiteral1() {
        String sparql = "PREFIX schema: <http://schema.org/>\n" +
                "PREFIX : <http://destination.example.org/ontology/dest#>" +
                "SELECT ?subject ?pred ?v {\n" +
                "   ?subject ?pred ?v . \n" +
                "   <http://destination.example.org/data/municipality/021036> schema:name ?v \n" +
                "}";
        runQueryAndCompare(sparql, ImmutableSet.of("Glorenza", "Glurns", "Glorenza/Glurns"));
    }

    @Test
    public void testMetaJoinByLiteral2() {
        String sparql = "PREFIX schema: <http://schema.org/>\n" +
                "PREFIX : <http://destination.example.org/ontology/dest#>" +
                "SELECT ?subject ?pred ?v {\n" +
                "    ?subject ?pred ?v . \n" +
                "    <http://destination.example.org/data/municipality/021036> schema:name ?v \n" +
                "    FILTER (langMatches(lang(?v), 'en'))\n" +
                "}";
        int count = runQueryAndCount(sparql);
        assertEquals(1, count);
    }
    @Test
    public void testSubClassOfPropertyPath1() {
        int count = runQueryAndCount("PREFIX schema: <http://schema.org/>\n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX : <http://noi.example.org/ontology/odh#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "\n" +
                "SELECT DISTINCT ?h \n" +
                "WHERE {\n" +
                "  ?h rdf:type/rdfs:subClassOf* schema:LodgingBusiness .\n" +
                "}\n");
        assertEquals(1, count);
    }

    @Test
    public void testSubClassOfPropertyPath2() {
        runQueryAndCompare("PREFIX schema: <http://schema.org/>\n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX : <http://noi.example.org/ontology/odh#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "\n" +
                "SELECT ?h ?v \n" +
                "WHERE {\n" +
                "  ?h rdf:type ?v . \n" +
                "  ?v rdfs:subClassOf* schema:LodgingBusiness .\n" +
                "}\n",
                ImmutableSet.of("http://schema.org/LodgingBusiness", "http://schema.org/Campground")); ;
    }

    @Test
    public void testSubClassOfPropertyPath3() {
        runQueryAndCompare("PREFIX schema: <http://schema.org/>\n" +
                        "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                        "PREFIX : <http://noi.example.org/ontology/odh#>\n" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "\n" +
                        "SELECT ?h ?v \n" +
                        "WHERE {\n" +
                        "  <http://destination.example.org/data/source1/hospitality/aaa> rdf:type ?v . \n" +
                        "  ?v rdfs:subClassOf* ?c .\n" +
                        "  FILTER (strends(str(?c), 'Business'))\n" +
                        "}\n",
                ImmutableSet.of("http://schema.org/LodgingBusiness", "http://schema.org/Campground",
                        "http://schema.org/LocalBusiness")); ;
    }

    @Test
    public void testSubClassOfPropertyPath4() {
        runQueryAndCompare("PREFIX schema: <http://schema.org/>\n" +
                        "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                        "PREFIX : <http://noi.example.org/ontology/odh#>\n" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "\n" +
                        "SELECT ?h ?v \n" +
                        "WHERE {\n" +
                        "  GRAPH ?g {\n" +
                        "   ?h rdf:type ?v . \n" +
                        "   ?v rdfs:subClassOf* ?c .\n" +
                        "  }\n" +
                        "  FILTER (strends(str(?c), 'Business'))\n" +
                        "}\n",
                ImmutableSet.of()); ;
    }

    @Test
    public void testSubClassOfPropertyPath5() {
        runQueryAndCompare("PREFIX schema: <http://schema.org/>\n" +
                        "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                        "PREFIX : <http://noi.example.org/ontology/odh#>\n" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "\n" +
                        "SELECT ?v \n" +
                        "WHERE {\n" +
                        "  BIND(schema:LodgingBusiness AS ?c)\n" +
                        "  <http://destination.example.org/data/source1/hospitality/aaa> rdf:type ?v . \n" +
                        "  ?v rdfs:subClassOf* ?c .\n" +
                        "}\n",
                ImmutableSet.of("http://schema.org/LodgingBusiness", "http://schema.org/Campground")); ;
    }

    @Ignore("Too inefficient")
    @Test
    public void testSubClassOfPropertyPath6() {
        runQueryAndCompare("PREFIX schema: <http://schema.org/>\n" +
                        "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                        "PREFIX : <http://noi.example.org/ontology/odh#>\n" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "\n" +
                        "SELECT ?v \n" +
                        "WHERE {\n" +
                        "  ?c rdf:type ?v . \n" +
                        "  ?v rdfs:subClassOf* ?c .\n" +
                        "}\n",
                ImmutableSet.of()); ;
    }

    @Test
    public void testSubClassOfPropertyPath7() {
        runQueryAndCompare("PREFIX schema: <http://schema.org/>\n" +
                        "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                        "PREFIX : <http://noi.example.org/ontology/odh#>\n" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "\n" +
                        "SELECT ?h ?v \n" +
                        "WHERE {\n" +
                        "  GRAPH ?x {\n" +
                        "   ?h rdf:type ?v . \n" +
                        "   ?v rdfs:subClassOf* ?x .\n" +
                        "  }\n" +
                        "  FILTER (strends(str(?x), 'Business'))\n" +
                        "}\n",
                ImmutableSet.of()); ;
    }

    @Test
    public void testSubClassOfPropertyPath8() {
        runQueryAndCompare("PREFIX schema: <http://schema.org/>\n" +
                        "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                        "PREFIX : <http://noi.example.org/ontology/odh#>\n" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "\n" +
                        "SELECT ?h ?v \n" +
                        "WHERE {\n" +
                        "  GRAPH ?x {\n" +
                        "   ?h rdf:type ?x . \n" +
                        "   ?x rdfs:subClassOf* ?v .\n" +
                        "  }\n" +
                        "  FILTER (strends(str(?v), 'Business'))\n" +
                        "}\n",
                ImmutableSet.of()); ;
    }

    @Ignore("Too inefficient")
    @Test
    public void testSubClassOfPropertyPath9() {
        runQueryAndCompare("PREFIX schema: <http://schema.org/>\n" +
                        "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                        "PREFIX : <http://noi.example.org/ontology/odh#>\n" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "\n" +
                        "SELECT ?h ?v \n" +
                        "WHERE {\n" +
                        "  ?h rdf:type ?v . \n" +
                        "  ?v rdfs:subClassOf* ?v .\n" +
                        "  FILTER (strends(str(?v), 'Business'))\n" +
                        "}\n",
                ImmutableSet.of()); ;
    }

    @Test
    public void testSubClassOfPropertyPath10() {
        runQueryAndCompare("PREFIX schema: <http://schema.org/>\n" +
                        "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                        "PREFIX : <http://noi.example.org/ontology/odh#>\n" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "\n" +
                        "SELECT ?h ?v \n" +
                        "WHERE {\n" +
                        "  GRAPH ?g {\n" +
                        "   ?h rdf:type ?v . \n" +
                        "   ?v rdfs:subClassOf* ?v .\n" +
                        "  }\n" +
                        "  FILTER (strends(str(?v), 'Business'))\n" +
                        "}\n",
                ImmutableSet.of()); ;
    }

    @Test
    public void testSubClassOfPropertyPath11() {
        runQueryAndCompare("PREFIX schema: <http://schema.org/>\n" +
                        "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                        "PREFIX : <http://noi.example.org/ontology/odh#>\n" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "\n" +
                        "SELECT ?h ?v \n" +
                        "WHERE {\n" +
                        "  GRAPH :not-existing-graph {\n" +
                        "   ?h rdf:type ?v . \n" +
                        "   ?v rdfs:subClassOf* ?c .\n" +
                        "  }\n" +
                        "  FILTER (strends(str(?c), 'Business'))\n" +
                        "}\n",
                ImmutableSet.of()); ;
    }

    @Test
    public void testSubClassOfPropertyPath12() {
        try {
            runQuery("PREFIX schema: <http://schema.org/>\n" +
                    "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                    "PREFIX : <http://noi.example.org/ontology/odh#>\n" +
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                    "\n" +
                    "SELECT ?v ?c \n" +
                    "WHERE {\n" +
                    " ?v rdfs:subClassOf* ?c .\n" +
                    "}\n");
        } catch (QueryEvaluationException e) {
            assertTrue(e.getCause() instanceof OntopUnsupportedInputQueryException);
            return;
        }
        fail();
    }

    @Test
    public void testSubClassOfPropertyPath13() {
        try {
            runQuery("PREFIX schema: <http://schema.org/>\n" +
                    "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                    "PREFIX : <http://noi.example.org/ontology/odh#>\n" +
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                    "\n" +
                    "SELECT ?v ?c \n" +
                    "WHERE {\n" +
                    " GRAPH ?g {\n" +
                    "    ?v rdfs:subClassOf* ?c .\n" +
                    "  }\n" +
                    "}\n");
        } catch (QueryEvaluationException e) {
            assertTrue(e.getCause() instanceof OntopUnsupportedInputQueryException);
            return;
        }
        fail();
    }

    // HACK. TODO: remove this test (temporary)
    @Test
    public void testSubClassOfPropertyPathHack() {
        runQuery("PREFIX schema: <http://schema.org/>\n" +
                        "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                        "PREFIX : <http://noi.example.org/ontology/odh#>\n" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
                        "\n" +
                        "SELECT ?h ?v \n" +
                        "WHERE {\n" +
                        "  <http://destination.example.org/data/source1/hospitality/aaa> rdf:type ?v . \n" +
                        "  ?v owl:priorVersion* ?c .\n" +
                        "}\n");
    }

}
