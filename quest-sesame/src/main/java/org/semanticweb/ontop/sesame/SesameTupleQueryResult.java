package org.semanticweb.ontop.sesame;

/*
 * #%L
 * ontop-quest-sesame
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


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.MapBindingSet;
import org.semanticweb.ontop.model.OBDAException;
import org.semanticweb.ontop.model.TupleResultSet;

public class SesameTupleQueryResult implements TupleQueryResult {

	TupleResultSet res;
	List<String> signature;
	Set<String> bindingNames;
	
	SesameTupleQueryResult(TupleResultSet res, List<String> signature){
		if(res == null)
			throw new NullPointerException();
		this.res = res;
		this.signature = signature;
		this.bindingNames = new HashSet<String>(signature);
	}
	
	@Override
	public void close() throws QueryEvaluationException {
		try {
			res.close();
		} catch (OBDAException e) {
			throw new QueryEvaluationException(e);
		}
	}

	@Override
	public boolean hasNext() throws QueryEvaluationException {
		try {
			return res.nextRow();
		} catch (OBDAException e) {
			throw new QueryEvaluationException(e);
		}
	}

	@Override
	public BindingSet next() throws QueryEvaluationException {
		MapBindingSet set = new MapBindingSet(this.signature.size() * 2);
		for (String name : this.signature) {
			Binding binding = createBinding(name, res, this.bindingNames);
			if (binding != null) {
				set.addBinding(binding);
			}
		}
		return set;
	}

	@Override
	public void remove() throws QueryEvaluationException {
		throw new QueryEvaluationException("The query result is read-only. Elements cannot be removed");
	}
	

	private Binding createBinding(String bindingName, TupleResultSet set, Set<String> bindingnames) {
		SesameBindingSet bset = new SesameBindingSet(set, bindingnames);
		return bset.getBinding(bindingName);
	}


	@Override
	public List<String> getBindingNames() throws QueryEvaluationException {
		return this.signature;
	}

}
