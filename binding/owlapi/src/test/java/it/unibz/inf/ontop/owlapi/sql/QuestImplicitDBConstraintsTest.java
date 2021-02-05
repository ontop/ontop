package it.unibz.inf.ontop.owlapi.sql;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.utils.OWLAPITestingTools;
import org.junit.After;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author dagc
 *
 */
public class QuestImplicitDBConstraintsTest {

	private static final String RESOURCE_DIR = "src/test/resources/userconstraints/";
	private static final String uc_owlfile = RESOURCE_DIR + "uc.owl";
	private static final String uc_obdafile = RESOURCE_DIR + "uc.obda";
	private static final String uc_keyfile = RESOURCE_DIR + "keys.lst";
	private static final String uc_create = RESOURCE_DIR + "create.sql";
	
	private static final String fk_owlfile = RESOURCE_DIR + "uc.owl";
	private static final String fk_obdafile = RESOURCE_DIR + "fk.obda";
	private static final String fk_keyfile = RESOURCE_DIR + "fk-keys.lst";
	private static final String fk_create = RESOURCE_DIR + "fk-create.sql";

	private static final String URL = "jdbc:h2:mem:countries";
	private static final String USER = "sa";
	private static final String PASSWORD = "";

	private OntopOWLConnection conn;

	private OntopOWLReasoner reasoner;
	private Connection sqlConnection;

	
	public void prepareDB(String sqlfile) throws Exception {
		sqlConnection = DriverManager.getConnection(URL, USER, PASSWORD);
		OWLAPITestingTools.executeFromFile(sqlConnection, sqlfile);
	}


	@After
	public void tearDown() throws Exception {
		if (conn != null)
			conn.close();
		if (reasoner != null)
			reasoner.dispose();
		if (!sqlConnection.isClosed()) {
			try (java.sql.Statement s = sqlConnection.createStatement()) {
				s.execute("DROP ALL OBJECTS DELETE FILES");
			}
			finally {
				sqlConnection.close();
			}
		}
	}

