package org.semanticweb.ontop.model;

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

import java.util.List;
import java.util.Set;

/**
 * This class defines a type of {@link Term} in which it denotes a mapping
 * of one or more elements in a set (called the domain of the function) into a
 * unique element of another set (the range of the function).
 * <p>
 * A function expression is a function symbol followed by its arguments. The
 * arguments are elements from the domain of the function; the number of
 * arguments is equal to the {@code arity} of the function. The arguments are
 * enclosed in parentheses and separated by commas, e.g.,
 * <p>
 * <code>
 * f(X,Y) <br />
 * father(david) <br />
 * price(apple) <br />
 * </code>
 * <p>
 * are all well-formed function expressions.
 */
public interface Function extends Term {

	/**
	 * Get a list of terms (or arguments) that are contained in the function
	 * symbol.
	 * 
	 * @return a list of terms.
	 */
	public List<Term> getTerms();

	/**
	 * Get the function symbol.
	 * 
	 * @return the predicate object.
	 */
	public Predicate getFunctionSymbol();

	/**
	 * Get the number of terms (or arguments) in the function symbol.
	 * 
	 * @return the arity.
	 */
	public int getArity();

	public Term getTerm(int index);

	public void setTerm(int index, Term term);

	public void updateTerms(List<Term> literals);

	public void setPredicate(Predicate p);

	boolean containsTerm(Term t);

	boolean isDataFunction();

	boolean isBooleanFunction();

	boolean isAlgebraFunction();
	
	boolean isArithmeticFunction();
	
	boolean isDataTypeFunction();

    public Set<Variable> getVariables();
}
