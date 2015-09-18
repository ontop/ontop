package org.semanticweb.ontop.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.File;
import java.util.Properties;
import java.util.Scanner;

import org.junit.After;
import org.junit.Test;
import org.openrdf.model.Model;
import static org.junit.Assert.*;

import org.semanticweb.ontop.injection.OBDAProperties;
import org.semanticweb.ontop.owlapi3.OWLAPI3TranslatorUtility;
import org.semanticweb.ontop.owlrefplatform.core.*;
import org.semanticweb.ontop.owlrefplatform.questdb.R2RMLQuestPreferences;
import org.semanticweb.ontop.r2rml.R2RMLManager;
import org.semanticweb.ontop.sesame.SesameVirtualRepo;
import org.semanticweb.ontop.sql.api.Attribute;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * Tests that user-applied constraints can be provided through 
 * sesameWrapper.SesameVirtualRepo 
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
	private OWLAPI3TranslatorUtility translator = new OWLAPI3TranslatorUtility();
	private QuestDBStatement qst = null;

	/*
	 * 	prepare ontop for rewriting and unfolding steps 
	 */
	public void init(boolean applyUserConstraints, boolean provideMetadata)  throws Exception {

		DBMetadata dbMetadata;
		OWLOntology ontology;
		Model model;

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
		 * Load the ontology from an external .owl file.
		 */
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument(new File(owlfile));

		/*
		 * Load the OBDA model from an external .r2rml file
		 */
		R2RMLManager rmanager = new R2RMLManager(r2rmlfile);
		model = rmanager.getModel();
		/*
		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
		obdaModel = fac.getOBDAModel();
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(obdafile);
		 */
		/*
		 * Prepare the configuration for the Quest instance. The example below shows the setup for
		 * "Virtual ABox" mode
		 */
		Properties p = new Properties();
		p.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setProperty(OBDAProperties.DB_NAME, "countries");
		p.setProperty(OBDAProperties.JDBC_URL, "jdbc:h2:mem:countries");
		p.setProperty(OBDAProperties.DB_USER, "sa");
		p.setProperty(OBDAProperties.DB_PASSWORD, "");
		p.setProperty(OBDAProperties.JDBC_DRIVER, "org.h2.Driver");

		if(applyUserConstraints){
			// Parsing user constraints
			ImplicitDBConstraints userConstraints = new ImplicitDBConstraints(uc_keyfile);
			p.put(QuestPreferences.DB_CONSTRAINTS, userConstraints);
		}

		QuestPreferences preferences = new R2RMLQuestPreferences(p);

		dbMetadata = getMeta();
		SesameVirtualRepo qest1;
		if(provideMetadata){
			qest1 = new SesameVirtualRepo("", ontology, model, dbMetadata, preferences);
		} else {
			qest1 = new SesameVirtualRepo("", ontology, model, preferences);
		}
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

	private TableDefinition defTable(String name){
		TableDefinition tableDefinition = new TableDefinition(name);
		Attribute attribute = null;
		//It starts from 1 !!!
		attribute = new Attribute("COL1", java.sql.Types.INTEGER, false, null);
		tableDefinition.addAttribute(attribute);
		attribute = new Attribute("COL2", java.sql.Types.INTEGER, false, null);
		tableDefinition.addAttribute(attribute);
		return tableDefinition;
	}
	private DBMetadata getMeta(){
		DBMetadata dbMetadata = new DBMetadata("org.h2.Driver");
		dbMetadata.add(defTable("\"TABLE1\""));
		dbMetadata.add(defTable("\"TABLE2\""));
		dbMetadata.add(defTable("\"TABLE3\""));
		return dbMetadata;
	}


	@Test
	public void testWithSelfJoinElimManualMetadata() throws Exception {
		init(true, true);
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x :hasVal1 ?v1; :hasVal2 ?v2.}";
		String sql = qst.getSQL(query);
		boolean m = sql.matches("(?ms)(.*)\"TABLE1\"(.*),(.*)\"TABLE1\"(.*)");
		assertFalse(m);
	}

	@Test
	public void testWithoutSelfJoinElimManualMetadata() throws Exception {
		init(false, true);
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x :hasVal1 ?v1; :hasVal2 ?v2.}";
		String sql = qst.getSQL(query);
		boolean m = sql.matches("(?ms)(.*)\"TABLE1\"(.*),(.*)\"TABLE1\"(.*)");
		assertTrue(m);
	}

	@Test
	public void testWithSelfJoinElimNoMetadata() throws Exception {
		init(true, false);
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x :hasVal1 ?v1; :hasVal2 ?v2.}";
		String sql = qst.getSQL(query);
		boolean m = sql.matches("(?ms)(.*)\"TABLE1\"(.*),(.*)\"TABLE1\"(.*)");
		assertFalse(m);
	}

	@Test
	public void testWithoutSelfJoinElimNoMetadata() throws Exception {
		init(false, false);
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x :hasVal1 ?v1; :hasVal2 ?v2.}";
		String sql = qst.getSQL(query);
		boolean m = sql.matches("(?ms)(.*)\"TABLE1\"(.*),(.*)\"TABLE1\"(.*)");
		assertTrue(m);
	}
}
