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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.tools.TypeConstantDictionary;
import it.unibz.inf.ontop.model.term.functionsymbol.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.IRIStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import org.apache.commons.rdf.api.IRI;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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

	ImmutableExpression getDisjunction(ImmutableExpression expression, ImmutableExpression... otherExpressions);

	/**
	 * May be empty.
	 *
	 * Takes care of flattening the arguments
	 */
	Optional<ImmutableExpression> getDisjunction(Stream<ImmutableExpression> expressions);

	ImmutableExpression getDBNot(ImmutableExpression expression);

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

	ImmutableFunctionalTerm.InjectivityDecomposition getInjectivityDecomposition(ImmutableFunctionalTerm injectiveFunctionalTerm);
	ImmutableFunctionalTerm.InjectivityDecomposition getInjectivityDecomposition(
			ImmutableFunctionalTerm injectiveFunctionalTerm,
			ImmutableMap<Variable, ImmutableTerm> subTermSubstitutionMap);



	public Function getFunction(Predicate functor, List<Term> terms);

	public ImmutableFunctionalTerm getImmutableFunctionalTerm(FunctionSymbol functor, ImmutableList<? extends ImmutableTerm> terms);

	public ImmutableFunctionalTerm getImmutableFunctionalTerm(FunctionSymbol functor, ImmutableTerm... terms);

	public NonGroundFunctionalTerm getNonGroundFunctionalTerm(FunctionSymbol functor, ImmutableTerm... terms);

	public NonGroundFunctionalTerm getNonGroundFunctionalTerm(FunctionSymbol functor, ImmutableList<ImmutableTerm> terms);


	public Expression getExpression(BooleanFunctionSymbol functor, Term... arguments);

	/*
	 * Boolean function terms
	 */

	// TODO: distinguish the strict and non-strict equalities
	default Expression getFunctionEQ(Term firstTerm, Term secondTerm) {
		return getFunctionStrictEQ(firstTerm, secondTerm);
	}

	public Expression getFunctionStrictEQ(Term firstTerm, Term secondTerm);

	ImmutableExpression getLexicalNonStrictEquality(ImmutableTerm lexicalTerm1, ImmutableTerm typeTerm1,
													ImmutableTerm lexicalTerm2, ImmutableTerm typeTerm2);

	ImmutableExpression getLexicalInequality(InequalityLabel inequalityLabel,
											 ImmutableTerm lexicalTerm1, ImmutableTerm typeTerm1,
											 ImmutableTerm lexicalTerm2, ImmutableTerm typeTerm2);

	ImmutableExpression getDBNonStrictNumericEquality(ImmutableTerm dbNumericTerm1, ImmutableTerm dbNumericTerm2);
	ImmutableExpression getDBNonStrictStringEquality(ImmutableTerm dbStringTerm1, ImmutableTerm dbStringTerm2);
	ImmutableExpression getDBNonStrictDatetimeEquality(ImmutableTerm dbDatetimeTerm1, ImmutableTerm dbDatetimeTerm2);

	/**
	 * Cannot be simplified --> has to be evaluated by the DB engine
	 *
	 * Only suitable for DB terms
	 */
	ImmutableExpression getDBNonStrictDefaultEquality(ImmutableTerm dbTerm1, ImmutableTerm dbTerm2);

	ImmutableExpression getDBNumericInequality(InequalityLabel inequalityLabel, ImmutableTerm dbNumericTerm1,
											   ImmutableTerm dbNumericTerm2);
	ImmutableExpression getDBBooleanInequality(InequalityLabel inequalityLabel, ImmutableTerm dbBooleanTerm1,
											   ImmutableTerm dbBooleanTerm2);
	ImmutableExpression getDBStringInequality(InequalityLabel inequalityLabel, ImmutableTerm dbStringTerm1,
											  ImmutableTerm dbStringTerm2);
	ImmutableExpression getDBDatetimeInequality(InequalityLabel inequalityLabel, ImmutableTerm dbDatetimeTerm1,
												ImmutableTerm dbDatetimeTerm2);
	ImmutableExpression getDBDefaultInequality(InequalityLabel inequalityLabel, ImmutableTerm dbTerm1,
											   ImmutableTerm dbTerm2);

	public Expression getFunctionNOT(Term term);

	public Expression getFunctionAND(Term term1, Term term2);

	public Expression getFunctionOR(Term term1, Term term2);

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

	/**
	 * Returns a DB boolean constant
	 */
	DBConstant getDBBooleanConstant(boolean value);

	/**
	 * Returns a DB string constant
	 */
	DBConstant getXsdBooleanLexicalConstant(boolean value);

	Constant getNullConstant();

	/**
	 * The resulting functional term may simplify to a regular NULL or not, depending on the DB system.
	 *
	 * Useful for PostgreSQL which has limited type inference capabilities when it comes to NULL and UNION (ALL).
	 *
	 */
	ImmutableFunctionalTerm getTypedNull(DBTermType termType);

	DBConstant getDBIntegerConstant(int value);

	/**
	 * Is empty if the DB does not support (and therefore does not store) not-a-number values
	 */
	Optional<DBConstant> getDoubleNaN();

	/**
	 * TODO: explain
	 */
	DBConstant getProvenanceSpecialConstant();

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
	 * temporaryCastToString == true must only be used when dealing with PRE-PROCESSED mapping
	 */
	ImmutableFunctionalTerm getIRIFunctionalTerm(Variable variable, boolean temporaryCastToString);

	/**
	 * At least one argument for the IRI functional term with an IRI template is required
	 */
	ImmutableFunctionalTerm getIRIFunctionalTerm(String iriTemplate, ImmutableList<? extends ImmutableTerm> arguments);

	/**
	 * When fact IRIs are decomposed (so as to be included in the mapping)
	 */
	ImmutableFunctionalTerm getIRIFunctionalTerm(IRIStringTemplateFunctionSymbol templateSymbol,
												 ImmutableList<DBConstant> arguments);

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
	 * May "normalize"
	 */
	ImmutableFunctionalTerm getConversion2RDFLexical(DBTermType inputType, ImmutableTerm term, RDFTermType rdfTermType);
	ImmutableFunctionalTerm getConversion2RDFLexical(ImmutableTerm term, RDFTermType rdfTermType);

	/**
	 * May "denormalize"
	 */
	ImmutableFunctionalTerm getConversionFromRDFLexical2DB(DBTermType targetDBType, ImmutableTerm dbTerm,
														   RDFTermType rdfType);

	ImmutableFunctionalTerm getConversionFromRDFLexical2DB(ImmutableTerm dbTerm, RDFTermType rdfType);


	/**
	 * Used when building (a fragment of) the lexical part of an RDF term
	 *   (either the full lexical value or a fragment involved in a template)
	 * in a PRE-PROCESSED mapping assertion.
	 *
	 * This functional term must not appear in the final mapping
	 */
	ImmutableFunctionalTerm getPartiallyDefinedToStringCast(Variable variable);

	ImmutableExpression getRDF2DBBooleanFunctionalTerm(ImmutableTerm xsdBooleanTerm);

	ImmutableFunctionalTerm getIfElseNull(ImmutableExpression condition, ImmutableTerm term);

	ImmutableExpression getBooleanIfElseNull(ImmutableExpression condition, ImmutableExpression thenExpression);

	ImmutableFunctionalTerm getIfThenElse(ImmutableExpression condition, ImmutableTerm thenTerm, ImmutableTerm elseTerm);

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

	ImmutableFunctionalTerm getDBReplace(ImmutableTerm arg, ImmutableTerm pattern, ImmutableTerm replacement);

	ImmutableFunctionalTerm getDBRegexpReplace(ImmutableTerm arg, ImmutableTerm pattern, ImmutableTerm replacement);

	ImmutableFunctionalTerm getDBRegexpReplace(ImmutableTerm arg, ImmutableTerm pattern, ImmutableTerm replacement,
											   ImmutableTerm flags);

	ImmutableExpression getDBStartsWith(ImmutableList<ImmutableTerm> terms);
	ImmutableExpression getDBEndsWith(ImmutableList<? extends ImmutableTerm> terms);
	ImmutableExpression getDBContains(ImmutableList<? extends ImmutableTerm> terms);
	ImmutableExpression getDBRegexpMatches(ImmutableList<ImmutableTerm> terms);


	ImmutableFunctionalTerm getR2RMLIRISafeEncodeFunctionalTerm(ImmutableTerm term);

	/**
	 * At least two terms are expected
	 */
	ImmutableFunctionalTerm getNullRejectingDBConcatFunctionalTerm(ImmutableList<? extends ImmutableTerm> terms);

    ImmutableFunctionalTerm getCommonDenominatorFunctionalTerm(ImmutableList<ImmutableTerm> typeTerms);

	/**
	 * terms must have at least two distinct elements
	 */
	ImmutableExpression getStrictEquality(ImmutableSet<ImmutableTerm> terms);

	/**
	 * terms must have at least two elements
	 */
	ImmutableExpression getStrictEquality(ImmutableList<? extends ImmutableTerm> terms);

	ImmutableExpression getStrictEquality(ImmutableTerm term1, ImmutableTerm term2, ImmutableTerm... otherTerms);

	/**
	 * terms must have at least two elements
	 * Logically equivalent to NOT(STRICT_EQx(...))
	 */
	ImmutableExpression getStrictNEquality(ImmutableSet<ImmutableTerm> terms);

	ImmutableExpression getDBIsStringEmpty(ImmutableTerm stringTerm);

	/**
	 * terms must have at least two elements
	 * Logically equivalent to NOT(STRICT_EQx(...))
	 */
	ImmutableExpression getStrictNEquality(ImmutableList<? extends ImmutableTerm> terms);

	ImmutableExpression getStrictNEquality(ImmutableTerm term1, ImmutableTerm term2, ImmutableTerm... otherTerms);

	/**
	 * Wraps a DB boolean constant/variable into an ImmutableExpression
	 */
	ImmutableExpression getIsTrue(NonFunctionalTerm dbBooleanTerm);

	ImmutableFunctionalTerm getDBSubString2(ImmutableTerm stringTerm, ImmutableTerm from);

	ImmutableFunctionalTerm getDBSubString3(ImmutableTerm stringTerm, ImmutableTerm from, ImmutableTerm to);

	ImmutableFunctionalTerm getDBRight(ImmutableTerm stringTerm, ImmutableTerm lengthTerm);

	ImmutableFunctionalTerm getDBUpper(ImmutableTerm stringTerm);

	ImmutableFunctionalTerm getDBLower(ImmutableTerm stringTerm);

	/**
	 * Do NOT confuse it with the LANG SPARQL function
	 */
	ImmutableFunctionalTerm getLangTypeFunctionalTerm(ImmutableTerm rdfTypeTerm);

	/**
	 * Do NOT confuse it with the langMatches SPARQL function
	 */
	ImmutableExpression getLexicalLangMatches(ImmutableTerm langTagTerm, ImmutableTerm langRangeTerm);

	TypeFactory getTypeFactory();

    VariableNullability createDummyVariableNullability(ImmutableFunctionalTerm functionalTerm);

    ImmutableFunctionalTerm getRDFDatatypeStringFunctionalTerm(ImmutableTerm rdfTypeTerm);

	ImmutableFunctionalTerm getDBUUID(UUID uuid);

	ImmutableFunctionalTerm getDBStrBefore(ImmutableTerm arg1, ImmutableTerm arg2);
	ImmutableFunctionalTerm getDBStrAfter(ImmutableTerm arg1, ImmutableTerm arg2);

	ImmutableFunctionalTerm getDBCharLength(ImmutableTerm stringTerm);

	ImmutableExpression getDBIsNull(ImmutableTerm immutableTerm);
	ImmutableExpression getDBIsNotNull(ImmutableTerm immutableTerm);

    ImmutableFunctionalTerm getDBMd5(ImmutableTerm stringTerm);
	ImmutableFunctionalTerm getDBSha1(ImmutableTerm stringTerm);
	ImmutableFunctionalTerm getDBSha256(ImmutableTerm stringTerm);
	ImmutableFunctionalTerm getDBSha512(ImmutableTerm stringTerm);

	ImmutableFunctionalTerm getCommonPropagatedOrSubstitutedNumericType(ImmutableTerm rdfTypeTerm1,
																		ImmutableTerm rdfTypeTerm2);

	DBFunctionSymbolFactory getDBFunctionSymbolFactory();

	/**
	 * TODO: find a better name
	 *
	 */
	ImmutableFunctionalTerm getBinaryNumericLexicalFunctionalTerm(String dbNumericOperationName,
																  ImmutableTerm lexicalTerm1,
																  ImmutableTerm lexicalTerm2,
																  ImmutableTerm rdfTypeTerm);

	ImmutableFunctionalTerm getDBBinaryNumericFunctionalTerm(String dbNumericOperationName, DBTermType dbNumericType,
															 ImmutableTerm dbTerm1, ImmutableTerm dbTerm2);

	ImmutableFunctionalTerm getUnaryLexicalFunctionalTerm(
			ImmutableTerm lexicalTerm, ImmutableTerm rdfDatatypeTerm,
			java.util.function.Function<DBTermType, DBFunctionSymbol> dbFunctionSymbolFct);

	/**
	 * Using the SPARQL "=" operator
	 *
	 * Returns an XSD.BOOLEAN
	 */
	ImmutableFunctionalTerm getSPARQLNonStrictEquality(ImmutableTerm rdfTerm1, ImmutableTerm rdfTerm2);

	/**
	 * Returns an XSD.BOOLEAN
	 */
	ImmutableFunctionalTerm getSPARQLEffectiveBooleanValue(ImmutableTerm rdfTerm);

	ImmutableExpression getLexicalEffectiveBooleanValue(ImmutableTerm lexicalTerm, ImmutableTerm rdfDatatypeTerm);

	ImmutableFunctionalTerm getDBRand(UUID uuid);

	ImmutableFunctionalTerm getDBYear(ImmutableTerm dbDatetimeTerm);
	ImmutableFunctionalTerm getDBMonth(ImmutableTerm dbDatetimeTerm);
	ImmutableFunctionalTerm getDBDay(ImmutableTerm dbDatetimeTerm);
	ImmutableFunctionalTerm getDBHours(ImmutableTerm dbDatetimeTerm);
	ImmutableFunctionalTerm getDBMinutes(ImmutableTerm dbDatetimeTerm);
	ImmutableFunctionalTerm getDBSeconds(ImmutableTerm dbDatetimeTerm);
	ImmutableFunctionalTerm getDBTz(ImmutableTerm dbDatetimeTerm);
	ImmutableFunctionalTerm getDBNow();
}
