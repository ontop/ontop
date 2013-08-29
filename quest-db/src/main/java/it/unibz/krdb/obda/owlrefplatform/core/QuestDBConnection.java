/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.core;

import it.unibz.krdb.obda.model.OBDAConnection;
import it.unibz.krdb.obda.model.OBDAException;

public class QuestDBConnection implements OBDAConnection {

	private final QuestConnection conn;

	public QuestDBConnection(QuestConnection conn) {
		this.conn = conn;
	}

	@Override
	public void close() throws OBDAException {
		conn.close();

	}

	@Override
	public QuestDBStatement createStatement() throws OBDAException {
		return new QuestDBStatement(conn.createStatement());
	}

	@Override
	public void commit() throws OBDAException {
		conn.commit();

	}

	@Override
	public void setAutoCommit(boolean autocommit) throws OBDAException {
		conn.setAutoCommit(autocommit);

	}

	@Override
	public boolean getAutoCommit() throws OBDAException {
		return conn.getAutoCommit();
	}

	@Override
	public boolean isClosed() throws OBDAException {
		return conn.isClosed();
	}

	@Override
	public boolean isReadOnly() throws OBDAException {
		return conn.isReadOnly();
	}

	@Override
	public void rollBack() throws OBDAException {
		conn.rollBack();

	}

}
