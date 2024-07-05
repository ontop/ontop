package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import eu.optique.r2rml.api.model.impl.R2RMLMappingCollectionImpl;
import it.unibz.inf.ontop.utils.R2RMLIRISafeEncoder;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InternshipNewQueryUnfolderTest extends AbstractRDF4JTest {
    private static final String OBDA_FILE = "/new-unfolder/new-unfolder.obda";
    private static final String SQL_SCRIPT = "/new-unfolder/schema.sql";
    private static final String ONTOLOGY_FILE = "/new-unfolder/new-unfolder.owl";
    private static final String PROPERTIES_FILE = "/new-unfolder/new-unfolder.properties";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE, ONTOLOGY_FILE, PROPERTIES_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    //http://destination.example.org/data/source1/hospitality/{} and http://destination.example.org/data/source2/hotels/{}
    @Test
    public void subjectHaveMultipleIRITemplate() {
        String sparql =
                "PREFIX schema: <http://schema.org/>\n" +
                "SELECT ?s ?p ?o\n" +
                "WHERE {\n" +
                "  ?s a schema:Hotel .\n" +
                "  ?s ?p ?o .\n" +
                "}\n" +
                "ORDER BY ASC(?s)";
        int count = runQueryAndCount(sparql);
        assertEquals(45, count);
    }

    //http://destination.example.org/data/municipality/{}
    @Test
    public void subjectHaveOneIRITemplate() {
        String sparql =
                "PREFIX schema: <http://schema.org/>\n" +
                        "PREFIX : <http://destination.example.org/ontology/dest#>\n" +
                        "SELECT ?sub ?pred ?obj WHERE {\n" +
                        "  ?sub a :Municipality .\n" +
                        "  ?sub ?pred ?obj .\n" +
                        "}";
        int count = runQueryAndCount(sparql);
        assertEquals(68, count);
    }

    @Test
    public void IRIConstantDoNotMatchAnyTemplateBecauseIsNotInOurKG() {
        String sparql =
                "SELECT *\n" +
                "WHERE {\n" +
                "    <https://tutorial.linked.data.world/d/sparqltutorial/row-got-0> ?p ?o .\n" +
                "}";
        int count = runQueryAndCount(sparql);
        assertEquals(0, count);
    }

    //http://destination.example.org/data/xyz/{x}{y}z e http://destination.example.org/data/xyz/xy{z}
    @Test
    public void IRIConstantMatchMultipleTemplateButThereIsNoGenericOne() {
        String sparql =
                "SELECT * WHERE {\n" +
                "  <http://destination.example.org/data/xyz/xyz> ?pred ?obj .\n" +
                        "}";
        int count = runQueryAndCount(sparql);
        assertEquals(1, count);
    }

    //http://destination.example.org/data/municipality/{} e http://destination.example.org/data/municipality/0{}
    @Test
    public void IRIConstantMatchMultipleTemplateButOneIsMoreGenericThanOther() {
        String sparql =
                "PREFIX schema: <http://schema.org/>\n" +
                "SELECT ?pred ?obj WHERE {\n" +
                "  <http://destination.example.org/data/municipality/021069> ?pred ?obj .\n" +
                        "} ";
        int count = runQueryAndCount(sparql);
        assertEquals(8, count);
    }

    //http://destination.example.org/data/weather/observation/{}
    @Test
    public void IRIConstantMatchJustOneTemplate() {
        String sparql =
                "PREFIX schema: <http://schema.org/>\n" +
                "SELECT *\n" +
                "WHERE {\n" +
                "    <http://destination.example.org/data/weather/observation/201539> ?p ?o .\n" +
                "}\n";
        int count = runQueryAndCount(sparql);
        assertEquals(5, count);
    }

    @Test
    public void subjectRDFTypeClass() {
        String sparql =
                "PREFIX schema: <http://schema.org/>\n" +
                "SELECT ?s ?c WHERE {\n" +
                "  ?s a ?c .\n" +
                "  ?s a schema:Hotel .\n" +
                "} ";
        int count = runQueryAndCount(sparql);
        assertEquals(24, count);
    }

    @Test
    public void IRIRDFTypeClass() {
        String sparql =
                "PREFIX schema: <http://schema.org/>\n" +
                "SELECT ?c WHERE {\n" +
                "  <http://destination.example.org/data/source1/hospitality/EFF0FACBA54C11D1AD760020AFF92740> a ?c .\n" +
                "} ";
        int count = runQueryAndCount(sparql);
        assertEquals(6, count);
    }

    @Test
    public void IRIConstantOnObjectOfSPOWithoutSubjDefinition() {
        String sparql =
                "PREFIX schema: <http://schema.org/>\n" +
                "SELECT DISTINCT ?s ?p ?o {\n" +
                "  ?s ?p <http://destination.example.org/data/source1/hospitality/EFF0FACBA54C11D1AD760020AFF92740> .\n" +
                "}\n";
        int count = runQueryAndCount(sparql);
        assertEquals(0, count);
    }

    @Test
    public void IRIConstantOnObjectOfSPOWithSubjDefinition() {
        String sparql = "PREFIX schema: <http://schema.org/>\n" +
                "SELECT DISTINCT ?s ?p ?o {\n" +
                "  ?s a schema:Hotel .\n" +
                "  ?s ?p <http://destination.example.org/data/municipality/021027> .\n" +
                "}\n";
        int count = runQueryAndCount(sparql);
        assertEquals(2, count);
    }

    @Test
    public void IRIConstantBothOnSubjectAndObjectOfSPOSameIRITemplate() {
        String sparql =
                "PREFIX schema: <http://schema.org/>\n" +
                "SELECT ?o \n" +
                "WHERE {\n" +
                "  <http://destination.example.org/data/municipality/021069> ?o <http://destination.example.org/data/geo/municipality/021069> .\n" +
                "}";
        int count = runQueryAndCount(sparql);
        assertEquals(1, count);
    }

    @Test
    public void IRIConstantFirstOnSubjectAndThenOnObject() {
        String sparql =
                "PREFIX schema: <http://schema.org/>\n" +
                "SELECT DISTINCT ?p {\n" +
                "  ?s ?p <http://destination.example.org/data/municipality/021027> .\n" +
                "  OPTIONAL{ <http://destination.example.org/data/municipality/021027> ?p ?o }\n" +
                "}\n";
        int count = runQueryAndCount(sparql);
        assertEquals(1, count);
    }

    @Test
    public void IRIConstantBothOnSubjectAndObjectOfSPODifferentIRITemplate() {
        String sparql =
                "PREFIX schema: <http://schema.org/>\n" +
                "SELECT ?p\n" +
                "WHERE {\n" +
                "  <http://destination.example.org/data/source1/rooms/0003A0F93DCC47F5967B091D2CE3D352> ?p <http://destination.example.org/data/source1/occupancy/rooms/0003A0F93DCC47F5967B091D2CE3D352> .\n" +
                "}\n";
        int count = runQueryAndCount(sparql);
        assertEquals(0, count);
    }

    @Test
    public void unionWithoutOptimization(){
        String sparql =
                "PREFIX schema: <http://schema.org/>\n" +
                "SELECT DISTINCT ?s ?o {\n" +
                "  {\n" +
                "    ?s schema:name ?o .\n" +
                "  }\n" +
                "  UNION\n" +
                "  {\n" +
                "    ?s schema:longitude ?o .\n" +
                "  }\n" +
                "}\n";
        int count = runQueryAndCount(sparql);
        assertEquals(100, count);
    }

    @Test
    public void unionWithSubjOptimizationOnFather(){
        String sparql =
                "PREFIX schema: <http://schema.org/>\n" +
                "SELECT DISTINCT ?s ?p ?o {\n" +
                "  ?s a schema:Hotel\n" +
                "  {\n" +
                "    ?s ?p ?o\n" +
                "  }\n" +
                "  UNION\n" +
                "  {\n" +
                "    ?s schema:name ?o .\n" +
                "  }\n" +
                "}\n";
        int count = runQueryAndCount(sparql);
        assertEquals(57, count);
    }

    @Test
    public void unionWithSubjOptimizationOnSingleChild(){
        String sparql =
                "PREFIX schema: <http://schema.org/>\n" +
                "SELECT DISTINCT ?s ?p ?o {\n" +
                "  {\n" +
                "    ?s a schema:Hotel .\n" +
                "    ?s ?p ?o .\n" +
                "  }\n" +
                "  UNION\n" +
                "  {\n" +
                "    ?s schema:name ?o .\n" +
                "  }\n" +
                "}\n";
        int count = runQueryAndCount(sparql);
        assertEquals(120, count);
    }

    @Test
    public void unionWithSubjOptimizationOnBothChild(){
        String sparql =
                "PREFIX schema: <http://schema.org/>\n" +
                "PREFIX : <http://destination.example.org/ontology/dest#>\n" +
                "SELECT DISTINCT ?s ?p ?o {\n" +
                "  {\n" +
                "    ?s a schema:Hotel .\n" +
                "    ?s ?p ?o .\n" +
                "  }\n" +
                "  UNION\n" +
                "  {\n" +
                "    ?s a :Municipality .\n" +
                "    ?s schema:name ?o .\n" +
                "  }\n" +
                "}";
        int count = runQueryAndCount(sparql);
        assertEquals(75, count);
    }

    @Test
    public void unionWithSubjOptimizationOnBothChildAndFather(){
        String sparql =
                "PREFIX schema: <http://schema.org/>\n" +
                "PREFIX : <http://destination.example.org/ontology/dest#>\n" +
                "SELECT DISTINCT ?s ?p ?o {\n" +
                "  ?s a schema:Hotel .\n" +
                "  {\n" +
                "    ?s a :Municipality .\n" +
                "    ?s ?p ?o .\n" +
                "  }\n" +
                "  UNION\n" +
                "  {\n" +
                "    ?s schema:name ?o .\n" +
                "  }\n" +
                "}\n";
        int count = runQueryAndCount(sparql);
        assertEquals(12, count);
    }

    @Test
    public void unionWithConstOptimizationOnFather(){
        String sparql =
                "PREFIX schema: <http://schema.org/>\n" +
                "SELECT DISTINCT ?s ?p ?o {\n" +
                "  ?s ?p <http://destination.example.org/data/source1/hospitality/EFF0FACBA54C11D1AD760020AFF92740> . \n" +
                "  {\n" +
                "    ?s ?p ?o .               \n" +
                "  }\n" +
                "  UNION                      \n" +
                "  {\n" +
                "    ?s schema:name ?o .     \n" +
                "  }\n" +
                "}\n";
        int count = runQueryAndCount(sparql);
        assertEquals(0, count);
    }

    @Test
    public void unionWithConstOptimizationOnSingleChild(){
        String sparql =
                "PREFIX schema: <http://schema.org/>\n" +
                "SELECT DISTINCT ?o {\n" +
                "  {\n" +
                "    <http://destination.example.org/data/source1/hospitality/EFF0FACBA54C11D1AD760020AFF92740> ?p ?o .\n" +
                "  }\n" +
                "  UNION\n" +
                "  {\n" +
                "    ?s schema:name ?o .\n" +
                "  }\n" +
                "}\n" +
                "LIMIT 100\n";
        int count = runQueryAndCount(sparql);
        assertEquals(83, count);
    }

    @Test
    public void unionWithConstOptimizationOnBothChild(){
        String sparql =
                "PREFIX schema: <http://schema.org/>\n" +
                "SELECT DISTINCT ?p ?o {\n" +
                "  {\n" +
                "    <http://destination.example.org/data/source1/hospitality/EFF0FACBA54C11D1AD760020AFF92740> ?p ?o .\n" +
                "  }\n" +
                "  UNION\n" +
                "  {\n" +
                "    <http://destination.example.org/data/source1/hospitality/A92A692C413911D483B90050BAC0A490> ?p ?o .\n" +
                "  }\n" +
                "}\n" +
                "LIMIT 100\n";
        int count = runQueryAndCount(sparql);
        assertEquals(17, count);
    }

    @Test
    public void unionWithConstOptimizationOnBothChildAndFather(){
        String sparql =
                "PREFIX schema: <http://schema.org/>\n" +
                "SELECT DISTINCT ?p ?o {\n" +
                "  <http://destination.example.org/data/municipality/021069> ?p ?o .\n" +
                "  {\n" +
                "    <http://destination.example.org/data/source1/hospitality/EFF0FACBA54C11D1AD760020AFF92740> ?p ?o .\n" +
                "  }\n" +
                "  UNION\n" +
                "  {\n" +
                "    <http://destination.example.org/data/source1/hospitality/A92A692C413911D483B90050BAC0A490> ?p ?o .\n" +
                "  }\n" +
                "}\n" +
                "LIMIT 100";
        int count = runQueryAndCount(sparql);
        assertEquals(2, count);
    }

    @Test
    public void unionWithConstAndSubjOptimization(){
        String sparql =
                "PREFIX schema: <http://schema.org/>\n" +
                "SELECT DISTINCT ?s ?p ?o {\n" +
                "  ?s a schema:Hotel .\n" +
                "  {\n" +
                "    <http://destination.example.org/data/source1/hospitality/EFF0FACBA54C11D1AD760020AFF92740> ?p ?o .\n" +
                "  }\n" +
                "  UNION\n" +
                "  {\n" +
                "    ?s ?p ?o .\n" +
                "  }\n" +
                "}\n" +
                "LIMIT 100";
        int count = runQueryAndCount(sparql);
        assertEquals(60, count);
    }

    @Test
    public void leftJoinWithoutOptimization(){
        String sparql = "PREFIX schema: <http://schema.org/>\n" +
                "\n" +
                "    SELECT ?hotel ?location ?name\n" +
                "    WHERE {\n" +
                "               ?hotel a schema:Hotel .\n" +
                "                ?hotel schema:containedInPlace ?location .\n" +
                "                OPTIONAL {\n" +
                "       ?location schema:name ?name .\n" +
                "        }\n" +
                "    }";
        int count = runQueryAndCount(sparql);
        assertEquals(2, count);
    }

    @Test
    public void leftJoinWithSubjOptimizationOnLeftChild(){
        String sparql =
                "PREFIX schema: <http://schema.org/>\n" +
                        "\n" +
                        "SELECT ?s ?place ?p ?o\n" +
                        "WHERE {\n" +
                        "  ?s a schema:Hotel .\n" +
                        "  ?s ?p ?place .\n" +
                        "  OPTIONAL {\n" +
                        "    ?place schema:name ?o .\n" +
                        "  }\n" +
                        "}\n";
        int count = runQueryAndCount(sparql);
        assertEquals(45, count);
    }

    @Test
    public void leftJoinWithSubjOptimizationOnRightChild(){
        String sparql =
                "PREFIX schema: <http://schema.org/>\n" +
                        "\n" +
                        "SELECT ?s ?place ?p ?o\n" +
                        "WHERE {\n" +
                        "  ?s schema:containedInPlace ?place .\n" +
                        "  OPTIONAL {\n" +
                        "    ?place ?p ?o .\n" +
                        "  }\n" +
                        "}\n";
        int count = runQueryAndCount(sparql);
        assertEquals(5, count);
    }

    @Test
    public void leftJoinWithSubjOptimizationOnBothChild(){
        String sparql = "PREFIX schema: <http://schema.org/>\n" +
                "PREFIX : <http://destination.example.org/ontology/dest#>\n" +
                "SELECT ?hotel ?location ?property1 ?property2 ?value\n" +
                "WHERE {\n" +
                "  ?hotel a schema:Hotel .\n" +
                "  ?hotel ?property1 ?location .\n" +
                "  OPTIONAL {\n" +
                "    ?location a :Municipality .\n" +
                "    ?location ?property2 ?value .\n" +
                "  }\n" +
                "}\n";
        int count = runQueryAndCount(sparql);
        assertEquals(45, count);
    }

    @Test
    public void leftJoinWithConstOptimizationOnLeftChild(){
        String sparql = "PREFIX schema: <http://schema.org/>\n" +
                "SELECT ?property ?location ?name\n" +
                "WHERE {\n" +
                "  <http://destination.example.org/data/source1/hospitality/EFF0FACBA54C11D1AD760020AFF92740> ?property ?location .\n" +
                "  OPTIONAL {\n" +
                "    ?location schema:name ?name .\n" +
                "  }\n" +
                "}\n";
        int count = runQueryAndCount(sparql);
        assertEquals(11, count);
    }

    @Test
    public void leftJoinWithConstOptimizationOnRightChild(){
        String sparql =
                "PREFIX schema: <http://schema.org/>\n" +
                "SELECT ?hotel ?property ?value\n" +
                "WHERE {\n" +
                "  ?hotel schema:containedInPlace <http://destination.example.org/data/municipality/021027> .\n" +
                "  OPTIONAL {\n" +
                "    <http://destination.example.org/data/municipality/021027> ?property ?value .\n" +
                "  }\n" +
                "}\n";
        int count = runQueryAndCount(sparql);
        assertEquals(2, count);
    }

    @Test
    public void leftJoinWithConstOptimizationOnBothChild(){
        String sparql =
                "PREFIX schema: <http://schema.org/>\n" +
                "SELECT ?pp ?vp ?ph ?vh\n" +
                "WHERE {\n" +
                "  <http://destination.example.org/data/source2/hotels/001AE4C0FA0781A2CDD3750811DBDAEB> ?ph ?vh .\n" +
                "  <http://destination.example.org/data/source2/hotels/001AE4C0FA0781A2CDD3750811DBDAEB> schema:containedInPlace <http://destination.example.org/data/municipality/021027> .\n" +
                "  OPTIONAL {\n" +
                "    <http://destination.example.org/data/municipality/021027> ?pp ?vp .\n" +
                "  }\n" +
                "}";
        int count = runQueryAndCount(sparql);
        assertEquals(11, count);
    }

    @Test
    public void leftJoinWithConstAndSubjOptimization(){
        String sparql =
                "PREFIX schema: <http://schema.org/>\n" +
                "SELECT ?s ?ph ?oh ?pv ?ov\n" +
                "WHERE {\n" +
                "  ?s a schema:Hotel .\n" +
                "  ?s ?ph ?oh .\n" +
                "  ?s schema:containedInPlace <http://destination.example.org/data/municipality/021027> .\n" +
                "  OPTIONAL {\n" +
                "    <http://destination.example.org/data/municipality/021027> ?pv ?ov .\n" +
                "  }\n" +
                "}\n";
        int count = runQueryAndCount(sparql);
        assertEquals(22, count);
    }


    @Test
    public void IRIConstantTakenFromDBColumnDuplicateCantHappen() {
        String sparql =
                "PREFIX schema: <http://schema.org/>\n" +
                "SELECT * WHERE {\n" +
                "  ?sub a schema:Place .\n" +
                "  ?sub schema:ratingValue ?obj .\n" +
                "}";
        int count = runQueryAndCount(sparql);
        assertEquals(10, count);
    }

    @Test
    public void IRIConstantTakenFromDBColumnDuplicateCanHappenAndNeedToAvoidThem() {
        String sparql =
                "PREFIX schema: <http://schema.org/>\n" +
                        "SELECT * WHERE {\n" +
                        "  ?sub a schema:Place .\n" +
                        "  ?sub ?p ?o .\n" +
                        "} \n" +
                        "ORDER BY ASC(?sub)";
        int count = runQueryAndCount(sparql);
        assertEquals(210, count);
    }

    @Test
    public void IRIConstantConstructedWithSQLTakenFromDB() {
        String sparql = "PREFIX schema: <http://schema.org/>\n" +
                "SELECT * WHERE {\n" +
                "  ?sub a schema:Thing .\n" +
                "  ?sub schema:value ?obj .\n" +
                "} \n" +
                "LIMIT 10";
        int count = runQueryAndCount(sparql);
        assertEquals(4, count);
    }

    @Test
    public void IRIConstantConstructedWithSparQL() {
        String sparql =
                "PREFIX schema: <http://schema.org/>\n" +
                "SELECT *\n" +
                "WHERE {\n" +
                "  BIND(IRI(CONCAT(\"http://destination.example.org/data/weather/observation/\", \"201539\")) AS ?subject)\n" +
                "  ?subject ?p ?o .\n" +
                "}\n";
        int count = runQueryAndCount(sparql);
        assertEquals(5, count);
    }
}
