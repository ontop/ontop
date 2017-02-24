package it.unibz.inf.ontop.parser;

import com.google.inject.Injector;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;
import it.unibz.inf.ontop.mapping.MappingParser;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class ConcatTest {

	@Test
	public void testConcat() throws DuplicateMappingException, InvalidMappingException, IOException {
		OntopMappingSQLAllConfiguration configuration = OntopMappingSQLAllConfiguration.defaultBuilder()
				.propertyFile("src/test/resources/format/obda/mapping-northwind.properties")
				.build();
		Injector injector = configuration.getInjector();

		MappingParser mappingParser = injector.getInstance(MappingParser.class);
		mappingParser.parse(new File("src/test/resources/format/obda/mapping-northwind.obda"));
	}

}
