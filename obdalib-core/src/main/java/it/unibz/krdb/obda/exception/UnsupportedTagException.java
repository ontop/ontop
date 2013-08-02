/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.exception;

import java.io.IOException;

public class UnsupportedTagException extends IOException {

	private static final long serialVersionUID = 1L;

	private String tagName;
	
	public UnsupportedTagException(String tagName) {
		super();
		this.tagName = tagName;
	}
	
	@Override
    public String getMessage() {
		return "The tag " + tagName + " is no longer supported. You may safely remove the content from the file.";
	}
}
