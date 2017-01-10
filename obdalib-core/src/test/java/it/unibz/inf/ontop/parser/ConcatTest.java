package it.unibz.inf.ontop.parser;

import com.google.inject.Injector;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.injection.OBDACoreConfiguration;
import it.unibz.inf.ontop.io.InvalidDataSourceException;
import it.unibz.inf.ontop.mapping.MappingParser;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class ConcatTest {

	@Test
	public void testConcat() throws DuplicateMappingException, InvalidMappingException, InvalidDataSourceException, IOException {
		OBDACoreConfiguration configuration = OBDACoreConfiguration.defaultBuilder().build();
		Injector injector = configuration.getInjector();

		NativeQueryLanguageComponentFactory nativeQLFactory = injector.getInstance(
				NativeQueryLanguageComponentFactory.class);

		MappingParser mappingParser = nativeQLFactory.create(new File("src/test/resources/format/obda/mapping-northwind.obda"));
		mappingParser.getOBDAModel();
	}

}
