/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.core.translator;

import it.unibz.krdb.obda.owlrefplatform.core.translator.UnknownFunctionSymbolException;

public class UnknownBooleanSymbolException extends UnknownFunctionSymbolException {

	private static final long serialVersionUID = 1L;

	public UnknownBooleanSymbolException(String unknownSymbol) {
		super(unknownSymbol);
	}
}
