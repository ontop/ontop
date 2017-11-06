package it.unibz.inf.ontop.temporal.mapping;


import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.*;
import junit.framework.TestCase;


public class TemporalMappingParserTest extends TestCase {

    public void testMapping(){

        String url = "jdbc:postgresql://obdalin.inf.unibz.it:5433/siemens_exp";
        String username = "postgres";
        String password = "postgres";

        OntopMappingSQLTemporalConfiguration configuration = OntopMappingSQLTemporalConfiguration.defaultBuilder()
                .nativeOntopTemporalMappingFile("src/test/resources/siemens.tobda")
                .nativeOntopMappingFile("src/test/resources/siemens.obda")
                .jdbcUrl(url)
                .jdbcUser(username)
                .jdbcPassword(password)
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
