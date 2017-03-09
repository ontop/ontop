package it.unibz.inf.ontop.obda;


import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.injection.QuestCoreSettings;
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.owlrefplatform.core.resultset.QuestDistinctTupleResultSet;
import it.unibz.inf.ontop.owlrefplatform.owlapi.*;
import it.unibz.inf.ontop.rdf4j.repository.OntopVirtualRepository;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Test to check the use of SPARQL Select distinct in Sesame and QuestOWL.
 * Use the class {@link QuestDistinctTupleResultSet}
 */

public class DistinctResultSetTest { //

    private Logger log = LoggerFactory.getLogger(this.getClass());

    final String owlFile = "src/test/resources/example/exampleBooks.owl";
    final String obdaFile = "src/test/resources/example/exampleBooks.obda";

    @Before
    public void setUp() throws Exception {


    }
    private int runTestsQuestOWL(Properties p, String query) throws Exception {

        // Creating a new instance of the reasoner
        QuestOWLFactory factory = new QuestOWLFactory();
        QuestConfiguration config = QuestConfiguration.defaultBuilder()
                .properties(p)
                .nativeOntopMappingFile(obdaFile)
                .ontologyFile(owlFile)
                .build();
        QuestOWL reasoner = factory.createReasoner(config);
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
            QuestConfiguration configuration = QuestConfiguration.defaultBuilder()
                    .ontologyFile(owlFile)
                    .nativeOntopMappingFile(obdaFile)
                    .propertyFile(configFile)
                    .build();

        repo = new OntopVirtualRepository("my_name", configuration);

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
                        log.debug("Binding : "+bindingSet.getBinding(b));
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


        Properties p = new Properties();
        p.setProperty(QuestCoreSettings.DISTINCT_RESULTSET, QuestConstants.TRUE);
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
