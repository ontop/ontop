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

import it.unibz.inf.ontop.datalog.ListenableFunction;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.datalog.impl.EventGeneratingLinkedList;
import it.unibz.inf.ontop.datalog.EventGeneratingList;

import java.util.*;

/**
 * TODO: rename ListenableFunctionImpl
 *
 * Please consider using ImmutableFunctionalTermImpl instead.
 */
public class FunctionalTermImpl implements Function {

	private final Predicate functor;
	private final List<Term> terms;

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FunctionalTermImpl) {
			FunctionalTermImpl other = (FunctionalTermImpl)obj;
			return this.functor.equals(other.functor) && this.terms.equals(other.terms);
		}
		return false;
	}

	@Override
	public int getArity() {
		return functor.getArity();
	}


	@Override
	public Predicate getFunctionSymbol() {
		return functor;
	}

	/**
	 * Check whether the function contains a particular term argument or not.
	 *
	 * @param t the term in question.
	 * @return true if the function contains the term, or false otherwise.
	 */
	@Override
	public boolean containsTerm(Term t) {
		return terms.contains(t);
	}


	/**
	 * The default constructor.
	 * 
	 * @param functor
	 *            the function symbol name. It is defined the same as a
	 *            predicate.
	 * @param terms
	 *            the list of arguments.
	 */

	protected FunctionalTermImpl(Predicate functor, List<Term> terms) {
		this.functor = functor;
		this.terms = new ArrayList<>(terms);
	}

	
	@Override
	public int hashCode() {
		return functor.hashCode() ^ terms.hashCode();
	}

	@Override
	public List<Term> getTerms() {
		return terms;
	}

	@Override
	public Function clone() {
		ArrayList<Term> copyTerms = new ArrayList<>(terms.size());
		for (Term term: terms)
			copyTerms.add(term.clone());
		return new FunctionalTermImpl(getFunctionSymbol(), copyTerms);
	}

	@Override
	public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(functor.toString());
			sb.append("(");
			boolean separator = false;
			for (Term innerTerm : terms) {
				if (separator) {
					sb.append(",");
				}
				sb.append(innerTerm.toString());
				separator = true;
			}
			sb.append(")");
		return sb.toString();
	}

	@Override
	public Term getTerm(int index) {
		return terms.get(index);
	}
}
