package it.unibz.krdb.odba;


import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.*;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Class to test if CONCAT in mapping is working properly.
 * rdfs:label is a concat of literal and variables
 *

 */
public class BoundTest {
    private OBDADataFactory fac;

    Logger log = LoggerFactory.getLogger(this.getClass());
    private OBDAModel obdaModel;
    private OWLOntology ontology;

    final String owlFile = "src/test/resources/publicaciones/vivo-isf-public-1.6.owl";
    final String obdaFile = "src/test/resources/publicaciones/vivo-isf-public-1.6.obda";

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




    @Test
    public void testBoundQuery() throws Exception {

        QuestPreferences p = new QuestPreferences();

//
        String queryBind = "PREFIX new: <http://addedMapping.org/>\n" +
                "PREFIX : <http://vivoweb.org/ontology/core#>\n" +
                "PREFIX vivo: <http://vivoweb.org/ontology/core#>\n" +
                "PREFIX bibo: <http://purl.org/ontology/bibo/>\n" +
                "\n" +
                "SELECT DISTINCT  ?cve ?type ?cve_status \n" +
                "WHERE { \n" +
                "?cve a new:CompletedDocument .\n" +
                "?cve  vivo:hasSubjectArea ?type . \n" +
                "?cve vivo:documentOf ?person .\n" +
                "\n" +

                "?cve bibo:status ?cve_status .\n" +

                "\n" +
                "\n" +
                "FILTER(BOUND(?cve_status) )\n" +
                "\n" +
                "FILTER (?type = vivo:subject2 || ?type = vivo:subject3 || ?type = vivo:subject4 || ?type = vivo:subject12 || ?type = vivo:subject41)\n" +
                "\n" +
                "\n" +
                "}";



        int results = runTestQuery(p, queryBind);
        assertEquals(22502, results);
    }

    @Test
    public void testBoundOPTIONALQuery() throws Exception {

        QuestPreferences p = new QuestPreferences();

//
        String queryBind = "PREFIX new: <http://addedMapping.org/>\n" +
                "PREFIX : <http://vivoweb.org/ontology/core#>\n" +
                "PREFIX vivo: <http://vivoweb.org/ontology/core#>\n" +
                "PREFIX bibo: <http://purl.org/ontology/bibo/>\n" +
                "\n" +
                "SELECT DISTINCT  ?cve ?type ?cve_status \n" +
                "WHERE { \n" +
                "?cve a new:CompletedDocument .\n" +
                "?cve  vivo:hasSubjectArea ?type . \n" +
                "?cve vivo:documentOf ?person .\n" +
                "\n" +
                "OPTIONAL {\n" +
            "?cve bibo:status ?cve_status .\n" +
                "}\n" +
                "\n" +
                "\n" +
                "FILTER(!BOUND(?cve_status) )\n" +
                "\n" +
                "FILTER (?type = vivo:subject2 || ?type = vivo:subject3 || ?type = vivo:subject4 || ?type = vivo:subject12 || ?type = vivo:subject41)\n" +
                "\n" +
                "\n" +
                "}";



        int results = runTestQuery(p, queryBind);
        assertEquals(520, results);
    }


    private int runTestQuery(Properties p, String query) throws Exception {

        // Creating a new instance of the reasoner
        QuestOWLFactory factory = new QuestOWLFactory();
        factory.setOBDAController(obdaModel);

        factory.setPreferenceHolder(p);

        QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

        // Now we are ready for querying
        QuestOWLConnection conn = reasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();


                log.debug("Executing query: ");
                log.debug("Query: \n{}", query);

                long start = System.nanoTime();
                QuestOWLResultSet res = st.executeTuple(query);
                long end = System.nanoTime();

                double time = (end - start) / 1000;

                int count = 0;
                while (res.nextRow()) {
                    count += 1;
//                    for (int i = 1; i <= res.getColumnCount(); i++) {
//                         log.debug(res.getSignature().get(i-1) + "=" + res.getOWLObject(i));
//
//                      }
                }
                log.debug("Total result: {}", count);

                assertFalse(count == 0);

                log.debug("Elapsed time: {} ms", time);

        return count;



    }


}

