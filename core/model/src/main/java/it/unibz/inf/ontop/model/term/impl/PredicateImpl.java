package it.unibz.inf.ontop.model.term.impl;

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

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.type.COL_TYPE;
import it.unibz.inf.ontop.model.type.TermType;

import javax.annotation.Nonnull;

import java.util.Optional;

import static it.unibz.inf.ontop.model.IriConstants.CANONICAL_IRI;
import static it.unibz.inf.ontop.model.IriConstants.SAME_AS;

public class PredicateImpl implements Predicate {

	private final ImmutableList<TermType> expectedBaseTypes;

	private int arity = -1;
	private String name = null;
	private int identifier = -1;

	protected PredicateImpl(@Nonnull String name, int arity, @Nonnull ImmutableList<TermType> expectedBaseTypes) {
		if (expectedBaseTypes.size() != arity)
			throw new IllegalArgumentException("expectedBaseTypes.size() must be equal to the arity");
		this.name = name;
		this.identifier = name.hashCode();
		this.arity = arity;
		this.expectedBaseTypes = expectedBaseTypes;
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
	public COL_TYPE getColType(int column) {
		return Optional.of(expectedBaseTypes.get(column))
				.flatMap(TermType::getOptionalColType)
				.orElse(null);
	}

	@Override
	public TermType getExpectedBaseType(int index) {
		return expectedBaseTypes.get(index);
	}

	@Override
	public ImmutableList<TermType> getExpectedBaseArgumentTypes() {
		return expectedBaseTypes;
	}

	@Override
	public boolean isClass() {
		return (arity == 1 && getColType(0) == COL_TYPE.OBJECT);
	}

	@Override
	public boolean isObjectProperty() {
		return (arity == 2 && getColType(0) == COL_TYPE.OBJECT && getColType(1) == COL_TYPE.OBJECT);
	}

	@Override
	public boolean isAnnotationProperty() {
		return (arity == 2 && getColType(0) == COL_TYPE.OBJECT && getColType(1) == COL_TYPE.NULL);
	}

	@Override
	@Deprecated
	public boolean isDataProperty() {
		return (arity == 2 && getColType(0) == COL_TYPE.OBJECT && getColType(1) == COL_TYPE.LITERAL);
	}

	@Override
	public boolean isSameAsProperty() {
		return (arity == 2 && name.equals(SAME_AS) && getColType(0) == COL_TYPE.OBJECT && getColType(1) == COL_TYPE.OBJECT);
	}

	@Override
	public boolean isCanonicalIRIProperty() {
		return (arity == 2 && name.equals(CANONICAL_IRI) && getColType(0) == COL_TYPE.OBJECT && getColType(1) == COL_TYPE.OBJECT);
	}


	@Override
	public boolean isTriplePredicate() {
		return (arity == 3 && name.equals("triple"));
	}

//    @Override
//    public boolean isAggregationPredicate() {
//        // The arity is supposed to be one
//        // but we prefer robustness to
//        // ill-defined arities
//
//        switch(getName()) {
//            case OBDAVocabulary.SPARQL_AVG_URI:
//            case OBDAVocabulary.SPARQL_SUM_URI:
//            case OBDAVocabulary.SPARQL_COUNT_URI:
//            case OBDAVocabulary.SPARQL_MAX_URI:
//            case OBDAVocabulary.SPARQL_MIN_URI:
//                return true;
//            default:
//                return false;
//        }
//    }
}
