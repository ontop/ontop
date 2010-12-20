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
package inf.unibz.it.obda.rdbmsgav.domain;

import inf.unibz.it.obda.domain.AbstractOBDAMappingAxiom;
import inf.unibz.it.obda.domain.VariableMap;

import java.security.InvalidParameterException;

import org.obda.query.domain.Query;
import org.obda.query.domain.imp.CQIEImpl;

public class RDBMSOBDAMappingAxiom extends AbstractOBDAMappingAxiom {

	private RDBMSSQLQuery sourceQuery = null;
	private CQIEImpl targetQuery = null;

	public RDBMSOBDAMappingAxiom(String id) {
		super(id);
	}

	public RDBMSOBDAMappingAxiom(String id, Query sourceQuery, Query targetQuery) {
		super(id);
		setSourceQuery(sourceQuery);
		setTargetQuery(targetQuery);
	}

	/***
	 * @param query An RDBMSSQLQuery object.
	 */
	public void setSourceQuery(Query query) {
		if (!(query instanceof RDBMSSQLQuery)) {
			throw new InvalidParameterException("RDBMSDataSourceMapping must receive a RDBMSSQLQuery as source query");
		}
		this.sourceQuery = (RDBMSSQLQuery) query;
	}

	/***
	 * @param query An OntologyQuery object;
	 */
	public void setTargetQuery(Query query) {
		if (!(query instanceof CQIEImpl)) {
			throw new InvalidParameterException("RDBMSDataSourceMapping must receive a OntologyQuery as target query");
		}
		this.targetQuery = (CQIEImpl) query;
	}

	/****
	 * Warning it doesn't do anything at the moment
	 */
	public void setVariableMappings(VariableMap map) {
		//TODO remove or keep method
	}

	public RDBMSSQLQuery getSourceQuery() {
		return sourceQuery;
	}

	public CQIEImpl getTargetQuery() {
		return targetQuery;
	}

	/****
	 * Warning doesnt do anything at the moment
	 */
	public VariableMap getVariableMappings() {
		// TODO Remove or keep method
		return null;
	}

	@Override
	public RDBMSOBDAMappingAxiom clone() {
		RDBMSOBDAMappingAxiom clone = new RDBMSOBDAMappingAxiom(new String(this.getId()));
		clone.setSourceQuery(sourceQuery.clone());
		clone.setTargetQuery(targetQuery.clone());

		return clone;
	}
}
