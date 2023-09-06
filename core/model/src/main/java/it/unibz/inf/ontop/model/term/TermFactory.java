package it.unibz.inf.ontop.model.term;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.tools.TypeConstantDictionary;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.functionsymbol.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.IRIStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.substitution.Substitution;
import org.apache.commons.rdf.api.IRI;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Accessible through Guice (recommended) or through CoreSingletons.
 */
public interface TermFactory {

	/*
	 * Built-in function predicates
	 */

	ImmutableExpression getImmutableExpression(BooleanFunctionSymbol functor, ImmutableTerm... arguments);

	ImmutableExpression getImmutableExpression(BooleanFunctionSymbol functor,
											   ImmutableList<? extends ImmutableTerm> arguments);

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
	 * May be empty.
	 *
	 * Takes care of flattening the of the optional expression, BUT not of expressionStream
	 */
	Optional<ImmutableExpression> getConjunction(Optional<ImmutableExpression> optionalExpression, Stream<ImmutableExpression> expressionStream);

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

	ImmutableFunctionalTerm.FunctionalTermDecomposition getFunctionalTermDecomposition(ImmutableTerm liftableTerm);
	ImmutableFunctionalTerm.FunctionalTermDecomposition getFunctionalTermDecomposition(
			ImmutableTerm liftableTerm,
			Substitution<ImmutableFunctionalTerm> subTermSubstitution);



	ImmutableFunctionalTerm getImmutableFunctionalTerm(FunctionSymbol functor, ImmutableList<? extends ImmutableTerm> terms);

	ImmutableFunctionalTerm getImmutableFunctionalTerm(FunctionSymbol functor, ImmutableTerm... terms);

	NonGroundFunctionalTerm getNonGroundFunctionalTerm(FunctionSymbol functor, ImmutableTerm... terms);

	NonGroundFunctionalTerm getNonGroundFunctionalTerm(FunctionSymbol functor, ImmutableList<ImmutableTerm> terms);


	/*
	 * Boolean function terms
	 */

	/**
	 * To be used when parsing the mapping and when an equality is found.
	 * Is expected to be replaced later by a proper equality (can be strict or not)
	 */
	ImmutableExpression getNotYetTypedEquality(ImmutableTerm t1, ImmutableTerm t2);

	ImmutableExpression getLexicalNonStrictEquality(ImmutableTerm lexicalTerm1, ImmutableTerm typeTerm1,
													ImmutableTerm lexicalTerm2, ImmutableTerm typeTerm2);

	ImmutableExpression getLexicalInequality(InequalityLabel inequalityLabel,
											 ImmutableTerm lexicalTerm1, ImmutableTerm typeTerm1,
											 ImmutableTerm lexicalTerm2, ImmutableTerm typeTerm2);

	ImmutableExpression getDBNonStrictNumericEquality(ImmutableTerm dbNumericTerm1, ImmutableTerm dbNumericTerm2);
	ImmutableExpression getDBNonStrictStringEquality(ImmutableTerm dbStringTerm1, ImmutableTerm dbStringTerm2);
	ImmutableExpression getDBNonStrictDatetimeEquality(ImmutableTerm dbDatetimeTerm1, ImmutableTerm dbDatetimeTerm2);
	ImmutableExpression getDBNonStrictDateEquality(ImmutableTerm dbTerm1, ImmutableTerm dbTerm2);

	/**
	 * Cannot be simplified {@code -->} has to be evaluated by the DB engine
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
	ImmutableExpression getDBDateInequality(InequalityLabel inequalityLabel, ImmutableTerm dbDateTerm1,
											ImmutableTerm dbDateTerm2);

	ImmutableExpression getDBDefaultInequality(InequalityLabel inequalityLabel, ImmutableTerm dbTerm1,
											   ImmutableTerm dbTerm2);

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
	 * http://example.org/some/paths <br>
	 * http://example.org/some/paths/to/resource#frag01 <br>
	 * ftp://example.org/resource.txt <br>
	 * </code>
	 * <p>
	 * are all well-formed URI strings.
	 *
	 * @param iri
	 *            the URI.
	 * @return a URI constant.
	 */
	IRIConstant getConstantIRI(IRI iri);

	IRIConstant getConstantIRI(String iri);

