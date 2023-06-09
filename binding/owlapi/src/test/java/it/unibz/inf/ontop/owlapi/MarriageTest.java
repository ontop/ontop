package it.unibz.inf.ontop.owlapi;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.NativeNode;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.impl.SimpleOntopOWLEngine;
import it.unibz.inf.ontop.owlapi.resultset.GraphOWLResultSet;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.apache.commons.lang3.StringUtils;
import org.junit.*;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

import static it.unibz.inf.ontop.utils.OWLAPITestingTools.executeFromFile;
import static org.junit.Assert.*;


public class MarriageTest {

	private static final String ONTOLOGY_FILE = "src/test/resources/marriage/marriage.ttl";
	private static final String OBDA_FILE = "src/test/resources/marriage/marriage.obda";
    private static final String CREATE_DB_FILE = "src/test/resources/marriage/create-db.sql";
	private static final String JDBC_URL = "jdbc:h2:mem:questjunitdb";
	private static final String JDBC_USER = "sa";
	private static final String JDBC_PASSWORD = "";
	private static final Logger LOGGER = LoggerFactory.getLogger(MarriageTest.class);

	private static Connection CONNECTION;
	private static OntopOWLEngine REASONER;

	@BeforeClass
	public static void setUp() throws Exception {
		CONNECTION = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
		executeFromFile(CONNECTION, CREATE_DB_FILE);

		OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.nativeOntopMappingFile(OBDA_FILE)
				.ontologyFile(ONTOLOGY_FILE)
				.jdbcUrl(JDBC_URL)
				.jdbcUser(JDBC_USER)
				.jdbcPassword(JDBC_PASSWORD)
				.enableTestMode()
				.build();

		REASONER = new SimpleOntopOWLEngine(config);
	}

	@AfterClass
	public static void tearDown() throws Exception {
		REASONER.close();
		CONNECTION.close();
	}


    /**
     * Tests that all the persons are not married.
     *
     * This test has been added to make sure the mapping saturation handles correctly domain
     * and nullable columns.
     *
     */
	@Test
    public void testSpouse() throws Exception {
        String queryBind = "PREFIX : <http://example.org/marriage/voc#>\n" +
                "\n" +
                "SELECT DISTINCT ?x \n" +
                "WHERE {\n" +
                "  ?x a :Spouse .\n" +
                "}";

        ImmutableSet<String> expectedValues = ImmutableSet.of(
                "<http://example.com/person/1>",
                "<http://example.com/person/2>"
        );
        checkReturnedValues(queryBind, expectedValues);
    }

	@Test
	public void testSpouseFirstName() throws Exception {
		String query = "PREFIX : <http://example.org/marriage/voc#>\n" +
				"\n" +
				"SELECT * \n" +
				"WHERE {\n" +
				"  ?x :lastName ?l \n" +
				"  OPTIONAL { \n" +
				"    ?x :hasSpouse ?s .\n" +
				"    OPTIONAL { ?s :firstName ?sn } \n" +
				"  }\n" +
				"}";

		ImmutableSet<String> expectedValues = ImmutableSet.of(
				"<http://example.com/person/1>",
				"<http://example.com/person/2>",
				"<http://example.com/person/3>"
		);
		checkReturnedValues(query, expectedValues);
		String reformulatedQuery = getReformulatedQuery(query);
		assertEquals(1, StringUtils.countMatches(reformulatedQuery.toUpperCase(),"LEFT OUTER JOIN"));
	}

	@Test
	public void testSpouseLastName() throws Exception {
		String query = "PREFIX : <http://example.org/marriage/voc#>\n" +
				"\n" +
				"SELECT * \n" +
				"WHERE {\n" +
				"  ?x :lastName ?l \n" +
				"  OPTIONAL { \n" +
				"    ?x :hasSpouse ?s .\n" +
				"    OPTIONAL { ?s :lastName ?sn } \n" +
				"  }\n" +
				"}";

		ImmutableSet<String> expectedValues = ImmutableSet.of(
				"<http://example.com/person/1>",
				"<http://example.com/person/2>",
				"<http://example.com/person/3>"
		);
		checkReturnedValues(query, expectedValues);
		String reformulatedQuery = getReformulatedQuery(query);
		assertEquals(1, StringUtils.countMatches(reformulatedQuery.toUpperCase(),"LEFT OUTER JOIN"));
	}

