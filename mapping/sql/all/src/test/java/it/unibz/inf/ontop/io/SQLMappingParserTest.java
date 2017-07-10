package it.unibz.inf.ontop.io;

import it.unibz.inf.ontop.exception.MappingIOException;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;

import org.junit.Test;

public class SQLMappingParserTest {

	@Test
	public void testSpaceBeforeEndCollectionSymbol() throws Exception {
        OntopMappingSQLAllConfiguration configuration = createConfiguration(
                "src/test/resources/format/obda/unusualCollectionEnding.obda");
        configuration.loadProvidedPPMapping();
	}

	@Test(expected = MappingIOException.class)
	public void testEndCollectionSymbolRequirement() throws Exception {
        OntopMappingSQLAllConfiguration configuration = createConfiguration(
                "src/test/resources/format/obda/missingCollectionEnding.obda");
        configuration.loadProvidedPPMapping();
	}

	private static OntopMappingSQLAllConfiguration createConfiguration(String obdaFile) {
        return OntopMappingSQLAllConfiguration.defaultBuilder()
                .nativeOntopMappingFile(obdaFile)
                .propertyFile("src/test/resources/format/obda/collectionEnding.properties")
                .build();
    }
}
