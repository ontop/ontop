package org.semanticweb.ontop.obda;

/**
 * Test case for the IMDB database see wiki Example_MovieOntology
 * Created by Sarah on 30/07/14.
 */

        import org.semanticweb.ontop.io.ModelIOManager;
        import org.semanticweb.ontop.io.QueryIOManager;
        import org.semanticweb.ontop.model.OBDADataFactory;
        import org.semanticweb.ontop.model.SQLOBDAModel;
        import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
        import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
        import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWL;
        import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLConnection;
        import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLFactory;
        import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLResultSet;
        import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLStatement;

        import java.io.File;
        import java.util.Properties;

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

public class ImdbTestPostgres {
    private OBDADataFactory fac;

    Logger log = LoggerFactory.getLogger(this.getClass());
    private SQLOBDAModel obdaModel;
    private OWLOntology ontology;

    final String owlFile = "src/test/resources/movieontology.owl";
    final String obdaFile = "src/test/resources/movieontology.obda";

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
                log.debug("Elapsed time: {} ms", time);
            }
        }


    }





    @Test
    public void testIMDBSeries() throws Exception {

        QuestPreferences p = new QuestPreferences();

        runTests(p);
    }

}

