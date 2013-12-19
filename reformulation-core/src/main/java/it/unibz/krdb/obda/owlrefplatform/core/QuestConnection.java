package it.unibz.krdb.obda.owlrefplatform.core;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
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

import it.unibz.krdb.obda.model.OBDAConnection;
import it.unibz.krdb.obda.model.OBDAException;

import java.sql.Connection;
import java.sql.SQLException;

/***
 * Quest connection is responsible for wrapping a JDBC connection to the data
 * source. It will translate calls to OBDAConnection into JDBC Connection calls
 * (in most cases directly).
 * 
 * @author mariano
 * 
 */
public class QuestConnection implements OBDAConnection {

	protected Connection conn;

	private Quest questinstance;
	
	private boolean isClosed;

	public QuestConnection(Quest questisntance, Connection connection) {
		this.questinstance = questisntance;
		this.conn = connection;
		isClosed = false;
	}

	@Override
	public void close() throws OBDAException {
		try {
			//isClosed = true;
			//System.out.println("Closing Connection:" + this.conn.toString());
			//this.conn.close();
			questinstance.releaseSQLPoolConnection(conn);		
		} catch (Exception e) {
			throw new OBDAException(e);
		}

	}

	@Override
	public QuestStatement createStatement() throws OBDAException {
		try {
			//System.out.println("Creating Statement...");
			//conn = questinstance.getSQLPoolConnection();
			//System.out.println("Pool Connection:" + conn.toString());
			//isClosed = false;
			// create statement
			QuestStatement st = new QuestStatement(this.questinstance, this,
					conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
							java.sql.ResultSet.CONCUR_READ_ONLY));
			st.setFetchSize(400);
			return st;

		} catch (SQLException e1) {
			OBDAException obdaException = new OBDAException(e1);
			throw obdaException;
		}
	}

	
	
//	@Override
//	public QuestStatement createStatement() throws OBDAException {
//		//System.out.println("Creating Statement...");
//		try {
//			QuestStatement st = new QuestStatement(this.questinstance,
//						this, conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
//								java.sql.ResultSet.CONCUR_READ_ONLY));
//				return st;
//		} catch (SQLException e) {
//			
//			System.out.println("Error creating the statement: " + e.getMessage());
//			// statement was closed, connection broken, recreate
//			try {
//				if (conn.isClosed()) {
//					//recreate sql connection
//					conn = questinstance.getSQLPoolConnection();
//					//isClosed = false;
//					//create statement again once
//					QuestStatement st = new QuestStatement(this.questinstance,
//							this, conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
//									java.sql.ResultSet.CONCUR_READ_ONLY));
//					return st;
//				} else {
//					OBDAException obdaException = new OBDAException(e.getMessage());
//					obdaException.setStackTrace(e.getStackTrace());
//					throw obdaException;
//				}
//			} catch (SQLException e1) {
//				OBDAException obdaException = new OBDAException(e1.getMessage());
//				obdaException.setStackTrace(e1.getStackTrace());
//				throw obdaException;
//			}
//		}
//
//	}

	@Override
	public void commit() throws OBDAException {
		try {
			conn.commit();
		} catch (Exception e) {
			OBDAException obdaException = new OBDAException(e.getMessage());
			obdaException.setStackTrace(e.getStackTrace());
			throw obdaException;
		}
	}

	@Override
	public void setAutoCommit(boolean autocommit) throws OBDAException {
		try {
			conn.setAutoCommit(autocommit);
		} catch (Exception e) {
			OBDAException obdaException = new OBDAException(e.getMessage());
			obdaException.setStackTrace(e.getStackTrace());
			throw obdaException;
		}

	}

	@Override
	public boolean getAutoCommit() throws OBDAException {
		try {
			return conn.getAutoCommit();
		} catch (Exception e) {
			OBDAException obdaException = new OBDAException(e.getMessage());
			obdaException.setStackTrace(e.getStackTrace());
			throw obdaException;
		}
	}

	@Override
	public boolean isClosed() throws OBDAException {
		try {
			isClosed = conn.isClosed();
			return isClosed;
		} catch (Exception e) {
			
			OBDAException obdaException = new OBDAException(e);
			throw obdaException;
		}
	}

	@Override
	public boolean isReadOnly() throws OBDAException {
		if (this.questinstance.dataRepository == null)
			return true;
		try {
			return conn.isReadOnly();
		} catch (Exception e) {
			OBDAException obdaException = new OBDAException(e.getMessage());
			obdaException.setStackTrace(e.getStackTrace());
			throw obdaException;
		}
	}

	@Override
	public void rollBack() throws OBDAException {
		try {
			conn.rollback();
		} catch (Exception e) {
			OBDAException obdaException = new OBDAException(e.getMessage());
			obdaException.setStackTrace(e.getStackTrace());
			throw obdaException;
		}
	}

}