	BNode getConstantBNode(String name);

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
	 * "Person"^^xsd:String <br>
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
	 * "This is American English"@en-US
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
	 * pred($x) <br>
	 * func(?x, ?y)
	 * </code>
	 *
	 * @param name
	 *            the name of the variable.
	 * @return the variable object.
	 */
	Variable getVariable(String name);

	RDFTermTypeConstant getRDFTermTypeConstant(RDFTermType type);

	ImmutableFunctionalTerm getRDFTermTypeFunctionalTerm(ImmutableTerm term, TypeConstantDictionary dictionary,
														 ImmutableSet<RDFTermTypeConstant> possibleConstants,
														 boolean isSimplifiable);

	ImmutableFunctionalTerm getRDFFunctionalTerm(ImmutableTerm lexicalTerm, ImmutableTerm typeTerm);

	/**
	 * @param term is a variable or a cast variable
	 */
	ImmutableFunctionalTerm getIRIFunctionalTerm(ImmutableTerm term);

	/**
	 * At least one argument for the IRI functional term with an IRI template is required
	 */
	ImmutableFunctionalTerm getIRIFunctionalTerm(ImmutableList<Template.Component> iriTemplate, ImmutableList<? extends ImmutableTerm> arguments);

	/**
	 * When fact IRIs are decomposed (so as to be included in the mapping)
	 */
	ImmutableFunctionalTerm getIRIFunctionalTerm(IRIStringTemplateFunctionSymbol templateSymbol,
												 ImmutableList<DBConstant> arguments);

	/**
	 * @param term is a variable or a cast variable
	 */
	ImmutableFunctionalTerm getBnodeFunctionalTerm(ImmutableTerm term);

	ImmutableFunctionalTerm getBnodeFunctionalTerm(ImmutableList<Template.Component> bnodeTemplate,
												   ImmutableList<? extends ImmutableTerm> arguments);

	ImmutableFunctionalTerm getDBCastFunctionalTerm(DBTermType targetType, ImmutableTerm term);
	ImmutableFunctionalTerm getDBCastFunctionalTerm(DBTermType inputType, DBTermType targetType, ImmutableTerm term);

	/**
	 * The first sub-term encodes the index of the term to return.
	 * Such values correspond to the following sub-terms
	 *
	 * For instance DB_IDX(1, "roger", "francis", "ernest") returns "francis"
	 *
	 */
	ImmutableFunctionalTerm getDBIntIndex(ImmutableTerm idTerm, ImmutableTerm... possibleValues);
	ImmutableFunctionalTerm getDBIntIndex(ImmutableTerm idTerm, ImmutableList<ImmutableTerm> possibleValues);

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

	ImmutableFunctionalTerm getConversionFromRDFLexical2DB(DBTermType targetDBType, ImmutableTerm dbTerm);

	ImmutableFunctionalTerm getConversionFromRDFLexical2DB(ImmutableTerm dbTerm, RDFTermType rdfType);


	/**
	 * Used when building (a fragment of) the lexical part of an RDF term
	 *   (either the full lexical value or a fragment involved in a template)
	 * in a PRE-PROCESSED mapping assertion.
	 *
	 * This functional term must not appear in the final mapping
	 */
	ImmutableFunctionalTerm getPartiallyDefinedConversionToString(Variable variable);

	ImmutableExpression getRDF2DBBooleanFunctionalTerm(ImmutableTerm xsdBooleanTerm);

	ImmutableFunctionalTerm getIfElseNull(ImmutableExpression condition, ImmutableTerm term);

	ImmutableExpression getBooleanIfElseNull(ImmutableExpression condition, ImmutableExpression thenExpression);

	ImmutableFunctionalTerm getIfThenElse(ImmutableExpression condition, ImmutableTerm thenTerm, ImmutableTerm elseTerm);

	/**
	 * IF THEN, ELSE IF ..., ELSE
	 *
	 * whenPairs must not be empty
	 *
	 * doOrderingMatter: if false, the when pairs can be re-ordered
	 */
	ImmutableFunctionalTerm getDBCase(Stream<? extends Map.Entry<ImmutableExpression, ? extends ImmutableTerm>> whenPairs,
									  ImmutableTerm defaultTerm, boolean doOrderingMatter);

