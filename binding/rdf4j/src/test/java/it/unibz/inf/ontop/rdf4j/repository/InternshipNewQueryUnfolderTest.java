package it.unibz.inf.ontop.rdf4j.repository;

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
                "WHERE \n" +
                "{\n" +
                "  ?s a schema:Hotel .\n" +
                "  ?s ?p ?o .\n" +
                "}";
        int count = runQueryAndCount(sparql);
        assertEquals(42, count);
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
        assertEquals(50, count);
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
                        "} \n" +
                "LIMIT 10";
        int count = runQueryAndCount(sparql);
        assertEquals(2, count);
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
        assertEquals(5, count);
    }

    //http://destination.example.org/data/weather/observation/{}
    @Test
    public void IRIConstantMatchJustOneTemplate() {
        String sparql =
                "SELECT *\n" +
                        "WHERE {\n" +
                        "    <http://destination.example.org/data/weather/observation/201539> ?p ?o .\n" +
                        "}\n";
        int count = runQueryAndCount(sparql);
        assertEquals(5, count);
    }

    /*@Test
    public void IRIConstantTakenFromDBColumn() {
        String sparql = "";

        int count = runQueryAndCount(sparql);
        assertEquals(5, count);
    }

    @Test
    public void IRIConstantConstructedWithSQL() {
        String sparql = "";
        int count = runQueryAndCount(sparql);
        assertEquals(5, count);
    }

    @Test
    public void IRIConstant() {
        String sparql = "";
        int count = runQueryAndCount(sparql);
        assertEquals(5, count);
    }*/

}
