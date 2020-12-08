package tboxFact;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.BooleanOWLResultSet;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import it.unibz.inf.ontop.utils.OWLAPITestingTools;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Zhenzhen Gu   @branch tbox-fact-test
 */
public class TBoxFactTest {

    public static OntopOWLReasoner res;
    public static OntopOWLConnection ct;
    public static OntopOWLStatement st;
    public static TupleOWLResultSet rs;
    public static BooleanOWLResultSet bs;

    public static String owlfile = "/tbox-fact/university.ttl";
    public static String obdafile = "/tbox-fact/university.obda";
    public static String propertyfile = "/tbox-fact/university.properties";

    //large ontologies
/*    public static String owlfile = "/tbox-fact/schemaorg.owl";
    public static String obdafile = "/tbox-fact/schemaorg.obda";
    public static String propertyfile = "/tbox-fact/schemaorg.properties";*/



    @BeforeClass
    public static void init() throws OWLException, Exception {
        createTable();

        owlfile = TBoxFactTest.class.getResource(owlfile).toString();
        obdafile =  TBoxFactTest.class.getResource(obdafile).toString();
        propertyfile =  TBoxFactTest.class.getResource(propertyfile).toString();

        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(obdafile)
                .ontologyFile(owlfile)
                .propertyFile(propertyfile)
                .enableTestMode()
                .build();
        res = factory.createReasoner(config);
        ct = res.getConnection();
        st = ct.createStatement();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        res.close();
        ct.close();
        st.close();
    }


    public static void createTable() throws SQLException, IOException {
        // String driver = "org.h2.Driver";
        String url = "jdbc:h2:mem:university;";
        String username = "sa";
        String password = "";

        Connection connection = DriverManager.getConnection(url, username, password);
        OWLAPITestingTools.executeFromFile(connection, "src/test/resources/tbox-fact/dataset-university.sql");
        //OWLAPITestingTools.executeFromFile(connection, "src/test/resources/tbox-fact/dataset-schemaorg.sql");
    }

    @Test
    public void getSubClassOfRelation() throws OWLException {
        String query =
                "PREFIX : <http://example.org/voc#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "select ?x  {?x rdfs:subClassOf ?y} ";

        Set<List<String>> result = runSelectQuery(query);
        System.out.println(result);
    }

    @Test
    public void getSubClass() throws OWLException {
        String query =
                "PREFIX : <http://example.org/voc#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "select ?x  {?x rdfs:subClassOf :Professor} ";

        Set<List<String>> result = runSelectQuery(query);
        System.out.println(result);
    }

    @Test
    public void getSupClass() throws OWLException {
        String query =
                "PREFIX : <http://example.org/voc#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "select ?x  {:Student rdfs:subClassOf ?x} ";

        Set<List<String>> result = runSelectQuery(query);
        System.out.println(result);
    }

    @Test
    public void getOWLClass() throws OWLException {
        String query =
                "PREFIX : <http://example.org/voc#>\n" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                        "select ?x  {?x rdf:type owl:Class} ";

        Set<List<String>> result = runSelectQuery(query);
        System.out.println(result);
    }

    @Test
    public void checkOWLClass() throws OWLException {
        String query =
                "PREFIX : <http://example.org/voc#>\n" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                        "ASK  {:Professor rdf:type owl:Class} ";

        Set<List<String>> result = runSelectQuery(query);
        System.out.println(result);
    }

    @Test
    public void getRDFSClass() throws OWLException {
        String query =
                "PREFIX : <http://example.org/voc#>\n" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "select ?x  {?x rdf:type rdfs:Class} ";

        Set<List<String>> result = runSelectQuery(query);
        System.out.println(result);
    }

    @Test
    public void checkRDFSClass() throws OWLException {
        String query =
                "PREFIX : <http://example.org/voc#>\n" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "ASK  {:Professor rdf:type rdfs:Class} ";

        Set<List<String>> result = runSelectQuery(query);
        System.out.println(result);
    }

    @Test
    public void checkSubClassOfRelation() throws OWLException {
        String query =
                "PREFIX : <http://example.org/voc#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        " ASK  {:Student rdfs:subClassOf :Professor} ";

        boolean bl = runASKQuery(query);
        System.out.println(bl);
    }

    @Test
    public void getDomainRelation() throws OWLException {
        String query =
                "PREFIX : <http://example.org/voc#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "select ?x  {?x rdfs:domain ?y} ";

        Set<List<String>> result = runSelectQuery(query);
        System.out.println(result);
    }

    @Test
    public void getDomain() throws OWLException {
        String query =
                "PREFIX : <http://example.org/voc#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "select ?x  {:teaching rdfs:domain ?x} ";

        Set<List<String>> result = runSelectQuery(query);
        System.out.println(result);
    }

    @Test
    public void getDomainProperty() throws OWLException {
        String query =
                "PREFIX : <http://example.org/voc#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "select ?x  {?x rdfs:domain :Professor} ";

        Set<List<String>> result = runSelectQuery(query);
        System.out.println(result);
    }

    @Test
    public void checkDomainRelation() throws OWLException {
        String query =
                "PREFIX : <http://example.org/voc#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        " ASK  {:teaching rdfs:domain :Professor} ";

        boolean bl = runASKQuery(query);
        System.out.println(bl);
    }

    @Test
    public void getRangeRelation() throws OWLException {
        String query =
                "PREFIX : <http://example.org/voc#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "select ?x  {?x rdfs:range ?y} ";

        Set<List<String>> result = runSelectQuery(query);
        System.out.println(result);
    }

    @Test
    public void getRange() throws OWLException {
        String query =
                "PREFIX : <http://example.org/voc#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "select ?x  {:teaching rdfs:range ?x} ";

        Set<List<String>> result = runSelectQuery(query);
        System.out.println(result);
    }

    @Test
    public void getRangeProperty() throws OWLException {
        String query =
                "PREFIX : <http://example.org/voc#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "select ?x  {?x rdfs:range :Course} ";

        Set<List<String>> result = runSelectQuery(query);
        System.out.println(result);
    }

    @Test
    public void checkRangeRelation() throws OWLException {
        String query =
                "PREFIX : <http://example.org/voc#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        " ASK  {:teaching rdfs:domain :Course} ";

        boolean bl = runASKQuery(query);
        System.out.println(bl);
    }


    public Set<List<String>> runSelectQuery(String query) throws OWLException {
        Set<List<String>> result = new HashSet<List<String>>();
        TupleOWLResultSet rs = st.executeSelectQuery(query);
        while(rs.hasNext()){
            OWLBindingSet os = rs.next();
            List<String> bind = new ArrayList<String>();
            List<String> vars = os.getBindingNames();
            for(String var : vars){
                bind.add(os.getOWLIndividual(var).toString());
            }
            result.add(bind);

        }

        return result;

    }

    public boolean runASKQuery(String query) throws OWLException {
        BooleanOWLResultSet rs=st.executeAskQuery(query);
        boolean bl=rs.getValue();
        return bl;
    }
}
