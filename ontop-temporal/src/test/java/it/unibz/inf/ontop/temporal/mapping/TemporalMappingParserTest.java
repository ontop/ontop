package it.unibz.inf.ontop.temporal.mapping;


import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.OntopTemporalSQLOWLAPIConfiguration;
import junit.framework.TestCase;


public class TemporalMappingParserTest extends TestCase {

    public void testMapping(){

        String url = "jdbc:postgresql://obdalin.inf.unibz.it:5433/siemens_exp";
        String username = "postgres";
        String password = "postgres";

        OntopTemporalSQLOWLAPIConfiguration configuration = OntopTemporalSQLOWLAPIConfiguration.defaultBuilder()
                .ontologyFile("src/test/resources/siemens.owl")
                .nativeOntopTemporalMappingFile("src/test/resources/siemens.tobda")
                .nativeOntopMappingFile("src/test/resources/siemens.obda")
                .nativeOntopTemporalRuleFile("src/test/resources/rule.dmtl")
                .jdbcUrl(url)
                .jdbcUser(username)
                .jdbcPassword(password)
                .enableTestMode()
                .build();

        try {
            configuration.loadSpecification();
        } catch (OBDASpecificationException e) {
            e.printStackTrace();
        }

    }
}
