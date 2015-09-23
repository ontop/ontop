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
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

public class TMappingUOBMShortTest {

	@Test
	public void testTMappings() throws Exception {

		String url = "jdbc:h2:mem:uobm";
		String username = "sa";
		String password = "";

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology owlOnto = manager.loadOntologyFromOntologyDocument(new File("src/test/resources/tmapping-uobm/univ-bench-dl.owl")); 		
		
		Connection conn = DriverManager.getConnection(url, username, password);

		execute(conn, "src/test/resources/tmapping-uobm/univ-bench-dl.sql");
		
		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
		OBDAModel obdaModel = fac.getOBDAModel();
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load("src/test/resources/tmapping-uobm/univ-bench-dl.obda");

		QuestPreferences pref = new QuestPreferences();
		//pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC_INDEX);
		pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
		pref.setCurrentValueOf(QuestPreferences.REWRITE, QuestConstants.TRUE);
		pref.setCurrentValueOf(QuestPreferences.PRINT_KEYS, QuestConstants.TRUE);

		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);
		factory.setPreferenceHolder(pref);
				
		QuestOWL reasoner = factory.createReasoner(owlOnto, new SimpleConfiguration());
		
	}
	
	private static void execute(Connection conn, String filename) throws IOException, SQLException {		
		
		Statement st = conn.createStatement();
		int i = 1;
		
		FileReader reader = new FileReader(filename);
		
		StringBuilder bf = new StringBuilder();
		try (BufferedReader in = new BufferedReader(reader)) {
			for (String line = in.readLine(); line != null; line = in.readLine()) {
				bf.append(line + "\n");
				if (line.startsWith("--")) {
					System.out.println("EXECUTING " + i++ + ":\n" + bf.toString());
					st.executeUpdate(bf.toString());
					conn.commit();
					bf = new StringBuilder();
				}
			}
		}
	}
	
}