	/**
	 * Complex Optional interaction
	 */
	@Test
	public void testComplexOptionalInteraction() throws Exception {
		String queryBind = "PREFIX : <http://example.org/marriage/voc#>\n" +
				"\n" +
				"SELECT ?x ?f2 \n" +
				"WHERE {\n" +
				"  ?x :firstName ?f1 .\n" +
				"  OPTIONAL {\n" +
				"     ?x :hasSpouse ?p2 .\n" +
				"  }\n" +
				"  OPTIONAL {\n" +
				"     ?p2 :firstName ?f2 .\n" +
				"  }\n" +
				"}";

		// All distinct values of x
		ImmutableSet<String> expectedValues = ImmutableSet.of(
				"<http://example.com/person/1>",
				"<http://example.com/person/2>",
				"<http://example.com/person/3>"
		);
		checkReturnedValues(queryBind, expectedValues);
	}

	@Test
	public void testPersonConstruct() throws Exception {
		String query = "PREFIX : <http://example.org/marriage/voc#>\n" +
				"\n" +
				"CONSTRUCT {\n" +
				" ?x a :Persona . \n" +
				"}\n" +
				"WHERE {\n" +
				"  ?x a :Person .\n" +
				"}";

		int count = runConstructQuery(query);
		assertEquals(3, count);
	}

	@Test
	public void testPersonConstructLimit() throws Exception {
		String query = "PREFIX : <http://example.org/marriage/voc#>\n" +
				"\n" +
				"CONSTRUCT {\n" +
				" ?x a :Persona . \n" +
				"}\n" +
				"WHERE {\n" +
				"  ?x a :Person .\n" +
				"}\n" +
				"LIMIT 2";

		int count = runConstructQuery(query);
		assertEquals(2, count);
	}

	@Test
	public void testPersonConstructOffset() throws Exception {
		String query = "PREFIX : <http://example.org/marriage/voc#>\n" +
				"\n" +
				"CONSTRUCT {\n" +
				" ?x a :Persona . \n" +
				"}\n" +
				"WHERE {\n" +
				"  ?x a :Person .\n" +
				"}\n" +
				"OFFSET 1";

		int count = runConstructQuery(query);
		assertEquals(2, count);
	}

	@Test
	public void testPersonConstructLimitOffset1() throws Exception {
		String query = "PREFIX : <http://example.org/marriage/voc#>\n" +
				"\n" +
				"CONSTRUCT {\n" +
				" ?x a :Persona . \n" +
				"}\n" +
				"WHERE {\n" +
				"  ?x a :Person .\n" +
				"}\n" +
				"OFFSET 3\n" +
				"LIMIT 1";

		int count = runConstructQuery(query);
		assertEquals(0, count);
	}

	@Test
	public void testPersonConstructLimitOffset2() throws Exception {
		String query = "PREFIX : <http://example.org/marriage/voc#>\n" +
				"\n" +
				"CONSTRUCT {\n" +
				" ?x a :Persona . \n" +
				"}\n" +
				"WHERE {\n" +
				"  ?x a :Person .\n" +
				"}\n" +
				"OFFSET 2\n" +
				"LIMIT 1";

		int count = runConstructQuery(query);
		assertEquals(1, count);
	}

	@Test
	public void testPersonConstructOrderByLimit() throws Exception {
		String query = "PREFIX : <http://example.org/marriage/voc#>\n" +
				"\n" +
				"CONSTRUCT {\n" +
				" ?x a :Persona . \n" +
				"}\n" +
				"WHERE {\n" +
				"  ?x a :Person .\n" +
				"}\n" +
				"ORDER BY ?x\n" +
				"LIMIT 2";

		int count = runConstructQuery(query);
		assertEquals(2, count);
	}

	@Test
	public void testOptionallyMarriedToMusician() throws Exception {
		String query = "PREFIX : <http://example.org/marriage/voc#>\n" +
				"\n" +
				"SELECT ?x ?s ?l2 \n" +
				"WHERE {\n" +
				"?x :firstName ?l1 .\n" +
				   "OPTIONAL { \n" +
				"    ?p :hasSpouse ?s .\n" +
				"       OPTIONAL {\n" +
				"        ?s :firstName ?l2 ;\n" +
				"          a :Musician .\n" +
				"       }\n" +
				"  }\n" +
				"}\n";

		ImmutableSet<String> expectedValues = ImmutableSet.of(
				"<http://example.com/person/1>",
				"<http://example.com/person/2>",
				"<http://example.com/person/3>"
		);
		checkReturnedValues(query, expectedValues);
	}

