package it.unibz.inf.ontop.answering.connection;

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

import it.unibz.inf.ontop.exception.OntopConnectionException;

public interface OBDAConnection extends AutoCloseable {

	@Override
    void close() throws OntopConnectionException;

	OBDAStatement createStatement() throws OntopConnectionException;

	boolean isClosed() throws OntopConnectionException;

	//---------------------------
	// Transaction (read-only)
	//---------------------------

	/**
	 * Set it to false if you want a transaction
	 */
	void setAutoCommit(boolean autocommit) throws OntopConnectionException;

	boolean getAutoCommit() throws OntopConnectionException;

	void rollBack() throws OntopConnectionException;
	void commit() throws OntopConnectionException;
}
