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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.iq.tools.TypeConstantDictionary;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.IRIStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;

import java.util.*;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.term.functionsymbol.BooleanExpressionOperation.OR;

@Singleton
public class TermFactoryImpl implements TermFactory {

	private final TypeFactory typeFactory;
	private final FunctionSymbolFactory functionSymbolFactory;
	private final DBFunctionSymbolFactory dbFunctionSymbolFactory;
	private final DBConstant valueTrue;
	private final DBConstant valueFalse;
	private final Constant valueNull;
	// TODO: make it be a DBConstant
	private final RDFLiteralConstant provenanceConstant;
	private final ImmutabilityTools immutabilityTools;
	private final Map<RDFTermType, RDFTermTypeConstant> termTypeConstantMap;
	private final boolean isTestModeEnabled;
	private final RDFTermTypeConstant iriTypeConstant, bnodeTypeConstant;
	private final RDF rdfFactory;
	private final ImmutableExpression.Evaluation positiveEvaluation, negativeEvaluation, nullEvaluation;

	@Inject
	private TermFactoryImpl(TypeFactory typeFactory, FunctionSymbolFactory functionSymbolFactory,
							DBFunctionSymbolFactory dbFunctionSymbolFactory, OntopModelSettings settings,
							RDF rdfFactory) {
		// protected constructor prevents instantiation from other classes.
		this.typeFactory = typeFactory;
		this.functionSymbolFactory = functionSymbolFactory;
		this.dbFunctionSymbolFactory = dbFunctionSymbolFactory;
		this.rdfFactory = rdfFactory;

		DBTermType dbBooleanType = typeFactory.getDBTypeFactory().getDBBooleanType();
		// TODO: let a DB-specific class have the control over DB constant creation
		this.valueTrue = new DBConstantImpl("true", dbBooleanType);
		this.valueFalse = new DBConstantImpl("false", dbBooleanType);
		this.valueNull = new NullConstantImpl();
		this.provenanceConstant = new RDFLiteralConstantImpl("ontop-provenance-constant", typeFactory.getXsdStringDatatype());
		this.immutabilityTools = new ImmutabilityTools(this);
		this.termTypeConstantMap = new HashMap<>();
		this.isTestModeEnabled = settings.isTestModeEnabled();
		this.iriTypeConstant = getRDFTermTypeConstant(typeFactory.getIRITermType());
		this.bnodeTypeConstant = getRDFTermTypeConstant(typeFactory.getBlankNodeType());
		this.positiveEvaluation = new ImmutableExpressionImpl.ValueEvaluationImpl(
				ImmutableExpression.Evaluation.BooleanValue.TRUE, valueTrue);
		this.negativeEvaluation = new ImmutableExpressionImpl.ValueEvaluationImpl(
				ImmutableExpression.Evaluation.BooleanValue.FALSE, valueFalse);
		this.nullEvaluation = new ImmutableExpressionImpl.ValueEvaluationImpl(
				ImmutableExpression.Evaluation.BooleanValue.NULL, valueNull);
	}

	@Override
	public IRIConstant getConstantIRI(IRI iri) {
		return new IRIConstantImpl(iri, typeFactory);
	}

	@Override
	public RDFLiteralConstant getRDFLiteralConstant(String value, RDFDatatype type) {
		return new RDFLiteralConstantImpl(value, type);
	}

	@Override
	public RDFLiteralConstant getRDFLiteralConstant(String value, IRI type) {
		return getRDFLiteralConstant(value, typeFactory.getDatatype(type));
	}

	@Override
	public Function getRDFLiteralMutableFunctionalTerm(Term lexicalTerm, RDFDatatype type) {
		return getFunction(functionSymbolFactory.getRDFTermFunctionSymbol(), lexicalTerm, getRDFTermTypeConstant(type));
	}

	@Override
	public Function getRDFLiteralMutableFunctionalTerm(Term lexicalTerm, IRI datatypeIRI) {
		return getRDFLiteralMutableFunctionalTerm(lexicalTerm, typeFactory.getDatatype(datatypeIRI));
	}

