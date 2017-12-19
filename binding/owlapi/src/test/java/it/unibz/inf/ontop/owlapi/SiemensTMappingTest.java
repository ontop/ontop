package it.unibz.inf.ontop.owlapi;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.Properties;

public class SiemensTMappingTest extends TestCase {

    @Test
    public void testTMappings() throws Exception {

        String url = "jdbc:postgresql://obdalin.inf.unibz.it:5433/siemens_exp";
        String username = "postgres";
        String password = "postgres";

        Properties pref = new Properties();

        OntopSQLOWLAPIConfiguration configuration = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile("src/test/resources/siemens.obda")
                .ontologyFile("src/test/resources/siemens.owl")
                .properties(pref)
                .jdbcUrl(url)
                .jdbcUser(username)
                .jdbcPassword(password)
                .enableTestMode()
                .build();

        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopOWLReasoner reasoner = factory.createReasoner(configuration);
    }
}
