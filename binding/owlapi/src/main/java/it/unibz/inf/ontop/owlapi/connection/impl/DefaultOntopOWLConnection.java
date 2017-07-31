package it.unibz.inf.ontop.owlapi.connection.impl;

/*
 * #%L
 * ontop-quest-owlapi
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

import it.unibz.inf.ontop.answering.reformulation.input.InputQueryFactory;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.answering.connection.OntopConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.semanticweb.owlapi.model.OWLException;


public class DefaultOntopOWLConnection implements OntopOWLConnection {

	private final OntopConnection conn;
	private final InputQueryFactory inputQueryFactory;

	public DefaultOntopOWLConnection(OntopConnection conn, InputQueryFactory inputQueryFactory) {
		this.conn = conn;
		this.inputQueryFactory = inputQueryFactory;
	}

	@Override
	public OntopOWLStatement createStatement() throws OWLException {
		try {
			return new DefaultOntopOWLStatement(conn.createStatement(), inputQueryFactory);
		} catch (OntopConnectionException e) {
			throw new OWLException(e);
		}
	}
	
	/***
	 * Releases the connection object
	 * 
	 * @throws OWLException
	 */
	@Override
	public void close() throws OWLException {
		try {
			conn.close();
		} catch (OntopConnectionException e) {
			throw new OWLException(e); 
		}
	}

	@Override
	public boolean isClosed() throws OWLException {
		try {
			return conn.isClosed();
		} catch (OntopConnectionException e) {
			throw new OWLException(e);
		}
	}

	@Override
	public void commit() throws OWLException {
		try {
			conn.close();
		} catch (OntopConnectionException e) {
			throw new OWLException(e);
		}
	}

	@Override
	public void setAutoCommit(boolean autocommit) throws OWLException {
		try {
			conn.setAutoCommit(autocommit);
		} catch (OntopConnectionException e) {
			throw new OWLException(e);
		}
	}

	@Override
	public boolean getAutoCommit() throws OWLException {
		try {
			return conn.getAutoCommit();
		} catch (OntopConnectionException e) {
			throw new OWLException(e);
		}
	}


	@Override
	public void rollBack() throws OWLException {
		try {
			conn.rollBack();
		} catch (OntopConnectionException e) {
			throw new OWLException(e);
		}
	}


}
