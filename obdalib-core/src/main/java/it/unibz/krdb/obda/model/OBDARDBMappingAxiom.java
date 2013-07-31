/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.model;

public interface OBDARDBMappingAxiom extends OBDAMappingAxiom {

	/**
	 * Set the source query for this mapping axiom.
	 * 
	 * @param query
	 *            a SQL Query object.
	 */
	public void setSourceQuery(OBDAQuery query);

	/**
	 * Set the target query for this mapping axiom.
	 * 
	 * @param query
	 *            a conjunctive query object;
	 */
	public void setTargetQuery(OBDAQuery query);

	public OBDASQLQuery getSourceQuery();

	public CQIE getTargetQuery();

	public OBDARDBMappingAxiom clone();
}
