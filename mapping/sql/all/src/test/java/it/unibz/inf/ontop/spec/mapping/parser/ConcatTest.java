package it.unibz.inf.ontop.spec.mapping.parser;

import com.google.inject.Injector;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.MappingIOException;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;

import java.io.File;

import org.junit.Test;

public class ConcatTest {

	@Test
	public void testConcat() throws InvalidMappingException, MappingIOException {
		OntopMappingSQLAllConfiguration configuration = OntopMappingSQLAllConfiguration.defaultBuilder()
				.propertyFile("src/test/resources/format/obda/mapping-northwind.properties")
				.build();
		Injector injector = configuration.getInjector();

		SQLMappingParser mappingParser = injector.getInstance(SQLMappingParser.class);
		mappingParser.parse(new File("src/test/resources/format/obda/mapping-northwind.obda"));
	}

}
