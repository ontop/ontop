/**
 * 
 */
package it.unibz.inf.ontop.sql;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.owlrefplatform.owlapi.*;
import org.junit.After;
import org.junit.Test;
import it.unibz.inf.ontop.owlrefplatform.core.SQLExecutableQuery;

/**
 * @author dagc
 *
 */
public class TestQuestImplicitDBConstraints {

	static String uc_owlfile = "src/test/resources/userconstraints/uc.owl";
	static String uc_obdafile = "src/test/resources/userconstraints/uc.obda";
	static String uc_keyfile = "src/test/resources/userconstraints/keys.lst";
	static String uc_create = "src/test/resources/userconstraints/create.sql";
	
	static String fk_owlfile = "src/test/resources/userconstraints/uc.owl";
	static String fk_obdafile = "src/test/resources/userconstraints/fk.obda";
	static String fk_keyfile = "src/test/resources/userconstraints/fk-keys.lst";
	static String fk_create = "src/test/resources/userconstraints/fk-create.sql";

	private static final String URL = "jdbc:h2:mem:countries";
	private static final String USER = "sa";
	private static final String PASSWORD = "";

	private OntopOWLConnection conn;

	private QuestOWL reasoner;
	private Connection sqlConnection;

	
	public void prepareDB(String sqlfile) throws Exception {
		try {
			sqlConnection= DriverManager.getConnection(URL, USER, PASSWORD);
			java.sql.Statement s = sqlConnection.createStatement();

			try {
				String text = new Scanner( new File(sqlfile) ).useDelimiter("\\A").next();
				s.execute(text);
				//Server.startWebServer(sqlConnection);

			} catch(SQLException sqle) {
				System.out.println("Exception in creating db from script");
			}

			s.close();

		} catch (Exception exc) {
			try {
				tearDown();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}	

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

	@Test
	public void testNoSelfJoinElim() throws Exception {
		this.prepareDB(uc_create);
		//this.reasoner = factory.createReasoner(new SimpleConfiguration());
		QuestOWLFactory factory = new QuestOWLFactory();
        QuestConfiguration config = QuestConfiguration.defaultBuilder()
				.ontologyFile(uc_owlfile)
				.nativeOntopMappingFile(uc_obdafile)
				.jdbcUrl(URL)
				.jdbcUser(USER)
				.jdbcPassword(PASSWORD)
				.build();
        reasoner = factory.createReasoner(config);
        

		// Now we are ready for querying
		this.conn = reasoner.getConnection();
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x :hasVal1 ?v1; :hasVal2 ?v2.}";
		OntopOWLStatement st = conn.createStatement();
		
		
		String sql = ((SQLExecutableQuery)st.getExecutableQuery(query)).getSQL();
		boolean m = sql.matches("(?ms)(.*)\"TABLE1\"(.*),(.*)\"TABLE1\"(.*)");
		assertTrue(m);
		
		
	}

	@Test
	public void testForeignKeysNoSelfJoinElim() throws Exception {
		this.prepareDB(uc_create);
		
		QuestOWLFactory factory = new QuestOWLFactory();
        QuestConfiguration config = QuestConfiguration.defaultBuilder()
				.nativeOntopMappingFile(uc_obdafile)
				.ontologyFile(uc_owlfile)
				.jdbcUrl(URL)
				.jdbcUser(USER)
				.jdbcPassword(PASSWORD)
				.build();
        reasoner = factory.createReasoner(config);
        
		
		//this.reasoner = factory.createReasoner(new SimpleConfiguration());


		// Now we are ready for querying
		this.conn = reasoner.getConnection();
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x :hasVal3 ?v1; :hasVal4 ?v4.}";
		OntopOWLStatement st = conn.createStatement();


		String sql = ((SQLExecutableQuery)st.getExecutableQuery(query)).getSQL();
		boolean m = sql.matches("(?ms)(.*)\"TABLE2\"(.*),(.*)\"TABLE2\"(.*)");
		assertTrue(m);
		
		
	}
	
	@Test
	public void testWithSelfJoinElim() throws Exception {
		this.prepareDB(uc_create);

		// Parsing user constraints
		ImplicitDBConstraintsReader userConstraints = new ImplicitDBConstraintsReader(new File(uc_keyfile));


		QuestOWLFactory factory = new QuestOWLFactory();
        QuestConfiguration config = QuestConfiguration.defaultBuilder()
				.ontologyFile(uc_owlfile)
				.nativeOntopMappingFile(uc_obdafile)
				.dbConstraintsReader(userConstraints)
				.jdbcUrl(URL)
				.jdbcUser(USER)
				.jdbcPassword(PASSWORD)
				.build();
        reasoner = factory.createReasoner(config);

		// Now we are ready for querying
		this.conn = reasoner.getConnection();
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x :hasVal1 ?v1; :hasVal2 ?v2.}";
		OntopOWLStatement st = conn.createStatement();


