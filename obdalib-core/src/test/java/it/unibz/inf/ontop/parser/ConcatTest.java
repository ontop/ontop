package it.unibz.inf.ontop.parser;

import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.InvalidPredicateDeclarationException;
import it.unibz.inf.ontop.io.ModelIOManager;
import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;

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
