/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.owlapi3;

import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.owlapi3.OWLConnection;
import it.unibz.krdb.obda.owlapi3.OWLStatement;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConnection;

import org.semanticweb.owlapi.model.OWLException;

public class QuestOWLConnection implements OWLConnection {

	private final QuestConnection conn;

	public QuestOWLConnection(QuestConnection conn) {
		this.conn = conn;
	}

	@Override
	public void close() throws OWLException {
		try {
			conn.close();
		} catch (OBDAException e) {
			throw new OWLException(e) {
			};
		}

	}

	@Override
	public OWLStatement createStatement() throws OWLException {
		try {
			return new QuestOWLStatement(conn.createStatement(), this);
		} catch (OBDAException e) {
			throw new OWLException(e) {
			};
		}
	}

	@Override
	public void commit() throws OWLException {
		try {
			conn.close();
		} catch (OBDAException e) {
			throw new OWLException(e) {
			};
		}

	}

	@Override
	public void setAutoCommit(boolean autocommit) throws OWLException {
		try {
			conn.setAutoCommit(autocommit);
		} catch (OBDAException e) {
			throw new OWLException(e) {
			};
		}

	}

	@Override
	public boolean getAutoCommit() throws OWLException {
		try {
			return conn.getAutoCommit();
		} catch (OBDAException e) {
			throw new OWLException(e) {
			};
		}
	}

	@Override
	public boolean isClosed() throws OWLException {
		try {
			return conn.isClosed();
		} catch (OBDAException e) {
			throw new OWLException(e) {
			};
		}
	}

	@Override
	public boolean isReadOnly() throws OWLException {
		try {
			return conn.isReadOnly();
		} catch (OBDAException e) {
			throw new OWLException(e) {
			};
		}
	}

	@Override
	public void rollBack() throws OWLException {
		try {
			conn.rollBack();
		} catch (OBDAException e) {
			throw new OWLException(e) {
			};
		}

	}

}
