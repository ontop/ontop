package it.unibz.inf.ontop.answering.connection.impl;

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
import java.util.Optional;

import it.unibz.inf.ontop.answering.connection.OntopConnection;
import it.unibz.inf.ontop.answering.connection.OntopStatement;
import it.unibz.inf.ontop.answering.reformulation.input.InputQueryFactory;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.answering.reformulation.IRIDictionary;
import it.unibz.inf.ontop.answering.reformulation.QueryReformulator;
import it.unibz.inf.ontop.injection.OntopSystemSQLSettings;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import org.apache.commons.rdf.api.RDF;

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
public class SQLConnection implements OntopConnection {

	private final QueryReformulator queryProcessor;
	private Connection conn;
	private final Optional<IRIDictionary> iriDictionary;
	private final DBMetadata dbMetadata;
	private final InputQueryFactory inputQueryFactory;
	private final TermFactory termFactory;
	private final TypeFactory typeFactory;
	private final OntopSystemSQLSettings settings;

	private final JDBCConnector jdbcConnector;
	private boolean isClosed;
	private final RDF rdfFactory;


	public SQLConnection(JDBCConnector jdbcConnector, QueryReformulator queryProcessor, Connection connection,
						 Optional<IRIDictionary> iriDictionary, DBMetadata dbMetadata,
						 InputQueryFactory inputQueryFactory, TermFactory termFactory, TypeFactory typeFactory,
						 RDF rdfFactory, OntopSystemSQLSettings settings) {
		this.jdbcConnector = jdbcConnector;
		this.queryProcessor = queryProcessor;
		this.conn = connection;
		this.iriDictionary = iriDictionary;
		this.dbMetadata = dbMetadata;
		this.inputQueryFactory = inputQueryFactory;
		this.termFactory = termFactory;
		this.typeFactory = typeFactory;
		this.settings = settings;
		this.rdfFactory = rdfFactory;
		this.isClosed = false;
	}
	
	@Override
	public void close() throws OntopConnectionException {
		try {
			conn.close();
		} catch (Exception e) {
			throw new OntopConnectionException(e);
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
					iriDictionary, dbMetadata, inputQueryFactory, termFactory, typeFactory, rdfFactory, settings);
		} catch (Exception e) {
			throw new OntopConnectionException(e);
		}
	}

	@Override
	public InputQueryFactory getInputQueryFactory() {
		return queryProcessor.getInputQueryFactory();
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
