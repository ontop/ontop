package it.unibz.inf.ontop.reformulation.tests;

import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWL;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLFactory;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class TMappingUOBMShortTest {

	@Test
	public void testTMappings() throws Exception {

		String url = "jdbc:h2:mem:uobm";
		String username = "sa";
		String password = "";
		
		Connection conn = DriverManager.getConnection(url, username, password);

		execute(conn, "src/test/resources/tmapping-uobm/univ-bench-dl.sql");

		Properties pref = new Properties();
		//pref.put(QuestCoreSettings.PRINT_KEYS, QuestConstants.TRUE);

		QuestOWLFactory factory = new QuestOWLFactory();
        QuestConfiguration config = QuestConfiguration.defaultBuilder()
				.nativeOntopMappingFile("src/test/resources/tmapping-uobm/univ-bench-dl.obda")
				.ontologyFile("src/test/resources/tmapping-uobm/univ-bench-dl.owl")
				.properties(pref)
				.jdbcUrl(url)
				.jdbcUser(username)
				.jdbcPassword(password)
				.enableExistentialReasoning(true)
				.build();
        QuestOWL reasoner = factory.createReasoner(config);
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
