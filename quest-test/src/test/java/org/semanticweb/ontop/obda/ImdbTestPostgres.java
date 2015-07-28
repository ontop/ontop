package org.semanticweb.ontop.obda;
/**
 * Test case for the IMDB database see wiki Example_MovieOntology
 * Created by Sarah on 30/07/14.
 */

import org.semanticweb.ontop.io.ModelIOManager;
import org.semanticweb.ontop.io.QueryIOManager;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.owlapi3.*;
import org.semanticweb.ontop.querymanager.QueryController;
import org.semanticweb.ontop.querymanager.QueryControllerGroup;
import org.semanticweb.ontop.querymanager.QueryControllerQuery;
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

public class ImdbTestPostgres {
    private OBDADataFactory fac;

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private OBDAModel obdaModel;
    private OWLOntology ontology;

    private final String owlFile = "src/test/resources/movieontology.owl";
    private final String obdaFile = "src/test/resources/movieontology.obda";

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
        qman.load("src/test/resources/movieontology.q");

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
                }
                log.debug("Total result: {}", count);
                assertFalse(count == 0);
                log.debug("Elapsed time: {} ms", time);
            }
        }


    }





    @Test
    public void testIMDBSeries() throws Exception {

        QuestPreferences p = new QuestPreferences();

        runTests(p);
    }


    @Test
    public void testOneQuery() throws Exception {

        QuestPreferences p = new QuestPreferences();

        String query = "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT $x $title $company_name\n" +
                "WHERE { \n" +
                "   $m a mo:Movie; mo:title ?title; mo:hasActor ?x; mo:hasDirector ?x; mo:isProducedBy $y; mo:belongsToGenre $z .\n" +
                "   $x dbpedia:birthName $actor_name .\n" +
                "   $y :companyName $company_name; :hasCompanyLocation [ a mo:Eastern_Asia ] .\n" +
                "   $z a mo:Love .\n" +
                "}\n";


        String query2 = "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT *\n" +
                "WHERE { \n" +
                "   $m a mo:Movie; mo:isProducedBy $y .\n" +
                "   $y :hasCompanyLocation [ a mo:Eastern_Asia ] .\n" +
                "}\n";

        assertEquals(15175, runTestQuery(p, query2));
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
                }
                log.debug("Total result: {}", count);

                assertFalse(count == 0);

                log.debug("Elapsed time: {} ms", time);

        return count;



    }

    @Test
    public void testIndividuals() throws Exception {

        QuestPreferences p = new QuestPreferences();

        String query = "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "SELECT DISTINCT $z \n" +
                "WHERE { \n" +
                "   $z a mo:Love .\n" +
                "}\n";


        assertEquals(29405, runTestQuery(p, query));
    }
}

