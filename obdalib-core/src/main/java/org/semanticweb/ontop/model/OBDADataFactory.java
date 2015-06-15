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




import java.io.Serializable;
import java.net.URI;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.Predicate.COL_TYPE;
import org.semanticweb.ontop.utils.JdbcTypeMapper;

public interface OBDADataFactory extends Serializable {

	public OBDAModel getOBDAModel();
	
	public DatatypeFactory getDatatypeFactory();

	public CQIE getCQIE(Function head, Function... body );
	
	public CQIE getCQIE(Function head, List<Function> body);

	public OBDADataSource getDataSource(URI id);

	public DatalogProgram getDatalogProgram();

	@Deprecated
	public DatalogProgram getDatalogProgram(CQIE rule);

	public DatalogProgram getDatalogProgram(OBDAQueryModifiers modifiers);

	public DatalogProgram getDatalogProgram(Collection<CQIE> rules);

	public DatalogProgram getDatalogProgram(OBDAQueryModifiers modifiers, Collection<CQIE> rules);



	public Function getTripleAtom(Term subject, Term predicate, Term object);

	/**
	 * Construct a {@link Predicate} object.
	 *
	 * @param uri
	 *            the name of the predicate (defined as a URI).
	 * @param arity
	 *            the number of elements inside the predicate.
	 * @return a predicate object.
	 */
	@Deprecated
	public Predicate getPredicate(String uri, int arity);

	public Predicate getPredicate(String uri, COL_TYPE[] types);

	public Predicate getObjectPropertyPredicate(String name);

	public Predicate getDataPropertyPredicate(String name, COL_TYPE type);

	/**
	 * with default type COL_TYPE.LITERAL
	 * @param name
	 * @return
	 */
	
	public Predicate getDataPropertyPredicate(String name);
	
	public Predicate getClassPredicate(String name);


	

	public JdbcTypeMapper getJdbcTypeMapper();

	

	/*
	 * Built-in function predicates
	 */

	public Function getUriTemplate(Term...terms);

	public Function getUriTemplate(List<Term> terms);
	
	public Function getUriTemplateForDatatype(String type);
	

	public Function getBNodeTemplate(List<Term> terms);

	public Function getBNodeTemplate(Term... terms);
	
	/**
	 * Construct a {@link Function} object. A function expression consists of
	 * functional symbol (or functor) and one or more arguments.
	 * 
	 * @param functor
	 *            the function symbol name.
	 * @param terms
	 *            a list of arguments.
	 * @return the function object.
	 */
	public Function getFunction(Predicate functor, Term... terms);

	public Function getFunction(Predicate functor, List<Term> terms);

	public ImmutableFunctionalTerm getImmutableFunctionalTerm(Predicate functor, ImmutableList<ImmutableTerm> terms);

	public ImmutableFunctionalTerm getImmutableFunctionalTerm(Predicate functor, ImmutableTerm... terms);


	public BooleanExpression getBooleanExpression(BooleanOperationPredicate functor, Term... arguments);

	/*
	 * Boolean function terms
	 */

	public BooleanExpression getFunctionEQ(Term firstTerm, Term secondTerm);

	public BooleanExpression getFunctionGTE(Term firstTerm, Term secondTerm);

	public BooleanExpression getFunctionGT(Term firstTerm, Term secondTerm);

	public BooleanExpression getFunctionLTE(Term firstTerm, Term secondTerm);

	public BooleanExpression getFunctionLT(Term firstTerm, Term secondTerm);

	public BooleanExpression getFunctionNEQ(Term firstTerm, Term secondTerm);

	public BooleanExpression getFunctionNOT(Term term);

	public BooleanExpression getFunctionAND(Term term1, Term term2);

	public BooleanExpression getFunctionOR(Term term1, Term term2);

	public BooleanExpression getFunctionIsTrue(Term term);
	
	public BooleanExpression getFunctionIsNull(Term term);

	public BooleanExpression getFunctionIsNotNull(Term term);

	public BooleanExpression getLANGMATCHESFunction(Term term1, Term term2);
	
	public BooleanExpression getFunctionLike(Term term1, Term term2);
	
	public BooleanExpression getFunctionRegex(Term term1, Term term2, Term term3);
	

	/*
	 * Numerical arithmethic functions
	 */

	public Function getFunctionMinus(Term term1);

