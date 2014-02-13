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
