package it.unibz.krdb.obda.owlrefplatform.core.translator;

import it.unibz.krdb.obda.owlrefplatform.core.translator.UnknownFunctionSymbolException;

public class UnknownBooleanSymbolException extends UnknownFunctionSymbolException {

	private static final long serialVersionUID = 1L;

	public UnknownBooleanSymbolException(String unknownSymbol) {
		super(unknownSymbol);
	}
}
