package it.unibz.inf.ontop.reformulation.tests;

import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.injection.QuestCorePreferences;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWL;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLFactory;
import junit.framework.TestCase;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class MovieOntologyTest extends TestCase {

	private Connection conn;

	final String testCase = "movieontology";
	final String owlfile = "src/test/resources/test/treewitness/" + testCase + ".owl"; 
	final String obdafile = "src/test/resources/test/treewitness/" + testCase + ".obda";
	final String qfile = "src/test/resources/test/treewitness/" + testCase + ".q";


	@Override
	public void setUp() throws Exception {

		// String driver = "org.h2.Driver";
		conn = DriverManager.getConnection("jdbc:h2:mem:questjunitdb", "sa",  "");
		executeUpdate("src/test/resources/test/treewitness/imdb-schema-create-h2.sql");
	}

	
	@Override
	public void tearDown() throws Exception {
		executeUpdate("src/test/resources/test/treewitness/imdb-schema-drop-h2.sql");		
	}


	public void testOntologyLoad() throws Exception {

		Properties p = new Properties();
		p.setProperty(QuestCorePreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
		// p.setProperty(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC_INDEX);
		p.setProperty(QuestCorePreferences.ABOX_MODE, QuestConstants.VIRTUAL); // CLASSIC IS A TRIPLE STORE
		p.setProperty(QuestCorePreferences.OPTIMIZE_EQUIVALENCES, "true");
		//p.setProperty(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		p.setProperty("rewrite", "true");
	

		/*
		 * Create the instance of Quest OWL reasoner.
		 */
        QuestOWLFactory factory = new QuestOWLFactory();
        QuestConfiguration config = QuestConfiguration.defaultBuilder()
				.nativeOntopMappingFile(obdafile)
				.ontologyFile(owlfile)
				.properties(p)
				.build();
        QuestOWL reasoner = factory.createReasoner(config);

				
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

	

