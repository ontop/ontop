package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

public class UniversityTBoxFactTest extends AbstractRDF4JTest {

    private static final String CREATE_DB_FILE = "/tbox-facts/university.sql";
    private static final String OBDA_FILE = "/tbox-facts/university.obda";
    private static final String OWL_FILE = "/tbox-facts/university-complete.ttl";
    private static final String PROPERTIES_FILE = "/tbox-facts/factextraction.properties";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(CREATE_DB_FILE, OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testSubClasses() {
        String query = "PREFIX : <http://example.org/voc#>" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "SELECT  ?v\n" +
                "WHERE {\n" +
                "?v rdfs:subClassOf :Professor .\n" +
                "}\n";

        ImmutableSet<String> results = ImmutableSet.of("http://example.org/voc#Professor",
                "http://example.org/voc#AssociateProfessor",
                "http://example.org/voc#FullProfessor",
                "http://example.org/voc#AssistantProfessor");
        runQueryAndCompare(query, results);
    }

    /**
     * Filter one BNode due to EQ from restriction
     */
    @Test
    public void testRDFSClasses() {
        String query = "SELECT  *\n" +
                "WHERE {\n" +
                "   ?v a rdfs:Class .\n" +
                "FILTER (!isBlank(?v)) . \n" +
                "}\n";

        runQueryAndCompare(query, getExpectedClasses());
    }

    /**
     * One BNode due to EQ from restriction
     */
    @Test
    public void testRDFSClassesBNodes() {
        String query = "SELECT  *\n" +
                "WHERE {\n" +
                "   ?v a rdfs:Class .\n" +
                "FILTER (isBlank(?v)) . \n" +
                "}\n";

        int resultCount = runQueryAndCount(query);
        assertEquals(1, resultCount);
    }

    /**
     * Filter one BNode due to EQ from restriction
     */
    @Test
    public void testOWLClasses() {
        String query = "SELECT  *\n" +
                "WHERE {\n" +
                "   ?v a owl:Class .\n" +
                "FILTER (!isBlank(?v)) . \n" +
                "}\n";

        runQueryAndCompare(query, getExpectedClasses());
    }

    /**
     * One BNode due to EQ from restriction
     */
    @Test
    public void testOWLClassesBNodes() {
        String query = "SELECT  *\n" +
                "WHERE {\n" +
                "   ?v a owl:Class .\n" +
                "FILTER (isBlank(?v)) . \n" +
                "}\n";

        int resultCount = runQueryAndCount(query);
        assertEquals(1, resultCount);
    }

    @Test
    public void testRDFSDomainRelation() {
        String query = "PREFIX : <http://example.org/voc#>" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "SELECT  ?v\n" +
                "WHERE {\n" +
                "   ?v rdfs:domain ?z .\n" +
                "}\n";

        ImmutableSet<String> results = ImmutableSet.of("http://example.org/voc#attends",
                "http://example.org/voc#isGivenAt",
                "http://example.org/voc#teaches",
                "http://example.org/voc#givesLab",
                "http://example.org/voc#isTaughtBy",
                "http://example.org/voc#givesLecture",
                "http://xmlns.com/foaf/0.1/firstName",
                "http://xmlns.com/foaf/0.1/lastName");
        runQueryAndCompare(query, results);
    }

    @Test
    public void testRDFSDomain() {
        String query = "PREFIX : <http://example.org/voc#>" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "SELECT  ?v\n" +
                "WHERE {\n" +
                "   ?z rdfs:domain ?v .\n" +
                "}\n";

        ImmutableSet<String> results = ImmutableSet.of("http://xmlns.com/foaf/0.1/Person",
                "http://example.org/voc#Teacher",
                "http://example.org/voc#FacultyMember",
                "http://example.org/voc#Course");
        runQueryAndCompare(query, results);
    }

    @Test
    public void testRDFSRangeRelation() {
        String query = "PREFIX : <http://example.org/voc#>" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "SELECT  ?v\n" +
                "WHERE {\n" +
                "   ?v rdfs:range ?z .\n" +
                "}\n";

        ImmutableSet<String> results = ImmutableSet.of("http://example.org/voc#attends",
                "http://example.org/voc#isGivenAt",
                "http://example.org/voc#teaches",
                "http://example.org/voc#givesLab",
                "http://example.org/voc#isTaughtBy",
                "http://example.org/voc#givesLecture");
        runQueryAndCompare(query, results);
    }

    @Test
    public void testRDFSRange() {
        String query = "PREFIX : <http://example.org/voc#>" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "SELECT  ?v\n" +
                "WHERE {\n" +
                "   ?z rdfs:range ?v .\n" +
                "}\n";

        ImmutableSet<String> results = ImmutableSet.of("http://example.org/voc#Course",
                "http://example.org/voc#EducationalInstitution",
                "http://example.org/voc#Teacher",
                "http://example.org/voc#FacultyMember",
                "http://xmlns.com/foaf/0.1/Person");
        runQueryAndCompare(query, results);
    }

    @Test
    public void testOWLInverseOf() {
        String query = "PREFIX : <http://example.org/voc#>" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "SELECT  ?v\n" +
                "WHERE {\n" +
                "   ?z owl:inverseOf ?v .\n" +
                "}\n";

        ImmutableSet<String> results = ImmutableSet.of("http://example.org/voc#isTaughtBy",
                "http://example.org/voc#teaches");
        runQueryAndCompare(query, results);
    }

    private ImmutableSet<String> getExpectedClasses() {
        return ImmutableSet.of("http://example.org/voc#Professor",
                "http://example.org/voc#AssociateProfessor",
                "http://example.org/voc#AssistantProfessor",
                "http://example.org/voc#FullProfessor",
                "http://example.org/voc#Course",
                "http://example.org/voc#EducationalInstitution",
                "http://example.org/voc#ExternalTeacher",
                "http://example.org/voc#FacultyMember",
                "http://example.org/voc#UndergraduateStudent",
                "http://example.org/voc#GraduateStudent",
                "http://example.org/voc#PhDStudent",
                "http://example.org/voc#PostDoc",
                "http://example.org/voc#Researcher",
                "http://example.org/voc#Teacher",
                "http://example.org/voc#Student",
                "http://xmlns.com/foaf/0.1/Person");
    }
}
