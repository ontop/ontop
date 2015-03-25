package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

/*
 * #%L
 * ontop-reformulation-core
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
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.AnonymousVariable;
import it.unibz.krdb.obda.model.impl.FunctionalTermImpl;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

import java.util.HashMap;
import java.util.LinkedList;

// TODO This class needs to be restructured

public class QueryAnonymizer {

	private static final OBDADataFactory fac = OBDADataFactoryImpl.getInstance();


	public static CQIE anonymize0(CQIE q) {

		// Collecting number of occurrences of all variables
		 
		HashMap<Variable, Integer> auxmap = new HashMap<Variable, Integer>();
		for (Function atom : q.getBody()) 
			for (Term t : atom.getTerms()) 
				collectVariableOccurrences(t, auxmap);

		if (auxmap.isEmpty())
			return q;
		
		LinkedList<Function> newBody = new LinkedList<Function>();
		for (Function atom : q.getBody()) {
			LinkedList<Term> vex = new LinkedList<Term>();
			for (Term t : atom.getTerms()) {
				Integer list = null;
				if (t instanceof Variable) {
					list = auxmap.get(((Variable) t));
				}
				if (list != null && list < 2 && !isVariableInHead(q, t)) {
//					vex.add(fac.getVariableNondistinguished());
				} else {
					vex.add(t);
				}
			}
			Function newatom = fac.getFunction(atom.getFunctionSymbol().clone(), vex);
			newBody.add(newatom);
		}
		CQIE query = fac.getCQIE(q.getHead(), newBody);
		return query;
	}

	/**
	 * collects occurrences of variables
	 * 
	 * @param term
	 * @param auxmap: maps variable names to atoms they occur in
	 */
	
	private static void collectVariableOccurrences(Term term, HashMap<Variable, Integer> auxmap) {
		if ((term instanceof Variable) && !(term instanceof AnonymousVariable)) {
			Variable var = (Variable) term;
			Integer value = auxmap.get(var);
			if (value == null) 
				auxmap.put(var, 1);
			else
				value++;
		} 
		else if (term instanceof Function) {
			Function funct = (Function) term;
			for (Term t : funct.getTerms()) {
				collectVariableOccurrences(t, auxmap);
			}
		} 
		else {
			// NO-OP
			// Ignore constants
		}
	}

	private static boolean isVariableInHead(CQIE q, Term t) {
//		if (t instanceof AnonymousVariable)
//			return false;

		for (Term headterm : q.getHead().getTerms()) {
			if (headterm instanceof FunctionalTermImpl) {
				FunctionalTermImpl fterm = (FunctionalTermImpl) headterm;
				if (fterm.containsTerm(t))
					return true;
			} 
			else if (headterm.equals(t))
				return true;
		}
		return false;
	}

	/**
	 * method that enumerates all undistinguished variables in the given data
	 * log program. This will also remove any instances of
	 * UndisinguishedVariable and replace them by instance of Variable
	 * (enumerated as mentioned before). This step is needed to ensure that the
	 * algorithm treats each undistinguished variable as a unique variable.
	 * 
	 * Needed because the rewriter might generate query bodies like this B(x,_),
	 * R(x,_), underscores represent unique anonymous variables. However, the
	 * SQL generator needs them to be explicitly unique. replacing B(x,newvar1),
	 * R(x,newvar2)
	 * 
	 * @param dp
	 */

	public static CQIE deAnonymize(CQIE query) {
		
		query = query.clone();
		Function head = query.getHead();
		
		int counter = 1;
		LinkedList<Term> newTerms = new LinkedList<Term>();
		for (Term t : head.getTerms()) {
			if (t instanceof AnonymousVariable) {
				String newName = "_uv-" + counter;
				counter++;
				Term newT = fac.getVariable(newName);
				newTerms.add(newT);
			} else {
				newTerms.add(t);
			}
		}
		head.updateTerms(newTerms);

		for (Function a : query.getBody()) {
			LinkedList<Term> vec = new LinkedList<Term>();
			for (Term t : a.getTerms()) {
				if (t instanceof AnonymousVariable) {
					String newName = "_uv-" + counter;
					counter++;
					Term newT = fac.getVariable(newName);
					vec.add(newT);
				} else {
					vec.add(t);
				}
			}
			a.updateTerms(vec);
		}
		return query;
	}
}
