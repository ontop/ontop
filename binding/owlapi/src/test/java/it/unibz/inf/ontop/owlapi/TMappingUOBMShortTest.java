package it.unibz.inf.ontop.owlapi;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.impl.SimpleOntopOWLEngine;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import static it.unibz.inf.ontop.utils.OWLAPITestingTools.executeFromFile;

public class TMappingUOBMShortTest {

	@Test
	public void testTMappings() throws Exception {

		String url = "jdbc:h2:mem:uobm";
		String username = "sa";
		String password = "";
		
		Connection conn = DriverManager.getConnection(url, username, password);

		executeFromFile(conn, "src/test/resources/tmapping-uobm/univ-bench-dl.sql");

		Properties pref = new Properties();
		//pref.put(QuestCoreSettings.PRINT_KEYS, QuestConstants.TRUE);

        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.nativeOntopMappingFile("src/test/resources/tmapping-uobm/univ-bench-dl.obda")
				.ontologyFile("src/test/resources/tmapping-uobm/univ-bench-dl.owl")
				.properties(pref)
				.jdbcUrl(url)
				.jdbcUser(username)
				.jdbcPassword(password)
				.enableExistentialReasoning(true)
				.enableTestMode()
				.build();
		OntopOWLEngine reasoner = new SimpleOntopOWLEngine(config);
	}
}
