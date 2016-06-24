package it.unibz.inf.ontop.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.File;
import java.util.Properties;
import java.util.Scanner;

import com.google.inject.Guice;
import com.google.inject.Injector;
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.owlrefplatform.core.QuestDBConnection;
import it.unibz.inf.ontop.owlrefplatform.core.QuestDBStatement;
import it.unibz.inf.ontop.owlrefplatform.core.QuestPreferences;
import org.junit.After;
import org.junit.Test;
import org.openrdf.model.Model;
import static org.junit.Assert.*;

import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.injection.OBDACoreModule;
import it.unibz.inf.ontop.injection.OBDAProperties;
import it.unibz.inf.ontop.owlrefplatform.core.*;
import it.unibz.inf.ontop.r2rml.R2RMLManager;
import it.unibz.inf.ontop.sesame.SesameVirtualRepo;
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
			ImplicitDBConstraintsReader userConstraints = new ImplicitDBConstraintsReader(new File(uc_keyfile));
			p.put(QuestPreferences.DB_CONSTRAINTS, userConstraints);
		}

		QuestPreferences preferences = new R2RMLQuestPreferences(p);

		SesameVirtualRepo qest1;

		/**
		 * TODO: simplify this part by updating the prototype of SesameVirtualRepo
		 */
		if(provideMetadata){
			/*
			 * Load the ontology from an external .owl file.
			 */
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(owlfile));

			Injector injector = Guice.createInjector(new OBDACoreModule(preferences));
			NativeQueryLanguageComponentFactory nativeQLFactory = injector.getInstance(
					NativeQueryLanguageComponentFactory.class);

			/*
			 * Load the OBDA model from an external .r2rml file
			 */
			R2RMLManager rmanager = new R2RMLManager(r2rmlfile, nativeQLFactory);
			Model model = rmanager.getModel();

			DBMetadata dbMetadata = getMeta();

			qest1 = new SesameVirtualRepo("", ontology, model, dbMetadata, preferences);
		} else {
			qest1 = new SesameVirtualRepo("", owlfile, r2rmlfile, preferences);
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

	private void defTable(DBMetadata dbMetadata, String name) {
		QuotedIDFactory idfac = dbMetadata.getQuotedIDFactory();
		DatabaseRelationDefinition tableDefinition = dbMetadata.createDatabaseRelation(idfac.createRelationID(null, name));
		tableDefinition.addAttribute(idfac.createAttributeID("COL1"), java.sql.Types.INTEGER, null, false);
		tableDefinition.addAttribute(idfac.createAttributeID("COL2"), java.sql.Types.INTEGER, null, false);
	}
	private DBMetadata getMeta(){
		DBMetadata dbMetadata = RDBMetadataExtractionTools.createDummyMetadata("org.h2.Driver");
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
