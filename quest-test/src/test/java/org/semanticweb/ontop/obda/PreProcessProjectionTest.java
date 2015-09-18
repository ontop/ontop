package org.semanticweb.ontop.obda;



import org.junit.Before;
import org.junit.Test;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.owlapi3.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PreProcessProjectionTest {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    private OWLOntology ontology;

    final String owlFile = "src/test/resources/northwind/northwind.owl";
    final String obdaFile = "src/test/resources/mappingStars.obda";

    @Before
    public void setUp() throws Exception {

        // Loading the OWL file
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        ontology = manager.loadOntologyFromOntologyDocument((new File(owlFile)));
    }

    private int runTests(String query) throws Exception {
        return runTests(new Properties(), query);
    }

    private int runTests(Properties p, String query) throws Exception {

        // Creating a new instance of the reasoner
        QuestOWLFactory factory = new QuestOWLFactory(new File(obdaFile), new QuestPreferences(p));

        QuestOWL reasoner = factory.createReasoner(ontology, new SimpleConfiguration());

        // Now we are ready for querying
        QuestOWLConnection conn = reasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();

        int results = 0;

        try {
           results= executeQueryAssertResults(query, st);

        } catch (Exception e) {
            st.close();
            e.printStackTrace();
            assertTrue(false);


        } finally {

            conn.close();
            reasoner.dispose();
        }
        return results;

    }

    private int executeQueryAssertResults(String query, QuestOWLStatement st) throws Exception {
        QuestOWLResultSet rs = st.executeTuple(query);
        int count = 0;
        while (rs.nextRow()) {
            count++;
//            for (int i = 1; i <= rs.getColumnCount(); i++) {
//
//                log.debug(rs.getSignature().get(i-1) + "=" + rs.getOWLObject(i));
//
//            }

        }
        rs.close();

        return count;

    }



    @Test
    public void testSimpleQuery() throws Exception {
        String query = "PREFIX : <http://www.semanticweb.org/vidar/ontologies/2014/11/northwind-handmade#>" +
                " select * {?x a :Category}";
        int nResults = runTests(query);
        assertEquals(8, nResults);
    }

    @Test
    public void testSimpleQueryJoin() throws Exception {


        QuestPreferences p = new QuestPreferences();
        String query = "PREFIX : <http://www.semanticweb.org/vidar/ontologies/2014/11/northwind-handmade#>" +
                " select * {?x a :Customer}";
        int nResults = runTests(query);
        assertEquals(2155, nResults);
    }

    @Test
    public void testSimpleQueryAlias() throws Exception {
        String query = "PREFIX : <http://www.semanticweb.org/vidar/ontologies/2014/11/northwind-handmade#>" +
                " select * {?x :locationRegion ?y}";
        int nResults = runTests(query);
        assertEquals(53, nResults);
    }

    @Test
    public void testSimpleQueryView() throws Exception {
        String query = "PREFIX : <http://www.semanticweb.org/vidar/ontologies/2014/11/northwind-handmade#>" +
                " select * {?x :orderDetailDiscount ?y}";
        int nResults = runTests(query);
        assertEquals(2155, nResults);
    }

    @Test
    public void testComplexQueryView() throws Exception {
        String query = "PREFIX : <http://www.semanticweb.org/vidar/ontologies/2014/11/northwind-handmade#>" +
                " select * {?x a :Location}";
        int nResults = runTests(query);
        assertEquals(91, nResults);
    }

    @Test
    public void testjoinWithSameName() throws Exception {
        String query = "PREFIX : <http://www.semanticweb.org/vidar/ontologies/2014/11/northwind-handmade#>" +
                " select * {?x a :OrderDetail}";
        int nResults = runTests(query);
        assertEquals(4310, nResults);
    }

    @Test
    public void testjoinWithAliasInSubQuery() throws Exception {
        String query = "PREFIX : <http://www.semanticweb.org/vidar/ontologies/2014/11/northwind-handmade#>" +
                " select * {?x :locationAddress ?y}";
        int nResults = runTests(query);
        assertEquals(19, nResults);
    }




}

