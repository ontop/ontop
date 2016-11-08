package it.unibz.inf.ontop.reformulation.tests;


import it.unibz.inf.ontop.io.QueryIOManager;
import it.unibz.inf.ontop.model.OBDAModel;

import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.owlrefplatform.core.QuestPreferences;
import it.unibz.inf.ontop.owlrefplatform.owlapi.*;
import it.unibz.inf.ontop.querymanager.QueryController;
import it.unibz.inf.ontop.querymanager.QueryControllerGroup;
import it.unibz.inf.ontop.querymanager.QueryControllerQuery;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;

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

    private QuestOWL reasoner;
    private QuestOWLConnection conn;
    Connection sqlConnection;


    @Before
    public void setUp() throws Exception{
        sqlConnection = DriverManager.getConnection("jdbc:h2:mem:university","sa", "");
        java.sql.Statement s = sqlConnection.createStatement();
        String text = new Scanner( new File("src/test/resources/canonicalIRI/university/dataset_dump.sql") ).useDelimiter("\\A").next();
        s.execute(text);
        s.close();
        OWLOntology ontology = OWLManager.createOWLOntologyManager()
                .loadOntologyFromOntologyDocument(new File(owlFile));

        OBDAModel obdaModel = new MappingLoader().loadFromOBDAFile(obdaFile);

        QuestPreferences preference = new QuestPreferences() ;
        preference.setCurrentValueOf(QuestPreferences.REWRITE, QuestConstants.TRUE);
		/*
		 * Create the instance of Quest OWL reasoner.
		 */
        QuestOWLFactory factory = new QuestOWLFactory();

        QuestOWLConfiguration config = QuestOWLConfiguration.builder().obdaModel(obdaModel).preferences(preference).build();

        reasoner = factory.createReasoner(ontology, config);
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
             QuestOWLStatement st = conn.createStatement();
        ) {
            QueryController qc = new QueryController();
            QueryIOManager qman = new QueryIOManager(qc);
            qman.load(sparqlFile);

            for (QueryControllerGroup group : qc.getGroups()) {
                for (QueryControllerQuery query : group.getQueries()) {

                    String sparqlQuery = query.getQuery();
                    QuestOWLResultSet res = st.executeTuple(sparqlQuery);
                    int columnSize = res.getColumnCount();
                    while (res.nextRow()) {
                        for (int idx = 1; idx <= columnSize; idx++) {
                            OWLObject binding = res.getOWLObject(idx);
                            System.out.print(binding.toString() + ", ");
                        }
                        System.out.print("\n");
                    }

                     /*
			            * Print the query summary
			         */

                    String sqlQuery = st.getUnfolding(sparqlQuery);

                    System.out.println();
                    System.out.println("The input SPARQL query:");
                    System.out.println("=======================");
                    System.out.println(sparqlQuery);
                    System.out.println();

                    System.out.println("The output SQL query:");
                    System.out.println("=====================");
                    System.out.println(sqlQuery);


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

        runQuery(query);

    }

    private void runQuery(String query) throws OWLException {
        QuestOWLStatement st = conn.createStatement();
        ArrayList<String> retVal = new ArrayList<>();
        try {
            QuestOWLResultSet rs = st.executeTuple(query);
            while(rs.nextRow()) {
                for (String s : rs.getSignature()) {
                    OWLObject binding = rs.getOWLObject(s);

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

        runQuery(query);

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

        runQuery(query);


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

        runQuery(query);


    }

}

