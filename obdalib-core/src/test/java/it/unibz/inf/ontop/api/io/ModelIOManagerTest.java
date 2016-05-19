package it.unibz.inf.ontop.api.io;

import java.io.IOException;

import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.InvalidPredicateDeclarationException;
import it.unibz.inf.ontop.io.ModelIOManager;
import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;

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
