package it.unibz.krdb.obda.owlrefplatform.owlapi3;

/*
 * #%L
 * ontop-quest-owlapi3
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

import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConnection;
import it.unibz.krdb.obda.owlrefplatform.core.QuestStatement;

import org.semanticweb.owlapi.model.OWLException;

/***
 * Handler for a connection. Note that as with JDBC, executing queries in
 * parallels over a single connection is inefficient, since JDBC drivers will
 * serialize each query execution and you get a bottle neck (even if using
 * multiple {@link QuestOWLStatment}) . Having explicit QuestOWLConnections
 * allows to initialize a single QuestOWLReasoner and make multiple queries in
 * parallel with good performance (as with JDBC).
 * 
 * <p>
 * Besides parallel connections, at the moment, the class is not very useful,
 * it will be when transactions and updates are implemented. Or when we allow
 * the user to setup the kind of statement that he wants to use.
 * 
 * <p>
 * Internally, this class is mostly an OWLAPI specific wrapper for the API
 * agnostic {@link QuestStatement}.
 * 
 * @author Mariano Rodriguez Muro <mariano.muro@gmail.com>
 * 
 * @see QuestOWLStatement
 * @see QuestOWL
 * @see QuestStatement
 */
public class QuestOWLConnection {

	private final QuestConnection conn;

	public QuestOWLConnection(QuestConnection conn) {
		this.conn = conn;
	}

	@Deprecated // used in one test only
	public Connection getConnection() {
		return conn.getConnection();
	}
	
	/***
	 * Releases the connection object
	 * 
	 * @throws OWLException
	 */
	public void close() throws OWLException {
		try {
			conn.close();
		} catch (OBDAException e) {
			throw new OWLException(e); 
		}

	}

	public QuestOWLStatement createStatement() throws OWLException {
		try {
			return new QuestOWLStatement(conn.createStatement(), this);
		} catch (OBDAException e) {
			throw new OWLException(e); 
		}
	}

	public void commit() throws OWLException {
		try {
			conn.close();
		} catch (OBDAException e) {
			throw new OWLException(e); 
		}
	}

	public void setAutoCommit(boolean autocommit) throws OWLException {
		try {
			conn.setAutoCommit(autocommit);
		} catch (OBDAException e) {
			throw new OWLException(e); 
		}
	}

	public boolean getAutoCommit() throws OWLException {
		try {
			return conn.getAutoCommit();
		} catch (OBDAException e) {
			throw new OWLException(e);
		}
	}

	public boolean isClosed() throws OWLException {
		try {
			return conn.isClosed();
		} catch (OBDAException e) {
			throw new OWLException(e);
		}
	}

	public boolean isReadOnly() throws OWLException {
		try {
			return conn.isReadOnly();
		} catch (OBDAException e) {
			throw new OWLException(e);
		}
	}

	public void rollBack() throws OWLException {
		try {
			conn.rollBack();
		} catch (OBDAException e) {
			throw new OWLException(e); 
		}
	}

}
