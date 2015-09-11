package org.semanticweb.ontop.parser;

import org.semanticweb.ontop.exception.InvalidMappingException;
import org.semanticweb.ontop.exception.InvalidPredicateDeclarationException;
import org.semanticweb.ontop.io.ModelIOManager;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;

import java.io.IOException;

import org.junit.Test;

public class ConcatTest {

	@Test
	public void testConcat() throws IOException,
			InvalidPredicateDeclarationException, InvalidMappingException {
		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
		OBDAModel obdaModel = fac.getOBDAModel();
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load("src/test/resources/format/obda/mapping-northwind.obda");
		//ioManager.load("src/test/resources/format/obda/mapping-northwind-h2-db-1422884770059.obda");
		//ioManager.save("src/test/resources/format/obda/mapping-northwind-h2-db-1422884770059.obda");
	}

}
