package it.unibz.inf.ontop.mssql;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.io.QueryIOManager;
import it.unibz.inf.ontop.owlrefplatform.owlapi.*;
import it.unibz.inf.ontop.querymanager.QueryController;
import it.unibz.inf.ontop.querymanager.QueryControllerGroup;
import it.unibz.inf.ontop.querymanager.QueryControllerQuery;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertFalse;

public class R2rmlJoinTest {
    Logger log = LoggerFactory.getLogger(this.getClass());

    final String owlFile = "src/test/resources/mssql/oreda/oreda_bootstrapped_ontology.owl";
    final String r2rmlFile = "src/test/resources/mssql/oreda/oreda_bootstrapped_mapping.ttl";
    final String obdaFile = "src/test/resources/mssql/oreda/oreda_bootstrapped_mapping.obda";
    final String propertyFile = "src/test/resources/mssql/oreda/oreda_bootstrapped_mapping.properties";

    private void runTests(String filename, boolean isR2rml) throws Exception {

        // Creating a new instance of the reasoner
        QuestOWLFactory factory = new QuestOWLFactory();
        OntopSQLOWLAPIConfiguration.Builder configBuilder = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .ontologyFile(owlFile)
                .propertyFile(propertyFile)
                .enableTestMode();

        if (isR2rml) {
            configBuilder
                    .r2rmlMappingFile(filename);
        }
        else {
            configBuilder.nativeOntopMappingFile(filename);
        }

        QuestOWL reasoner = factory.createReasoner(configBuilder.build());

        // Now we are ready for querying
        OntopOWLConnection conn = reasoner.getConnection();
        OntopOWLStatement st = conn.createStatement();


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


        log.info("Loading r2rml file");

        runTests(r2rmlFile, true);
    }


    @Test
    public void testOBDA() throws Exception {

        log.info("Loading OBDA file");
        runTests(obdaFile, false);
    }

}

