package it.unibz.inf.ontop.datalog.impl;

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.datalog.ListenableFunction;

import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.datalog.EventGeneratingList;
import it.unibz.inf.ontop.datalog.ListListener;

/**
 * This is a rule implementation that keeps track of changes in the query by
 * externals. It is also optimized for .equals calls.
 */
public class CQIEImpl implements CQIE, ListListener {

	private static final long serialVersionUID = 5789854661851692098L;
	private Function head = null;
	private EventGeneratingList<Function> body = null;

	private static final String SPACE = " ";
	private static final String COMMA = ",";
	private static final String INV_IMPLIES = ":-";

	protected CQIEImpl(Function head, List<Function> body) {
		// The syntax for CQ may contain no body, thus, this condition will
		// check whether the construction of the link list is possible or not.
		if (body != null) {
            this.body = new EventGeneratingLinkedList<>(body);
			registerListeners(this.body);
			// TODO possible memory leak!!! we should also de-register when objects are removed
		}
		setHead(head);
	}

	protected CQIEImpl(Function head, Function[] body) {
		// The syntax for CQ may contain no body, thus, this condition will
		// check whether the construction of the link list is possible or not.
		if (body != null) {
			this.body = new EventGeneratingLinkedList<>(body);
			registerListeners(this.body);
			// TODO possible memory leak!!! we should also de-register when objects are removed
		}
		setHead(head);
	}

	private void setHead(Function head) {
		// The syntax for CQ may also contain no head, thus, this condition
		// will check whether we can look for the head terms or not.
		if (head != null) {
			this.head = head;

			if (head instanceof ListenableFunction) {
				EventGeneratingList<Term> headterms = ((ListenableFunction)head).getTerms();
				headterms.addListener(this);
			}
			else if (!(head instanceof ImmutableFunctionalTerm)) {
				throw new RuntimeException("Unknown type of function: not listenable nor immutable:  "
						+ head);
			}
		}
	}

	private void registerListeners(EventGeneratingList<? extends Term> functions) {

		functions.addListener(this);

		for (Object o : functions) {
			if (!(o instanceof Function)) {
				continue;
			}
			else if (o instanceof ListenableFunction) {
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
	public List<Function> getBody() { return body; }

	@Override
	public Function getHead() { return head; }


	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(head.toString());
		sb.append(SPACE);
		sb.append(INV_IMPLIES);
		sb.append(SPACE);

		Iterator<Function> bit = body.iterator();
		while (bit.hasNext()) {
			Function atom = bit.next();
			sb.append(atom.toString());

			if (bit.hasNext()) { // if there is a next atom.
				sb.append(COMMA);
				sb.append(SPACE); // print ", "
			}
		}
		return sb.toString();
	}

	@Override
	public CQIEImpl clone() {
		Function copyHead = (Function)head.clone();
		List<Function> copyBody = new ArrayList<>(body.size() + 10);

		for (Function atom : body) {
			if (atom != null) {
				copyBody.add((Function) atom.clone());
			}
		}
		
		return new CQIEImpl(copyHead, copyBody);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CQIEImpl)) {
			return false;
		}
		CQIEImpl q2 = (CQIEImpl) obj;
		return hashCode() == q2.hashCode();
	}

	@Override
	public void listChanged() {
	}
}
