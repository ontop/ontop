/**
 * 
 */
package org.semanticweb.ontop.sql;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Test;
import org.semanticweb.ontop.injection.NativeQueryLanguageComponentFactory;
import org.semanticweb.ontop.injection.OntopCoreModule;
import org.semanticweb.ontop.mapping.MappingParser;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWL;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLConnection;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLFactory;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLStatement;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final NativeQueryLanguageComponentFactory nativeQLFactory;


    private OBDADataFactory fac;
	private QuestOWLConnection conn;
	private QuestOWLFactory factory;
	
	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;

	private QuestOWL reasoner;
	private Connection sqlConnection;

    public TestQuestImplicitDBConstraints() {
        Injector injector = Guice.createInjector(new OntopCoreModule(new Properties()));
        nativeQLFactory = injector.getInstance(NativeQueryLanguageComponentFactory.class);
    }


	
	public void start_reasoner(String owlfile, String obdafile, String sqlfile) throws Exception {
		try {
			sqlConnection= DriverManager.getConnection("jdbc:h2:mem:countries","sa", "");
			java.sql.Statement s = sqlConnection.createStatement();

			try {
				String text = new Scanner( new File(sqlfile) ).useDelimiter("\\A").next();
				s.execute(text);
				//Server.startWebServer(sqlConnection);

			} catch(SQLException sqle) {
				System.out.println("Exception in creating db from script");
			}

			s.close();


			// Loading the OWL file
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));

			// Loading the OBDA data
            MappingParser parser = nativeQLFactory.create(new FileReader(obdafile));
			obdaModel = parser.getOBDAModel();

			QuestPreferences p = new QuestPreferences();
			p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
			p.setCurrentValueOf(QuestPreferences.OBTAIN_FULL_METADATA, QuestConstants.FALSE);

			
			// Creating a new instance of the reasoner
			this.factory = new QuestOWLFactory();
			factory.setOBDAController(obdaModel);

			factory.setPreferenceHolder(p);

		} catch (Exception exc) {
			try {
				tearDown();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}	

	}


	@After
	public void tearDown() throws Exception{
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
		this.start_reasoner(uc_owlfile, uc_obdafile, uc_create);
		this.reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());


		// Now we are ready for querying
		this.conn = reasoner.getConnection();
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x :hasVal1 ?v1; :hasVal2 ?v2.}";
		QuestOWLStatement st = conn.createStatement();
		
		
		String sql = st.getUnfolding(query);
		boolean m = sql.matches("(?ms)(.*)\"TABLE1\"(.*),(.*)\"TABLE1\"(.*)");
		assertTrue(m);
		
		
	}

	@Test
	public void testForeignKeysNoSelfJoinElim() throws Exception {
		this.start_reasoner(uc_owlfile, uc_obdafile, uc_create);
		this.reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());


		// Now we are ready for querying
		this.conn = reasoner.getConnection();
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x :hasVal3 ?v1; :hasVal4 ?v4.}";
		QuestOWLStatement st = conn.createStatement();
		
		
		String sql = st.getUnfolding(query);
		boolean m = sql.matches("(?ms)(.*)\"TABLE2\"(.*),(.*)\"TABLE2\"(.*)");
		assertTrue(m);
		
		
	}
	
	@Test
	public void testWithSelfJoinElim() throws Exception {
		this.start_reasoner(uc_owlfile, uc_obdafile, uc_create);
		
		// Parsing user constraints
		ImplicitDBConstraints userConstraints = new ImplicitDBConstraints(uc_keyfile);
		factory.setImplicitDBConstraints(userConstraints);
		this.reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());


		// Now we are ready for querying
		this.conn = reasoner.getConnection();
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x :hasVal1 ?v1; :hasVal2 ?v2.}";
		QuestOWLStatement st = conn.createStatement();
		
		
		String sql = st.getUnfolding(query);
		boolean m = sql.matches("(?ms)(.*)\"TABLE1\"(.*),(.*)\"TABLE1\"(.*)");
		assertFalse(m);
		
		
	}
	
	@Test
	public void testForeignKeysWithSelfJoinElim() throws Exception {
		this.start_reasoner(uc_owlfile, uc_obdafile, uc_create);
		// Parsing user constraints
		ImplicitDBConstraints userConstraints = new ImplicitDBConstraints(uc_keyfile);
		factory.setImplicitDBConstraints(userConstraints);
		this.reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());


		// Now we are ready for querying
		this.conn = reasoner.getConnection();
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x :hasVal3 ?v1; :hasVal4 ?v4.}";
		QuestOWLStatement st = conn.createStatement();
		
		
		String sql = st.getUnfolding(query);
		boolean m = sql.matches("(?ms)(.*)\"TABLE2\"(.*),(.*)\"TABLE2\"(.*)");
		assertTrue(m);
		
		
	}
	
	
	/**
	 * Testing foreign keys referring to tables not mentioned by mappings
	 * @throws Exception
	 */
	@Test
	public void testForeignKeysTablesNOUc() throws Exception {
		this.start_reasoner(fk_owlfile, fk_obdafile, fk_create);
		
		this.reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());


		// Now we are ready for querying
		this.conn = reasoner.getConnection();
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x :relatedTo ?y; :hasVal1 ?v1. ?y :hasVal2 ?v2.}";
		QuestOWLStatement st = conn.createStatement();
		
		
		String sql = st.getUnfolding(query);
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
		this.start_reasoner(fk_owlfile, fk_obdafile, fk_create);
		// Parsing user constraints
		ImplicitDBConstraints userConstraints = new ImplicitDBConstraints(fk_keyfile);
		factory.setImplicitDBConstraints(userConstraints);
		this.reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());


		// Now we are ready for querying
		this.conn = reasoner.getConnection();
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x :relatedTo ?y; :hasVal1 ?v1. ?y :hasVal2 ?v2.}";
		QuestOWLStatement st = conn.createStatement();
		
		
		String sql = st.getUnfolding(query);
		System.out.println(sql);
		boolean m = sql.matches("(?ms)(.*)\"TABLE2\"(.*),(.*)\"TABLE2\"(.*)");
		assertFalse(m);
		
		
	}


}
