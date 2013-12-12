package it.unibz.krdb.obda.owlrefplatform.core.translator;

public abstract class UnknownFunctionSymbolException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private String unknownSymbol;

	public UnknownFunctionSymbolException(String unknownSymbol) {
		this.unknownSymbol = unknownSymbol;
	}

	@Override
	public String getMessage() {
		return "Found unknown function symbol: " + unknownSymbol;
	}
}
