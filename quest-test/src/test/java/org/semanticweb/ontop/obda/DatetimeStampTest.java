package org.semanticweb.ontop.obda;


import org.junit.Before;
import org.junit.Test;
import org.semanticweb.ontop.io.QueryIOManager;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.OBDADataSource;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.owlapi3.*;
import org.semanticweb.ontop.owlrefplatform.questdb.R2RMLQuestPreferences;
import org.semanticweb.ontop.querymanager.QueryController;
import org.semanticweb.ontop.querymanager.QueryControllerGroup;
import org.semanticweb.ontop.querymanager.QueryControllerQuery;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.util.Properties;

import static org.junit.Assert.assertFalse;

public class DatetimeStampTest {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    private OWLOntology ontology;

    final String owlFile = "src/test/resources/northwind/northwind-dmo.owl";
    final String r2rmlFile = "src/test/resources/northwind/mapping-northwind-dmo.ttl";
    final String obdaFile = "src/test/resources/northwind/mapping-northwind-dmo.obda";

    @Before
    public void setUp() throws Exception {
        // Loading the OWL file
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        ontology = manager.loadOntologyFromOntologyDocument((new File(owlFile)));
    }

    private void runTests(QuestPreferences preferences, String filename) throws Exception {

        // Creating a new instance of the reasoner
        QuestOWLFactory factory = new QuestOWLFactory(new File(filename), preferences);

        QuestOWL reasoner = factory.createReasoner(ontology, new SimpleConfiguration());

        // Now we are ready for querying
        QuestOWLConnection conn = reasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();


        QueryController qc = new QueryController();
        QueryIOManager qman = new QueryIOManager(qc);
        qman.load("src/test/resources/northwind/northwind.q");

        for (QueryControllerGroup group : qc.getGroups()) {
            for (QueryControllerQuery query : group.getQueries()) {

                log.debug("Executing query: {}", query.getID());
                log.debug("Query: \n{}", query.getQuery());

                long start = System.nanoTime();
                QuestOWLResultSet res = st.executeTuple(query.getQuery());
                long end = System.nanoTime();

                double time = (end - start) / 1000;

                int count = 0;
                while (res.nextRow()) {
                    count += 1;

//                    for (int i = 1; i <= res.getColumnCount(); i++) {
//                        log.debug(res.getSignature().get(i-1) +" = " + res.getOWLObject(i));
//
//                    }
                }
                log.debug("Total result: {}", count);
                assertFalse(count == 0);
                log.debug("Elapsed time: {} ms", time);
            }
        }

        st.close();
        conn.close();


    }





    @Test
    public void testR2rml() throws Exception {
        Properties p = new Properties();
        p.setProperty(QuestPreferences.JDBC_URL, "jdbc:mysql://10.7.20.39/northwind");
        p.setProperty(QuestPreferences.DB_USER, "fish");
        p.setProperty(QuestPreferences.DB_PASSWORD, "fish");
        p.setProperty(QuestPreferences.JDBC_DRIVER, "com.mysql.jdbc.Driver");

        log.info("Loading r2rml file");

        QuestPreferences prefs = new R2RMLQuestPreferences(p);
        runTests(prefs, r2rmlFile);
    }


    @Test
    public void testOBDA() throws Exception {

        log.info("Loading OBDA file");

        QuestPreferences p = new QuestPreferences();
        runTests(p, obdaFile);
    }





}

