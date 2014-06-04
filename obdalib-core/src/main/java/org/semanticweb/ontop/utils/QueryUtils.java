package org.semanticweb.ontop.utils;

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


import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.DatalogProgram;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.OBDAQuery;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.model.Term;
import org.semanticweb.ontop.model.Variable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.google.common.collect.Lists;

public class QueryUtils {

	public static void copyQueryModifiers(OBDAQuery source, OBDAQuery target) {
		target.getQueryModifiers().copy(source.getQueryModifiers());
	}

	public static boolean isBoolean(DatalogProgram query) {
		for (CQIE rule : query.getRules()) {
			if (!isBoolean(rule))
				return false;
		}
		return true;
	}

	public static boolean isBoolean(CQIE query) {
		return query.getHead().getArity() == 0;
	}

	public static boolean isGrounded(Term term) {
		boolean result = true;
		if (term instanceof Variable) {
			result = false;
		} else if (term instanceof Function) {
			Function func = (Function) term;
			for (Term subTerm : func.getTerms()) {
				if (!isGrounded(subTerm))
					result = false;
			}
		} 
		return result;
	}
	
	/**
	 * Finds atoms with predicate <code>pred</code> in the body of the
	 * <code>rule</code>
	 * 
	 * @param rule
	 * @param pred
	 * @return
	 */
	public static Collection<Function> findAtomsInRuleBody(CQIE rule,
			Predicate pred) {

		List<Function> results = Lists.newArrayList();

		Queue<Term> queue = new LinkedList<Term>(rule.getBody());
		
		while (!queue.isEmpty()) {
			Term qHead = queue.poll();
			if (qHead instanceof Function) {
				Function func = (Function) qHead;

				if (func.isBooleanFunction() || func.isArithmeticFunction()
						|| func.isDataTypeFunction()
						|| func.isAlgebraFunction()) {
					queue.addAll(func.getTerms());
				} else if (func.isDataFunction()) {
					if (func.getFunctionSymbol().equals(pred)) {
						results.add(func);
					}
				}
			} else /* !(queueHead instanceof Function) */{
				// NO-OP
			}
		}

		return results;
	}

	
	/**
	 * Finds one atom with variable <code>var</code> in the body of the
	 * <code>rule</code>
	 * 
	 * @param rule
	 * @param var
	 * @return
	 */
	public static Function findOneAtomInRuleBody(CQIE rule,
			Variable var) {

		Queue<Term> queue = new LinkedList<Term>(rule.getBody());
		
		while (!queue.isEmpty()) {
			Term qHead = queue.poll();
			if (qHead instanceof Function) {
				Function func = (Function) qHead;

				if (func.isBooleanFunction() || func.isArithmeticFunction()
						|| func.isDataTypeFunction()
						|| func.isAlgebraFunction()) {
					queue.addAll(func.getTerms());
				} else if (func.isDataFunction()) {
					
					if (func.getTerms().contains(var)){
						return func;
					}
					
					
				}
			} else /* !(queueHead instanceof Function) */{
				// NO-OP
			}
		}

		return null;
	}
	
	/**
	 * 
	 * collects the variable names in the input <code>atom</code>
	 * 
	 * 
	 * 
	 * @param atom
	 *            
	 * @return 
	 */
	public static List<String> getVariableNamesInAtom(Function atom) {

		List<String> results = new ArrayList<>();

		Queue<Term> queue = new LinkedList<Term>();

		queue.add(atom);
		while (!queue.isEmpty()) {
			Term queueHead = queue.poll();

			if (queueHead instanceof Function) {
				Function funcRoot = (Function) queueHead;

				if (funcRoot.isDataTypeFunction()
						|| funcRoot.isAlgebraFunction()
						|| funcRoot.isDataFunction()) {
					for (Term term : funcRoot.getTerms()) {
						queue.add(term);
					}
				}
			} else if (queueHead instanceof Variable) {
				Variable var = (Variable) queueHead;
				results.add(var.getName());
			}

		} // end while innerAtom
		return results;

	}
	
	
	/**
	 * 
	 * collects the variable names in the input <code>atom</code>
	 * 
	 * 
	 * 
	 * @param atom
	 *            
	 * @return 
	 */
	public static List<Variable> getVariablesInAtom(Function atom) {

		List<Variable> results = new ArrayList<>();

		Queue<Term> queue = new LinkedList<Term>();

		queue.add(atom);
		while (!queue.isEmpty()) {
			Term queueHead = queue.poll();

			if (queueHead instanceof Function) {
				Function funcRoot = (Function) queueHead;

				if (funcRoot.isDataTypeFunction()
						|| funcRoot.isAlgebraFunction()
						|| funcRoot.isDataFunction()) {
					for (Term term : funcRoot.getTerms()) {
						queue.add(term);
					}
				}
			} else if (queueHead instanceof Variable) {
				Variable var = (Variable) queueHead;
				results.add(var);
			}

		} // end while innerAtom
		return results;

	}

	/**
	 * we need to update the cache of this rule manually, as Unifier sometimes is not smart enough 
	 */
	public static void clearCache(CQIE query) {
		clearCache(query.getHead());
		
		for(Function atom : query.getBody()){
			clearCache(atom);
		}
		
		if(query instanceof ListListener){
			((ListListener)query).listChanged();
		}
	}

	public static void clearCache(Function atom) {
		if(atom instanceof ListListener){
			((ListListener)atom).listChanged();
		}
		
		/**
		 * sub terms
		 */
		for(Term term : atom.getTerms()){			
			if(term instanceof Function){
				clearCache((Function)term);	
			}
		}
	}
}
