package it.unibz.inf.ontop.io;

import java.io.File;
import java.io.IOException;

import com.google.inject.Injector;
import it.unibz.inf.ontop.injection.OBDACoreConfiguration;
import it.unibz.inf.ontop.mapping.MappingParser;

import org.junit.Test;

public class SQLMappingParserTest {

    private final MappingParser mappingParser;

    public SQLMappingParserTest() {
        OBDACoreConfiguration configuration = OBDACoreConfiguration.defaultBuilder().build();
        Injector injector = configuration.getInjector();
        mappingParser = injector.getInstance(MappingParser.class);
    }

	@Test
	public void testSpaceBeforeEndCollectionSymbol() throws Exception {
        mappingParser.parse(new File(
                "src/test/resources/format/obda/unusualCollectionEnding.obda"));
	}

	@Test(expected = IOException.class)
	public void testEndCollectionSymbolRequirement() throws Exception {
            mappingParser.parse(new File(
                    "src/test/resources/format/obda/missingCollectionEnding.obda"));
	}

}
