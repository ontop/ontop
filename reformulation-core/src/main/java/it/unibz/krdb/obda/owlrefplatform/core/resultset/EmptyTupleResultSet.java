package it.unibz.krdb.obda.owlrefplatform.core.resultset;

/*
 * #%L
 * ontop-reformulation-core
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

import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAStatement;
import it.unibz.krdb.obda.model.TupleResultSet;

import java.util.List;
import java.util.NoSuchElementException;

public class EmptyTupleResultSet implements TupleResultSet {

	List<String> signature = null;
	private OBDAStatement st;

	public EmptyTupleResultSet(List<String> signature, OBDAStatement st) {
		this.signature = signature;
		this.st = st;
	}

	@Override
	public void close() throws OBDAException {
	}

	@Override
	public int getColumnCount() throws OBDAException {
		return signature.size();
	}

	@Override
	public int getFetchSize() throws OBDAException {
		return 0;
	}

	@Override
	public List<String> getSignature() {
		return signature;
	}

	@Override
	public boolean nextRow() throws OBDAException {
		return false;
	}

	@Override
	public OBDAStatement getStatement() {
		return st;
	}

	@Override
	public Constant getConstant(int column) {
        throw new NoSuchElementException();
	}

	@Override
	public Constant getConstant(String name) {
        throw new NoSuchElementException();
	}
}
