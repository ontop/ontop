package it.unibz.krdb.obda.api.io;

import java.io.IOException;

import it.unibz.krdb.obda.exception.InvalidMappingException;
import it.unibz.krdb.obda.exception.InvalidPredicateDeclarationException;
import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

import org.junit.Test;

public class ModelIOManagerTest {

	@Test
	public void testSpaceBeforeEndCollectionSymbol() throws IOException,
			InvalidPredicateDeclarationException, InvalidMappingException {
		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
		OBDAModel obdaModel = fac.getOBDAModel();
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager
				.load("src/test/resources/format/obda/unusualCollectionEnding.obda");
	}

	@Test(expected = IOException.class)
	public void testEndCollectionSymbolRequirement() throws IOException,
			InvalidPredicateDeclarationException, InvalidMappingException {
		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
		OBDAModel obdaModel = fac.getOBDAModel();
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager
				.load("src/test/resources/format/obda/missingCollectionEnding.obda");
	}

}
