package it.unibz.inf.ontop.obda;

import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.injection.QuestCoreSettings;
import it.unibz.inf.ontop.io.QueryIOManager;
import it.unibz.inf.ontop.owlrefplatform.owlapi.*;
import it.unibz.inf.ontop.querymanager.QueryController;
import it.unibz.inf.ontop.querymanager.QueryControllerGroup;
import it.unibz.inf.ontop.querymanager.QueryControllerQuery;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Properties;

import static org.junit.Assert.assertFalse;

public class R2rmlJoinTest {
    Logger log = LoggerFactory.getLogger(this.getClass());

    final String owlFile = "src/test/resources/oreda/oreda_bootstrapped_ontology.owl";
    final String r2rmlFile = "src/test/resources/oreda/oreda_bootstrapped_mapping.ttl";
    final String obdaFile = "src/test/resources/oreda/oreda_bootstrapped_mapping.obda";

    private void runTests(Optional<Properties> optionalProperties, String filename) throws Exception {

        // Creating a new instance of the reasoner
        QuestOWLFactory factory = new QuestOWLFactory();
        QuestConfiguration.Builder configBuilder = QuestConfiguration.defaultBuilder()
                .ontologyFile(owlFile);

        if (optionalProperties.isPresent()) {
            configBuilder
                    .r2rmlMappingFile(filename)
                    .properties(optionalProperties.get());
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
        Properties p = new Properties();
        p.setProperty(QuestCoreSettings.JDBC_NAME, "mssql");
        p.setProperty(QuestCoreSettings.JDBC_USER, "mssql");
        p.setProperty(QuestCoreSettings.JDBC_PASSWORD, "obdaps83");
        p.setProperty(QuestCoreSettings.JDBC_URL, "jdbc:sqlserver://10.7.20.91;databaseName=OREDA_OPTIQUE");
        p.setProperty(QuestCoreSettings.JDBC_DRIVER, "com.microsoft.sqlserver.jdbc.SQLServerDriver");

        log.info("Loading r2rml file");

        runTests(Optional.of(p), r2rmlFile);
    }


    @Test
    public void testOBDA() throws Exception {

        log.info("Loading OBDA file");
        runTests(Optional.empty(), obdaFile);
    }

}

