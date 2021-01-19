package it.unibz.inf.ontop.docker.mysql;


import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import it.unibz.inf.ontop.querymanager.QueryIOManager;
import it.unibz.inf.ontop.querymanager.QueryController;
import it.unibz.inf.ontop.querymanager.QueryControllerGroup;
import it.unibz.inf.ontop.querymanager.QueryControllerQuery;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.junit.Assert.assertNotEquals;

public class DatetimeStampTest {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String owlFile = "/mysql/northwind/northwind-dmo.owl";
    private static final String r2rmlFile = "/mysql/northwind/mapping-northwind-dmo.ttl";
    private static final String obdaFile = "/mysql/northwind/mapping-northwind-dmo.obda";
    private static final String propertyFile = "/mysql/northwind/mapping-northwind-dmo.properties";

    private void runTests(boolean isR2rml) throws Exception {

        String owlFileName =  this.getClass().getResource(owlFile).toString();
        String obdaFileName =  this.getClass().getResource(obdaFile).toString();
        String r2rmlFileName =  this.getClass().getResource(r2rmlFile).toString();
        String propertyFileName =  this.getClass().getResource(propertyFile).toString();

        // Creating a new instance of the reasoner
        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration.Builder configBuilder = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .ontologyFile(owlFileName)
                .propertyFile(propertyFileName)
                .enableTestMode();

        if (isR2rml) {
            configBuilder.r2rmlMappingFile(r2rmlFileName);
        }
        else {
            configBuilder.nativeOntopMappingFile(obdaFileName);
        }

        OntopOWLReasoner reasoner = factory.createReasoner(configBuilder.build());

        try (OWLConnection conn = reasoner.getConnection();
            OWLStatement st = conn.createStatement()) {
            QueryController qc = new QueryController();
            QueryIOManager qman = new QueryIOManager(qc);
            qman.load(new File("src/test/resources/northwind/northwind.q"));

            for (QueryControllerGroup group : qc.getGroups()) {
                for (QueryControllerQuery query : group.getQueries()) {
                    log.debug("Executing query: {}", query.getID());
                    log.debug("Query: \n{}", query.getQuery());

                    long start = System.nanoTime();
                    TupleOWLResultSet res = st.executeSelectQuery(query.getQuery());
                    long end = System.nanoTime();

                    int count = 0;
                    while (res.hasNext()) {
                        count += 1;
                    }
                    log.debug("Total result: {}", count);
                    assertNotEquals(0, count);
                    log.debug("Elapsed time: {} ms", (end - start) / 1000);
                }
            }
        }
    }

    @Test
    public void testR2rml() throws Exception {
        log.info("Loading r2rml file");
        runTests( true);
    }

    @Test
    public void testOBDA() throws Exception {
        log.info("Loading OBDA file");
        runTests(false);
    }
}

