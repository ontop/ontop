package org.semanticweb.ontop.obda;



import org.junit.Before;
import org.junit.Test;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.semanticweb.ontop.io.ModelIOManager;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.owlapi3.*;
import org.semanticweb.ontop.sesame.SesameVirtualRepo;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Test to check the use of SPARQL Select distinct in Sesame and QuestOWL.
 * Use the class {@link org.semanticweb.ontop.owlrefplatform.core.resultset.QuestDistinctResultset}
 */

public class DistinctResultSetTest { //

    private OBDADataFactory fac;
    Logger log = LoggerFactory.getLogger(this.getClass());
    private OBDAModel obdaModel;
    private OWLOntology ontology;

    final String owlFile = "src/test/resources/example/exampleBooks.owl";
    final String obdaFile = "src/test/resources/example/exampleBooks.obda";

    @Before
    public void setUp() throws Exception {


    }
    private int runTestsQuestOWL(Properties p, String query) throws Exception {

        fac = OBDADataFactoryImpl.getInstance();

        // Loading the OWL file
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        ontology = manager.loadOntologyFromOntologyDocument((new File(owlFile)));

        // Loading the OBDA data
        obdaModel = fac.getOBDAModel();

        ModelIOManager ioManager = new ModelIOManager(obdaModel);
        ioManager.load(obdaFile);

        // Creating a new instance of the reasoner
        QuestOWLFactory factory = new QuestOWLFactory();
        factory.setOBDAController(obdaModel);

        factory.setPreferenceHolder(p);

        QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

        // Now we are ready for querying
        QuestOWLConnection conn = reasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();

        int results = 0;

        try {
            results= executeQueryAssertResults(query, st);

        } catch (Exception e) {
            st.close();
            e.printStackTrace();
            assertTrue(false);


        } finally {

            conn.close();
            reasoner.dispose();
        }
        return results;

    }

    private int runTestsSesame(String query, String configFile){
        //create a sesame repository
        RepositoryConnection con = null;
        Repository repo = null;
        int count = 0;
        try {

        repo = new SesameVirtualRepo("my_name", owlFile, obdaFile, configFile);



        repo.initialize();

        con = repo.getConnection();

        ///query repo
        try {

            TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, query);
            TupleQueryResult result = tupleQuery.evaluate();
            try {
                List<String> bindings = result.getBindingNames();
                while (result.hasNext()) {
                    count++;
                    BindingSet bindingSet = result.next();
                    for (String b : bindings)
                        System.out.println(bindingSet.getBinding(b));
                }
            } finally {
                result.close();
            }
        }
            catch(Exception e)
            {
                e.printStackTrace();
            }

        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return count;

        }

    private int executeQueryAssertResults(String query, QuestOWLStatement st) throws Exception {
        QuestOWLResultSet rs = st.executeTuple(query);
        int count = 0;
        while (rs.nextRow()) {
            count++;
            for (int i = 1; i <= rs.getColumnCount(); i++) {

                log.debug(rs.getSignature().get(i-1) + "=" + rs.getOWLObject(i));

            }

        }
        rs.close();

        return count;

    }

    @Test
    public void testDistinctQuestOWL() throws Exception {


        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.DISTINCT_RESULTSET, QuestConstants.TRUE);
        String query = "PREFIX : <http://meraka/moss/exampleBooks.owl#>" +
                " select distinct * {?x a :Author}";
        int nResults = runTestsQuestOWL(p, query);
        assertEquals(25, nResults);
    }

    @Test
    public void testDistinctSesame() throws Exception {

        String query = "PREFIX : <http://meraka/moss/exampleBooks.owl#>" +
                " select distinct * {?x a :Book}";
        File f = new File("src/test/resources/example/exampleDistinct.preferences");
        String pref = "file:" + f.getAbsolutePath();
        int nResults = runTestsSesame(query,pref) ;
        assertEquals(24, nResults);
    }



}
