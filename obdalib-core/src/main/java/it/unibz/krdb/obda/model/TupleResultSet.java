/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.model;

import java.util.List;

public interface TupleResultSet extends ResultSet{

	/*
	 * ResultSet management functions
	 */

	public int getColumCount() throws OBDAException;

	public List<String> getSignature() throws OBDAException;

	public int getFetchSize() throws OBDAException;

	public void close() throws OBDAException;

	public OBDAStatement getStatement();

	public boolean nextRow() throws OBDAException;

	/*
	 * Main data fetching functions
	 */

	/***
	 * Returns the constant at column "column" recall that columns start at index 1.
	 * 
	 * @param column The column index of the value to be returned, start at 1
	 * @return
	 * @throws OBDAException
	 */
	public Constant getConstant(int column) throws OBDAException;

	public Constant getConstant(String name) throws OBDAException;

}
