/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package sesameWrapper;

import java.sql.SQLException;

import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.obda.model.OBDAQueryModifiers;
import it.unibz.krdb.obda.model.TupleResultSet;
import it.unibz.krdb.obda.owlrefplatform.core.QuestDBConnection;
import it.unibz.krdb.obda.owlrefplatform.core.QuestDBStatement;
import it.unibz.krdb.obda.owlrefplatform.core.QuestStatement;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.Dataset;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;

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
