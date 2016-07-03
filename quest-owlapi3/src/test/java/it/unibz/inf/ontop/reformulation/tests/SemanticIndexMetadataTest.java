package it.unibz.inf.ontop.reformulation.tests;

import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.ontology.OntologyFactory;
import it.unibz.inf.ontop.ontology.OntologyVocabulary;
import it.unibz.inf.ontop.ontology.impl.OntologyFactoryImpl;
import it.unibz.inf.ontop.owlrefplatform.core.Quest;
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.owlrefplatform.core.QuestPreferences;
import it.unibz.inf.ontop.owlrefplatform.core.abox.RDBMSSIRepositoryManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWL;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLConfiguration;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLFactory;
import junit.framework.TestCase;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class SemanticIndexMetadataTest  extends TestCase {
	private Connection conn;

	private OWLOntology ontology;

	private static final String testCase = "twr-predicate";
	private static final String owlfile = "src/test/resources/test/treewitness/" + testCase + ".owl"; 
	//private static final String obdafile = "src/test/resources/test/treewitness/" + testCase + ".obda";


	@Override
	public void setUp() throws Exception {
		/*
		 * Initializing and H2 database 
		 */
		String url = "jdbc:h2:mem:questjunitdb";
		String username = "sa";
		String password = "";

		conn = DriverManager.getConnection(url, username, password);
		Statement st = conn.createStatement();

		FileReader reader = new FileReader("src/test/resources/test/treewitness/bsbm-schema-create-mysql.sql");
		BufferedReader in = new BufferedReader(reader);
		StringBuilder bf = new StringBuilder();
		String line = in.readLine();
		while (line != null) {
			bf.append(line);
			line = in.readLine();
		}
		in.close();

		//st.executeUpdate(bf.toString());
		st.close();
		conn.commit();

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));
	}

	@Override
	public void tearDown() throws Exception {
		Statement st = conn.createStatement();

		FileReader reader = new FileReader("src/test/resources/test/treewitness/bsbm-schema-drop-mysql.sql");
		BufferedReader in = new BufferedReader(reader);
		StringBuilder bf = new StringBuilder();
		String line = in.readLine();
		while (line != null) {
			bf.append(line);
			line = in.readLine();
		}
		in.close();

		st.executeUpdate(bf.toString());
		st.close();
		conn.commit();

		conn.close();
	}

	public void testSemanticIndexMetadata() throws Exception {

		//prepareTestQueries(tuples);
		{
			Properties p = new Properties();
			p.put(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
			p.put(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC_INDEX);
			p.put(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
			p.put(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
			p.put(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
			p.put(QuestPreferences.STORAGE_LOCATION, QuestConstants.INMEMORY);
			
			p.setProperty("rewrite", "true");

			OntologyFactory ofac = OntologyFactoryImpl.getInstance();
			OntologyVocabulary vb = ofac.createVocabulary();

			vb.createClass("A");
			vb.createObjectProperty("P");
			vb.createDataProperty("P");
			vb.createObjectProperty("Q");
			vb.createDataProperty("D");

			Ontology ont = ofac.createOntology(vb);

			QuestOWLFactory factory = new QuestOWLFactory();
			QuestOWLConfiguration config = QuestOWLConfiguration.builder()
					.properties(p)
					.build();
			QuestOWL reasoner = factory.createReasoner(ontology, config);
			
			RDBMSSIRepositoryManager si = reasoner.getQuestInstance().getOptionalSemanticIndexRepository().get();
			
			si.createDBSchemaAndInsertMetadata(conn);

			
			Statement st = conn.createStatement();
			ResultSet res = st.executeQuery("SELECT * FROM IDX ORDER BY IDX");
			while (res.next()) {
				String string = res.getString(1);
				int idx = res.getInt(2);
				int type = res.getInt(3);
				System.out.println(string + ", " + idx + ", "+ type);
			}
			st.close();
			
			// load metadata back from the DB
			si.loadMetadata(conn);	
			

			
		}
		
	}


}
