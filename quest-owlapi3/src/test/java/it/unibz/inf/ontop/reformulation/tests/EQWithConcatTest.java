package it.unibz.inf.ontop.reformulation.tests;


import it.unibz.inf.ontop.io.ModelIOManager;
import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.owlrefplatform.core.QuestPreferences;
import it.unibz.inf.ontop.owlrefplatform.owlapi.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EQWithConcatTest {

    /**
     * Test method for
     * {@link it.unibz.inf.ontop.owlrefplatform.core.basicoperations.EQNormalizer#enforceEqualities)}
     *
     * Check behaviour for equality that cannot be resolved at the level of subquery
     */
    private OBDADataFactory fac;
    private Connection conn;

    private OBDAModel obdaModel;
    private OWLOntology ontology;

    final String owlfile = "src/test/resources/test/bind/sparqlBind.owl";
    final String obdafile = "src/test/resources/test/bind/sparqlConcat.obda";

    @Before
    public void setUp() throws Exception {

        String url = "jdbc:h2:mem:questjunitdb";
        String username = "sa";
        String password = "";

        fac = OBDADataFactoryImpl.getInstance();

        conn = DriverManager.getConnection(url, username, password);


        Statement st = conn.createStatement();

        FileReader reader = new FileReader("src/test/resources/test/bind/sparqlBindWithFns-create-h2.sql");
        BufferedReader in = new BufferedReader(reader);
        StringBuilder bf = new StringBuilder();
        String line = in.readLine();
        while (line != null) {
            bf.append(line);
            line = in.readLine();
        }
        in.close();

        st.executeUpdate(bf.toString());
        conn.commit();

        // Loading the OWL file
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));

        // Loading the OBDA data
        obdaModel = fac.getOBDAModel();
        ModelIOManager ioManager = new ModelIOManager(obdaModel);
        ioManager.load(obdafile);
    }

    @After
    public void tearDown() throws Exception {

        dropTables();
        conn.close();

    }

    private void dropTables() throws SQLException, IOException {

        Statement st = conn.createStatement();

        FileReader reader = new FileReader("src/test/resources/test/bind/sparqlBindWithFns-drop-h2.sql");
        BufferedReader in = new BufferedReader(reader);
        StringBuilder bf = new StringBuilder();
        String line = in.readLine();
        while (line != null) {
            bf.append(line);
            line = in.readLine();
        }
        in.close();

        st.executeUpdate(bf.toString());
        st.close();
        conn.commit();
    }


    @Test
    public void testuuid() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");


        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "PREFIX :   <http://example.org/book>"
                + "SELECT  ?w WHERE \n"
                + "{  ?x :uuid ?w .\n"
                + "FILTER (str(?w) = 'adres-1')" +
                "}"
                +" ORDER BY ?w";


        runTests(p, queryBind);
    }

    private void runTests(QuestPreferences p, String query) throws Exception {

        // Creating a new instance of the reasoner

        QuestOWLFactory factory = new QuestOWLFactory();
        QuestOWLConfiguration config = QuestOWLConfiguration.builder().obdaModel(obdaModel).preferences(p).build();
        QuestOWL reasoner = factory.createReasoner(ontology, config);

        // Now we are ready for querying
        QuestOWLConnection conn = reasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();


        int i = 0;

        try {
            QuestOWLResultSet rs = st.executeTuple(query);
            String result = "";
            while (rs.nextRow()) {
                OWLObject ind1 = rs.getOWLObject("w");

                result = ind1.toString();
                System.out.println(ind1);
                i++;
            }
            assertTrue(i == 1);
            assertEquals("adres-1", result);

        } catch (Exception e) {
            throw e;
        } finally {
            conn.close();
            reasoner.dispose();
        }
    }


}
