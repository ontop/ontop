/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.NewLiteral;

public abstract class AbstractLiteral implements NewLiteral {

	private static final long serialVersionUID = 626920825158789773L;

	private NewLiteral parent = null;

	public void setParent(NewLiteral parent) {
		this.parent = parent;
	}

	public NewLiteral getParent() {
		return parent;
	}

	public abstract NewLiteral clone();
}
