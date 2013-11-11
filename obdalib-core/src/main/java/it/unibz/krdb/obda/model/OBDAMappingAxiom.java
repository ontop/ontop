/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.model;

import java.io.Serializable;

public interface OBDAMappingAxiom extends Cloneable, Serializable {

	public void setSourceQuery(OBDAQuery query);

	public OBDAQuery getSourceQuery();

	public void setTargetQuery(OBDAQuery query);

	public OBDAQuery getTargetQuery();

	public Object clone();

	public void setId(String id);

	public String getId();
}
