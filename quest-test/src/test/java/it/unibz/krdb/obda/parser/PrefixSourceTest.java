package it.unibz.krdb.obda.parser;


import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.*;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Class to test if annotation property can be treated as data property and object property
 *
 *
 */
public class PrefixSourceTest {

    private OBDADataFactory fac;

    Logger log = LoggerFactory.getLogger(this.getClass());
    private OBDAModel obdaModel;
    private OWLOntology ontology;

    final String owlFile = "src/test/resources/movieontology.owl";
    final String obdaFile = "src/test/resources/newPrefixMovieOntology.obda";

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
    public void testPrefixInsideURI() throws Exception {

        QuestPreferences p = new QuestPreferences();

//        p.setCurrentValueOf(QuestPreferences.SQL_GENERATE_REPLACE, QuestConstants.FALSE);

        String queryBind = "PREFIX :  <http://www.movieontology.org/2009/10/01/movieontology.owl>" +
                "PREFIX mo:  <http://www.movieontology.org/2009/10/01/movieontology.owl#>" +
                "PREFIX mo2:		<http://www.movieontology.org/2009/11/09/movieontology.owl#>" +
                "\n" +
                "SELECT  ?r " +

                "WHERE {?x a mo:East_Asian_Company ; mo2:hasCompanyLocation ?r .  \n" +

                "}";



        String results = runTestQuery(p, queryBind);
        assertEquals("<http://example.com/resource/?repository=repo&uri=http://www.movieontology.org/2009/10/01/movieontology.owl/China-24951>", results);
    }


    private String runTestQuery(QuestPreferences p, String query) throws Exception {

        // Creating a new instance of the reasoner
        QuestOWLFactory factory = new QuestOWLFactory();
        QuestOWLConfiguration config = QuestOWLConfiguration.builder().obdaModel(obdaModel).preferences(p).build();
        QuestOWL reasoner = factory.createReasoner(ontology, config);

        // Now we are ready for querying
        QuestOWLConnection conn = reasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();


        log.debug("Executing query: ");
        log.debug("Query: \n{}", query);

        long start = System.nanoTime();
        QuestOWLResultSet res = st.executeTuple(query);
        long end = System.nanoTime();

        double time = (end - start) / 1000;
        String result = "";
        int count = 0;
        while (res.nextRow()) {
            count += 1;
            if (count == 1) {
                for (int i = 1; i <= res.getColumnCount(); i++) {
                    log.debug("Example result " + res.getSignature().get(i - 1) + " = " + res.getOWLObject(i));

                }
                result = ToStringRenderer.getInstance().getRendering(res.getOWLObject("r"));
            }
        }
        log.debug("Total results: {}", count);

        assertFalse(count == 0);

        log.debug("Elapsed time: {} ms", time);

        return result;



    }


}