	@Override
	public RDFLiteralConstant getRDFLiteralConstant(String value, String language) {
		return new RDFLiteralConstantImpl(value, language.toLowerCase(), typeFactory);
	}

	@Override
	public RDFConstant getRDFConstant(String lexicalValue, RDFTermType termType) {
		if (termType.isAbstract())
			throw new IllegalArgumentException("Cannot create an RDFConstant out of a abstract term type");
		if (termType instanceof RDFDatatype)
			return getRDFLiteralConstant(lexicalValue, (RDFDatatype)termType);
		else if (termType.equals(typeFactory.getIRITermType()))
			return getConstantIRI(rdfFactory.createIRI(lexicalValue));
		else if (termType.equals(typeFactory.getBlankNodeType()))
			return getConstantBNode(lexicalValue);
		throw new MinorOntopInternalBugException("Unexpected RDF term type: " + termType);
	}

	@Override
	public Function getRDFLiteralMutableFunctionalTerm(Term lexicalTerm, String language) {
		return getRDFLiteralMutableFunctionalTerm(lexicalTerm, typeFactory.getLangTermType(language));
	}

	@Override
	public ImmutableFunctionalTerm getRDFLiteralFunctionalTerm(ImmutableTerm lexicalTerm, RDFDatatype type) {
		return getRDFFunctionalTerm(lexicalTerm, getRDFTermTypeConstant(type));
	}

	@Override
	public ImmutableFunctionalTerm getRDFLiteralFunctionalTerm(ImmutableTerm lexicalTerm, IRI datatypeIRI) {
		return getRDFLiteralFunctionalTerm(lexicalTerm, typeFactory.getDatatype(datatypeIRI));
	}

	@Override
	public DBConstant getDBConstant(String value, DBTermType termType) {
		return new DBConstantImpl(value, termType);
	}

	@Override
	public DBConstant getDBStringConstant(String value) {
		return getDBConstant(value, typeFactory.getDBTypeFactory().getDBStringType());
	}

	@Override
	public ImmutableFunctionalTerm getRDFLiteralFunctionalTerm(ImmutableTerm lexicalTerm, String language) {
		return getRDFLiteralFunctionalTerm(lexicalTerm, typeFactory.getLangTermType(language));
	}

	@Override
	public Variable getVariable(String name) {
		return new VariableImpl(name);
	}

	@Override
	public RDFTermTypeConstant getRDFTermTypeConstant(RDFTermType type) {
		return termTypeConstantMap
				.computeIfAbsent(type, t -> new RDFTermTypeConstantImpl(t, typeFactory.getMetaRDFTermType()));
	}

	@Override
	public ImmutableFunctionalTerm getRDFTermTypeFunctionalTerm(ImmutableTerm term, TypeConstantDictionary dictionary,
			ImmutableSet<RDFTermTypeConstant> possibleConstants) {
		return getImmutableFunctionalTerm(functionSymbolFactory.getRDFTermTypeFunctionSymbol(dictionary, possibleConstants), term);
	}

	@Override
	public Function getFunction(Predicate functor, Term... arguments) {
		return getFunction(functor, Arrays.asList(arguments));
	}
	
	@Override
	public Expression getExpression(BooleanFunctionSymbol functor, Term... arguments) {
		return getExpression(functor, Arrays.asList(arguments));
	}

	@Override
	public Expression getExpression(BooleanFunctionSymbol functor, List<Term> arguments) {
		if (isTestModeEnabled) {
			checkMutability(arguments);
		}
		return new ExpressionImpl(functor, arguments);
	}

	@Override
	public ImmutableExpression getImmutableExpression(BooleanFunctionSymbol functor, ImmutableTerm... arguments) {
		return getImmutableExpression(functor, ImmutableList.copyOf(arguments));
	}

	@Override
	public ImmutableExpression getImmutableExpression(BooleanFunctionSymbol functor,
													  ImmutableList<? extends ImmutableTerm> arguments) {
		if (GroundTermTools.areGroundTerms(arguments)) {
			return new GroundExpressionImpl(functor, (ImmutableList<GroundTerm>)arguments, this);
		}
		else {
			return new NonGroundExpressionImpl(functor, arguments, this);
		}
	}

