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
 * Class to test if bind in SPARQL is working properly.
 * Some tests check everything is working in combination with CONCAT
 * {@link it.unibz.krdb.obda.owlrefplatform.core.translator.SparqlAlgebraToDatalogTranslator}

 */
public class AssignmentTest {
    private OBDADataFactory fac;

    Logger log = LoggerFactory.getLogger(this.getClass());
    private OBDAModel obdaModel;
    private OWLOntology ontology;

    final String owlFile = "src/test/resources/bindTest/ontologyOdbs.owl";
    final String obdaFile = "src/test/resources/bindTest/mappingsOdbs.obda";

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
    public void testBindQuery() throws Exception {

        QuestPreferences p = new QuestPreferences();

//
        String queryBind = "PREFIX : <http://myproject.org/odbs#> \n" +
                "\n" +
                "SELECT DISTINCT ?f ?d " +
                " ?price \n" +
                "WHERE {?f a :Film; :hasDirector ?d . \n" +
                "BIND (\"123\" AS ?price) \n" +
                "}";



        int results = runTestQuery(p, queryBind);
        assertEquals(500, results);
    }
    @Test
    public void testBindAndConcatQuery() throws Exception {

        QuestPreferences p = new QuestPreferences();

//


        String queryConcat1 = "PREFIX : <http://myproject.org/odbs#> \n" +
                "\n" +
                "SELECT DISTINCT ?f ?d " +
                " ?price \n" +
                "WHERE {?f a :Film; :title ?t; :hasDirector ?d . \n" +
                "BIND (CONCAT(\"123\", \"456\")  as ?price  )    " +
                "}";

        String queryConcat2 = "PREFIX : <http://myproject.org/odbs#> \n" +
                "\n" +
                "SELECT DISTINCT ?f ?d " +
                " ?price \n" +
                "WHERE {?f a :Film; :title ?t; :hasDirector ?d . \n" +
                "BIND (CONCAT(?t, ?t)  as ?price  )    " +
                "}";

//        String queryConcat3 = "PREFIX : <http://myproject.org/odbs#> \n" +
//                "\n" +
//                "SELECT DISTINCT ?f ?d " +
//                " ?price \n" +
//                "WHERE {?f a :Film; :title ?t; :hasDirector ?d . \n" +
//                "BIND (CONCAT(\"123\", \"456\")  as ?price  ) " +
//                "FILTER (REGEX(?price, 6, \"i\"))   " +
//                "}";



        int results = runTestQuery(p, queryConcat2);
        assertEquals(500, results);
    }

    @Test
    public void testSelectQuery() throws Exception {

        QuestPreferences p = new QuestPreferences();


        String querySelect = "PREFIX : <http://myproject.org/odbs#> \n" +

                "SELECT DISTINCT ?f ?d (\"123\" AS ?price)  \n" +
                "WHERE {?f a :Film; :hasDirector ?d .  \n" +
                "}";



        int results = runTestQuery(p, querySelect);
        assertEquals(500, results);
    }

    @Test
    public void testSelectWithConcatQuery() throws Exception {

        QuestPreferences p = new QuestPreferences();


        String querySelConcat = "PREFIX : <http://myproject.org/odbs#> \n" +

                "SELECT DISTINCT ?f ?d (CONCAT(\"123\", \"456\") AS ?price)  \n" +
                "WHERE {?f a :Film; :hasDirector ?d .  \n" +
                "}";



        int results = runTestQuery(p, querySelConcat);
        assertEquals(500, results);
    }

    @Test
    public void testConcatWithIntegerQuery() throws Exception {

        QuestPreferences p = new QuestPreferences();


        String querySelConcat = "PREFIX : <http://myproject.org/odbs#> \n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n" +

                "SELECT DISTINCT ?f ?d (CONCAT(\"123\"^^xsd:integer, 456) AS ?price)  \n" +
                "WHERE {?f a :Film; :hasDirector ?d .  \n" +
                "}";



        int results = runTestQuery(p, querySelConcat);
        assertEquals(500, results);
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
                    for (int i = 1; i <= res.getColumnCount(); i++) {
                         log.debug(res.getSignature().get(i-1) + "=" + res.getOWLObject(i));

                      }
                }
                log.debug("Total result: {}", count);

                assertFalse(count == 0);

                log.debug("Elapsed time: {} ms", time);

        return count;



    }


}

