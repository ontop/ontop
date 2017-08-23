package it.unibz.inf.ontop.owlapi;


import it.unibz.inf.ontop.answering.reformulation.ExecutableQuery;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import it.unibz.inf.ontop.utils.querymanager.QueryController;
import it.unibz.inf.ontop.utils.querymanager.QueryControllerGroup;
import it.unibz.inf.ontop.utils.querymanager.QueryControllerQuery;
import it.unibz.inf.ontop.utils.querymanager.QueryIOManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLObject;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

import static org.junit.Assert.assertTrue;

public class CanonicalIRIUniversityTest {


    final String owlFile = "src/test/resources/canonicalIRI/university/univ-ontology.ttl";
    final String obdaFile = "src/test/resources/canonicalIRI/university/univ-ontology.obda";
    final String sparqlFile = "src/test/resources/canonicalIRI/university/univ-ontology.q";

    private OntopOWLReasoner reasoner;
    private OntopOWLConnection conn;
    Connection sqlConnection;

    private static final String JDBC_URL =  "jdbc:h2:mem:uni";
    private static final String JDBC_USER =  "sa";
    private static final String JDBC_PASSWORD =  "";


    @Before
    public void setUp() throws Exception{
        sqlConnection = DriverManager.getConnection(JDBC_URL,JDBC_USER, JDBC_PASSWORD);
        java.sql.Statement s = sqlConnection.createStatement();
        String text = new Scanner( new File("src/test/resources/canonicalIRI/university/dataset_dump.sql") ).useDelimiter("\\A").next();
        s.execute(text);
        s.close();

        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .ontologyFile(owlFile)
                .nativeOntopMappingFile(obdaFile)
                .enableExistentialReasoning(true)
                .jdbcUrl(JDBC_URL)
                .jdbcUser(JDBC_USER)
                .jdbcPassword(JDBC_PASSWORD)
                .enableTestMode()
                .build();

		/*
		 * Create the instance of Quest OWL reasoner.
		 */
        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();

        reasoner = factory.createReasoner(config);
        conn = reasoner.getConnection();


    }

    @After
    public void tearDown() throws Exception {
        conn.close();
        reasoner.dispose();
        if (!sqlConnection.isClosed()) {
            java.sql.Statement s = sqlConnection.createStatement();
            try {
                s.execute("DROP ALL OBJECTS DELETE FILES");
            } catch (SQLException sqle) {
                System.out.println("Table not found, not dropping");
            } finally {
                s.close();
                sqlConnection.close();
            }
        }
    }

    @Ignore
    public void testUniversity() throws Exception {


		/*
		 * Prepare the data connection for querying.
		 */
//        String sparqlQuery = Files.lines(Paths.get(sparqlFile)).collect(joining("\n"));


        try (
                OntopOWLStatement st = conn.createStatement();
        ) {
            QueryController qc = new QueryController();
            QueryIOManager qman = new QueryIOManager(qc);
            qman.load(sparqlFile);

            for (QueryControllerGroup group : qc.getGroups()) {
                for (QueryControllerQuery query : group.getQueries()) {

                    String sparqlQuery = query.getQuery();
                    TupleOWLResultSet res = st.executeSelectQuery(sparqlQuery);
                    int columnSize = res.getColumnCount();
                    while (res.hasNext()) {
                        final OWLBindingSet bindingSet = res.next();
                        for (int idx = 1; idx <= columnSize; idx++) {
                            OWLObject binding = bindingSet.getOWLObject(idx);
                            System.out.print(binding.toString() + ", ");
                        }
                        System.out.print("\n");
                    }

                     /*
			            * Print the query summary
			         */

                    ExecutableQuery executableQuery = st.getExecutableQuery(sparqlQuery);

                    System.out.println();
                    System.out.println("The input SELECT SPARQL query:");
                    System.out.println("=======================");
                    System.out.println(sparqlQuery);
                    System.out.println();

                    System.out.println("The output SQL query:");
                    System.out.println("=====================");
                    System.out.println(executableQuery);


                    res.close();




                }
            }




        }
    }

    @Ignore
    public void testOptional() throws Exception {
        String query = "PREFIX : <http://example.org/voc#>\n" +
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                "SELECT ?title ?fName ?lName {\n" +
                "  ?teacher rdf:type :Professor . \n" +
                "  ?teacher :teaches ?course . \n" +
                "  ?teacher foaf:lastName ?lName .\n" +
                "\n" +
                "  ?course :title ?title .\n" +
                "  OPTIONAL {\n" +
                "    ?teacher foaf:firstName ?fName .\n" +
                "  }\n" +
                "}";

        runSelectQuery(query);

    }

    private void runSelectQuery(String query) throws OWLException {
        OWLStatement st = conn.createStatement();
        ArrayList<String> retVal = new ArrayList<>();
        try {
            TupleOWLResultSet  rs = st.executeSelectQuery(query);
            while(rs.hasNext()) {
                final OWLBindingSet bindingSet = rs.next();
                for (String s : rs.getSignature()) {
                    OWLObject binding = bindingSet.getOWLObject(s);
                    String rendering = ToStringRenderer.getInstance().getRendering(binding);
                    retVal.add(rendering);
                    System.out.println((s + ":  " + rendering));
                }
            }

        } catch (Exception e) {
            throw e;
        } finally {
            try {

            } catch (Exception e) {
                st.close();
                assertTrue(false);
            }
            conn.close();
            reasoner.dispose();
        }
    }

    @Test
    public void testDistinctResults() throws Exception {
        String query = "PREFIX : <http://example.org/voc#>\n" +
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                "SELECT DISTINCT ?teacher ?lastName {\n " +
            "  ?teacher a :Teacher ; foaf:lastName ?lastName .\n" +
                    "}\n";

        runSelectQuery(query);

    }

    @Test
    public void testResearcher() throws Exception {

        String query =
                "PREFIX : <http://example.org/voc#>\n" +
                        "\n" +
                        "SELECT ?researcher\n" +
                        "WHERE {\n" +
                        "   ?researcher a :Researcher .\n" +
                        "}";

        runSelectQuery(query);


    }

    @Test
    public void testSupervisedByProfessor() throws Exception {

        String query =
                "PREFIX : <http://example.org/voc#>\n" +
                        "\n" +
                        "SELECT ?x\n" +
                        "WHERE {\n" +
                        "   ?x :isSupervisedBy [ a :Professor ] .\n" +
                        "}";

        runSelectQuery(query);


    }

}