	/**
	 * IF THEN, ELSE IF ..., ELSE NULL
	 *
	 * whenPairs must not be empty
	 */
	ImmutableFunctionalTerm getDBCaseElseNull(Stream<? extends Map.Entry<ImmutableExpression, ? extends ImmutableTerm>> whenPairs,
											  boolean doOrderingMatter);

	ImmutableExpression getDBBooleanCase(Stream<Map.Entry<ImmutableExpression, ImmutableExpression>> whenPairs,
										 ImmutableExpression defaultValue, boolean doOrderingMatter);

	ImmutableFunctionalTerm getDBCoalesce(ImmutableTerm term1, ImmutableTerm term2, ImmutableTerm... terms);

	ImmutableFunctionalTerm getDBCoalesce(ImmutableList<ImmutableTerm> terms);

	ImmutableExpression getDBBooleanCoalesce(ImmutableList<ImmutableTerm> terms);

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
	 * NB: encodes international characters (i.e. not safe for IRIs in general)
	 */
	ImmutableFunctionalTerm getDBEncodeForURI(ImmutableTerm term);

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

	Optional<ImmutableExpression> getDBIsNotNull(Stream<? extends ImmutableTerm> immutableTerms);

	ImmutableFunctionalTerm getDBMd5(ImmutableTerm stringTerm);
	ImmutableFunctionalTerm getDBSha1(ImmutableTerm stringTerm);
	ImmutableFunctionalTerm getDBSha256(ImmutableTerm stringTerm);
	ImmutableFunctionalTerm getDBSha384(ImmutableTerm stringTerm);
	ImmutableFunctionalTerm getDBSha512(ImmutableTerm stringTerm);

//	ImmutableFunctionalTerm getDBIndexIn(Variable arg);

	ImmutableFunctionalTerm getCommonPropagatedOrSubstitutedNumericType(ImmutableTerm rdfTypeTerm1,
																		ImmutableTerm rdfTypeTerm2);

	DBFunctionSymbolFactory getDBFunctionSymbolFactory();

	/**
	 * Minimalist substitution with minimal dependencies.
	 * Designed to be used by FunctionSymbols.
	 *
	 * See the SubstitutionFactory for richer substitutions
	 */
	<T extends ImmutableTerm> Substitution<T> getSubstitution(ImmutableMap<Variable, T> map);

	/**
	 * TODO: find a better name
	 *
	 */
	ImmutableFunctionalTerm getBinaryNumericLexicalFunctionalTerm(String dbNumericOperationName,
																  ImmutableTerm lexicalTerm1,
																  ImmutableTerm lexicalTerm2,
																  ImmutableTerm rdfTypeTerm);

	ImmutableFunctionalTerm getDBBinaryNumericFunctionalTerm(String dbNumericOperationName, DBTermType dbNumericType,
															 ImmutableTerm dbTerm1, ImmutableTerm dbTerm2);

	ImmutableFunctionalTerm getDBBinaryNumericFunctionalTerm(String dbNumericOperationName, DBTermType argumentType1, DBTermType argumentType2,
															 ImmutableTerm dbTerm1, ImmutableTerm dbTerm2);

	ImmutableFunctionalTerm getUnaryLatelyTypedFunctionalTerm(
			ImmutableTerm lexicalTerm, ImmutableTerm inputRDFTypeTerm, DBTermType targetType,
			java.util.function.Function<DBTermType, Optional<DBFunctionSymbol>> dbFunctionSymbolFct);

	ImmutableFunctionalTerm getUnaryLexicalFunctionalTerm(
			ImmutableTerm lexicalTerm, ImmutableTerm rdfDatatypeTerm,
			java.util.function.Function<DBTermType, Optional<DBFunctionSymbol>> dbFunctionSymbolFct);

	ImmutableFunctionalTerm getBinaryLatelyTypedFunctionalTerm(
			ImmutableTerm lexicalTerm0, ImmutableTerm lexicalTerm1, ImmutableTerm inputRDFTypeTerm0,
			ImmutableTerm inputRDFTypeTerm1, DBTermType targetType,
			java.util.function.Function<DBTermType, Optional<DBFunctionSymbol>> dbFunctionSymbolFct);

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

