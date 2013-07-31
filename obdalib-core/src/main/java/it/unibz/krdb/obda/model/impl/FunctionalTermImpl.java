/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.NewLiteral;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.utils.EventGeneratingArrayList;
import it.unibz.krdb.obda.utils.EventGeneratingLinkedList;
import it.unibz.krdb.obda.utils.ListListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FunctionalTermImpl extends AbstractLiteral implements Function, ListListener {

	protected static final long serialVersionUID = 2832481815465364535L;
	
	protected Predicate functor = null;
	protected EventGeneratingArrayList<NewLiteral> terms = null;
	protected int identifier = -1;

	// true when the list of terms has been modified
	protected boolean rehash = true;

	// null when the list of terms has been modified
	protected String string = null;

	protected Atom asAtom = null;

	/**
	 * The default constructor.
	 * 
	 * @param functor
	 *            the function symbol name. It is defined the same as a
	 *            predicate.
	 * @param terms
	 *            the list of arguments.
	 */
	protected FunctionalTermImpl(Predicate functor, NewLiteral... terms) {
		this.functor = functor;

		EventGeneratingArrayList<NewLiteral> eventlist = new EventGeneratingArrayList<NewLiteral>(
				terms.length * 10);
		for (NewLiteral term : terms) {
			eventlist.add(term);
		}
		this.terms = eventlist;
		registerListeners(eventlist);
	}

	protected FunctionalTermImpl(Predicate functor, List<NewLiteral> terms) {
		this.functor = functor;

		EventGeneratingArrayList<NewLiteral> eventlist = new EventGeneratingArrayList<NewLiteral>(
				terms.size() * 10);
		for (NewLiteral term : terms) {
			eventlist.add(term);
		}
		this.terms = eventlist;
		registerListeners(eventlist);
	}

	protected FunctionalTermImpl(Predicate functor, EventGeneratingArrayList<NewLiteral> terms) {
		this.functor = functor;
		this.terms = terms;
		registerListeners(terms);
	}
	
	private void registerListeners(EventGeneratingArrayList<? extends NewLiteral> functions) {
		functions.addListener(this);
		for (Object o : functions) {
			if (!(o instanceof Function)) {
				continue;
			}
			Function f = (Function) o;
			EventGeneratingArrayList<NewLiteral> list = (EventGeneratingArrayList<NewLiteral>) f.getTerms();
			list.addListener(this);
			registerListeners(list);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof FunctionalTermImpl)) {
			return false;
		}
		FunctionalTermImpl functor2 = (FunctionalTermImpl) obj;
		return this.hashCode() == functor2.hashCode();
	}

	@Override
	public int hashCode() {
		if (rehash) {
			identifier = toString().hashCode();
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
		for (NewLiteral t : terms) {
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
	public List<NewLiteral> getTerms() {
		return terms;
	}

	@Override
	public int getArity() {
		return functor.getArity();
	}

	@Override
	public FunctionalTermImpl clone() {
		EventGeneratingLinkedList<NewLiteral> copyTerms = new EventGeneratingLinkedList<NewLiteral>();
		Iterator<NewLiteral> it = terms.iterator();
		while (it.hasNext()) {
			copyTerms.add(it.next().clone());
		}
		FunctionalTermImpl clone = new FunctionalTermImpl(functor, copyTerms);
		clone.identifier = identifier;
		clone.string = string;
		clone.rehash = rehash;
		return clone;
	}

	@Override
	public String toString() {
		return TermUtil.toString(this);
	}

	/**
	 * Check whether the function contains a particular term argument or not.
	 * 
	 * @param t
	 *            the term in question.
	 * @return true if the function contains the term, or false otherwise.
	 */
	public boolean containsTerm(NewLiteral t) {
		for (int i = 0; i < terms.size(); i++) {
			NewLiteral t2 = terms.get(i);
			if (t2.equals(t))
				return true;
		}
		return false;
	}

	@Override
	public int getFirstOcurrance(NewLiteral t, int i) {
		int size = terms.size();
		for (int j = 0; j < size; j++) {
			NewLiteral t2 = terms.get(j);
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
		for (NewLiteral t : terms) {
			for (Variable v : t.getReferencedVariables())
				vars.add(v);
		}
		return vars;
	}

	@Override
	public Map<Variable, Integer> getVariableCount() {
		Map<Variable, Integer> currentcount = new HashMap<Variable, Integer>();
		for (NewLiteral t : terms) {
			Map<Variable, Integer> atomCount = t.getVariableCount();
			for (Variable var : atomCount.keySet()) {
				Integer count = currentcount.get(var);
				if (count != null) {
					currentcount.put(var, count + atomCount.get(var));
				} else {
					currentcount.put(var, new Integer(atomCount.get(var)));
				}
			}
		}
		return currentcount;
	}

	@Override
	public NewLiteral getTerm(int index) {
		return terms.get(index);
	}

	@Override
	public void setTerm(int index, NewLiteral newTerm) {
		listChanged();
		terms.set(index, newTerm);
	}

	public void updateTerms(List<NewLiteral> newterms) {
		for (NewLiteral term : terms) {
			if (term instanceof FunctionalTermImpl) {
				FunctionalTermImpl function = (FunctionalTermImpl) term;
				EventGeneratingArrayList<NewLiteral> innertermlist = (EventGeneratingArrayList<NewLiteral>) function.getTerms();
				innertermlist.removeListener(this);
			}
		}
		terms.clear();
		terms.addAll(newterms);

		for (NewLiteral term : terms) {
			if (term instanceof FunctionalTermImpl) {
				FunctionalTermImpl function = (FunctionalTermImpl) term;
				EventGeneratingArrayList<NewLiteral> innertermlist = (EventGeneratingArrayList<NewLiteral>) function.getTerms();
				innertermlist.addListener(this);
			}
		}
		listChanged();
	}

	@Override
	public Atom asAtom() {
		if (asAtom == null) {
			asAtom = new AtomWrapperImpl(this);
		}
		return asAtom;
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
