package it.unibz.inf.ontop.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.File;
import java.util.Properties;
import java.util.Scanner;

import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.owlrefplatform.core.QuestDBConnection;
import it.unibz.inf.ontop.owlrefplatform.core.QuestDBStatement;
import it.unibz.inf.ontop.injection.QuestCoreSettings;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

import it.unibz.inf.ontop.injection.OBDASettings;
import it.unibz.inf.ontop.rdf4j.repository.OntopVirtualRepository;

/**
 * Tests that user-applied constraints can be provided through 
 * sesameWrapper.OntopVirtualRepository
 * with manually instantiated metadata.
 * 
 * This is quite similar to the setting in the optique platform
 * 
 * Some stuff copied from ExampleManualMetadata 
 * 
 * @author dhovl
 *
 */
public class TestSesameImplicitDBConstraints {
	static String owlfile = "src/test/resources/userconstraints/uc.owl";
	static String obdafile = "src/test/resources/userconstraints/uc.obda";
	static String r2rmlfile = "src/test/resources/userconstraints/uc.ttl";

	static String uc_keyfile = "src/test/resources/userconstraints/keys.lst";
	static String uc_create = "src/test/resources/userconstraints/create.sql";

	private Connection sqlConnection;
	private QuestDBStatement qst = null;

	/*
	 * 	prepare ontop for rewriting and unfolding steps 
	 */
	public void init(boolean applyUserConstraints, boolean provideMetadata)  throws Exception {

		sqlConnection= DriverManager.getConnection("jdbc:h2:mem:countries","sa", "");
		java.sql.Statement s = sqlConnection.createStatement();

		try {
			String text = new Scanner( new File(uc_create) ).useDelimiter("\\A").next();
			s.execute(text);
			//Server.startWebServer(sqlConnection);

		} catch(SQLException sqle) {
			System.out.println("Exception in creating db from script");
		}

		s.close();

		/*
		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
		obdaModel = fac.parse();
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(obdafile);
		 */
		/*
		 * Prepare the configuration for the Quest instance. The example below shows the setup for
		 * "Virtual ABox" mode
		 */
		Properties p = new Properties();
		p.setProperty(QuestCoreSettings.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setProperty(OBDASettings.JDBC_NAME, "countries");
		p.setProperty(OBDASettings.JDBC_URL, "jdbc:h2:mem:countries");
		p.setProperty(OBDASettings.JDBC_USER, "sa");
		p.setProperty(OBDASettings.JDBC_PASSWORD, "");
		p.setProperty(OBDASettings.JDBC_DRIVER, "org.h2.Driver");

		QuestConfiguration.Builder configurationBuilder = QuestConfiguration.defaultBuilder()
				.ontologyFile(owlfile)
				.r2rmlMappingFile(r2rmlfile)
				.properties(p);

		if(applyUserConstraints){
			// Parsing user constraints
			ImplicitDBConstraintsReader userConstraintReader = new ImplicitDBConstraintsReader(new File(uc_keyfile));
			configurationBuilder.dbConstraintsReader(userConstraintReader);
		}

		if(provideMetadata) {
			configurationBuilder.dbMetadata(getMeta());
		}

		OntopVirtualRepository qest1 = new OntopVirtualRepository("", configurationBuilder.build());
		qest1.initialize();
		/*
		 * Prepare the data connection for querying.
		 */
		QuestDBConnection conn  = qest1.getQuestConnection();
		qst = conn.createStatement();		

	}


	@After
	public void tearDown() throws Exception{
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

	private void defTable(RDBMetadata dbMetadata, String name) {
		QuotedIDFactory idfac = dbMetadata.getQuotedIDFactory();
		DatabaseRelationDefinition tableDefinition = dbMetadata.createDatabaseRelation(idfac.createRelationID(null, name));
		tableDefinition.addAttribute(idfac.createAttributeID("COL1"), java.sql.Types.INTEGER, null, false);
		tableDefinition.addAttribute(idfac.createAttributeID("COL2"), java.sql.Types.INTEGER, null, false);
	}
	private RDBMetadata getMeta(){
		RDBMetadata dbMetadata = RDBMetadataExtractionTools.createDummyMetadata("org.h2.Driver");
		defTable(dbMetadata, "TABLE1");
		defTable(dbMetadata, "TABLE2");
		defTable(dbMetadata, "TABLE3");
		return dbMetadata;
	}


	@Test
	public void testWithSelfJoinElimManualMetadata() throws Exception {
		init(true, true);
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x :hasVal1 ?v1; :hasVal2 ?v2.}";
		String sql = qst.getSQL(query);
		boolean m = sql.matches("(?ms)(.*)TABLE1 (.*),(.*)TABLE1 (.*)");
		assertFalse(m);
	}

	@Test
	public void testWithoutSelfJoinElimManualMetadata() throws Exception {
		init(false, true);
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x :hasVal1 ?v1; :hasVal2 ?v2.}";
		String sql = qst.getSQL(query);
		boolean m = sql.matches("(?ms)(.*)TABLE1 (.*),(.*)TABLE1 (.*)");
		assertTrue(m);
	}

	@Test
	public void testWithSelfJoinElimNoMetadata() throws Exception {
		init(true, false);
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x :hasVal1 ?v1; :hasVal2 ?v2.}";
		String sql = qst.getSQL(query);
		boolean m = sql.matches("(?ms)(.*)TABLE1 (.*),(.*)TABLE1 (.*)");
		assertFalse(m);
	}

	@Test
	public void testWithoutSelfJoinElimNoMetadata() throws Exception {
		init(false, false);
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x :hasVal1 ?v1; :hasVal2 ?v2.}";
		String sql = qst.getSQL(query);
		boolean m = sql.matches("(?ms)(.*)TABLE1\"(.*),(.*)TABLE1\"(.*)");
		assertTrue(m);
	}
}
