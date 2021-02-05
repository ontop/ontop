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

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.answering.logging.QueryLogger;
import it.unibz.inf.ontop.answering.resultset.OntopBindingSet;
import it.unibz.inf.ontop.answering.resultset.TupleResultSet;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.List;
import java.util.NoSuchElementException;

public class EmptyTupleResultSet implements TupleResultSet {

	private final ImmutableList<String> signature;
	private final QueryLogger queryLogger;
	private boolean hasNextCalled = false;

	public EmptyTupleResultSet(ImmutableList<Variable> answerVariables,
							   QueryLogger queryLogger) {
		this.signature = answerVariables.stream()
				.map(Variable::getName)
				.collect(ImmutableCollectors.toList());

		this.queryLogger = queryLogger;
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
	public boolean isConnectionAlive() throws OntopConnectionException {
		return false;
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
		if (!hasNextCalled)
			queryLogger.declareLastResultRetrievedAndSerialize(0);
		hasNextCalled = true;
		return false;
	}

}
