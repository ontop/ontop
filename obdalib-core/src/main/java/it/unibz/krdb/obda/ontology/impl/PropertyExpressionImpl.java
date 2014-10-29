package it.unibz.krdb.obda.ontology.impl;

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

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.PropertyExpression;

public class PropertyExpressionImpl implements PropertyExpression {

	private static final long serialVersionUID = -2514037755762973974L;
	
	private final boolean isInverse;
	private final Predicate predicate;
	private final String string;
	private final PropertyExpressionImpl inverseProperty;

	PropertyExpressionImpl(Predicate p, boolean isInverse) {
		this.predicate = p;
		this.isInverse = isInverse;
		this.inverseProperty = new PropertyExpressionImpl(p, !isInverse, this);
		StringBuilder bf = new StringBuilder();
		bf.append(predicate.toString());
		if (isInverse) 
			bf.append("^-");
		this.string =  bf.toString();
	}

	private PropertyExpressionImpl(Predicate p, boolean isInverse, PropertyExpressionImpl inverseProperty) {
		this.predicate = p;
		this.isInverse = isInverse;
		this.inverseProperty = inverseProperty;
		StringBuilder bf = new StringBuilder();
		bf.append(predicate.toString());
		if (isInverse) 
			bf.append("^-");
		this.string =  bf.toString();
	}

	@Override
	public boolean isInverse() {
		return isInverse;
	}

	@Override
	public Predicate getPredicate() {
		return predicate;
	}
	
	@Override
	public PropertyExpression getInverse() {
		return inverseProperty;
	}	

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PropertyExpressionImpl) {
			PropertyExpressionImpl other = (PropertyExpressionImpl) obj;
			return (isInverse == other.isInverse) && predicate.equals(other.predicate);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return string.hashCode();
	}
	
	@Override
	public String toString() {
		return string;
	}
}
