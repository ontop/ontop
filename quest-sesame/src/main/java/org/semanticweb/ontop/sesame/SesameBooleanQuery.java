package org.semanticweb.ontop.sesame;

/*
 * #%L
 * ontop-quest-sesame
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

import java.sql.SQLException;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.Dataset;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.semanticweb.ontop.model.Constant;
import org.semanticweb.ontop.model.OBDAException;
import org.semanticweb.ontop.model.OBDAQuery;
import org.semanticweb.ontop.model.OBDAQueryModifiers;
import org.semanticweb.ontop.model.TupleResultSet;
import org.semanticweb.ontop.owlrefplatform.core.QuestDBConnection;
import org.semanticweb.ontop.owlrefplatform.core.QuestDBStatement;
import org.semanticweb.ontop.owlrefplatform.core.QuestStatement;

public class SesameBooleanQuery implements BooleanQuery {

	private String queryString, baseURI;
	private QuestDBConnection conn;
	private SesameAbstractRepo repo; 
	
	public SesameBooleanQuery(String queryString, String baseURI, QuestDBConnection conn) throws MalformedQueryException {
		// check if valid query string
//		if (queryString.contains("ASK")) {
			this.queryString = queryString;
			this.baseURI = baseURI;
			this.conn = conn;

//		} else
//			throw new MalformedQueryException("Boolean Query expected!");
	}

	public boolean evaluate() throws QueryEvaluationException {
		TupleResultSet rs = null;
		QuestDBStatement stm = null;
		try {
			stm = conn.createStatement();
			rs = (TupleResultSet) stm.execute(queryString);
			boolean next = rs.nextRow();
			if (next){
				return true;
				
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			throw new QueryEvaluationException(e);
		} finally {
			try {
				if (rs != null)
				rs.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (stm != null)
				stm.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public int getMaxQueryTime() {
//		try {
//			return stm.getQueryTimeout();
//		} catch (OBDAException e) {
//			e.printStackTrace();
//		}
		return -1;
	}

	public void setMaxQueryTime(int maxQueryTime) {
//		try {
//			stm.setQueryTimeout(maxQueryTime);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

	}

	public void clearBindings() {
		// TODO Auto-generated method stub

	}

	public BindingSet getBindings() {
		// TODO Auto-generated method stub
		return null;
	}

	public Dataset getDataset() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean getIncludeInferred() {
		return true;
	}

	public void removeBinding(String name) {
		// TODO Auto-generated method stub

	}

	public void setBinding(String name, Value value) {
		// TODO Auto-generated method stub

	}

	public void setDataset(Dataset dataset) {
		// TODO Auto-generated method stub

	}

	public void setIncludeInferred(boolean includeInferred) {

	}

	public OBDAQueryModifiers getQueryModifiers() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setQueryModifiers(OBDAQueryModifiers modifiers) {
		// TODO Auto-generated method stub

	}

}
