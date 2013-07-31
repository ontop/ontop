/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.Term;

public abstract class AbstractLiteral implements Term {

	private static final long serialVersionUID = 626920825158789773L;

	private Term parent = null;

	public void setParent(Term parent) {
		this.parent = parent;
	}

	public Term getParent() {
		return parent;
	}

	public abstract Term clone();
}
