package org.semanticweb.ontop.obda;

import org.junit.Before;
import org.junit.Test;
import org.semanticweb.ontop.io.ModelIOManager;
import org.semanticweb.ontop.io.QueryIOManager;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.OBDADataSource;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.owlapi3.*;
import org.semanticweb.ontop.querymanager.QueryController;
import org.semanticweb.ontop.querymanager.QueryControllerGroup;
import org.semanticweb.ontop.querymanager.QueryControllerQuery;
import org.semanticweb.ontop.r2rml.R2RMLReader;
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

public class R2rmlJoinTest {
    private OBDADataFactory fac;

    Logger log = LoggerFactory.getLogger(this.getClass());
    private OBDAModel obdaModel;
    private OWLOntology ontology;

    final String owlFile = "src/test/resources/oreda/oreda_bootstrapped_ontology.owl";
    final String r2rmlFile = "src/test/resources/oreda/oreda_bootstrapped_mapping.ttl";
    final String obdaFile = "src/test/resources/oreda/oreda_bootstrapped_mapping.obda";

    @Before
    public void setUp() throws Exception {

        fac = OBDADataFactoryImpl.getInstance();

        // Loading the OWL file
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        ontology = manager.loadOntologyFromOntologyDocument((new File(owlFile)));




    }

    private void runTests(Properties p) throws Exception {

        // Creating a new instance of the reasoner
        QuestOWLFactory factory = new QuestOWLFactory();
        factory.setOBDAController(obdaModel);

        factory.setPreferenceHolder(p);

        QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

        // Now we are ready for querying
        QuestOWLConnection conn = reasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();


        QueryController qc = new QueryController();
        QueryIOManager qman = new QueryIOManager(qc);
        qman.load("src/test/resources/oreda/oreda.q");

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
                assertFalse(count != 0);
                log.debug("Elapsed time: {} ms", time);
            }
        }

        st.close();
        conn.close();


    }





    @Test
    public void testR2rml() throws Exception {


        String jdbcurl = "jdbc:sqlserver://10.7.20.91;databaseName=OREDA_OPTIQUE";
        String username = "mssql";
        String password = "obdaps83";
        String driverclass = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

        OBDADataFactory f = OBDADataFactoryImpl.getInstance();
        // String sourceUrl = "http://example.org/customOBDA";
        URI obdaURI = new File(r2rmlFile).toURI();
        String sourceUrl = obdaURI.toString();
        OBDADataSource dataSource = f.getJDBCDataSource(sourceUrl, jdbcurl,
                username, password, driverclass);


        log.info("Loading r2rml file");

        R2RMLReader reader = new R2RMLReader(r2rmlFile);

        obdaModel = reader.readModel(dataSource);

        QuestPreferences p = new QuestPreferences();

        runTests(p);
    }


    @Test
    public void testOBDA() throws Exception {

        log.info("Loading OBDA file");

        // Loading the OBDA data
        obdaModel = fac.getOBDAModel();
        ModelIOManager ioManager = new ModelIOManager(obdaModel);
        ioManager.load(obdaFile);

        QuestPreferences p = new QuestPreferences();

        runTests(p);
    }





}

