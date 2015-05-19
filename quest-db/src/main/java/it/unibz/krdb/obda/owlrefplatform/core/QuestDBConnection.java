package it.unibz.krdb.obda.owlrefplatform.core;

/*
 * #%L
 * ontop-quest-db
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

	public Connection getConnection() {
		return conn.getConnection();
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
