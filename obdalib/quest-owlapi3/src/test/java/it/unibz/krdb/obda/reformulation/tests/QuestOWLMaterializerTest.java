package it.unibz.krdb.obda.reformulation.tests;

import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyAssertion;
import it.unibz.krdb.obda.ontology.ObjectPropertyAssertion;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.owlapi3.OWLAPI3Translator;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.abox.QuestMaterializer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuestOWLMaterializerTest extends TestCase {

	private Connection jdbcconn = null;

	private Logger log =  LoggerFactory.getLogger(this.getClass());

	private static QuestPreferences prefs;
	static {
		prefs = new QuestPreferences();
		prefs.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		prefs.setCurrentValueOf(QuestPreferences.REWRITE, QuestConstants.TRUE);
	}

	@Override
	public void setUp() throws Exception {
		createTables();
	}

	private String readSQLFile(String file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File(file)));
		StringBuffer bf = new StringBuffer();
		String line = reader.readLine();
		while (line != null) {
			bf.append(line + "\n");
			line = reader.readLine();
		}
		return bf.toString();
	}

	private void createTables() throws IOException, SQLException, URISyntaxException {
		String createDDL = readSQLFile("src/test/resources/test/materializer/createMaterializeTest.sql");

		// Initializing and H2 database with the data		
		// String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:questjunitdb";
		String username = "sa";
		String password = "";
		
		jdbcconn = DriverManager.getConnection(url, username, password);
		Statement st = jdbcconn.createStatement();

		st.executeUpdate(createDDL);
		jdbcconn.commit();
	}

	@Override
	public void tearDown() throws Exception {
		try {
			dropTables();
			// conn.close();
			jdbcconn.close();
		} catch (Exception e) {
			log.debug(e.getMessage());
		}
	}

	private void dropTables() throws SQLException, IOException {
		String dropDDL = readSQLFile("src/test/resources/test/materializer/dropMaterializeTest.sql");
		Statement st = jdbcconn.createStatement();
		st.executeUpdate(dropDDL);
		st.close();
		jdbcconn.commit();
	}

	public void testDataWithModel() {
		try {
			File f = new File("src/test/resources/test/materializer/MaterializeTest.obda");
			OBDAModel model = OBDADataFactoryImpl.getInstance().getOBDAModel();
			ModelIOManager man = new ModelIOManager(model);
			man.load(f);
			QuestMaterializer mat = new QuestMaterializer(model, prefs);
			Iterator<Assertion> iterator = mat.getAssertionIterator();
			int classAss = 0;
			int propAss = 0;
			int objAss = 0;
			while (iterator.hasNext()) {
				Assertion assertion = iterator.next();
				if (assertion instanceof ClassAssertion) {
					classAss++;
				} else if (assertion instanceof DataPropertyAssertion) {
					propAss++;
				} else if (assertion instanceof ObjectPropertyAssertion) {
					objAss++;
				}
			}
			Assert.assertEquals(3, classAss); //3 data rows for T1
			Assert.assertEquals(21, propAss); //7 tables * 3 data rows each T2-T8
			Assert.assertEquals(3, objAss); //3 data rows for T9
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void testDataWithModelAndOnto() {
		try {
			// read model
			File f = new File("src/test/resources/test/materializer/MaterializeTest.obda");
			OBDAModel model = OBDADataFactoryImpl.getInstance().getOBDAModel();
			ModelIOManager man = new ModelIOManager(model);
			man.load(f);
			
			// read onto 
			File fo = new File("src/test/resources/test/materializer/MaterializeTest.owl");
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology owl_onto = manager.loadOntologyFromOntologyDocument(fo);
			Ontology onto =  new OWLAPI3Translator().translate(owl_onto);
			System.out.println(onto.getAssertions());
			
			QuestMaterializer mat = new QuestMaterializer(model, onto, prefs);
			Iterator<Assertion> iterator = mat.getAssertionIterator();
			int classAss = 0;
			int propAss = 0;
			int objAss = 0;
			while (iterator.hasNext()) {
				Assertion assertion = iterator.next();
				if (assertion instanceof ClassAssertion) {
					classAss++;
				} else if (assertion instanceof DataPropertyAssertion) {
					propAss++;
				} else if (assertion instanceof ObjectPropertyAssertion) {
					objAss++;
				}
			}
			Assert.assertEquals(6, classAss); //3 data rows x2 for subclass prop
			Assert.assertEquals(42, propAss); //8 tables * 3 data rows each x2 for subclass
			Assert.assertEquals(3, objAss); //3 since no subprop for obj prop
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
