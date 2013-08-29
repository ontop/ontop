/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.OBDAMappingAxiom;

public abstract class AbstractOBDAMappingAxiom implements OBDAMappingAxiom {

	private static final long serialVersionUID = 5512895151633505075L;

	private String id = "";
	
	public AbstractOBDAMappingAxiom(String id) {
		this.id = id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}
	
	public abstract Object clone();
}
