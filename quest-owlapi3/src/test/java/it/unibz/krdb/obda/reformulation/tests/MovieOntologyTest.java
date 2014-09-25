package it.unibz.krdb.obda.reformulation.tests;

import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.TestCase;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

public class MovieOntologyTest extends TestCase {

	private Connection conn;

	private OBDAModel obdaModel = null;
	private OWLOntology ontology;

	final String testCase = "movieontology";
	final String owlfile = "src/test/resources/test/treewitness/" + testCase + ".owl"; 
	final String obdafile = "src/test/resources/test/treewitness/" + testCase + ".obda";
	final String qfile = "src/test/resources/test/treewitness/" + testCase + ".q";


	@Override
	public void setUp() throws Exception {

		// String driver = "org.h2.Driver";
		conn = DriverManager.getConnection("jdbc:h2:mem:questjunitdb", "sa",  "");
		executeUpdate("src/test/resources/test/treewitness/imdb-schema-create-h2.sql");		

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));
		
		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
		
		// Loading the OBDA data
		obdaModel = fac.getOBDAModel();
		ModelIOManager ioManager = new ModelIOManager(obdaModel); 		
		ioManager.load(new File(obdafile)); 
	}

	
	@Override
	public void tearDown() throws Exception {
		executeUpdate("src/test/resources/test/treewitness/imdb-schema-drop-h2.sql");		
	}


	public void testOntologyLoad() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
		// p.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC_INDEX);
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL); // CLASSIC IS A TRIPLE STORE
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		//p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setProperty("rewrite", "true");
	

		/*
		 * Create the instance of Quest OWL reasoner.
		 */
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);
		factory.setPreferenceHolder(p);
		QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());
		
				
		//for (Entry<URI, ArrayList<OBDAMappingAxiom>> m: obdaModel.getMappings().entrySet()) {
		//	System.out.println(m.getKey());
		//	for (OBDAMappingAxiom mm :  m.getValue()) {
		//		System.out.println(mm);
		//	}
		//}
			
		
		boolean fail = false;

		reasoner.dispose();

		assertFalse(fail);
	}
	
	private void executeUpdate(String filename) {
		Statement st;
		try {
			st = conn.createStatement();
			FileReader reader = new FileReader(filename);
			BufferedReader in = new BufferedReader(reader);
			StringBuilder bf = new StringBuilder();
			String line = in.readLine();
			while (line != null) {
				bf.append(line);
				bf.append("\n");
				line = in.readLine();
				if (line !=null && line.isEmpty()) {
					st.execute(bf.toString());
					conn.commit();		
					bf = new StringBuilder();
				}
			}
			in.close();
			st.execute(bf.toString());
			conn.commit();		
			
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
}

	

