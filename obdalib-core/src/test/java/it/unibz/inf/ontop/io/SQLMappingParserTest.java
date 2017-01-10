package it.unibz.inf.ontop.io;

import java.io.File;
import java.io.IOException;

import com.google.inject.Injector;
import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.injection.OBDACoreConfiguration;
import it.unibz.inf.ontop.mapping.MappingParser;

import org.junit.Test;

public class SQLMappingParserTest {

    private final NativeQueryLanguageComponentFactory factory;

    public SQLMappingParserTest() {
        OBDACoreConfiguration configuration = OBDACoreConfiguration.defaultBuilder().build();
        Injector injector = configuration.getInjector();
        factory = injector.getInstance(NativeQueryLanguageComponentFactory.class);
    }

	@Test
	public void testSpaceBeforeEndCollectionSymbol() throws Exception {
		MappingParser mappingParser = factory.create(new File(
                "src/test/resources/format/obda/unusualCollectionEnding.obda"));
        mappingParser.getOBDAModel();
	}

	@Test(expected = IOException.class)
	public void testEndCollectionSymbolRequirement() throws Exception {
            MappingParser mappingParser = factory.create(new File(
                    "src/test/resources/format/obda/missingCollectionEnding.obda"));
            mappingParser.getOBDAModel();
	}

}