	@Override
	public ImmutableExpression getImmutableExpression(Expression expression) {
		if (GroundTermTools.isGroundTerm(expression)) {
			return new GroundExpressionImpl(expression.getFunctionSymbol(),
					(ImmutableList<? extends GroundTerm>)(ImmutableList<?>)convertTerms(expression), this);
		}
		else {
			return new NonGroundExpressionImpl(expression.getFunctionSymbol(), convertTerms(expression), this);
		}
	}

	@Override
	public ImmutableExpression getConjunction(ImmutableList<ImmutableExpression> conjunctionOfExpressions) {
		final int size = conjunctionOfExpressions.size();
		switch (size) {
			case 0:
				throw new IllegalArgumentException("conjunctionOfExpressions must be non-empty");
			case 1:
				return conjunctionOfExpressions.get(0);
			default:
				return getImmutableExpression(dbFunctionSymbolFactory.getDBAnd(size), conjunctionOfExpressions);
		}
	}

	@Override
	public ImmutableExpression getConjunction(ImmutableExpression expression, ImmutableExpression... otherExpressions) {
		return getConjunction(
				Stream.concat(Stream.of(expression), Stream.of(otherExpressions))
				.collect(ImmutableCollectors.toList()));
	}

	@Override
	public Optional<ImmutableExpression> getConjunction(Stream<ImmutableExpression> expressionStream) {
		ImmutableList<ImmutableExpression> conjuncts = expressionStream
				.flatMap(ImmutableExpression::flattenAND)
				.distinct()
				.collect(ImmutableCollectors.toList());

		return Optional.of(conjuncts)
				.filter(c -> !c.isEmpty())
				.map(this::getConjunction);
	}

	@Override
	public ImmutableExpression getDisjunction(ImmutableList<ImmutableExpression> disjunctionOfExpressions) {
		final int size = disjunctionOfExpressions.size();
		switch (size) {
			case 0:
				throw new IllegalArgumentException("disjunctionOfExpressions must be non-empty");
			case 1:
				return disjunctionOfExpressions.get(0);
			case 2:
				return getImmutableExpression(OR, disjunctionOfExpressions);
			default:
				// Non-final
				ImmutableExpression cumulativeExpression = getImmutableExpression(
						OR,
						disjunctionOfExpressions.get(0),
						disjunctionOfExpressions.get(1));
				for (int i = 2; i < size; i++) {
					cumulativeExpression = getImmutableExpression(
							OR,
							cumulativeExpression,
							disjunctionOfExpressions.get(i));
				}
				return cumulativeExpression;
		}
	}

	@Override
	public Optional<ImmutableExpression> getDisjunction(Stream<ImmutableExpression> expressionStream) {
		ImmutableList<ImmutableExpression> disjuncts = expressionStream
				.flatMap(ImmutableExpression::flattenOR)
				.distinct()
				.collect(ImmutableCollectors.toList());

		return Optional.of(disjuncts)
				.filter(c -> !c.isEmpty())
				.map(this::getDisjunction);
	}

	@Override
	public ImmutableExpression getFalseOrNullFunctionalTerm(ImmutableList<ImmutableExpression> arguments) {
		throw new RuntimeException("TODO: implement getFalseOrNullFunctionalTerm()");
	}

	@Override
	public ImmutableExpression getTrueOrNullFunctionalTerm(ImmutableList<ImmutableExpression> arguments) {
		throw new RuntimeException("TODO: implement getTrueOrNullFunctionalTerm()");
	}

	@Override
	public ImmutableExpression getIsAExpression(ImmutableTerm termTypeTerm, RDFTermType baseType) {
		return getImmutableExpression(functionSymbolFactory.getIsARDFTermTypeFunctionSymbol(baseType), termTypeTerm);
	}

    @Override
    public ImmutableExpression getAreCompatibleRDFStringExpression(ImmutableTerm typeTerm1, ImmutableTerm typeTerm2) {
        return getImmutableExpression(functionSymbolFactory.getAreCompatibleRDFStringFunctionSymbol(), typeTerm1, typeTerm2);
    }

