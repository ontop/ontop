package it.unibz.krdb.obda.model.impl;

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


/**
 * For supporting sql string functions, presently only CONCAT and REPLACE
 * 
 * @author Dag Hovland
 */
import it.unibz.krdb.obda.model.StringOperationPredicate;


public class StringOperationPredicateImpl extends PredicateImpl implements StringOperationPredicate {

	
	private static final long serialVersionUID = 5119873524278488136L;


    public StringOperationPredicateImpl(int arity){
        super(OBDAVocabulary.CONCAT_STR, arity, null);
    }

	protected StringOperationPredicateImpl(String name, int arity, COL_TYPE[] types) {
		super(name, arity, types);
	}


	@Override
	public StringOperationPredicate clone() {
		return this;
	}
}
