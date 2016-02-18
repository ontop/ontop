package it.unibz.krdb.ontop.parser;

import it.unibz.krdb.ontop.exception.InvalidMappingException;
import it.unibz.krdb.ontop.exception.InvalidPredicateDeclarationException;
import it.unibz.krdb.ontop.io.ModelIOManager;
import it.unibz.krdb.ontop.model.OBDADataFactory;
import it.unibz.krdb.ontop.model.OBDAModel;
import it.unibz.krdb.ontop.model.impl.OBDADataFactoryImpl;

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
