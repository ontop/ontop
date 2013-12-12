package it.unibz.krdb.obda.owlrefplatform.core.translator;

import it.unibz.krdb.obda.owlrefplatform.core.translator.UnknownFunctionSymbolException;

public class UnknownArithmeticSymbolException extends UnknownFunctionSymbolException {

	private static final long serialVersionUID = 1L;

	public UnknownArithmeticSymbolException(String unknownSymbol) {
		super(unknownSymbol);
	}
}
