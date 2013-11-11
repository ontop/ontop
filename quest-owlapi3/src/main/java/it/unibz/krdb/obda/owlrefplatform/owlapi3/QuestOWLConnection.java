/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano This source code is
 * available under the terms of the Affero General Public License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.owlapi3;

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
 * Besides parallel connections, at the moment, the class is not very usefull,
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

	/***
	 * Releases the connection object
	 * 
	 * @throws OWLException
	 */
	public void close() throws OWLException {
		try {
			conn.close();
		} catch (OBDAException e) {
			throw new OWLException(e) {
			};
		}

	}

	public QuestOWLStatement createStatement() throws OWLException {
		try {
			return new QuestOWLStatement(conn.createStatement(), this);
		} catch (OBDAException e) {
			throw new OWLException(e) {
			};
		}
	}

	public void commit() throws OWLException {
		try {
			conn.close();
		} catch (OBDAException e) {
			throw new OWLException(e) {
			};
		}

	}

	public void setAutoCommit(boolean autocommit) throws OWLException {
		try {
			conn.setAutoCommit(autocommit);
		} catch (OBDAException e) {
			throw new OWLException(e) {
			};
		}

	}

	public boolean getAutoCommit() throws OWLException {
		try {
			return conn.getAutoCommit();
		} catch (OBDAException e) {
			throw new OWLException(e) {
			};
		}
	}

	public boolean isClosed() throws OWLException {
		try {
			return conn.isClosed();
		} catch (OBDAException e) {
			throw new OWLException(e) {
			};
		}
	}

	public boolean isReadOnly() throws OWLException {
		try {
			return conn.isReadOnly();
		} catch (OBDAException e) {
			throw new OWLException(e) {
			};
		}
	}

	public void rollBack() throws OWLException {
		try {
			conn.rollBack();
		} catch (OBDAException e) {
			throw new OWLException(e) {
			};
		}

	}

}