		String sql = ((SQLExecutableQuery)st.getExecutableQuery(query)).getSQL();
		boolean m = sql.matches("(?ms)(.*)\"TABLE1\"(.*),(.*)\"TABLE1\"(.*)");
		assertFalse(m);
		
		
	}
	
	@Test
	public void testForeignKeysWithSelfJoinElim() throws Exception {
		this.prepareDB(uc_create);
		// Parsing user constraints
		ImplicitDBConstraintsReader userConstraints = new ImplicitDBConstraintsReader(new File(uc_keyfile));
//		factory.setImplicitDBConstraints(userConstraints);
//		this.reasoner = factory.createReasoner(new SimpleConfiguration());

		QuestOWLFactory factory = new QuestOWLFactory();
        QuestConfiguration config = QuestConfiguration.defaultBuilder()
				.ontologyFile(uc_owlfile)
				.nativeOntopMappingFile(uc_obdafile)
				.dbConstraintsReader(userConstraints)
				.jdbcUrl(URL)
				.jdbcUser(USER)
				.jdbcPassword(PASSWORD)
				.build();
        reasoner = factory.createReasoner(config);
        
		// Now we are ready for querying
		this.conn = reasoner.getConnection();
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x :hasVal3 ?v1; :hasVal4 ?v4.}";
		OntopOWLStatement st = conn.createStatement();


		String sql = ((SQLExecutableQuery)st.getExecutableQuery(query)).getSQL();
		boolean m = sql.matches("(?ms)(.*)\"TABLE2\"(.*),(.*)\"TABLE2\"(.*)");
		assertTrue(m);
		
		
	}
	
	
	/**
	 * Testing foreign keys referring to tables not mentioned by mappings
	 * @throws Exception
	 */
	@Test
	public void testForeignKeysTablesNOUc() throws Exception {
		this.prepareDB(fk_create);
		
		QuestOWLFactory factory = new QuestOWLFactory();
        QuestConfiguration config = QuestConfiguration.defaultBuilder()
				.ontologyFile(fk_owlfile)
				.nativeOntopMappingFile(fk_obdafile)
				.jdbcUrl(URL)
				.jdbcUser(USER)
				.jdbcPassword(PASSWORD)
				.build();
        reasoner = factory.createReasoner(config);
        
		//this.reasoner = factory.createReasoner(new SimpleConfiguration());


		// Now we are ready for querying
		this.conn = reasoner.getConnection();
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x :relatedTo ?y; :hasVal1 ?v1. ?y :hasVal2 ?v2.}";
		OntopOWLStatement st = conn.createStatement();


		String sql = ((SQLExecutableQuery)st.getExecutableQuery(query)).getSQL();
		System.out.println(sql);
		boolean m = sql.matches("(?ms)(.*)\"TABLE2\"(.*),(.*)\"TABLE2\"(.*)");
		assertTrue(m);
		
		
	}
	

	/**
	 * Testing foreign keys referring to tables not mentioned by mappings
	 * @throws Exception
	 */
	@Test
	public void testForeignKeysTablesWithUC() throws Exception {
		this.prepareDB(fk_create);
		// Parsing user constraints
		ImplicitDBConstraintsReader userConstraints = new ImplicitDBConstraintsReader(new File(fk_keyfile));
//		factory.setImplicitDBConstraints(userConstraints);
//		this.reasoner = factory.createReasoner(new SimpleConfiguration());

		QuestOWLFactory factory = new QuestOWLFactory();
        QuestConfiguration config = QuestConfiguration.defaultBuilder()
				.ontologyFile(fk_owlfile)
				.nativeOntopMappingFile(fk_obdafile)
				.dbConstraintsReader(userConstraints)
				.jdbcUrl(URL)
				.jdbcUser(USER)
				.jdbcPassword(PASSWORD)
				.build();
        reasoner = factory.createReasoner(config);
        
		// Now we are ready for querying
		this.conn = reasoner.getConnection();
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x :relatedTo ?y; :hasVal1 ?v1. ?y :hasVal2 ?v2.}";
		OntopOWLStatement st = conn.createStatement();


		String sql = ((SQLExecutableQuery)st.getExecutableQuery(query)).getSQL();
		System.out.println(sql);
		boolean m = sql.matches("(?ms)(.*)\"TABLE2\"(.*),(.*)\"TABLE2\"(.*)");
		assertFalse(m);
		
		
	}


}