    @Override
	public ImmutableExpression.Evaluation getEvaluation(ImmutableExpression expression) {
		return new ImmutableExpressionImpl.ExpressionEvaluationImpl(expression);
	}

	@Override
	public ImmutableExpression.Evaluation getPositiveEvaluation() {
		return positiveEvaluation;
	}

	@Override
	public ImmutableExpression.Evaluation getNegativeEvaluation() {
		return negativeEvaluation;
	}

	@Override
	public ImmutableExpression.Evaluation getNullEvaluation() {
		return nullEvaluation;
	}

	@Override
	public Function getFunction(Predicate functor, List<Term> arguments) {
		if (isTestModeEnabled) {
			checkMutability(arguments);
		}

		if (functor instanceof BooleanFunctionSymbol) {
			return getExpression((BooleanFunctionSymbol) functor, arguments);
		}

		// Default constructor
		return new FunctionalTermImpl(functor, arguments);
	}

	private void checkMutability(List<Term> terms) {
		for(Term term : terms) {
			if (term instanceof ImmutableFunctionalTerm)
				throw new IllegalArgumentException("Was expecting a mutable term, not a " + term.getClass());
			else if (term instanceof Function)
				checkMutability(((Function) term).getTerms());
		}
	}

	@Override
	public ImmutableFunctionalTerm getImmutableFunctionalTerm(FunctionSymbol functor, ImmutableList<? extends ImmutableTerm> terms) {
		if (functor instanceof BooleanFunctionSymbol) {
			return getImmutableExpression((BooleanFunctionSymbol) functor, terms);
		}

		if (GroundTermTools.areGroundTerms(terms)) {
			return new GroundFunctionalTermImpl((ImmutableList<? extends GroundTerm>)terms, functor, this);
		}
		else {
			// Default constructor
			return new NonGroundFunctionalTermImpl(functor, terms, this);
		}
	}

	@Override
	public ImmutableFunctionalTerm getImmutableFunctionalTerm(FunctionSymbol functor, ImmutableTerm... terms) {
		return getImmutableFunctionalTerm(functor, ImmutableList.copyOf(terms));
	}

	@Override
	public NonGroundFunctionalTerm getNonGroundFunctionalTerm(FunctionSymbol functor, ImmutableTerm... terms) {
		return new NonGroundFunctionalTermImpl(this, functor, terms);
	}

	@Override
	public NonGroundFunctionalTerm getNonGroundFunctionalTerm(FunctionSymbol functor, ImmutableList<ImmutableTerm> terms) {
		return new NonGroundFunctionalTermImpl(functor, terms, this);
	}

	public TypeFactory getTypeFactory() {
		return typeFactory;
	}

	@Override
	public Expression getFunctionEQ(Term firstTerm, Term secondTerm) {
		return getExpression(BooleanExpressionOperation.EQ, firstTerm, secondTerm);
	}

	@Override
	public Expression getFunctionGTE(Term firstTerm, Term secondTerm) {
		return getExpression(BooleanExpressionOperation.GTE, firstTerm, secondTerm);
	}

	@Override
	public Expression getFunctionGT(Term firstTerm, Term secondTerm) {
		return getExpression(BooleanExpressionOperation.GT, firstTerm, secondTerm);
	}

	@Override
	public Expression getFunctionLTE(Term firstTerm, Term secondTerm) {
		return getExpression(BooleanExpressionOperation.LTE, firstTerm, secondTerm);
	}

	@Override
	public Expression getFunctionLT(Term firstTerm, Term secondTerm) {
		return getExpression(BooleanExpressionOperation.LT, firstTerm, secondTerm);
	}

	@Override
	public Expression getFunctionNEQ(Term firstTerm, Term secondTerm) {
		return getExpression(BooleanExpressionOperation.NEQ, firstTerm, secondTerm);
	}

	@Override
	public Expression getFunctionNOT(Term term) {
		return getExpression(BooleanExpressionOperation.NOT, term);
	}

	@Override
	public Expression getFunctionAND(Term term1, Term term2) {
		return getExpression(dbFunctionSymbolFactory.getDBAnd(2), term1, term2);
	}

