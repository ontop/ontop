package org.semanticweb.ontop.parser;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.semanticweb.ontop.exception.DuplicateMappingException;
import org.semanticweb.ontop.exception.InvalidMappingException;
import org.semanticweb.ontop.injection.NativeQueryLanguageComponentFactory;
import org.semanticweb.ontop.injection.OBDACoreModule;
import org.semanticweb.ontop.injection.OBDAProperties;
import org.semanticweb.ontop.io.InvalidDataSourceException;
import org.semanticweb.ontop.mapping.MappingParser;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class ConcatTest {

	@Test
	public void testConcat() throws DuplicateMappingException, InvalidMappingException, InvalidDataSourceException, IOException {


		Injector injector = Guice.createInjector(new OBDACoreModule(new OBDAProperties()));
		NativeQueryLanguageComponentFactory nativeQLFactory = injector.getInstance(
				NativeQueryLanguageComponentFactory.class);

		MappingParser mappingParser = nativeQLFactory.create(new File("src/test/resources/format/obda/mapping-northwind.obda"));
		mappingParser.getOBDAModel();
	}

}
