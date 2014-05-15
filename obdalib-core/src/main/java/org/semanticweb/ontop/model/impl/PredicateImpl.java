package org.semanticweb.ontop.model.impl;

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

import org.semanticweb.ontop.model.AlgebraOperatorPredicate;
import org.semanticweb.ontop.model.BooleanOperationPredicate;
import org.semanticweb.ontop.model.DataTypePredicate;
import org.semanticweb.ontop.model.NumericalOperationPredicate;
import org.semanticweb.ontop.model.Predicate;

public class PredicateImpl implements Predicate {

	private static final long serialVersionUID = -7096056207721170465L;

	private int arity = -1;
	private String name = null;
	private int identifier = -1;
	private COL_TYPE[] types = null;

	protected PredicateImpl(String name, int arity, COL_TYPE[] types) {
		this.name = name;
		this.identifier = name.hashCode();
		this.arity = arity;
		this.types = types;
	}

	@Override
	public int getArity() {
		return arity;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof PredicateImpl)) {
			return false;
		}
		PredicateImpl pred2 = (PredicateImpl) obj;
		return this.identifier == pred2.identifier;
	}

	@Override
	public int hashCode() {
		return identifier;
	}

	@Override
	public Predicate clone() {
		return this;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public COL_TYPE getType(int column) {
		if (types != null) {
			return types[column];
		}
		return null;
	}

	@Override
	public boolean isClass() {
		if (arity == 1 && types[0] == COL_TYPE.OBJECT) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isObjectProperty() {
		if (arity == 2 && types[0] == COL_TYPE.OBJECT && types[1] == COL_TYPE.OBJECT) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isDataProperty() {
		if (arity == 2 && types[0] == COL_TYPE.OBJECT && types[1] == COL_TYPE.LITERAL) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isDataPredicate() {
		return (!(isBooleanPredicate() || isAlgebraPredicate() || isArithmeticPredicate() || isDataTypePredicate()));
	}

	@Override
	public boolean isBooleanPredicate() {
		return this instanceof BooleanOperationPredicate;
	}
	
	@Override
	public boolean isArithmeticPredicate() {
		return this instanceof NumericalOperationPredicate;
	}

	@Override
	public boolean isAlgebraPredicate() {
		return this instanceof AlgebraOperatorPredicate;
	}

	@Override
	public boolean isDataTypePredicate() {
		return this instanceof DataTypePredicate;
	}

	@Override
	public boolean isTriplePredicate() {
		if (arity == 3 && name.equals(OBDAVocabulary.QUEST_TRIPLE_STR)) {
			return true;
		}
		return false;
	}
}
