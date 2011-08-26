/***
 * Copyright (c) 2008, Mariano Rodriguez-Muro.
 * All rights reserved.
 *
 * The OBDA-API is licensed under the terms of the Lesser General Public
 * License v.3 (see OBDAAPI_LICENSE.txt for details). The components of this
 * work include:
 *
 * a) The OBDA-API developed by the author and licensed under the LGPL; and,
 * b) third-party components licensed under terms that may be different from
 *   those of the LGPL.  Information about such licenses can be found in the
 *   file named OBDAAPI_3DPARTY-LICENSES.txt.
 */
package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.obda.model.OBDARDBMappingAxiom;
import it.unibz.krdb.obda.model.OBDASQLQuery;

import java.security.InvalidParameterException;


public class RDBMSMappingAxiomImpl extends AbstractOBDAMappingAxiom implements OBDARDBMappingAxiom {

	private OBDASQLQuery sourceQuery = null;
	private CQIEImpl targetQuery = null;

//	public RDBMSMappingAxiom(String id) {
//		super(id);
//	}

	protected RDBMSMappingAxiomImpl(String id, OBDAQuery sourceQuery, OBDAQuery targetQuery) {
		super(id);
		setSourceQuery(sourceQuery);
		setTargetQuery(targetQuery);
	}

	/* (non-Javadoc)
	 * @see inf.unibz.it.obda.model.impl.RDBMSMappingAxiom#setSourceQuery(inf.unibz.it.obda.model.Query)
	 */
	@Override
	public void setSourceQuery(OBDAQuery query) {
		if (!(query instanceof OBDASQLQuery)) {
			throw new InvalidParameterException("RDBMSDataSourceMapping must receive a RDBMSSQLQuery as source query");
		}
		this.sourceQuery = (OBDASQLQuery) query;
	}

	/* (non-Javadoc)
	 * @see inf.unibz.it.obda.model.impl.RDBMSMappingAxiom#setTargetQuery(inf.unibz.it.obda.model.Query)
	 */
	@Override
	public void setTargetQuery(OBDAQuery query) {
		if (!(query instanceof CQIEImpl)) {
			throw new InvalidParameterException("RDBMSDataSourceMapping must receive a OntologyQuery as target query");
		}
		this.targetQuery = (CQIEImpl) query;
	}


	/* (non-Javadoc)
	 * @see inf.unibz.it.obda.model.impl.RDBMSMappingAxiom#getSourceQuery()
	 */
	@Override
	public OBDASQLQuery getSourceQuery() {
		return sourceQuery;
	}

	/* (non-Javadoc)
	 * @see inf.unibz.it.obda.model.impl.RDBMSMappingAxiom#getTargetQuery()
	 */
	@Override
	public CQIEImpl getTargetQuery() {
		return targetQuery;
	}


	/* (non-Javadoc)
	 * @see inf.unibz.it.obda.model.impl.RDBMSMappingAxiom#clone()
	 */
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
