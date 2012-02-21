package it.unibz.krdb.obda.owlrefplatform.core;

import it.unibz.krdb.obda.model.OBDAConnection;
import it.unibz.krdb.obda.model.OBDAException;

import java.sql.Connection;

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

	public QuestConnection(Quest questisntance, Connection connection) {
		this.questinstance = questisntance;
		this.conn = connection;
	}

	@Override
	public void close() throws OBDAException {
		try {
			this.conn.close();
		} catch (Exception e) {
			throw new OBDAException(e);
		}

	}

	@Override
	public QuestStatement createStatement() throws OBDAException {
		try {
			QuestStatement st = new QuestStatement(this.questinstance, this, conn.createStatement());
			return st;
		} catch (Exception e) {
			throw new OBDAException(e);
		}

	}

	@Override
	public void commit() throws OBDAException {
		try {
			conn.commit();
		} catch (Exception e) {
			throw new OBDAException(e);
		}
	}

	@Override
	public void setAutoCommit(boolean autocommit) throws OBDAException {
		try {
			conn.setAutoCommit(autocommit);
		} catch (Exception e) {
			throw new OBDAException(e);
		}

	}

	@Override
	public boolean getAutoCommit() throws OBDAException {
		try {
			return conn.getAutoCommit();
		} catch (Exception e) {
			throw new OBDAException(e);
		}
	}

	@Override
	public boolean isClosed() throws OBDAException {
		try {
			return conn.isClosed();
		} catch (Exception e) {
			throw new OBDAException(e);
		}
	}

	@Override
	public boolean isReadOnly() throws OBDAException {
		if (this.questinstance.dataRepository == null)
			return true;
		try {
			return conn.isReadOnly();
		} catch (Exception e) {
			throw new OBDAException(e);
		}
	}

	@Override
	public void rollBack() throws OBDAException {
		try {
			conn.rollback();
		} catch (Exception e) {
			throw new OBDAException(e);
		}
	}

}
