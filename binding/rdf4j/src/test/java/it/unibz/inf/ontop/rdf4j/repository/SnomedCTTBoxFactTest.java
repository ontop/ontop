package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

public class SnomedCTTBoxFactTest extends AbstractRDF4JTest {

    private static final String CREATE_DB_FILE = "/tbox-facts/university.sql";
    private static final String OBDA_FILE = "/tbox-facts/university.obda";
    private static final String OWL_FILE = "/tbox-facts/SCTO.owl";
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
        String query = "PREFIX obo: <http://purl.obolibrary.org/obo/>" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "SELECT  ?v\n" +
                "WHERE {\n" +
                "?v rdfs:subClassOf obo:BFO_0000004 .\n" +
                "}\n";

        ImmutableSet<String> results = ImmutableSet.of("http://purl.obolibrary.org/obo/BFO_0000004",
                "http://purl.obolibrary.org/obo/BFO_0000040", "http://purl.obolibrary.org/obo/BFO_0000141",
                "http://purl.obolibrary.org/obo/OGMS_0000045", "http://purl.obolibrary.org/obo/OGMS_0000087",
                "http://purl.obolibrary.org/obo/OGMS_0000084", "http://purl.obolibrary.org/obo/OGMS_0000078",
                "http://purl.obolibrary.org/obo/OGMS_0000079", "http://purl.obolibrary.org/obo/OGMS_0000089",
                "http://purl.obolibrary.org/obo/BFO_0000030", "http://purl.obolibrary.org/obo/BFO_0000027",
                "http://purl.obolibrary.org/obo/OGMS_0000077", "http://purl.obolibrary.org/obo/BFO_0000024",
                "http://purl.obolibrary.org/obo/BFO_0000006", "http://purl.obolibrary.org/obo/BFO_0000029",
                "http://purl.obolibrary.org/obo/BFO_0000140", "http://purl.obolibrary.org/obo/OGMS_0000102",
                "http://purl.obolibrary.org/obo/OGMS_0000046", "http://purl.obolibrary.org/obo/OGMS_0000047",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_123038009",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_105590001",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_260787004",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_410607006",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_373873005",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_123037004",
                "http://purl.obolibrary.org/obo/BFO_0000018", "http://purl.obolibrary.org/obo/BFO_0000009",
                "http://purl.obolibrary.org/obo/BFO_0000026", "http://purl.obolibrary.org/obo/BFO_0000028",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_223496003",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_276339004",
                "http://purl.obolibrary.org/obo/BFO_0000147", "http://purl.obolibrary.org/obo/BFO_0000146",
                "http://purl.obolibrary.org/obo/BFO_0000142", "http://purl.obolibrary.org/obo/OGMS_0000050",
                "http://purl.obolibrary.org/obo/OGMS_0000051", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_49062001",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_442083009", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_118956008",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_280115004", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_91723000",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_49755003");
        runQueryAndCompare(query, results);
    }

    @Test
    public void testRDFSClasses() {
        String query = "SELECT  *\n" +
                "WHERE {\n" +
                "   ?v a rdfs:Class .\n" +
                "}\n";

        runQueryAndCompare(query, getExpectedClasses());
    }

    @Test
    public void testOWLClasses() {
        String query = "SELECT  *\n" +
                "WHERE {\n" +
                "   ?v a owl:Class .\n" +
                "}\n";

        runQueryAndCompare(query, getExpectedClasses());
    }

    @Test
    public void testRDFSDomainRelation() {
        String query = "SELECT  ?v\n" +
                "WHERE {\n" +
                "   ?v rdfs:domain ?z .\n" +
                "}\n";

        ImmutableSet<String> results = ImmutableSet.of("http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000031",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000030",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000011",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000033",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000010",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_000000003",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000013",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000035",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000004",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000015",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000014",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000036",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000006",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000017",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000005",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000016",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000008",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000019",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000018",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000009",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000020",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000022",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000021",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000024",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000012",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000023",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_0000006");
        runQueryAndCompare(query, results);
    }

    @Test
    public void testRDFSDomain() {
        String query = "PREFIX obo: <http://purl.obolibrary.org/obo/>" +
                "SELECT  ?v\n" +
                "WHERE {\n" +
                "   ?z rdfs:domain ?v .\n" +
                "}\n";

        ImmutableSet<String> results = ImmutableSet.of("http://purl.obolibrary.org/obo/ogms.owl#SCTO_138875005",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000000",
                "http://purl.obolibrary.org/obo/IAO_0000030", "http://purl.obolibrary.org/obo/BFO_0000031",
                "http://purl.obolibrary.org/obo/BFO_0000002", "http://purl.obolibrary.org/obo/BFO_0000001",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000001",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000002");
        runQueryAndCompare(query, results);
    }

    @Test
    public void testRDFSRangeRelation() {
        String query = "SELECT  ?v\n" +
                "WHERE {\n" +
                "   ?v rdfs:range ?z .\n" +
                "}\n";

        ImmutableSet<String> results = ImmutableSet.of("http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000020",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000022",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000021",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000024",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000012",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000023",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_0000006");
        runQueryAndCompare(query, results);
    }

    @Test
    public void testRDFSRange() {
        String query = "PREFIX obo: <http://purl.obolibrary.org/obo/>" +
                "SELECT  ?v\n" +
                "WHERE {\n" +
                "   ?z rdfs:range ?v .\n" +
                "}\n";

        ImmutableSet<String> results = ImmutableSet.of("http://purl.obolibrary.org/obo/ogms.owl#SCTO_138875005",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000000",
                "http://purl.obolibrary.org/obo/IAO_0000030", "http://purl.obolibrary.org/obo/BFO_0000031",
                "http://purl.obolibrary.org/obo/BFO_0000002", "http://purl.obolibrary.org/obo/BFO_0000001",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000001",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000002",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_106237007");
        runQueryAndCompare(query, results);
    }

    @Test
    public void testOWLInverseOf() {
        String query = "PREFIX obo: <http://purl.obolibrary.org/obo/>" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "SELECT  ?v\n" +
                "WHERE {\n" +
                "   ?z owl:inverseOf ?v .\n" +
                "}\n";

        ImmutableSet<String> results = ImmutableSet.of("http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000023",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000024",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000021",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_0000006",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000020",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000012");
        runQueryAndCompare(query, results);
    }

    private ImmutableSet<String> getExpectedClasses() {
        return ImmutableSet.of("http://purl.obolibrary.org/obo/BFO_0000001", "http://purl.obolibrary.org/obo/BFO_0000002",
                "http://purl.obolibrary.org/obo/BFO_0000031", "http://purl.obolibrary.org/obo/IAO_0000030",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000000", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000001",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_138875005", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_106237007",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_246061005", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_362981000",
                "http://purl.obolibrary.org/obo/BFO_0000003", "http://purl.obolibrary.org/obo/BFO_0000015",
                "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_129264002", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_129264002_2",
                "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_360220002", "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_106229004",
                "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_363555003", "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_129325002",
                "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_257867005", "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_425362007",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_410662002", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_246456000",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_272099008", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_277302009",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_277367001", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_363675004",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_309825002", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_272424004",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_182353008", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_24028007",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_51440002", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_00000002",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_246513007", "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_129284003",
                "http://purl.obolibrary.org/obo/IAO_0000027", "http://purl.obolibrary.org/obo/IAO_0000102",
                "http://purl.obolibrary.org/obo/IAO_0000078", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_260507000",
                "http://purl.obolibrary.org/obo/BFO_0000004", "http://purl.obolibrary.org/obo/BFO_0000040",
                "http://purl.obolibrary.org/obo/BFO_0000024", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_123037004",
                "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_257544000", "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_308910008",
                "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_129300006", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_272103003",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_272125009", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_309795001",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_370131001", "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_422096002",
                "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_288529006", "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_410510008",
                "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_410511007", "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_410513005",
                "http://purl.obolibrary.org/obo/IAO_0000409", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_281296001",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_405815000", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_424226004",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_425391005", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_363702006",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_260870009", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_71388002",
                "https://bioportal.bioontology.org/ontologies/SCTO#DTO:0001943", "https://bioportal.bioontology.org/ontologies/SCTO#DTO:0001944",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_260245000", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_272126005",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_272127001", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_255231005",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_363704007", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_405814001",
                "http://purl.obolibrary.org/obo/BFO_0000030", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_260787004",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_49062001", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_363698007",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_105590001", "http://purl.obolibrary.org/obo/BFO_0000141",
                "http://purl.obolibrary.org/obo/BFO_0000029", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_276339004",
                "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_129371009", "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_307120002",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_260686004", "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_21703008",
                "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_276214006", "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_106235004",
                "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_288532009", "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_410536001",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_118956008", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_49755003",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_123038009", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_272396007",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_281586009", "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_419988009",
                "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_419385000", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_370132008",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_7771000", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_419066007",
                "http://www.geneontology.org/formats/oboInOwl#Synonym", "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_410514004",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_264752007", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_258349007",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_272141005", "http://purl.obolibrary.org/obo/IAO_0000033",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_363701004", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_118171006",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_127489000", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_103379005",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_47429007", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_42752001",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_303102005", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_424876005",
                "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_430975009", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_399455000",
                "http://purl.obolibrary.org/obo/BFO_0000035", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_272379006",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_257958009", "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_260299005",
                "http://purl.obolibrary.org/obo/BFO_0000020", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_78621006",
                "http://www.geneontology.org/formats/oboInOwl#Definition", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_246454002",
                "http://purl.obolibrary.org/obo/BFO_0000008", "http://purl.obolibrary.org/obo/BFO_0000038",
                "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_7389001", "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_422133006",
                "http://www.geneontology.org/formats/oboInOwl#Subset", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_246112005",
                "http://purl.obolibrary.org/obo/OGMS_0000104", "http://purl.obolibrary.org/obo/OGMS_0000105",
                "http://purl.obolibrary.org/obo/OGMS_0000106", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_261424001",
                "http://purl.obolibrary.org/obo/OGMS_0000096", "http://purl.obolibrary.org/obo/OGMS_0000097",
                "http://purl.obolibrary.org/obo/OGMS_0000100", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_66459002",
                "http://purl.obolibrary.org/obo/OGMS_0000101", "http://purl.obolibrary.org/obo/OGMS_0000045",
                "http://purl.obolibrary.org/obo/OGMS_0000102", "http://purl.obolibrary.org/obo/OGMS_0000103",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_370133003", "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_297289008",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_246514001", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_405813007",
                "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_106239005", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_442083009",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_280115004", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_405816004",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_363700003", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_308489006",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_123005000", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_263680009",
                "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_272512009", "http://purl.obolibrary.org/obo/BFO_0000019",
                "http://purl.obolibrary.org/obo/OGMS_0000039", "http://purl.obolibrary.org/obo/OGMS_0000040",
                "http://purl.obolibrary.org/obo/BFO_0000017", "http://purl.obolibrary.org/obo/BFO_0000016",
                "http://purl.obolibrary.org/obo/OGMS_0000032", "http://purl.obolibrary.org/obo/OGMS_0000038",
                "http://purl.obolibrary.org/obo/OGMS_0000014", "http://purl.obolibrary.org/obo/OGMS_0000031",
                "http://purl.obolibrary.org/obo/OGMS_0000034", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_246075003",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_91723000", "http://purl.obolibrary.org/obo/OGMS_0000035",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_272741003", "http://purl.obolibrary.org/obo/OGMS_0000037",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_118170007", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_118169006",
                "http://www.geneontology.org/formats/oboInOwl#DbXref", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_363705008",
                "http://purl.obolibrary.org/obo/OGMS_0000055", "http://purl.obolibrary.org/obo/OGMS_0000047",
                "http://purl.obolibrary.org/obo/OGMS_0000050", "http://purl.obolibrary.org/obo/OGMS_0000051",
                "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_36692007", "http://purl.obolibrary.org/obo/OGMS_0000046",
                "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_129407005", "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_410820007",
                "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_129347002", "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_257937003",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_263502005", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_255234002",
                "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_129379006", "http://purl.obolibrary.org/obo/OGMS_0000020",
                "http://purl.obolibrary.org/obo/OGMS_0000022", "http://purl.obolibrary.org/obo/OGMS_0000016",
                "http://purl.obolibrary.org/obo/OGMS_0000017", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_370134009",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_410675002", "http://purl.obolibrary.org/obo/OGMS_0000018",
                "http://purl.obolibrary.org/obo/OGMS_0000019", "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_394731006",
                "http://purl.obolibrary.org/obo/OGMS_0000015", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_288526004",
                "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_272099008", "http://purl.obolibrary.org/obo/OGMS_0000030",
                "http://purl.obolibrary.org/obo/OGMS_0000033", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_246093002",
                "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_288531002", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_424244007",
                "http://purl.obolibrary.org/obo/OGMS_0000023", "http://purl.obolibrary.org/obo/OGMS_0000027",
                "http://purl.obolibrary.org/obo/OGMS_0000028", "http://purl.obolibrary.org/obo/OGMS_0000024",
                "http://purl.obolibrary.org/obo/OGMS_0000029", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_116686009",
                "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_413529001", "http://purl.obolibrary.org/obo/BFO_0000182",
                "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_129348007", "http://purl.obolibrary.org/obo/OGMS_0000025",
                "http://purl.obolibrary.org/obo/OGMS_0000026", "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_261665006",
                "http://purl.obolibrary.org/obo/OGMS_0000060", "http://purl.obolibrary.org/obo/OGMS_0000061",
                "http://purl.obolibrary.org/obo/OGMS_0000080", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_363710007",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_363699004", "http://purl.obolibrary.org/obo/BFO_0000006",
                "http://purl.obolibrary.org/obo/BFO_0000018", "http://purl.obolibrary.org/obo/OGMS_0000085",
                "http://purl.obolibrary.org/obo/OGMS_0000086", "http://purl.obolibrary.org/obo/OGMS_0000087",
                "http://purl.obolibrary.org/obo/OGMS_0000088", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_254291000",
                "http://purl.obolibrary.org/obo/OGMS_0000081", "http://purl.obolibrary.org/obo/OGMS_0000082",
                "http://purl.obolibrary.org/obo/OGMS_0000083", "http://purl.obolibrary.org/obo/OGMS_0000084",
                "http://purl.obolibrary.org/obo/OGMS_0000078", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_288524001",
                "http://purl.obolibrary.org/obo/OGMS_0000079", "http://purl.obolibrary.org/obo/BFO_0000011",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_363713009", "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_698078002",
                "http://www.geneontology.org/formats/oboInOwl#SynonymType", "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_360032005",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_307142001", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_282032007",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_284009009", "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_415240008",
                "http://purl.obolibrary.org/obo/BFO_0000009", "http://purl.obolibrary.org/obo/OGMS_0000090",
                "http://purl.obolibrary.org/obo/OGMS_0000091", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_105904009",
                "http://purl.obolibrary.org/obo/OGMS_0000098", "http://purl.obolibrary.org/obo/OGMS_0000099",
                "http://purl.obolibrary.org/obo/OGMS_0000092", "http://purl.obolibrary.org/obo/OGMS_0000093",
                "http://purl.obolibrary.org/obo/OGMS_0000063", "http://purl.obolibrary.org/obo/OGMS_0000094",
                "http://purl.obolibrary.org/obo/OGMS_0000095", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_424361007",
                "http://purl.obolibrary.org/obo/OGMS_0000089", "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_703763000",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_370135005", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_370129005",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_363709002", "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_129380009",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_410607006", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_118168003",
                "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_414753003", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_419891008",
                "http://purl.obolibrary.org/obo/OGMS_0000064", "http://purl.obolibrary.org/obo/OGMS_0000065",
                "http://purl.obolibrary.org/obo/OGMS_0000066", "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_115670007",
                "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_419652001", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_411116001",
                "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_278444000", "http://purl.obolibrary.org/obo/OGMS_0000056",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_370130000", "http://purl.obolibrary.org/obo/OGMS_0000057",
                "http://purl.obolibrary.org/obo/OGMS_0000059", "http://purl.obolibrary.org/obo/BFO_0000034",
                "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_410512000", "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_115268005",
                "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_360038009", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_363703001",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_116676008", "http://purl.obolibrary.org/obo/BFO_0000026",
                "http://purl.obolibrary.org/obo/BFO_0000140", "http://purl.obolibrary.org/obo/BFO_0000147",
                "http://purl.obolibrary.org/obo/BFO_0000027", "http://purl.obolibrary.org/obo/BFO_0000148",
                "http://purl.obolibrary.org/obo/BFO_0000028", "http://purl.obolibrary.org/obo/OBI_0000011",
                "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_123035007", "http://purl.obolibrary.org/obo/OGMS_0000074",
                "http://purl.obolibrary.org/obo/OGMS_0000077", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_223496003",
                "http://purl.obolibrary.org/obo/OGMS_0000073", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_373873005",
                "http://purl.obolibrary.org/obo/ogms.owl#SCTO_363787002", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_363714003",
                "http://purl.obolibrary.org/obo/BFO_0000023", "http://purl.obolibrary.org/obo/BFO_0000144",
                "http://purl.obolibrary.org/obo/BFO_0000145", "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_385660001",
                "http://purl.obolibrary.org/obo/BFO_0000146", "http://purl.obolibrary.org/obo/IAO_0000225",
                "https://bioportal.bioontology.org/ontologies/SCTO#SCTO_118598001", "http://purl.obolibrary.org/obo/ogms.owl#SCTO_418775008",
                "http://purl.obolibrary.org/obo/BFO_0000142"
        );
    }
}
