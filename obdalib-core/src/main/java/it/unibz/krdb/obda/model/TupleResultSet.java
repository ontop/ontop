package it.unibz.krdb.obda.model;

/*
 * #%L
 * ontop-obdalib-core
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

import java.util.List;

public interface TupleResultSet extends ResultSet{

	/*
	 * ResultSet management functions
	 */

	public int getColumnCount() throws OBDAException;

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