	@Override
	public Expression getFunctionOR(Term term1, Term term2) {
		return getExpression(OR,term1, term2);
	}


	@Override
	public Expression getFunctionIsNull(Term term) {
		return getExpression(BooleanExpressionOperation.IS_NULL, term);
	}

	@Override
	public Expression getFunctionIsNotNull(Term term) {
		return getExpression(BooleanExpressionOperation.IS_NOT_NULL, term);
	}


	@Override
	public Expression getLANGMATCHESFunction(Term term1, Term term2) {
		return getExpression(BooleanExpressionOperation.LANGMATCHES, term1, term2);
	}

	@Override
	public Expression getSQLFunctionLike(Term term1, Term term2) {
		return getExpression(BooleanExpressionOperation.SQL_LIKE, term1, term2);
	}

	@Override
	public Function getFunctionCast(Term term1, Term term2) {
		// TODO implement cast function
		return getFunction(ExpressionOperation.QUEST_CAST, term1, term2);
	}

	
	@Override
	public BNode getConstantBNode(String name) {
		return new BNodeConstantImpl(name, typeFactory);
	}

	@Override
	public Expression getFunctionIsTrue(Term term) {
		return getExpression(BooleanExpressionOperation.IS_TRUE, term);
	}

	@Override
	public DBConstant getDBBooleanConstant(boolean value) {
		return value ? valueTrue : valueFalse;
	}

	@Override
	public Constant getNullConstant() {
		return valueNull;
	}

	@Override
	public RDFLiteralConstant getProvenanceSpecialConstant() {
		return provenanceConstant;
	}

	private ImmutableList<ImmutableTerm> convertTerms(Function functionalTermToClone) {
		ImmutableList.Builder<ImmutableTerm> builder = ImmutableList.builder();
		for (Term term : functionalTermToClone.getTerms()) {
			builder.add(immutabilityTools.convertIntoImmutableTerm(term));
		}
		return builder.build();
	}

	@Override
	public ImmutableFunctionalTerm getRDFFunctionalTerm(ImmutableTerm lexicalTerm, ImmutableTerm typeTerm) {
		return getImmutableFunctionalTerm(functionSymbolFactory.getRDFTermFunctionSymbol(), lexicalTerm, typeTerm);
	}

    @Override
    public ImmutableFunctionalTerm getNullRDFFunctionalTerm() {
		return getRDFFunctionalTerm(valueNull, valueNull);
    }

    @Override
    public GroundFunctionalTerm getIRIFunctionalTerm(IRI iri) {
		DBConstant lexicalConstant = getDBStringConstant(iri.getIRIString());
		return (GroundFunctionalTerm) getRDFFunctionalTerm(lexicalConstant, iriTypeConstant);
    }

	@Override
	public ImmutableFunctionalTerm getIRIFunctionalTerm(Variable variable, boolean temporaryCastToString) {
		ImmutableTerm lexicalTerm = temporaryCastToString ? getPartiallyDefinedToStringCast(variable) : variable;
		return getRDFFunctionalTerm(lexicalTerm, iriTypeConstant);
	}

	@Override
	public ImmutableFunctionalTerm getIRIFunctionalTerm(String iriTemplate,
														ImmutableList<? extends ImmutableTerm> arguments) {
		if (arguments.isEmpty())
			throw new IllegalArgumentException("At least one argument for the IRI functional term " +
					"with an IRI template is required");

		FunctionSymbol templateFunctionSymbol = dbFunctionSymbolFactory.getIRIStringTemplateFunctionSymbol(iriTemplate);
		ImmutableFunctionalTerm templateFunctionalTerm = getImmutableFunctionalTerm(templateFunctionSymbol, arguments);

		return getRDFFunctionalTerm(templateFunctionalTerm, iriTypeConstant);

	}

	@Override
	public ImmutableFunctionalTerm getRDFFunctionalTerm(int encodedIRI) {
		// TODO: use an int-to-string casting function
		DBConstant lexicalValue = getDBStringConstant(String.valueOf(encodedIRI));
		return getRDFFunctionalTerm(lexicalValue, iriTypeConstant);
	}

