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
import inf.unibz.it.obda.domain.SourceQuery;
import inf.unibz.it.obda.domain.TargetQuery;
import inf.unibz.it.obda.domain.VariableMap;
import inf.unibz.it.ucq.domain.ConjunctiveQuery;
import inf.unibz.it.ucq.parser.exception.QueryParseException;

import java.security.InvalidParameterException;

public class RDBMSOBDAMappingAxiom extends AbstractOBDAMappingAxiom {
	


	RDBMSSQLQuery source_query = null;
	ConjunctiveQuery target_query = null;
	
	public RDBMSOBDAMappingAxiom(String id) throws QueryParseException {
		super(id);
		source_query = new RDBMSSQLQuery();
		target_query = new ConjunctiveQuery();
	}
	
	/***
	 * @param query An RDBMSSQLQuery object.
	 */
	public void setSourceQuery(SourceQuery query) {
		if (query == null) {
			target_query = null;
			return;
		}
		if (!(query instanceof RDBMSSQLQuery)) {
			throw new InvalidParameterException("RDBMSDataSourceMapping must receive a RDBMSSQLQuery as source query");
		}
		source_query = (RDBMSSQLQuery)query;
	}

	/***
	 * @param query An OntologyQuery object;
	 */
	public void setTargetQuery(TargetQuery query) {
		if (query == null) {
			target_query = null;
			return;
		}
		if (!(query instanceof ConjunctiveQuery)) {
			throw new InvalidParameterException("RDBMSDataSourceMapping must receive a OntologyQuery as target query");
		}
		target_query = (ConjunctiveQuery)query;
		
	}

	
	/**** 
	 * Warning it doesnt do anything at the moment
	 */
	public void setVariableMappings(VariableMap map) {
		//TODO remove or keep method
		
	}

	
	public RDBMSSQLQuery getSourceQuery() {
		return source_query;
	}

	
	public ConjunctiveQuery getTargetQuery() {
		return target_query;
	}

	
	/****
	 * Warning doesnt do anything at the moment
	 */
	public VariableMap getVariableMappings() {
		// TODO Remove or keep method
		return null;
	}
	
	public RDBMSOBDAMappingAxiom clone() {
		RDBMSOBDAMappingAxiom clone;
		try {
			clone = new RDBMSOBDAMappingAxiom(new String(this.getId()));
		} catch (QueryParseException e) {
			// this hsould never happen
			throw new RuntimeException(e);
		}
		clone.setSourceQuery(source_query.clone());
		clone.setTargetQuery(target_query.clone());
		return clone;
	}

}
