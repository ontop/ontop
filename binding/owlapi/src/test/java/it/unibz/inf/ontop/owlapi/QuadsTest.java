package it.unibz.inf.ontop.owlapi;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.node.NativeNode;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.OWLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import static it.unibz.inf.ontop.injection.OntopReformulationSettings.*;
import static it.unibz.inf.ontop.utils.OWLAPITestingTools.executeFromFile;
import static java.util.stream.Collectors.joining;
import static junit.framework.TestCase.assertEquals;

public class QuadsTest {
  private static final String CREATE_SCRIPT = "src/test/resources/quads/create.sql";
  private static final String DROP_SCRIPT = "src/test/resources/quads/drop.sql";
  private static final String OWL_FILE = "src/test/resources/quads/test.owl";
  private static final String MAPPING_FILE = "src/test/resources/quads/test.obda";
  private static final String RESULT_FILE="src/test/resources/quads/query-result.txt";
  private static final ToStringRenderer renderer = ToStringRenderer.getInstance();

  private static final String URL = "jdbc:h2:mem:job";
  private static final String USER = "sa";
  private static final String PASSWORD = "sa";
  private static final Logger LOGGER = LoggerFactory.getLogger(QuadsTest.class);

  private Connection conn;

  @Before
  public void setUp() throws Exception {
    conn = DriverManager.getConnection(URL, USER, PASSWORD);
    executeFromFile(conn, CREATE_SCRIPT);
  }

  @After
  public void tearDown() throws Exception {
    executeFromFile(conn, DROP_SCRIPT);
    conn.close();
  }

  @Test
  public void testQuery() throws Exception {

    String queryQuad = "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
            "PREFIX xml: <http://www.w3.org/XML/1998/namespace>\n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "SELECT  ?g ?subject ?predicate ?object " +
            "WHERE " +
            "{ GRAPH ?g {?subject  ?predicate ?object } } ";


    int expectedCardinality = 30;
    String sql = execute(queryQuad, expectedCardinality);

    LOGGER.debug("SQL Query: \n" + sql);
  }


  private String execute(String query, int expectedCardinality) throws Exception {

    Properties properties = new Properties();
    properties.setProperty(QUERY_LOGGING, "true");
    properties.setProperty(REFORMULATED_INCLUDED_QUERY_LOGGING, "true");

    OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
    OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
            .nativeOntopMappingFile(MAPPING_FILE)
            .ontologyFile(OWL_FILE)
            .jdbcUrl(URL)
            .jdbcUser(USER)
            .jdbcPassword(PASSWORD)
            .properties(properties)
            .enableTestMode()
            .build();
    OntopOWLReasoner reasoner = factory.createReasoner(config);

    // Now we are ready for querying
    OntopOWLConnection conn = reasoner.getConnection();
    OntopOWLStatement st = conn.createStatement();
    String sql;

    int i = 0;
    String expectedResult = Files.lines(Paths.get(RESULT_FILE)).collect(joining("\n")) + "\n";

    try {
      IQ executableQuery = st.getExecutableQuery(query);
      sql = ((NativeNode) executableQuery.getTree().getChildren().get(0)).getNativeQueryString();
      TupleOWLResultSet rs = st.executeSelectQuery(query);
      StringBuilder builder = new StringBuilder();
      while (rs.hasNext()) {
        final OWLBindingSet bindingSet = rs.next();
        builder.append(i + "\n");
        String graph = stringify(bindingSet.getBinding("g").getValue());
        String subject = stringify(bindingSet.getBinding("subject").getValue());
        String predicate = stringify(bindingSet.getBinding("predicate").getValue());
        String object = stringify(bindingSet.getBinding("object").getValue());
        builder.append("\t" + "g" + "\t" + graph + "\n");
        builder.append("\t" + "subject" + "\t" + subject + "\n");
        builder.append("\t" + "predicate" + "\t" + predicate + "\n");
        builder.append("\t" + "object" + "\t" + object + "\n");
        i++;
      }
      assertEquals(expectedCardinality, i);
      assertEquals(expectedResult, builder.toString());
    }
    finally {
      conn.close();
      reasoner.dispose();
    }
    return sql;
  }

  private String stringify(OWLObject owlObject) {
        return renderer.getRendering(owlObject);
    }
}