	ImmutableFunctionalTerm getDBYearFromDatetime(ImmutableTerm dbDatetimeTerm);
	ImmutableFunctionalTerm getDBMonthFromDatetime(ImmutableTerm dbDatetimeTerm);
	ImmutableFunctionalTerm getDBDayFromDatetime(ImmutableTerm dbDatetimeTerm);
	ImmutableFunctionalTerm getDBHours(ImmutableTerm dbDatetimeTerm);
	ImmutableFunctionalTerm getDBMinutes(ImmutableTerm dbDatetimeTerm);
	ImmutableFunctionalTerm getDBSeconds(ImmutableTerm dbDatetimeTerm);
	ImmutableFunctionalTerm getDBTz(ImmutableTerm dbDatetimeTerm);
	ImmutableFunctionalTerm getDBNow();

	ImmutableFunctionalTerm getDBRowUniqueStr();

	ImmutableFunctionalTerm getDBRowNumber();

	ImmutableFunctionalTerm getDBIriStringResolution(IRI baseIRI, ImmutableTerm argLexical);

	//-------------
	// Aggregation
	//-------------

	ImmutableFunctionalTerm getDBCount(boolean isDistinct);
    ImmutableFunctionalTerm getDBCount(ImmutableTerm subTerm, boolean isDistinct);

	ImmutableFunctionalTerm getDBSum(ImmutableTerm subTerm, DBTermType dbType, boolean isDistinct);
	ImmutableFunctionalTerm getDBAvg(ImmutableTerm subTerm, DBTermType dbType, boolean isDistinct);
	ImmutableFunctionalTerm getDBStdev(ImmutableTerm subTerm, DBTermType dbType, boolean isPop, boolean isDistinct);
	ImmutableFunctionalTerm getDBVariance(ImmutableTerm subTerm, DBTermType dbType, boolean isPop, boolean isDistinct);

	ImmutableFunctionalTerm getDBMin(ImmutableTerm subTerm, DBTermType dbType);
    ImmutableFunctionalTerm getDBMax(ImmutableTerm subTerm, DBTermType dbType);

	ImmutableFunctionalTerm getDBSample(ImmutableTerm subTerm, DBTermType dbType);

	ImmutableFunctionalTerm getDBGroupConcat(ImmutableTerm subTerm, String separator, boolean isDistinct);

	// Topological functions
	ImmutableTerm getDBSTWithin(ImmutableTerm arg1, ImmutableTerm arg2);
	ImmutableTerm getDBSTContains(ImmutableTerm arg1, ImmutableTerm arg2);
	ImmutableTerm getDBSTCrosses(ImmutableTerm arg1, ImmutableTerm arg2);
	ImmutableTerm getDBSTDisjoint(ImmutableTerm arg1, ImmutableTerm arg2);
	ImmutableTerm getDBSTEquals(ImmutableTerm arg1, ImmutableTerm arg2);
	ImmutableTerm getDBSTIntersects(ImmutableTerm arg1, ImmutableTerm arg2);
	ImmutableTerm getDBSTOverlaps(ImmutableTerm arg1, ImmutableTerm arg2);
	ImmutableTerm getDBSTTouches(ImmutableTerm arg1, ImmutableTerm arg2);
	ImmutableTerm getDBSTCoveredBy(ImmutableTerm arg1, ImmutableTerm arg2);
	ImmutableTerm getDBSTCovers(ImmutableTerm arg1, ImmutableTerm arg2);
	ImmutableTerm getDBSTContainsProperly(ImmutableTerm arg1, ImmutableTerm arg2);

