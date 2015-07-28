package org.semanticweb.ontop.obda;



import org.junit.Before;
import org.junit.Test;
import org.semanticweb.ontop.io.ModelIOManager;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
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

public class ViewGenerationParserTest {
    private OBDADataFactory fac;

    Logger log = LoggerFactory.getLogger(this.getClass());
    private OBDAModel obdaModel;
    private OWLOntology ontology;

    final String owlFile = "src/test/resources/northwind/northwind.owl";
    final String obdaFile = "src/test/resources/northwind/northwindComplex.obda";

    @Before
    public void setUp() throws Exception {

        fac = OBDADataFactoryImpl.getInstance();

        // Loading the OWL file
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        ontology = manager.loadOntologyFromOntologyDocument((new File(owlFile)));

        // Loading the OBDA data
        obdaModel = fac.getOBDAModel();
        ModelIOManager ioManager = new ModelIOManager(obdaModel);
        ioManager.load(obdaFile);


    }

    private int runTests(Properties p, String query) throws Exception {

        // Creating a new instance of the reasoner
        QuestOWLFactory factory = new QuestOWLFactory();
        factory.setOBDAController(obdaModel);

        factory.setPreferenceHolder(p);

        QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

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
            for (int i = 1; i <= rs.getColumnCount(); i++) {
                System.out.print(rs.getSignature().get(i-1));
                System.out.print("=" + rs.getOWLObject(i));
                System.out.print(" ");
            }
            System.out.println();
        }
        rs.close();

        return count;

    }







    @Test
    public void testComplexFunction() throws Exception {


        QuestPreferences p = new QuestPreferences();
        String query = "PREFIX : <http://www.semanticweb.org/vidar/ontologies/2014/11/northwind-handmade#>" +
                " select * {?x a :Category}";
        int nResults = runTests(p, query);
        assertEquals(8, nResults);
    }

    @Test
    public void testComplexFunction2() throws Exception {


        QuestPreferences p = new QuestPreferences();
        String query = "PREFIX : <http://www.semanticweb.org/vidar/ontologies/2014/11/northwind-handmade#>" +
                " select * {?x a :Customer}";
        int nResults = runTests(p, query);
        assertEquals(93, nResults);
    }

    @Test
    public void testSimpleFunction() throws Exception {


        QuestPreferences p = new QuestPreferences();
        String query = "PREFIX : <http://www.semanticweb.org/vidar/ontologies/2014/11/northwind-handmade#>" +
                " select * {?x :locationRegion ?y}";
        int nResults = runTests(p, query);
        assertEquals(4, nResults);
    }

    @Test
    public void testStar() throws Exception {


        QuestPreferences p = new QuestPreferences();
        String query = "PREFIX : <http://www.semanticweb.org/vidar/ontologies/2014/11/northwind-handmade#>" +
                " select * {?x :orderDetailDiscount ?y}";
        int nResults = runTests(p, query);
        assertEquals(830, nResults);
    }




}

