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

public class DistinctProjectionOutTest extends AbstractRDF4JTest {

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
    public void testDistinctLargeBGP() {
        long before = System.currentTimeMillis();
        int count = runQueryAndCount("prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                " select distinct\n" +
                " ?P2_Label7\n" +
                " ?P10\n" +
                " ?P1_Title1\n" +
                " ?P02\n" +
                " ?P0_Title3\n" +
                " ?P35\n" +
                " ?P26\n" +
                " ?A5_Title12\n" +
                " ?A511\n" +
                " ?A4_Level13\n" +
                " ?A5_Level14\n" +
                " ?P3_Label8\n" +
                " ?A4_Title10\n" +
                " ?A49\n" +
                " ?P0_Code15\n" +
                " ?P1_Level16\n" +
                " ?P2_Level17\n" +
                " ?P3_Level18\n" +
                " ?P1_ID20\n" +
                " ?P2_ID21\n" +
                "where\n" +
                " { {\n" +
                " {\n" +
                "?P10 <http://example.org/onto1#propX> ?P1_Title1 .\n" +
                "?P10 <http://example.org/voc/6db1816d-a2f5-4d88-9536-9e0a86529d0c#X__X> ?P02 .\n" +
                "?P02 <http://example.org/onto1#propX> ?P0_Title3 .\n" +
                "?P02 <http://example.org/onto1#b> ?P0_Level4 .\n" +
                "?P35 <http://example.org/voc/6db1816d-a2f5-4d88-9536-9e0a86529d0c#X__X> ?P26 .\n" +
                "?P26 <http://example.org/onto1#propX> ?P2_Label7 .\n" +
                "?P26 <http://example.org/voc/6db1816d-a2f5-4d88-9536-9e0a86529d0c#X__X> ?P10 .\n" +
                "?P35 <http://example.org/onto1#propX> ?P3_Label8 .\n" +
                "?A49 <http://example.org/voc/56ed4167-aea1-4cf4-b84d-2d55f962ea23#P> ?P35 .\n" +
                "?A49 <http://example.org/onto4#t> ?A4_Title10 .\n" +
                "?A511 <http://example.org/voc/443edfad-f6a1-43cc-8664-1ff1273cfa98#Y> ?A49 .\n" +
                "?A511 <http://example.org/onto4#t> ?A5_Title12 .\n" +
                "?A49 <http://example.org/onto4#x> ?A4_Level13 .\n" +
                "?A511 <http://example.org/onto4#x> ?A5_Level14 .\n" +
                "?P02 <http://example.org/onto1#a> ?P0_Code15 .\n" +
                "?P10 <http://example.org/onto1#b> ?P1_Level16 .\n" +
                "?P26 <http://example.org/onto1#b> ?P2_Level17 .\n" +
                "?P35 <http://example.org/onto1#b> ?P3_Level18 .\n" +
                "?P02 <http://example.org/onto1#n> ?P0_ID19 .\n" +
                "?P10 <http://example.org/onto1#n> ?P1_ID20 .\n" +
                "?P26 <http://example.org/onto1#n> ?P2_ID21 .\n" +
                "?P35 <http://example.org/onto1#n> ?P3_ID22 .\n" +
                "?A49 <http://example.org/onto4#id> ?A4_ID23 .\n" +
                "?A511 <http://example.org/onto4#id> ?A5_ID24 .\n" +
                "?P10 rdf:type <http://example.org/onto2/X> .\n" +
                "?P02 rdf:type <http://example.org/onto2/X> .\n" +
                "?P35 rdf:type <http://example.org/onto2/X> .\n" +
                "?P26 rdf:type <http://example.org/onto2/X> .\n" +
                "?A49 rdf:type <http://example.org/onto3#Y> .\n" +
                "?A511 rdf:type <http://example.org/onto3#Y> .\n" +
                "FILTER (lcase(str(?P0_Level4)) = \"aa\" ) .\n" +
                "FILTER (lcase(str(?A4_Level13)) = \"bb\" ) .\n" +
                "FILTER (lcase(str(?A5_Level14)) = \"cc\" ) .\n" +
                "FILTER (lcase(str(?P1_Level16)) = \"dd\" ) .\n" +
                "FILTER (lcase(str(?P2_Level17)) = \"ee\" ) .\n" +
                "FILTER (lcase(str(?P3_Level18)) = \"ff\" ) }\n" +
                "\n" +
                "}\n" +
                "  }");
        assertEquals(0, count);
        assertTrue("The reformulation is excessively slow", System.currentTimeMillis() - before < 10000);
    }

    @Test
    public void testBGPDistinctConcat() {
        int count = runQueryAndCount("prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                " select distinct\n" +
                " ?P10\n" +
                " ?P02\n" +
                " (CONCAT(?P1_Title1, ?P0_Title3) AS ?v)\n" +
                "where\n" +
                " {\n" +
                "?P10 <http://example.org/onto1#propX> ?P1_Title1 .\n" +
                "?P02 <http://example.org/onto1#propX> ?P0_Title3 .\n" +
                "\n" +
                "}");
        assertEquals(0, count);
    }

}
