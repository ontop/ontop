/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlapi3;

import org.semanticweb.owlapi.model.OWLException;

public interface OWLConnection {

	public void close() throws OWLException;

	public OWLStatement createStatement() throws OWLException;

	public void commit() throws OWLException;

	public void setAutoCommit(boolean autocommit) throws OWLException;

	public boolean getAutoCommit() throws OWLException;

	public boolean isClosed() throws OWLException;

	public boolean isReadOnly() throws OWLException;

	public void rollBack() throws OWLException;
}
