package org.semanticweb.ontop.model.impl;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.security.InvalidParameterException;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.OBDAQuery;
import org.semanticweb.ontop.model.OBDARDBMappingAxiom;
import org.semanticweb.ontop.model.OBDASQLQuery;
import org.semanticweb.ontop.utils.IDGenerator;

public class RDBMSMappingAxiomImpl extends AbstractOBDAMappingAxiom implements OBDARDBMappingAxiom {

	private static final long serialVersionUID = 5793656631843898419L;
	
	private OBDASQLQuery sourceQuery = null;
	private CQIE targetQuery = null;

	@AssistedInject
	protected RDBMSMappingAxiomImpl(@Assisted String id, @Assisted("sourceQuery") OBDAQuery sourceQuery,
									@Assisted("targetQuery") OBDAQuery targetQuery) {
		super(id);
		setSourceQuery(sourceQuery);
		setTargetQuery(targetQuery);
	}

	@AssistedInject
	private RDBMSMappingAxiomImpl(@Assisted("sourceQuery") OBDAQuery sourceQuery,
								  @Assisted("targetQuery") OBDAQuery targetQuery) {
		this(new String(IDGenerator.getNextUniqueID("MAPID-")), sourceQuery, targetQuery);
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
	public CQIE getTargetQuery() {
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
