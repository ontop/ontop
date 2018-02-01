package it.unibz.inf.ontop.temporal.mapping;


import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.OntopTemporalSQLOWLAPIConfiguration;
import junit.framework.TestCase;


public class TemporalMappingParserTest extends TestCase {

    public void testMapping(){

        String url = "jdbc:postgresql://obdalin.inf.unibz.it:5433/siemens_exp";
        String username = "postgres";
        String password = "postgres";

        //OntopTemporalMappingSQLAllConfiguration configuration = OntopTemporalMappingSQLAllConfiguration.defaultBuilder()
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

        //Injector injector = configuration.getInjector();
        //OntopNativeTemporalMappingParser tmParser = (OntopNativeTemporalMappingParser) injector.getInstance(SQLMappingParser.class);

        try {
            configuration.loadSpecification();
        } catch (OBDASpecificationException e) {
            e.printStackTrace();
        }

//        try {
//            tmParser.parse(new File("src/test/resources/siemens.tobda"));
//        } catch (InvalidMappingException e) {
//            e.printStackTrace();
//        } catch (DuplicateMappingException e) {
//            e.printStackTrace();
//        } catch (MappingIOException e) {
//            e.printStackTrace();
//        }

    }
}
