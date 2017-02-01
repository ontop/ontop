package it.unibz.inf.ontop.owlrefplatform.core;

/*
 * #%L
 * ontop-reformulation-core
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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.model.OBDAException;
import it.unibz.inf.ontop.answering.reformulation.IRIDictionary;
import it.unibz.inf.ontop.answering.reformulation.OntopQueryReformulator;

/***
 * Quest connection is responsible for wrapping a JDBC connection to the data
 * source. It will translate calls to OBDAConnection into JDBC Connection calls
 * (in most cases directly).
 *
 * SQL-specific implementation (specific to the JDBCConnector)!
 *
 * TODO: rename it SQLQuestConnection
 *
 * @author mariano
 * 
 */
public class QuestConnection implements OntopConnection {

	private final OntopQueryReformulator queryProcessor;
	private Connection conn;
	private final Optional<IRIDictionary> iriDictionary;

	private final JDBCConnector jdbcConnector;
	private boolean isClosed;


	public QuestConnection(JDBCConnector jdbcConnector, OntopQueryReformulator queryProcessor, Connection connection,
						   Optional<IRIDictionary> iriDictionary) {
		this.jdbcConnector = jdbcConnector;
		this.queryProcessor = queryProcessor;
		this.conn = connection;
		this.iriDictionary = iriDictionary;
		this.isClosed = false;
	}
	
	@Override
	public void close() throws OBDAException {
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			//throw new OBDAException(e);
		}
	}

	@Override
	public OntopStatement createStatement() throws OntopConnectionException {
		try {
			if (conn.isClosed()) {
				// Sometimes it gets dropped, reconnect
				conn = jdbcConnector.getSQLPoolConnection();
			}
			return new SQLQuestStatement(
					this.queryProcessor,
					conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY),
					iriDictionary);
		} catch (SQLException e1) {
			throw new OntopConnectionException(e1);
		}
	}

	@Override
	public void commit() throws OntopConnectionException {
		try {
			conn.commit();
		} catch (Exception e) {
			throw new OntopConnectionException(e);
		}
	}

	@Override
	public void setAutoCommit(boolean autocommit) throws OntopConnectionException {
		try {
			conn.setAutoCommit(autocommit);
		} catch (Exception e) {
			throw new OntopConnectionException(e);
		}

	}

	@Override
	public boolean getAutoCommit() throws OntopConnectionException {
		try {
			return conn.getAutoCommit();
		} catch (Exception e) {
			throw new OntopConnectionException(e);
		}
	}

	@Override
	public boolean isClosed() throws OntopConnectionException {
		try {
			isClosed = conn.isClosed();
			return isClosed;
		} catch (Exception e) {
			throw new OntopConnectionException(e);
		}
	}

	@Override
	public void rollBack() throws OntopConnectionException {
		try {
			conn.rollback();
		} catch (Exception e) {
			throw new OntopConnectionException(e);
		}
	}

}