	@Test
	public void testSelfJoinElimSameVariables() throws Exception {
		this.prepareDB(uc_create);
		//this.reasoner = factory.createReasoner(new SimpleConfiguration());
		OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.ontologyFile(uc_owlfile)
				.nativeOntopMappingFile(uc_obdafile)
				.jdbcUrl(URL)
				.jdbcUser(USER)
				.jdbcPassword(PASSWORD)
				.enableTestMode()
				.build();
        reasoner = factory.createReasoner(config);
        

		// Now we are ready for querying
		this.conn = reasoner.getConnection();
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x :hasVal1 ?v1; :hasVal2 ?v2.}";
		OntopOWLStatement st = conn.createStatement();
		
		
		String queryString = st.getExecutableQuery(query).toString();
		boolean m = queryString.matches("(?ms)(.*)\"TABLE1\"(.*),(.*)\"TABLE1\"(.*)");
		assertFalse(m);
	}

	@Test
	public void testForeignKeysSelfJoinElimSameVar() throws Exception {
		this.prepareDB(uc_create);
		
		OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.nativeOntopMappingFile(uc_obdafile)
				.ontologyFile(uc_owlfile)
				.jdbcUrl(URL)
				.jdbcUser(USER)
				.jdbcPassword(PASSWORD)
				.enableTestMode()
				.build();
        reasoner = factory.createReasoner(config);
        
		
		//this.reasoner = factory.createReasoner(new SimpleConfiguration());


		// Now we are ready for querying
		this.conn = reasoner.getConnection();
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x :hasVal3 ?v1; :hasVal4 ?v4.}";
		OntopOWLStatement st = conn.createStatement();

		String queryString = st.getExecutableQuery(query).toString();
		boolean m = queryString.matches("(?ms)(.*)\"TABLE2\"(.*),(.*)\"TABLE2\"(.*)");
		assertFalse(m);
	}
	
	@Test
	public void testWithSelfJoinElim() throws Exception {
		this.prepareDB(uc_create);


		OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.ontologyFile(uc_owlfile)
				.nativeOntopMappingFile(uc_obdafile)
				.basicImplicitConstraintFile(uc_keyfile)
				.jdbcUrl(URL)
				.jdbcUser(USER)
				.jdbcPassword(PASSWORD)
				.enableTestMode()
				.build();
        reasoner = factory.createReasoner(config);

		// Now we are ready for querying
		this.conn = reasoner.getConnection();
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x :hasVal1 ?v1; :hasVal2 ?v2.}";
		OntopOWLStatement st = conn.createStatement();


		String queryString = st.getExecutableQuery(query).toString();
		boolean m = queryString.matches("(?ms)(.*)\"TABLE1\"(.*),(.*)\"TABLE1\"(.*)");
		assertFalse(m);
	}
	
	@Test
	public void testForeignKeysWithSelfJoinElim() throws Exception {
		this.prepareDB(uc_create);

		OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.ontologyFile(uc_owlfile)
				.nativeOntopMappingFile(uc_obdafile)
				.basicImplicitConstraintFile(uc_keyfile)
				.jdbcUrl(URL)
				.jdbcUser(USER)
				.jdbcPassword(PASSWORD)
				.enableTestMode()
				.build();
        reasoner = factory.createReasoner(config);
        
		// Now we are ready for querying
		this.conn = reasoner.getConnection();
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x :hasVal3 ?v1; :hasVal4 ?v4.}";
		OntopOWLStatement st = conn.createStatement();

		String queryString = st.getExecutableQuery(query).toString();
		boolean m = queryString.matches("(?ms)(.*)\"TABLE2\"(.*),(.*)\"TABLE2\"(.*)");
		assertFalse(m);
	}
	
	
	/**
	 * Testing foreign keys referring to tables not mentioned by mappings
	 * @throws Exception
	 */
	@Test
	public void testForeignKeysTablesNOUc() throws Exception {
		this.prepareDB(fk_create);
		
		OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.ontologyFile(fk_owlfile)
				.nativeOntopMappingFile(fk_obdafile)
				.jdbcUrl(URL)
				.jdbcUser(USER)
				.jdbcPassword(PASSWORD)
				.enableTestMode()
				.build();
        reasoner = factory.createReasoner(config);
        
		//this.reasoner = factory.createReasoner(new SimpleConfiguration());

		// Now we are ready for querying
		this.conn = reasoner.getConnection();
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x :relatedTo ?y; :hasVal1 ?v1. ?y :hasVal2 ?v2.}";
		OntopOWLStatement st = conn.createStatement();

		String queryString = st.getExecutableQuery(query).toString();
		boolean m = queryString.matches("(?ms)(.*)\"TABLE2\"(.*),(.*)\"TABLE2\"(.*)");
		assertTrue(m);
	}
	

	/**
	 * Testing foreign keys referring to tables not mentioned by mappings
	 * @throws Exception
	 */
	@Test
	public void testForeignKeysTablesWithUC() throws Exception {
		this.prepareDB(fk_create);

		OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.ontologyFile(fk_owlfile)
				.nativeOntopMappingFile(fk_obdafile)
				.basicImplicitConstraintFile(fk_keyfile)
				.jdbcUrl(URL)
				.jdbcUser(USER)
				.jdbcPassword(PASSWORD)
				.enableTestMode()
				.build();
        reasoner = factory.createReasoner(config);
        
		// Now we are ready for querying
		this.conn = reasoner.getConnection();
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x :relatedTo ?y; :hasVal1 ?v1. ?y :hasVal2 ?v2.}";
		OntopOWLStatement st = conn.createStatement();

		String queryString = st.getExecutableQuery(query).toString();
		System.out.println(queryString);
		boolean m = queryString.matches("(?ms)(.*)\"TABLE2\"(.*),(.*)\"TABLE2\"(.*)");
		assertFalse(m);
	}


}
