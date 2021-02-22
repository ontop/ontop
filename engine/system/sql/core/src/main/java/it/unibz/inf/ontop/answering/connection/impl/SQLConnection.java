package it.unibz.inf.ontop.answering.connection.impl;

import java.sql.Connection;

import it.unibz.inf.ontop.answering.connection.JDBCStatementInitializer;
import it.unibz.inf.ontop.answering.connection.OntopConnection;
import it.unibz.inf.ontop.answering.connection.OntopStatement;
import it.unibz.inf.ontop.answering.reformulation.input.InputQueryFactory;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.answering.reformulation.QueryReformulator;
import it.unibz.inf.ontop.injection.OntopSystemSQLSettings;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
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
	private final TermFactory termFactory;
	private final SubstitutionFactory substitutionFactory;
	private final OntopSystemSQLSettings settings;

	private final JDBCConnector jdbcConnector;
	private boolean isClosed;
	private final RDF rdfFactory;
	private final JDBCStatementInitializer statementInitializer;


	public SQLConnection(JDBCConnector jdbcConnector, QueryReformulator queryProcessor, Connection connection,
						 TermFactory termFactory, RDF rdfFactory, SubstitutionFactory substitutionFactory,
						 JDBCStatementInitializer statementInitializer,
						 OntopSystemSQLSettings settings) {
		this.jdbcConnector = jdbcConnector;
		this.queryProcessor = queryProcessor;
		this.conn = connection;
		this.termFactory = termFactory;
		this.substitutionFactory = substitutionFactory;
		this.statementInitializer = statementInitializer;
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
					statementInitializer.createAndInitStatement(conn),
					statementInitializer,
					termFactory, rdfFactory, substitutionFactory, settings);
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
