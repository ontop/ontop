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

import it.unibz.krdb.obda.model.Query;
import it.unibz.krdb.obda.model.RDBMSMappingAxiom;
import it.unibz.krdb.obda.model.SQLQuery;

import java.security.InvalidParameterException;


public class RDBMSMappingAxiomImpl extends AbstractOBDAMappingAxiom implements RDBMSMappingAxiom {

	private SQLQuery sourceQuery = null;
	private CQIEImpl targetQuery = null;

//	public RDBMSMappingAxiom(String id) {
//		super(id);
//	}

	protected RDBMSMappingAxiomImpl(String id, Query sourceQuery, Query targetQuery) {
//		super(id);
		setSourceQuery(sourceQuery);
		setTargetQuery(targetQuery);
	}

	/* (non-Javadoc)
	 * @see inf.unibz.it.obda.model.impl.RDBMSMappingAxiom#setSourceQuery(inf.unibz.it.obda.model.Query)
	 */
	@Override
	public void setSourceQuery(Query query) {
		if (!(query instanceof SQLQuery)) {
			throw new InvalidParameterException("RDBMSDataSourceMapping must receive a RDBMSSQLQuery as source query");
		}
		this.sourceQuery = (SQLQuery) query;
	}

	/* (non-Javadoc)
	 * @see inf.unibz.it.obda.model.impl.RDBMSMappingAxiom#setTargetQuery(inf.unibz.it.obda.model.Query)
	 */
	@Override
	public void setTargetQuery(Query query) {
		if (!(query instanceof CQIEImpl)) {
			throw new InvalidParameterException("RDBMSDataSourceMapping must receive a OntologyQuery as target query");
		}
		this.targetQuery = (CQIEImpl) query;
	}


	/* (non-Javadoc)
	 * @see inf.unibz.it.obda.model.impl.RDBMSMappingAxiom#getSourceQuery()
	 */
	@Override
	public SQLQuery getSourceQuery() {
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
	public RDBMSMappingAxiom clone() {
		RDBMSMappingAxiom clone = new RDBMSMappingAxiomImpl(this.getId(), sourceQuery.clone(),targetQuery.clone());
		
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