	public Function getFunctionAdd(Term term1, Term term2);

	public Function getFunctionSubstract(Term term1, Term term2);

	public Function getFunctionMultiply(Term term1, Term term2);
	
	/*
	 * Casting values cast(source-value AS destination-type)
	 */
	public Function getFunctionCast(Term term1, Term term2);
	
	/*
	 * JDBC objects
	 */

	public OBDADataSource getJDBCDataSource(String jdbcurl, String username,
			String password, String driverclass);

	public OBDADataSource getJDBCDataSource(String sourceuri, String jdbcurl,
			String username, String password, String driverclass);

	/**
	 * Construct a {@link URIConstant} object. This type of term is written as a
	 * usual URI construction following the generic URI syntax specification
	 * (RFC 3986).
	 * <p>
	 * <code>
	 * scheme://host:port/path#fragment
	 * </code>
	 * <p>
	 * Examples:
	 * <p>
	 * <code>
	 * http://example.org/some/paths <br />
	 * http://example.org/some/paths/to/resource#frag01 <br />
	 * ftp://example.org/resource.txt <br />
	 * </code>
	 * <p>
	 * are all well-formed URI strings.
	 * 
	 * @param uri
	 *            the URI.
	 * @return a URI constant.
	 */
	public URIConstant getConstantURI(String uri);
	
	public BNode getConstantBNode(String name);

	public ValueConstant getBooleanConstant(boolean value);
	
	/**
	 * Construct a {@link ValueConstant} object.
	 * 
	 * @param value
	 *            the value of the constant.
	 * @return the value constant.
	 */
	public ValueConstant getConstantLiteral(String value);

	/**
	 * Construct a {@link ValueConstant} object with a type definition.
	 * <p>
	 * Example:
	 * <p>
	 * <code>
	 * "Person"^^xsd:String <br />
	 * 22^^xsd:Integer
	 * </code>
	 * 
	 * @param value
	 *            the value of the constant.
	 * @param type
	 *            the type of the constant.
	 * @return the value constant.
	 */
	public ValueConstant getConstantLiteral(String value, Predicate.COL_TYPE type);


	/**
	 * Construct a {@link ValueConstant} object with a language tag.
	 * <p>
	 * Example:
	 * <p>
	 * <code>
	 * "This is American English"@en-US <br />
	 * </code>
	 * 
	 * @param value
	 *            the value of the constant.
	 * @param language
	 *            the language tag for the constant.
	 * @return the value constant.
	 */
	public ValueConstant getConstantLiteral(String value, String language);

	public Function getTypedTerm(Term value, String language);
	public Function getTypedTerm(Term value, Term language);
	public Function getTypedTerm(Term value, Predicate.COL_TYPE type);
	
	/**
	 * Construct a {@link ValueConstant} object with a system-assigned name
	 * that is automatically generated.
	 * 
	 * @return the value constant.
	 */
	public ValueConstant getConstantFreshLiteral();
	
	/**
	 * Construct a {@link Variable} object. The variable name is started by a
	 * dollar sign ('$') or a question mark sign ('?'), e.g.:
	 * <p>
	 * <code>
	 * pred($x) <br />
	 * func(?x, ?y)
	 * </code>
	 * 
	 * @param name
	 *            the name of the variable.
	 * @return the variable object.
	 */
	public Variable getVariable(String name);
	
	

	/**
	 * Construct a {@link Variable} object with empty name.
	 * 
	 * @return the variable object.
	 */
	public Variable getVariableNondistinguished();

	public OBDARDBMappingAxiom getRDBMSMappingAxiom(String id, OBDAQuery sourceQuery, OBDAQuery targetQuery);

	public OBDARDBMappingAxiom getRDBMSMappingAxiom(String id, String sql, OBDAQuery targetQuery);

	public OBDARDBMappingAxiom getRDBMSMappingAxiom(String sql, OBDAQuery targetQuery);

	public OBDASQLQuery getSQLQuery(String query);

	//public Predicate getTypePredicate(Predicate.COL_TYPE type);

	/**
	 * TODO: deprecate it?
	 */
	public Function getSPARQLJoin(Term t1, Term t2);

	public Function getSPARQLJoin(Function t1, Function t2, Function joinCondition);

	public Function getSPARQLLeftJoin(Term t1, Term t2);	
}