	@Override
	public ImmutableFunctionalTerm getIRIFunctionalTerm(IRIStringTemplateFunctionSymbol templateSymbol,
														ImmutableList<DBConstant> arguments) {
		ImmutableFunctionalTerm lexicalTerm = getImmutableFunctionalTerm(templateSymbol, arguments);
		return getRDFFunctionalTerm(lexicalTerm, iriTypeConstant);
	}

	@Override
	public Function getIRIMutableFunctionalTerm(String iriTemplate, Term... arguments) {
		FunctionSymbol templateFunctionSymbol = dbFunctionSymbolFactory.getIRIStringTemplateFunctionSymbol(iriTemplate);
		Function lexicalTerm = getFunction(templateFunctionSymbol, arguments);
		return getIRIMutableFunctionalTermFromLexicalTerm(lexicalTerm);
	}

	@Override
	public Function getIRIMutableFunctionalTerm(IRI iri) {
		DBConstant lexicalConstant = getDBStringConstant(iri.getIRIString());
		return getIRIMutableFunctionalTermFromLexicalTerm(lexicalConstant);
	}

	@Override
	public Function getNullRDFMutableFunctionalTerm() {
		return getFunction(functionSymbolFactory.getRDFTermFunctionSymbol(), valueNull, valueNull);
	}

	@Override
	public ImmutableFunctionalTerm getFreshBnodeFunctionalTerm(Variable variable) {
		return getRDFFunctionalTerm(variable, bnodeTypeConstant);
	}

	@Override
	public ImmutableFunctionalTerm getBnodeFunctionalTerm(String bnodeTemplate, 
														  ImmutableList<? extends ImmutableTerm> arguments) {
		ImmutableFunctionalTerm lexicalTerm = getImmutableFunctionalTerm(
				dbFunctionSymbolFactory.getBnodeStringTemplateFunctionSymbol(bnodeTemplate),
				arguments);
		return getRDFFunctionalTerm(lexicalTerm, bnodeTypeConstant);
	}

	@Override
	public ImmutableFunctionalTerm getFreshBnodeFunctionalTerm(ImmutableList<ImmutableTerm> arguments) {
		ImmutableFunctionalTerm lexicalTerm = getImmutableFunctionalTerm(
				dbFunctionSymbolFactory.getFreshBnodeStringTemplateFunctionSymbol(arguments.size()),
				arguments);
		return getRDFFunctionalTerm(lexicalTerm, bnodeTypeConstant);
	}

