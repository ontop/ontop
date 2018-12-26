package it.unibz.inf.ontop.model.term;

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
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.tools.TypeConstantDictionary;
import it.unibz.inf.ontop.model.term.functionsymbol.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.IRIStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.RDFTermType;
import org.apache.commons.rdf.api.IRI;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public interface TermFactory {

	/*
	 * Built-in function predicates
	 */
	
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
	Function getFunction(Predicate functor, Term... terms);

	Expression getExpression(BooleanFunctionSymbol functor, List<Term> arguments);

	ImmutableExpression getImmutableExpression(BooleanFunctionSymbol functor, ImmutableTerm... arguments);

	ImmutableExpression getImmutableExpression(BooleanFunctionSymbol functor,
											   ImmutableList<? extends ImmutableTerm> arguments);

	ImmutableExpression getImmutableExpression(Expression expression);

	/**
	 * Must be non-empty
	 *
	 * Does NOT take care of flattening conjunctions in the arguments
	 */
	ImmutableExpression getConjunction(ImmutableList<ImmutableExpression> nonEmptyExpressionList);

	/**
	 * Does NOT take care of flattening conjunctions in the arguments
	 */
	ImmutableExpression getConjunction(ImmutableExpression expression, ImmutableExpression... otherExpressions);

	/**
	 * May be empty.
	 *
	 * Takes care of flattening the arguments
	 */
	Optional<ImmutableExpression> getConjunction(Stream<ImmutableExpression> expressionStream);

	/**
	 * Must be non-empty
	 */
	ImmutableExpression getDisjunction(ImmutableList<ImmutableExpression> nonEmptyExpressionList);

	/**
	 * May be empty.
	 *
	 * Takes care of flattening the arguments
	 */
	Optional<ImmutableExpression> getDisjunction(Stream<ImmutableExpression> expressions);

	/**
	 * When filled with constants, evaluates to FALSE if one argument is FALSE or to NULL otherwise.
	 *
	 * Must be non-empty
	 */
	ImmutableExpression getFalseOrNullFunctionalTerm(ImmutableList<ImmutableExpression> arguments);

	/**
	 * When filled with constants, evaluates to TRUE if one argument is TRUE or to NULL otherwise.
	 *
	 * Must be non-empty
	 */
	ImmutableExpression getTrueOrNullFunctionalTerm(ImmutableList<ImmutableExpression> arguments);

	/**
	 * Compares a TermType term to a base type
	 */
	ImmutableExpression getIsAExpression(ImmutableTerm termTypeTerm, RDFTermType baseType);


	/**
	 * See https://www.w3.org/TR/sparql11-query/#func-arg-compatibility
	 */
	ImmutableExpression getAreCompatibleRDFStringExpression(ImmutableTerm typeTerm1, ImmutableTerm typeTerm2);

	/**
	 * Just wraps the expression into an Evaluation object
	 */
	ImmutableExpression.Evaluation getEvaluation(ImmutableExpression expression);
	ImmutableExpression.Evaluation getPositiveEvaluation();
	ImmutableExpression.Evaluation getNegativeEvaluation();
	ImmutableExpression.Evaluation getNullEvaluation();


	public Function getFunction(Predicate functor, List<Term> terms);

	public ImmutableFunctionalTerm getImmutableFunctionalTerm(FunctionSymbol functor, ImmutableList<? extends ImmutableTerm> terms);

	public ImmutableFunctionalTerm getImmutableFunctionalTerm(FunctionSymbol functor, ImmutableTerm... terms);

	public NonGroundFunctionalTerm getNonGroundFunctionalTerm(FunctionSymbol functor, ImmutableTerm... terms);

	public NonGroundFunctionalTerm getNonGroundFunctionalTerm(FunctionSymbol functor, ImmutableList<ImmutableTerm> terms);


	public Expression getExpression(BooleanFunctionSymbol functor, Term... arguments);

	/*
	 * Boolean function terms
	 */

	public Expression getFunctionEQ(Term firstTerm, Term secondTerm);

	public Expression getFunctionGTE(Term firstTerm, Term secondTerm);

	public Expression getFunctionGT(Term firstTerm, Term secondTerm);

	public Expression getFunctionLTE(Term firstTerm, Term secondTerm);

	public Expression getFunctionLT(Term firstTerm, Term secondTerm);

	public Expression getFunctionNEQ(Term firstTerm, Term secondTerm);

	public Expression getFunctionNOT(Term term);

	public Expression getFunctionAND(Term term1, Term term2);

	public Expression getFunctionOR(Term term1, Term term2);

	public Expression getFunctionIsTrue(Term term);

	public Expression getFunctionIsNull(Term term);

	public Expression getFunctionIsNotNull(Term term);

	public Expression getLANGMATCHESFunction(Term term1, Term term2);
	
	// ROMAN (23 Dec 2015): LIKE comes only from mappings
	public Expression getSQLFunctionLike(Term term1, Term term2);


	/*
	 * Casting values cast(source-value AS destination-type)
	 */
	public Function getFunctionCast(Term term1, Term term2);

	/**
	 * Construct a {@link IRIConstant} object. This type of term is written as a
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
	 * @param iri
	 *            the URI.
	 * @return a URI constant.
	 */
	public IRIConstant getConstantIRI(IRI iri);
	
	public BNode getConstantBNode(String name);

	DBConstant getDBBooleanConstant(boolean value);

	Constant getNullConstant();

	/**
	 * TODO: explain
	 */
	RDFLiteralConstant getProvenanceSpecialConstant();

	/**
	 * Construct a {@link RDFLiteralConstant} object with a type definition.
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
	RDFLiteralConstant getRDFLiteralConstant(String value, RDFDatatype type);

	RDFLiteralConstant getRDFLiteralConstant(String value, IRI type);


	/**
	 * Construct a {@link RDFLiteralConstant} object with a language tag.
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
	RDFLiteralConstant getRDFLiteralConstant(String value, String language);

	RDFConstant getRDFConstant(String lexicalValue, RDFTermType termType);

	Function getRDFLiteralMutableFunctionalTerm(Term lexicalTerm, String language);
	Function getRDFLiteralMutableFunctionalTerm(Term lexicalTerm, RDFDatatype type);
	Function getRDFLiteralMutableFunctionalTerm(Term lexicalTerm, IRI datatype);

	ImmutableFunctionalTerm getRDFLiteralFunctionalTerm(ImmutableTerm lexicalTerm, String language);
	ImmutableFunctionalTerm getRDFLiteralFunctionalTerm(ImmutableTerm lexicalTerm, RDFDatatype type);
	ImmutableFunctionalTerm getRDFLiteralFunctionalTerm(ImmutableTerm lexicalTerm, IRI datatypeIRI);

	DBConstant getDBConstant(String value, DBTermType termType);
	DBConstant getDBStringConstant(String value);

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

	RDFTermTypeConstant getRDFTermTypeConstant(RDFTermType type);
	ImmutableFunctionalTerm getRDFTermTypeFunctionalTerm(ImmutableTerm term, TypeConstantDictionary dictionary,
														 ImmutableSet<RDFTermTypeConstant> possibleConstants);

	ImmutableFunctionalTerm getRDFFunctionalTerm(ImmutableTerm lexicalTerm, ImmutableTerm typeTerm);

	/**
	 * Returns RDF(NULL, NULL)
	 */
	ImmutableFunctionalTerm getNullRDFFunctionalTerm();

	/**
	 * TODO: use a more precise type for the argument
	 */
	GroundFunctionalTerm getIRIFunctionalTerm(IRI iri);

	/**
	 * temporaryCastToString == true must only be used when dealing with PRE-PROCESSED mapping
	 */
	ImmutableFunctionalTerm getIRIFunctionalTerm(Variable variable, boolean temporaryCastToString);

	/**
	 * At least one argument for the IRI functional term with an IRI template is required
	 */
	ImmutableFunctionalTerm getIRIFunctionalTerm(String iriTemplate, ImmutableList<? extends ImmutableTerm> arguments);

	/**
	 * When IRIs are encoded into numbers using a dictionary
	 */
	ImmutableFunctionalTerm getRDFFunctionalTerm(int encodedIRI);

	/**
	 * When fact IRIs are decomposed (so as to be included in the mapping)
	 */
	ImmutableFunctionalTerm getIRIFunctionalTerm(IRIStringTemplateFunctionSymbol templateSymbol,
												 ImmutableList<DBConstant> arguments);

	/**
	 * Temporary
	 */
	Function getIRIMutableFunctionalTerm(String iriTemplate, Term... arguments);
	Function getIRIMutableFunctionalTerm(IRI iri);

	Function getNullRDFMutableFunctionalTerm();



	/**
	 * NB: a fresh Bnode template is created
	 */
	ImmutableFunctionalTerm getFreshBnodeFunctionalTerm(Variable variable);
	ImmutableFunctionalTerm getBnodeFunctionalTerm(String bnodeTemplate,
												   ImmutableList<? extends ImmutableTerm> arguments);

	/**
	 * NB: a fresh Bnode template is created
	 */
	ImmutableFunctionalTerm getFreshBnodeFunctionalTerm(ImmutableList<ImmutableTerm> terms);

	ImmutableFunctionalTerm getDBCastFunctionalTerm(DBTermType targetType, ImmutableTerm term);
	ImmutableFunctionalTerm getDBCastFunctionalTerm(DBTermType inputType, DBTermType targetType, ImmutableTerm term);

	/**
	 * TODO: explain
	 */
	ImmutableFunctionalTerm getConversion2RDFLexicalFunctionalTerm(DBTermType inputType, ImmutableTerm term, RDFTermType rdfTermType);

	/**
	 * Used when building (a fragment of) the lexical part of an RDF term
	 *   (either the full lexical value or a fragment involved in a template)
	 * in a PRE-PROCESSED mapping assertion.
	 *
	 * This functional term must not appear in the final mapping
	 */
	ImmutableFunctionalTerm getPartiallyDefinedToStringCast(Variable variable);

	ImmutableFunctionalTerm getIfElseNull(ImmutableExpression condition, ImmutableTerm term);

	/**
	 * IF THEN, ELSE IF ..., ELSE
	 *
	 * whenPairs must not be empty
	 */
	ImmutableFunctionalTerm getDBCase(Stream<? extends Map.Entry<ImmutableExpression, ? extends ImmutableTerm>> whenPairs,
									  ImmutableTerm defaultTerm);

	/**
	 * IF THEN, ELSE IF ..., ELSE NULL
	 *
	 * whenPairs must not be empty
	 */
	ImmutableFunctionalTerm getDBCaseElseNull(Stream<? extends Map.Entry<ImmutableExpression, ? extends ImmutableTerm>> whenPairs);

	ImmutableFunctionalTerm getDBReplaceFunctionalTerm(ImmutableTerm text, ImmutableTerm from, ImmutableTerm to);

	ImmutableExpression getDBStartsWithFunctionalTerm(ImmutableList<ImmutableTerm> terms);

	ImmutableFunctionalTerm getR2RMLIRISafeEncodeFunctionalTerm(ImmutableTerm term);

	/**
	 * At least two terms are expected
	 */
	ImmutableFunctionalTerm getDBConcatFunctionalTerm(ImmutableList<ImmutableTerm> terms);

    ImmutableFunctionalTerm getCommonDenominatorFunctionalTerm(ImmutableList<ImmutableTerm> typeTerms);

	/**
	 * terms must have at least two distinct elements
	 */
	ImmutableExpression getStrictEquality(ImmutableSet<ImmutableTerm> terms);

	/**
	 * terms must have at least two elements
	 */
	ImmutableExpression getStrictEquality(ImmutableList<ImmutableTerm> terms);

	ImmutableExpression getStrictEquality(ImmutableTerm term1, ImmutableTerm term2, ImmutableTerm... otherTerms);

	/**
	 * terms must have at least two elements
	 * Logically equivalent to NOT(STRICT_EQx(...))
	 */
	ImmutableExpression getStrictNEquality(ImmutableSet<ImmutableTerm> terms);

	/**
	 * terms must have at least two elements
	 * Logically equivalent to NOT(STRICT_EQx(...))
	 */
	ImmutableExpression getStrictNEquality(ImmutableList<ImmutableTerm> terms);

}