	@Test
	public void testLJUnion() throws Exception {
		String query = "PREFIX : <http://example.org/marriage/voc#>\n" +
				"\n" +
				"SELECT ?p ?x \n" +
				"WHERE {\n" +
				"?p a :Person .\n" +
				"OPTIONAL { \n" +
				"    { ?p :firstName ?x . }\n" +
				"    UNION \n" +
				"    { ?p :lastName ?x . }\n" +
				"  }\n" +
				"}\n";

		ImmutableSet<String> expectedValues = ImmutableSet.of("Mary", "Bob", "John", "Smith", "Forester", "Doe");
		checkReturnedValues(query, expectedValues);
	}

	@Ignore
	@Test
	public void testLJJoinUnion() throws Exception {
		String query = "PREFIX : <http://example.org/marriage/voc#>\n" +
				"\n" +
				"SELECT ?p ?x \n" +
				"WHERE {\n" +
				"?p a :Person .\n" +
				"OPTIONAL { \n" +
				"    { ?p :firstName ?x . }\n" +
				"    UNION \n" +
				"    { ?p :lastName ?x . }\n" +
				"    ?p a :Musician ." +
				"  }\n" +
				"}\n";

		ImmutableSet<String> expectedValues = ImmutableSet.of("Mary", "Bob", "John", "Smith", "Forester", "Doe");
		checkReturnedValues(query, expectedValues);
	}

	@Test
	public void testEmptyClass1() throws Exception {
		String query = "PREFIX : <http://example.org/marriage/voc#>\n" +
				"\n" +
				"SELECT ?x ?i ?e \n" +
				"WHERE {\n" +
				"?x :firstName ?l1 .\n" +
				"OPTIONAL { \n" +
				"    ?x :playsInstrument ?i .\n" +
				"       OPTIONAL {\n" +
				"         ?e a :EmptyElement .\n " +
				"         ?r a :Musician \n" +
				"       }\n" +
				"  }\n" +
				"}\n";

		ImmutableSet<String> expectedValues = ImmutableSet.of(
				"<http://example.com/person/1>",
				"<http://example.com/person/2>",
				"<http://example.com/person/3>"
		);
		checkReturnedValues(query, expectedValues);
	}

    private void checkReturnedValues(String query, Set<String> expectedValues) throws Exception {

        // Now we are ready for querying
        OWLConnection conn = REASONER.getConnection();
        OWLStatement st = conn.createStatement();

        Set<String> returnedValues = new HashSet<>();
        try {
            TupleOWLResultSet rs = st.executeSelectQuery(query);

            while (rs.hasNext()) {
                final OWLBindingSet bindingSet = rs.next();

				OWLObject value = bindingSet.getOWLObject("x");
				String stringValue = (value instanceof OWLLiteral)
						? ((OWLLiteral) value).getLiteral()
						: (value == null) ? null : value.toString();

                returnedValues.add(stringValue);
            }
        } finally {
            conn.close();
        }
		assertEquals(String.format("%s instead of \n %s", returnedValues, expectedValues), expectedValues, returnedValues);
    }

	private String getReformulatedQuery(String query) throws Exception {
		try (OntopOWLConnection conn = REASONER.getConnection();
			 OntopOWLStatement st = conn.createStatement()) {
			IQTree iqTree =  st.getExecutableQuery(query).getTree();

			return extractNativeQueryString(iqTree);
		}
	}

	private String extractNativeQueryString(IQTree iqTree) {
		if (iqTree.getRootNode() instanceof NativeNode)
			return ((NativeNode) iqTree).getNativeQueryString();
		if (iqTree.getRootNode() instanceof ConstructionNode)
			return extractNativeQueryString(iqTree.getChildren().get(0));
		throw new RuntimeException("Unexpected executable IQTree: " + iqTree);
	}

	private int runConstructQuery(String constructQuery) throws Exception {
		int count = 0;
		try (OWLConnection conn = REASONER.getConnection();
			 OWLStatement st = conn.createStatement()) {
			GraphOWLResultSet rs = st.executeConstructQuery(constructQuery);
			while (rs.hasNext()) {
				OWLAxiom axiom = rs.next();
				LOGGER.debug(axiom.toString());
				count++;
			}
		}
		return count;
	}


}
