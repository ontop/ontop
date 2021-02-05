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

import static org.junit.Assert.assertFalse;

public class DatetimeStampTest {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    final String owlFile = "/mysql/northwind/northwind-dmo.owl";
    final String r2rmlFile = "/mysql/northwind/mapping-northwind-dmo.ttl";
    final String obdaFile = "/mysql/northwind/mapping-northwind-dmo.obda";
    final String propertyFile = "/mysql/northwind/mapping-northwind-dmo.properties";

    private void runTests(String filename, boolean isR2rml, String propertyFile) throws Exception {

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
        // Now we are ready for querying
        OWLConnection conn = reasoner.getConnection();
        OWLStatement st = conn.createStatement();


        QueryController qc = new QueryController();
        QueryIOManager qman = new QueryIOManager(qc);
        qman.load("src/test/resources/northwind/northwind.q");

        for (QueryControllerGroup group : qc.getGroups()) {
            for (QueryControllerQuery query : group.getQueries()) {

                log.debug("Executing query: {}", query.getID());
                log.debug("Query: \n{}", query.getQuery());

                long start = System.nanoTime();
                TupleOWLResultSet res = st.executeSelectQuery(query.getQuery());
                long end = System.nanoTime();

                double time = (end - start) / 1000;

                int count = 0;
                while (res.hasNext()) {
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

        log.info("Loading r2rml file");
        runTests(r2rmlFile, true, propertyFile);
    }


    @Test
    public void testOBDA() throws Exception {

        log.info("Loading OBDA file");
        runTests(obdaFile, false, propertyFile);
    }





}

