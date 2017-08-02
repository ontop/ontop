package it.unibz.inf.ontop.temporal.mapping;

import com.google.inject.Injector;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.MappingIOException;
import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;
import it.unibz.inf.ontop.injection.SQLPPMappingFactory;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.mapping.SQLMappingParser;
import junit.framework.TestCase;

import java.io.File;

public class TemporalMappingParserTest extends TestCase {

    public void testMapping(){
        NativeQueryLanguageComponentFactory nativeQLFactory = null;
        SQLPPMappingFactory ppMappingFactory = null;
        SpecificationFactory specificationFactory = null;
        
        OntopNativeTemporalMappingParser tmParser = new OntopNativeTemporalMappingParser(nativeQLFactory,specificationFactory, ppMappingFactory);

        OntopMappingSQLAllConfiguration configuration = OntopMappingSQLAllConfiguration.defaultBuilder()
                .jdbcUrl("fake_url")
                .jdbcUser("fake_user")
                .jdbcPassword("fake_password")
                .build();

        Injector injector = configuration.getInjector();
        //tmParser = injector.getInstance(SQLMappingParser.class);
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
