package it.unibz.inf.ontop.docker.mysql;


import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepository;
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

import java.util.List;

import static org.junit.Assert.assertEquals;


public class DistinctResultSetTest {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String owlFile = "/mysql/example/exampleBooks.owl";
    private static final String obdaFile = "/mysql/example/exampleBooks.obda";
    private static  final String propertyFile = "/mysql/example/exampleBooks.properties";
    private static String owlFileName;
    private static String obdaFileName;
    private static String propertyFileName;

    @Before
    public void setUp() throws Exception {
        owlFileName =  this.getClass().getResource(owlFile).toString();
        obdaFileName =  this.getClass().getResource(obdaFile).toString();
        propertyFileName =  this.getClass().getResource(propertyFile).toString();

    }

    private int runTestsQuestOWL( String query) throws Exception {
        // Creating a new instance of the reasoner
        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(obdaFileName)
                .ontologyFile(owlFileName)
                .propertyFile(propertyFileName)
//                .enableTestMode()
                .build();
        OntopOWLReasoner reasoner = factory.createReasoner(config);
        // Now we are ready for querying
        OWLConnection conn = reasoner.getConnection();

        int results = 0;

        try (OWLStatement st = conn.createStatement()) {
            results= executeQueryAssertResults(query, st);
        }
        finally {
            conn.close();
            reasoner.dispose();
        }
        return results;

    }

    private int runTestsSesame(String query, String configFile) {
        //create a sesame repository
        OntopSQLOWLAPIConfiguration configuration = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .ontologyFile(owlFileName)
                .nativeOntopMappingFile(obdaFileName)
                .propertyFile(configFile)
                .enableTestMode()
                .build();

        Repository repo = OntopRepository.defaultRepository(configuration);

        repo.initialize();

        RepositoryConnection con = repo.getConnection();

        ///query repo
        int count = 0;
        TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, query);
        try (TupleQueryResult result = tupleQuery.evaluate()) {
            List<String> bindings = result.getBindingNames();
            while (result.hasNext()) {
                count++;
                BindingSet bindingSet = result.next();
                for (String b : bindings)
                    log.debug("Binding : " + bindingSet.getBinding(b));
            }
        }
        return count;
    }

    private int executeQueryAssertResults(String query, OWLStatement st) throws Exception {
        TupleOWLResultSet rs = st.executeSelectQuery(query);
        int count = 0;
        while (rs.hasNext()) {
            final OWLBindingSet bindingSet = rs.next();
            count++;
            for (int i = 1; i <= rs.getColumnCount(); i++) {
                String bindingName = rs.getSignature().get(i - 1);
                log.debug(bindingName + "=" + bindingSet.getOWLObject(bindingName));
            }

        }
        rs.close();
        return count;
    }

    @Test
    public void testDistinctQuestOWL() throws Exception {

        String query = "PREFIX : <http://meraka/moss/exampleBooks.owl#>" +
                " select distinct * {?x a :Author}";
        int nResults = runTestsQuestOWL(query);
        assertEquals(25, nResults);
    }

    @Test
    public void testDistinctSesame() throws Exception {

        String query = "PREFIX : <http://meraka/moss/exampleBooks.owl#>" +
                " select distinct * {?x a :Book}";
        String pref = this.getClass().getResource("/mysql/example/exampleDistinct.properties").toString();

        int nResults = runTestsSesame(query,pref) ;
        assertEquals(24, nResults);
    }

}
