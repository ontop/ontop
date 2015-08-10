package it.unibz.krdb.obda.reformulation.tests;

import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.OntologyVocabularyBuilder;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.Quest;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.abox.RDBMSSIRepositoryManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import junit.framework.TestCase;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class SemanticIndexMetadataTest  extends TestCase {

	private OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
	private Connection conn;

	private OBDAModel obdaModel = null;
	private OWLOntology ontology;

	private static final String testCase = "twr-predicate";
	private static final String owlfile = "src/test/resources/test/treewitness/" + testCase + ".owl"; 
	private static final String obdafile = "src/test/resources/test/treewitness/" + testCase + ".obda";


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

		// Loading the OBDA data
		obdaModel = fac.getOBDAModel();
		ModelIOManager ioManager = new ModelIOManager(obdaModel); 
		ioManager.load(new File(obdafile));
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
			QuestPreferences p = new QuestPreferences();
			p.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
			p.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC_INDEX);
			p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
			p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
			p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
			p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
			p.setCurrentValueOf(QuestPreferences.STORAGE_LOCATION, QuestConstants.INMEMORY);
			
			p.setProperty("rewrite", "true");

			OntologyFactory ofac = OntologyFactoryImpl.getInstance();
			OntologyVocabularyBuilder vb = ofac.createVocabularyBuilder();
						
			vb.declareClass("A");
			vb.declareObjectProperty("P");
			vb.declareDataProperty("P");
			vb.declareObjectProperty("Q");
			vb.declareDataProperty("D");

			Ontology ont = ofac.createOntology(vb);
			
			Quest quest = new Quest(ont, p);
			quest.setupRepository();
			
			RDBMSSIRepositoryManager si = quest.getSemanticIndexRepository();
			
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
