package it.unibz.inf.ontop.answering.resultset.impl;

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

import it.unibz.inf.ontop.answering.resultset.OntopBindingSet;
import it.unibz.inf.ontop.answering.resultset.TupleResultSet;

import java.util.List;
import java.util.NoSuchElementException;

public class EmptyTupleResultSet implements TupleResultSet {

	private final List<String> signature;

	public EmptyTupleResultSet(List<String> signature) {
		this.signature = signature;
	}

	@Override
	public void close() {
	}

	@Override
	public int getColumnCount()  {
		return signature.size();
	}

	@Override
	public int getFetchSize() {
		return 0;
	}

    @Override
    public OntopBindingSet next() {
        throw new NoSuchElementException();
    }

    @Override
	public List<String> getSignature() {
		return signature;
	}

	@Override
	public boolean hasNext()  {
		return false;
	}

}
