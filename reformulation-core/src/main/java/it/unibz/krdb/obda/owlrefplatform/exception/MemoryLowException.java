/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.exception;

public class MemoryLowException extends Exception {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 2004858092400545767L;

	public MemoryLowException(String msg) {
		super(msg);
	}

	public MemoryLowException() {
		super();
	}
}
