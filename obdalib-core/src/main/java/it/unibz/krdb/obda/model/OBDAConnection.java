/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.model;

public interface OBDAConnection {

	public void close() throws OBDAException;

	public OBDAStatement createStatement() throws OBDAException;

	public void commit() throws OBDAException;

	public void setAutoCommit(boolean autocommit) throws OBDAException;

	public boolean getAutoCommit() throws OBDAException;

	public boolean isClosed() throws OBDAException;

	public boolean isReadOnly() throws OBDAException;

	public void rollBack() throws OBDAException;
}
