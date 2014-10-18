package org.semanticweb.ontop.api.io;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.semanticweb.ontop.exception.InvalidMappingException;
import org.semanticweb.ontop.exception.InvalidPredicateDeclarationException;
import org.semanticweb.ontop.injection.NativeQueryLanguageComponentFactory;
import org.semanticweb.ontop.injection.OBDACoreModule;
import org.semanticweb.ontop.injection.OBDAProperties;
import org.semanticweb.ontop.mapping.MappingParser;

import org.junit.Test;

public class SQLMappingParserTest {

    private final NativeQueryLanguageComponentFactory factory;

    public SQLMappingParserTest() {
        Injector injector = Guice.createInjector(new OBDACoreModule(new OBDAProperties()));
        factory = injector.getInstance(NativeQueryLanguageComponentFactory.class);
    }

	@Test
	public void testSpaceBeforeEndCollectionSymbol() throws IOException,
            InvalidPredicateDeclarationException, InvalidMappingException {
		MappingParser mappingParser = factory.create(new File(
                "src/test/resources/format/obda/unusualCollectionEnding.obda"));
        mappingParser.getOBDAModel();
	}

	@Test(expected = IOException.class)
	public void testEndCollectionSymbolRequirement() throws IOException,
            InvalidPredicateDeclarationException, InvalidMappingException {
            MappingParser mappingParser = factory.create(new File(
                    "src/test/resources/format/obda/missingCollectionEnding.obda"));
            mappingParser.getOBDAModel();
	}

}
