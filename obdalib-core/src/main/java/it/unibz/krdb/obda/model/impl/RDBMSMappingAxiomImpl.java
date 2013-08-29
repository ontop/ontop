/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.obda.model.OBDARDBMappingAxiom;
import it.unibz.krdb.obda.model.OBDASQLQuery;

import java.security.InvalidParameterException;

public class RDBMSMappingAxiomImpl extends AbstractOBDAMappingAxiom implements OBDARDBMappingAxiom {

	private static final long serialVersionUID = 5793656631843898419L;
	
	private OBDASQLQuery sourceQuery = null;
	private CQIEImpl targetQuery = null;

	protected RDBMSMappingAxiomImpl(String id, OBDAQuery sourceQuery, OBDAQuery targetQuery) {
		super(id);
		setSourceQuery(sourceQuery);
		setTargetQuery(targetQuery);
	}

	@Override
	public void setSourceQuery(OBDAQuery query) {
		if (!(query instanceof OBDASQLQuery)) {
			throw new InvalidParameterException("RDBMSDataSourceMapping must receive a RDBMSSQLQuery as source query");
		}
		this.sourceQuery = (OBDASQLQuery) query;
	}

	@Override
	public void setTargetQuery(OBDAQuery query) {
		if (!(query instanceof CQIEImpl)) {
			throw new InvalidParameterException("RDBMSDataSourceMapping must receive a OntologyQuery as target query");
		}
		this.targetQuery = (CQIEImpl) query;
	}

	@Override
	public OBDASQLQuery getSourceQuery() {
		return sourceQuery;
	}

	@Override
	public CQIEImpl getTargetQuery() {
		return targetQuery;
	}

	@Override
	public OBDARDBMappingAxiom clone() {
		OBDARDBMappingAxiom clone = new RDBMSMappingAxiomImpl(this.getId(), sourceQuery.clone(),targetQuery.clone());
		return clone;
	}
	
	@Override
	public String toString() {
		StringBuffer bf = new StringBuffer();
		bf.append(sourceQuery.toString());
		bf.append(" ==> ");
		bf.append(targetQuery.toString());
		return bf.toString();
	}
}
