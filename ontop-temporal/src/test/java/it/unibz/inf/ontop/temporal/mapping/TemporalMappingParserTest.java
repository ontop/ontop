package it.unibz.inf.ontop.temporal.mapping;

import com.google.inject.Injector;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.MappingIOException;
import it.unibz.inf.ontop.injection.*;
import it.unibz.inf.ontop.spec.mapping.MappingExtractor;
import it.unibz.inf.ontop.spec.mapping.impl.SQLMappingExtractor;
import it.unibz.inf.ontop.spec.mapping.parser.SQLMappingParser;
import junit.framework.TestCase;

import java.io.File;
import java.util.Properties;

public class TemporalMappingParserTest extends TestCase {

    public void testMapping(){

        String url = "jdbc:postgresql://obdalin.inf.unibz.it:5433/siemens_exp";
        String username = "postgres";
        String password = "postgres";

        Properties pref = new Properties();
        
        //OntopNativeTemporalMappingParser tmParser = new OntopNativeTemporalMappingParser(specificationFactory, ppMappingFactory);

        OntopMappingSQLTemporalConfiguration configuration = OntopMappingSQLTemporalConfiguration.defaultBuilder()
                .nativeOntopTemporalMappingFile("src/test/resources/siemens.tobda")
                .nativeOntopMappingFile("src/test/resources/siemens.obda")
                .jdbcUrl("fake_url")
                .jdbcUser("fake_user")
                .jdbcPassword("fake_password")
                .build();

        Injector injector = configuration.getInjector();
        OntopNativeTemporalMappingParser tmParser = (OntopNativeTemporalMappingParser) injector.getInstance(SQLMappingParser.class);

        MappingExtractor extractor = injector.getInstance(MappingExtractor.class);

        try {
            tmParser.parse(new File("src/test/resources/siemens.tobda"));
        } catch (InvalidMappingException e) {
            e.printStackTrace();
        } catch (DuplicateMappingException e) {
            e.printStackTrace();
        } catch (MappingIOException e) {
            e.printStackTrace();
        }

    }
}
