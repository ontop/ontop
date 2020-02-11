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
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.datalog.impl.EventGeneratingLinkedList;
import it.unibz.inf.ontop.datalog.EventGeneratingList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TODO: rename ListenableFunctionImpl
 *
 * Please consider using ImmutableFunctionalTermImpl instead.
 */
public class FunctionalTermImpl extends AbstractFunctionalTerm implements ListenableFunction {

	private static final long serialVersionUID = 2832481815465364535L;

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
		super(functor);

		EventGeneratingList<Term> eventlist = new EventGeneratingLinkedList<>();
		Collections.addAll(eventlist, terms);
		
		this.terms = eventlist;
		registerListeners(eventlist);
	}

	protected FunctionalTermImpl(Predicate functor, List<Term> terms) {
		super(functor);

		EventGeneratingList<Term> eventlist = new EventGeneratingLinkedList<>();
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
			if (o instanceof ListenableFunction) {
				ListenableFunction f = (ListenableFunction) o;
				EventGeneratingList<Term> list = f.getTerms();
				list.addListener(this);
				registerListeners(list);
			}
			else if (!(o instanceof ImmutableFunctionalTerm)) {
				throw new IllegalArgumentException("Unknown type of function: not listenable nor immutable:  " + o);
			}
		}
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
	public EventGeneratingList<Term> getTerms() {
		return terms;
	}

	@Override
	public Function clone() {
		ArrayList<Term> copyTerms = new ArrayList<>(terms.size());
		
		for (Term term: terms) {
			copyTerms.add(term.clone());
		}
		FunctionalTermImpl clone = new FunctionalTermImpl(getFunctionSymbol(), copyTerms);
		clone.identifier = identifier;
		clone.string = string;
		clone.rehash = rehash;
		return clone;
	}

	@Override
	public String toString() {
		if (string == null) {
			string = super.toString();
		}
		return string;
	}


	@Override
	public void listChanged() {
		rehash = true;
		string = null;
	}

	@Override
	public Term getTerm(int index) {
		return terms.get(index);
	}


	@Override
	public boolean isOperation() {
		return getFunctionSymbol() instanceof FunctionSymbol;
	}

	
	@Override
	public boolean isDataTypeFunction() {
		return false;
	}
	
}
