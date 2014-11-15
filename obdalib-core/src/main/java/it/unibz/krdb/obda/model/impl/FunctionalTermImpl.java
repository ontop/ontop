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

import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.utils.EventGeneratingArrayList;
import it.unibz.krdb.obda.utils.EventGeneratingLinkedList;
import it.unibz.krdb.obda.utils.EventGeneratingList;
import it.unibz.krdb.obda.utils.ListListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class FunctionalTermImpl implements Function, ListListener {

	private static final long serialVersionUID = 2832481815465364535L;
	
	private Predicate functor;
	private EventGeneratingList<Term> terms;
	private int identifier = -1;

	// true when the list of terms has been modified
	private boolean rehash = true;

	// null when the list of terms has been modified
	private String string = null;

	/**
	 * The default constructor.
	 * 
	 * @param functor
	 *            the function symbol name. It is defined the same as a
	 *            predicate.
	 * @param terms
	 *            the list of arguments.
	 */
	protected FunctionalTermImpl(Predicate functor, Term... terms) {
		this.functor = functor;

		EventGeneratingList<Term> eventlist = new EventGeneratingLinkedList<Term>();
		Collections.addAll(eventlist, terms);
		
		this.terms = eventlist;
		registerListeners(eventlist);
	}

	protected FunctionalTermImpl(Predicate functor, List<Term> terms) {
		this.functor = functor;

		EventGeneratingList<Term> eventlist = new EventGeneratingLinkedList<Term>();
		eventlist.addAll(terms);	
		
		this.terms = eventlist;		
		registerListeners(eventlist);
	}

	
	private void registerListeners(EventGeneratingList<? extends Term> functions) {
		functions.addListener(this);
		for (Object o : functions) {
			if (!(o instanceof Function)) {
				continue;
			}
			Function f = (Function) o;
			EventGeneratingList<Term> list = (EventGeneratingList<Term>) f.getTerms();
			list.addListener(this);
			registerListeners(list);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof FunctionalTermImpl)) {
			return false;
		}
		FunctionalTermImpl other = (FunctionalTermImpl) obj;
		return this.hashCode() == other.hashCode();
	}

	@Override
	public int hashCode() {
		if (rehash) {
			string = toString();
			identifier = string.hashCode();
			rehash = false;
		}
		return identifier;
	}

	@Override
	public void setPredicate(Predicate predicate) {
		this.functor = predicate;
		listChanged();
	}

	@Override
	public Set<Variable> getVariables() {
		HashSet<Variable> variables = new LinkedHashSet<Variable>();
		for (Term t : terms) {
			for (Variable v : t.getReferencedVariables())
				variables.add(v);
		}
		return variables;
	}

	@Override
	public Predicate getPredicate() {
		return getFunctionSymbol();
	}

	@Override
	public Predicate getFunctionSymbol() {
		return functor;
	}

	@Override
	public List<Term> getTerms() {
		return terms;
	}

	@Override
	public int getArity() {
		return functor.getArity();
	}

	@Override
	public FunctionalTermImpl clone() {
		ArrayList<Term> copyTerms = new ArrayList<Term>(terms.size());
		
		for (Term term: terms) {
			copyTerms.add(term.clone());
		}
		FunctionalTermImpl clone = new FunctionalTermImpl(functor, copyTerms);
		clone.identifier = identifier;
		clone.string = string;
		clone.rehash = rehash;
		return clone;
	}

	@Override
	public String toString() {
		if (string == null) {
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
			string = sb.toString();
		}
		return string;
	}

	/**
	 * Check whether the function contains a particular term argument or not.
	 * 
	 * @param t
	 *            the term in question.
	 * @return true if the function contains the term, or false otherwise.
	 */
	public boolean containsTerm(Term t) {
		for (int i = 0; i < terms.size(); i++) {
			Term t2 = terms.get(i);
			if (t2.equals(t))
				return true;
		}
		return false;
	}

	@Override
	public int getFirstOcurrance(Term t, int i) {
		int size = terms.size();
		for (int j = 0; j < size; j++) {
			Term t2 = terms.get(j);
			if (t2 instanceof FunctionalTermImpl) {
				FunctionalTermImpl f = (FunctionalTermImpl) t2;
				int newindex = f.getFirstOcurrance(t, 0);
				if (newindex != -1)
					return j;
			} else {
				if (t2.equals(t))
					return j;
			}
		}
		return -1;
	}

	@Override
	public void listChanged() {
		rehash = true;
		string = null;
	}

	@Override
	public Set<Variable> getReferencedVariables() {
		Set<Variable> vars = new LinkedHashSet<Variable>();
		for (Term t : terms) {
			for (Variable v : t.getReferencedVariables())
				vars.add(v);
		}
		return vars;
	}

	@Override
	public Term getTerm(int index) {
		return terms.get(index);
	}

	@Override
	public void setTerm(int index, Term newTerm) {
		listChanged();
		terms.set(index, newTerm);
	}

	public void updateTerms(List<Term> newterms) {
		for (Term term : terms) {
			if (term instanceof FunctionalTermImpl) {
				FunctionalTermImpl function = (FunctionalTermImpl) term;
				EventGeneratingList<Term> innertermlist = (EventGeneratingList<Term>) function.getTerms();
				innertermlist.removeListener(this);
			}
		}
		terms.clear();
		terms.addAll(newterms);

		for (Term term : terms) {
			if (term instanceof FunctionalTermImpl) {
				FunctionalTermImpl function = (FunctionalTermImpl) term;
				EventGeneratingList<Term> innertermlist = (EventGeneratingList<Term>) function.getTerms();
				innertermlist.addListener(this);
			}
		}
		listChanged();
	}

	@Override
	public boolean isDataFunction() {
		return this.functor.isDataPredicate();
	}

	@Override
	public boolean isBooleanFunction() {
		return this.functor.isBooleanPredicate();
	}

	@Override
	public boolean isAlgebraFunction() {
		return this.functor.isAlgebraPredicate();
	}

	@Override
	public boolean isArithmeticFunction() {
		return this.functor.isArithmeticPredicate();
	}

	@Override
	public boolean isDataTypeFunction() {
		return this.functor.isDataTypePredicate();
	}
}
