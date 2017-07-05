package it.unibz.inf.ontop.temporal.mapping;

import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.MappingIOException;
import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.injection.SQLPPMappingFactory;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import junit.framework.TestCase;

import java.io.File;

public class TemporalMappingParserTest extends TestCase {

    public void testMapping(){
        NativeQueryLanguageComponentFactory nativeQLFactory = null;
        SQLPPMappingFactory ppMappingFactory = null;
        SpecificationFactory specificationFactory = null;
        
        OntopNativeTemporalMappingParser tmParser = new OntopNativeTemporalMappingParser(nativeQLFactory,specificationFactory, ppMappingFactory);

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