	@Override
	public ImmutableFunctionalTerm getDBCastFunctionalTerm(DBTermType targetType, ImmutableTerm term) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBCastFunctionSymbol(targetType), term);
	}

	@Override
	public ImmutableFunctionalTerm getDBCastFunctionalTerm(DBTermType inputType, DBTermType targetType, ImmutableTerm term) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBCastFunctionSymbol(inputType, targetType), term);
	}

	@Override
	public ImmutableFunctionalTerm getConversion2RDFLexicalFunctionalTerm(DBTermType inputType, ImmutableTerm term, RDFTermType rdfTermType) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getConversion2RDFLexicalFunctionSymbol(inputType, rdfTermType), term);
	}

	@Override
	public ImmutableFunctionalTerm getPartiallyDefinedToStringCast(Variable variable) {
		return getImmutableFunctionalTerm(
				dbFunctionSymbolFactory.getTemporaryConversionToDBStringFunctionSymbol(),
				variable);
	}

	@Override
	public ImmutableExpression getRDF2DBBooleanFunctionalTerm(ImmutableTerm xsdBooleanTerm) {
		return getImmutableExpression(functionSymbolFactory.getRDF2DBBooleanFunctionSymbol(), xsdBooleanTerm);
	}

	@Override
	public ImmutableFunctionalTerm getIfElseNull(ImmutableExpression condition, ImmutableTerm term) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBIfElseNull(), condition, term);
	}

	@Override
	public ImmutableFunctionalTerm getDBCase(
			Stream<? extends Map.Entry<ImmutableExpression, ? extends ImmutableTerm>> whenPairs, ImmutableTerm defaultTerm) {
		ImmutableList<ImmutableTerm> terms = Stream.concat(
				whenPairs
						.flatMap(e -> Stream.of(e.getKey(), e.getValue())),
				Stream.of(defaultTerm))
				.collect(ImmutableCollectors.toList());

		int arity = terms.size();

		if (arity < 3) {
			throw new IllegalArgumentException("whenPairs must be non-empty");
		}

		if ((arity == 3) && defaultTerm.equals(valueNull))
			return getIfElseNull((ImmutableExpression) terms.get(0), terms.get(1));

		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBCase(arity), terms);
	}

	@Override
	public ImmutableFunctionalTerm getDBCaseElseNull(Stream<? extends Map.Entry<ImmutableExpression, ? extends ImmutableTerm>> whenPairs) {
		return getDBCase(whenPairs, valueNull);
	}

    @Override
    public ImmutableFunctionalTerm getDBReplaceFunctionalTerm(ImmutableTerm text, ImmutableTerm from, ImmutableTerm to) {
        return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBReplace(), text, from, to);
    }

    @Override
    public ImmutableExpression getDBStartsWithFunctionalTerm(ImmutableList<ImmutableTerm> terms) {
		return getImmutableExpression(dbFunctionSymbolFactory.getDBStartsWith(), terms);
    }

    @Override
    public ImmutableFunctionalTerm getR2RMLIRISafeEncodeFunctionalTerm(ImmutableTerm term) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getR2RMLIRISafeEncode(), term);
    }

    @Override
	public ImmutableFunctionalTerm getDBConcatFunctionalTerm(ImmutableList<ImmutableTerm> terms) {
		int arity = terms.size();
		if (arity < 2)
			throw new IllegalArgumentException("CONCAT needs at least two arguments");
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBConcat(arity), terms);
	}

	@Override
	public ImmutableFunctionalTerm getCommonDenominatorFunctionalTerm(ImmutableList<ImmutableTerm> typeTerms) {
		int arity = typeTerms.size();
		if (arity < 2)
			throw new IllegalArgumentException("Expected arity >= 2 for a common denominator");

		return getImmutableFunctionalTerm(functionSymbolFactory.getCommonDenominatorFunctionSymbol(arity), typeTerms);
	}

	@Override
	public ImmutableExpression getStrictEquality(ImmutableSet<ImmutableTerm> terms) {
		if (terms.size() < 2)
			throw new IllegalArgumentException("At least two distinct values where expected in " + terms);
		return getStrictEquality(ImmutableList.copyOf(terms));
	}

	@Override
	public ImmutableExpression getStrictEquality(ImmutableList<ImmutableTerm> terms) {
		if (terms.size() < 2)
			throw new IllegalArgumentException("At least two values where expected in " + terms);
		return getImmutableExpression(dbFunctionSymbolFactory.getDBStrictEquality(terms.size()), terms);
	}

	@Override
	public ImmutableExpression getStrictEquality(ImmutableTerm term1, ImmutableTerm term2, ImmutableTerm... otherTerms) {
		return getStrictEquality(Stream.concat(Stream.of(term1, term2), Stream.of(otherTerms))
				.collect(ImmutableCollectors.toList()));
	}

	@Override
	public ImmutableExpression getStrictNEquality(ImmutableSet<ImmutableTerm> terms) {
		if (terms.size() < 2)
			throw new IllegalArgumentException("At least two distinct values where expected in " + terms);
		return getStrictNEquality(ImmutableList.copyOf(terms));
	}

	@Override
	public ImmutableExpression getStrictNEquality(ImmutableList<ImmutableTerm> terms) {
		if (terms.size() < 2)
			throw new IllegalArgumentException("At least two values where expected in " + terms);
		throw new RuntimeException("TODO: implement getStrictNEquality(...)");
	}

	private Function getIRIMutableFunctionalTermFromLexicalTerm(Term lexicalTerm) {
		return getFunction(functionSymbolFactory.getRDFTermFunctionSymbol(), lexicalTerm,
				iriTypeConstant);
	}

}