	// Non-topological and common form functions
	ImmutableTerm getDBSTSTransform(ImmutableTerm arg1, ImmutableTerm srid);
	ImmutableTerm getDBSTSetSRID(ImmutableTerm arg1, ImmutableTerm arg2);
	ImmutableTerm getDBSTGeomFromText(ImmutableTerm arg1);
	ImmutableTerm getDBSTMakePoint(ImmutableTerm arg1, ImmutableTerm arg2);
	ImmutableTerm getDBSTFlipCoordinates(ImmutableTerm arg1);
	ImmutableTerm getDBSTDistanceSphere(ImmutableTerm arg1, ImmutableTerm arg2);
	ImmutableTerm getDBSTDistanceSpheroid(ImmutableTerm arg1, ImmutableTerm arg2, ImmutableTerm arg3);
	ImmutableTerm getDBSTDistance(ImmutableTerm arg1, ImmutableTerm arg2);
	ImmutableTerm getDBIntersection(ImmutableTerm arg1, ImmutableTerm arg2);
	ImmutableTerm getDBBoundary(ImmutableTerm arg1);
	ImmutableTerm getDBConvexHull(ImmutableTerm arg1);
	ImmutableTerm getDBDifference(ImmutableTerm arg1, ImmutableTerm arg2);
	ImmutableTerm getDBEnvelope(ImmutableTerm arg1);
	ImmutableTerm getDBSymDifference(ImmutableTerm arg1, ImmutableTerm arg2);
	ImmutableTerm getDBUnion(ImmutableTerm arg1, ImmutableTerm arg2);
	ImmutableTerm getDBRelate(ImmutableTerm arg1, ImmutableTerm arg2, ImmutableTerm arg3);
	ImmutableTerm getDBRelateMatrix(ImmutableTerm arg1, ImmutableTerm arg2);
	ImmutableTerm getDBGetSRID(ImmutableTerm arg1);
	ImmutableTerm getDBAsText(ImmutableTerm arg1);
	ImmutableTerm getDBBuffer(ImmutableTerm arg1, ImmutableTerm arg2);

	/**
	 * Time extension - duration arithmetic
	 */
	ImmutableFunctionalTerm getDBWeeksBetweenFromDateTime(ImmutableTerm arg1, ImmutableTerm arg2);
	ImmutableFunctionalTerm getDBWeeksBetweenFromDate(ImmutableTerm arg1, ImmutableTerm arg2);
	ImmutableFunctionalTerm getDBDaysBetweenFromDateTime(ImmutableTerm arg1, ImmutableTerm arg2);
	ImmutableFunctionalTerm getDBDaysBetweenFromDate(ImmutableTerm arg1, ImmutableTerm arg2);
	ImmutableFunctionalTerm getDBHoursBetweenFromDateTime(ImmutableTerm arg1, ImmutableTerm arg2);
	ImmutableFunctionalTerm getDBMinutesBetweenFromDateTime(ImmutableTerm arg1, ImmutableTerm arg2);
	ImmutableFunctionalTerm getDBSecondsBetweenFromDateTime(ImmutableTerm arg1, ImmutableTerm arg2);
	ImmutableFunctionalTerm getDBMillisBetweenFromDateTime(ImmutableTerm arg1, ImmutableTerm arg2);


	// JSON
	ImmutableFunctionalTerm getDBJsonElement(ImmutableTerm arg, ImmutableList<String> path);
	ImmutableFunctionalTerm getDBJsonElementAsText(ImmutableTerm arg, ImmutableList<String> path);
	ImmutableExpression getDBJsonIsBoolean(DBTermType dbType, ImmutableTerm arg);
	ImmutableExpression getDBJsonIsNumber(DBTermType dbType, ImmutableTerm arg);
	ImmutableExpression getDBJsonIsScalar(DBTermType dbType, ImmutableTerm arg);
	ImmutableExpression getDBIsArray(DBTermType dbType, ImmutableTerm arg);


	// Ontop-defined functions

	ImmutableFunctionalTerm getDBWeek(ImmutableTerm dbDatetimeTerm);
	ImmutableFunctionalTerm getDBQuarter(ImmutableTerm dbDatetimeTerm);
	ImmutableFunctionalTerm getDBDecade(ImmutableTerm dbDatetimeTerm);
	ImmutableFunctionalTerm getDBCentury(ImmutableTerm dbDatetimeTerm);
	ImmutableFunctionalTerm getDBMillennium(ImmutableTerm dbDatetimeTerm);
	ImmutableFunctionalTerm getDBMilliseconds(ImmutableTerm dbDatetimeTerm);
	ImmutableFunctionalTerm getDBMicroseconds(ImmutableTerm dbDatetimeTerm);

	ImmutableFunctionalTerm getDBDateTrunc(ImmutableTerm dbDatetimeTerm, ImmutableTerm datePartTerm, String datePart);
}
