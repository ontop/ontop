package it.unibz.inf.ontop.docker.oracle;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/***
 * Tests that the SPARQL ORDER BY statement is correctly translated to ORDER BY in SQL.
 */
public class OracleORDERBYTest extends AbstractVirtualModeTest {

    private static final Logger log = LoggerFactory.getLogger(OracleORDERBYTest.class);
    static final String owlFile = "/oracle/orderby/orderBy.owl";
    static final String obdaFile = "/oracle/orderby/orderBy.obda";
    static final String propertyFile = "/oracle/oracle.properties";

    private static OntopOWLReasoner REASONER;
    private static OntopOWLConnection CONNECTION;

    @BeforeClass
    public static void before() throws OWLOntologyCreationException {
        REASONER = createReasoner(owlFile, obdaFile, propertyFile);
        CONNECTION = REASONER.getConnection();
    }

    @Override
    protected OntopOWLStatement createStatement() throws OWLException {
        return CONNECTION.createStatement();
    }

    @AfterClass
    public static void after() throws OWLException {
        CONNECTION.close();
        REASONER.dispose();
    }

    private void runQueryAndCheckSQL(String query) throws OWLException{

        OntopOWLStatement st = createStatement();
        String sql = st.getExecutableQuery(query).toString();
        //boolean m = sql.matches("(?ms)(.*)ORDER BY country_name (.*)");
        boolean m = sql.matches("(?ms)(.*)ORDER BY (.*)");
        log.debug(sql);
        assertTrue(m);
    }


    @Test
    public void testOrderBy() throws Exception {

        String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> " +
                "SELECT ?x ?name " +
                "WHERE { ?x a :Country; :name ?name . } "
                + "ORDER BY ?name"
                ;

        runQueryAndCheckSQL(query);

        List<String> expectedUris = new ArrayList<>();
        expectedUris.add("http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-Argentina");
        expectedUris.add("http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-Australia");
        expectedUris.add("http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-Belgium");
        expectedUris.add("http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-Brazil");
        expectedUris.add("http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-Canada");
        expectedUris.add("http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-China");
        expectedUris.add("http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-Denmark");
        expectedUris.add("http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-Egypt");
        expectedUris.add("http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-France");
        expectedUris.add("http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-Germany");
        expectedUris.add("http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-India");
        expectedUris.add("http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-Israel");
        expectedUris.add("http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-Italy");
        expectedUris.add("http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-Japan");
        expectedUris.add("http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-Kuwait");
        expectedUris.add("http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-Malaysia");
        expectedUris.add("http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-Mexico");
        expectedUris.add("http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-Netherlands");
        expectedUris.add("http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-Nigeria");
        expectedUris.add("http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-Singapore");
        expectedUris.add("http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-Switzerland");
        expectedUris.add("http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-United%20Kingdom");
        expectedUris.add("http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-United%20States%20of%20America");
        expectedUris.add("http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-Zambia");
        expectedUris.add("http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-Zimbabwe");

        checkReturnedUris(expectedUris, query);
    }


    @Test
    public void testOrderByAndLimit() throws Exception {
        String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> " +
                "SELECT ?x ?name " +
                "WHERE { ?x a :Country; :name ?name . } "
                + "ORDER BY ?name "
                + "LIMIT 2 " ;

        runQueryAndCheckSQL(query);
        List<String> expectedUris = new ArrayList<>();
        expectedUris.add("http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-Argentina");
        expectedUris.add("http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-Australia");
        checkReturnedUris(expectedUris, query);
    }


}
