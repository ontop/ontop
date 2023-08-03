package it.unibz.inf.ontop.model.term.impl;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.tools.TypeConstantDictionary;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.IRIStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.impl.SubstitutionImpl;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

@Singleton
public class TermFactoryImpl implements TermFactory {

	private final TypeFactory typeFactory;
	private final FunctionSymbolFactory functionSymbolFactory;
	private final DBFunctionSymbolFactory dbFunctionSymbolFactory;
	private final CoreUtilsFactory coreUtilsFactory;
	private final DBConstant valueTrue, valueFalse, lexicalTrue, lexicalFalse;
	private final Constant valueNull;
	@Nullable
	private final DBConstant doubleNaN;
	private final DBConstant provenanceConstant;
	private final Map<RDFTermType, RDFTermTypeConstant> termTypeConstantMap;
	private final RDFTermTypeConstant iriTypeConstant, bnodeTypeConstant;
	private final RDF rdfFactory;
	private final ImmutableExpression.Evaluation positiveEvaluation, negativeEvaluation, nullEvaluation;

	@Inject
	private TermFactoryImpl(TypeFactory typeFactory, FunctionSymbolFactory functionSymbolFactory,
							DBFunctionSymbolFactory dbFunctionSymbolFactory, CoreUtilsFactory coreUtilsFactory, OntopModelSettings settings,
							RDF rdfFactory) {
		// protected constructor prevents instantiation from other classes.
		this.typeFactory = typeFactory;
		this.functionSymbolFactory = functionSymbolFactory;
		this.dbFunctionSymbolFactory = dbFunctionSymbolFactory;
		this.coreUtilsFactory = coreUtilsFactory;
		this.rdfFactory = rdfFactory;

		DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();

		DBTermType dbBooleanType = dbTypeFactory.getDBBooleanType();

		this.valueTrue = new DBConstantImpl(dbTypeFactory.getDBTrueLexicalValue(), dbBooleanType);
		this.valueFalse = new DBConstantImpl(dbTypeFactory.getDBFalseLexicalValue(), dbBooleanType);
		this.lexicalTrue = getDBStringConstant("true");
		this.lexicalFalse = getDBStringConstant("false");
		this.valueNull = new NullConstantImpl(dbTypeFactory.getNullLexicalValue());
		this.doubleNaN = dbTypeFactory.getDBNaNLexicalValue()
				.map(v -> new DBConstantImpl(v, dbTypeFactory.getDBDoubleType()))
				.orElse(null);
		this.provenanceConstant = new DBConstantImpl("ontop-provenance-constant", dbTypeFactory.getDBStringType());
		this.termTypeConstantMap = new HashMap<>();
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
	public IRIConstant getConstantIRI(String iri) {
		return getConstantIRI(rdfFactory.createIRI(iri));
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
																ImmutableSet<RDFTermTypeConstant> possibleConstants,
																boolean isSimplifiable) {
		return getImmutableFunctionalTerm(
				functionSymbolFactory.getRDFTermTypeFunctionSymbol(dictionary, possibleConstants, isSimplifiable), term);
	}

	@Override
	public ImmutableExpression getImmutableExpression(BooleanFunctionSymbol functor, ImmutableTerm... arguments) {
		return getImmutableExpression(functor, ImmutableList.copyOf(arguments));
	}

	@Override
	public ImmutableExpression getImmutableExpression(BooleanFunctionSymbol functor, ImmutableList<? extends ImmutableTerm> arguments) {

		if (arguments.stream().allMatch(t -> t instanceof GroundTerm)) {
			return new GroundExpressionImpl(functor, (ImmutableList<GroundTerm>)arguments, this);
		}
		else {
			return new NonGroundExpressionImpl(functor, arguments, this);
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
		return getConjunction(Stream.concat(Stream.of(expression), Stream.of(otherExpressions))
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
	public Optional<ImmutableExpression> getConjunction(Optional<ImmutableExpression> optionalExpression, Stream<ImmutableExpression> expressionStream) {
		ImmutableList<ImmutableExpression> conjuncts = Stream.concat(
						optionalExpression
								.map(ImmutableExpression::flattenAND)
								.orElseGet(Stream::empty),
						expressionStream)
				//.distinct()
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
			default:
				return getImmutableExpression(dbFunctionSymbolFactory.getDBOr(size), disjunctionOfExpressions);
		}
	}

	@Override
	public ImmutableExpression getDisjunction(ImmutableExpression expression, ImmutableExpression... otherExpressions) {
		return getDisjunction(
				Stream.concat(Stream.of(expression), Stream.of(otherExpressions))
						.collect(ImmutableCollectors.toList()));
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
    public ImmutableExpression getDBNot(ImmutableExpression expression) {
		return getImmutableExpression(dbFunctionSymbolFactory.getDBNot(), expression);
    }

    @Override
	public ImmutableExpression getFalseOrNullFunctionalTerm(ImmutableList<ImmutableExpression> arguments) {
		if (arguments.isEmpty())
			throw new IllegalArgumentException("Arity must be >= 1");
		return getImmutableExpression(dbFunctionSymbolFactory.getFalseOrNullFunctionSymbol(arguments.size()), arguments);
	}

	@Override
	public ImmutableExpression getTrueOrNullFunctionalTerm(ImmutableList<ImmutableExpression> arguments) {
		if (arguments.isEmpty())
			throw new IllegalArgumentException("Arity must be >= 1");
		return getImmutableExpression(dbFunctionSymbolFactory.getTrueOrNullFunctionSymbol(arguments.size()), arguments);
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
    public ImmutableFunctionalTerm.FunctionalTermDecomposition getFunctionalTermDecomposition(
    		ImmutableTerm liftableTerm) {
		return new FunctionalTermDecompositionImpl(liftableTerm, getSubstitution(ImmutableMap.of()));
    }

	@Override
	public ImmutableFunctionalTerm.FunctionalTermDecomposition getFunctionalTermDecomposition(
			ImmutableTerm liftableTerm,
			Substitution<ImmutableFunctionalTerm> subTermSubstitution) {

		return new FunctionalTermDecompositionImpl(liftableTerm, subTermSubstitution);
	}

	@Override
	public ImmutableFunctionalTerm getImmutableFunctionalTerm(FunctionSymbol functor, ImmutableList<? extends ImmutableTerm> terms) {
		if (functor instanceof BooleanFunctionSymbol) {
			return getImmutableExpression((BooleanFunctionSymbol) functor, terms);
		}

		if (terms.stream().allMatch(t -> t instanceof GroundTerm)) {
			return new GroundFunctionalTermImpl((ImmutableList<GroundTerm>)terms, functor, this);
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
		if (functor instanceof BooleanFunctionSymbol)
			return new NonGroundExpressionImpl(this, (BooleanFunctionSymbol) functor, terms);
		else
			return new NonGroundFunctionalTermImpl(this, functor, terms);
	}

	@Override
	public NonGroundFunctionalTerm getNonGroundFunctionalTerm(FunctionSymbol functor, ImmutableList<ImmutableTerm> terms) {
		if (functor instanceof BooleanFunctionSymbol)
			return new NonGroundExpressionImpl((BooleanFunctionSymbol) functor, terms, this);
		else
			return new NonGroundFunctionalTermImpl(functor, terms, this);
	}

	@Override
	public TypeFactory getTypeFactory() {
		return typeFactory;
	}

    @Override
    public VariableNullability createDummyVariableNullability(ImmutableFunctionalTerm functionalTerm) {
		return coreUtilsFactory.createSimplifiedVariableNullability(functionalTerm);
    }

    @Override
    public ImmutableFunctionalTerm getRDFDatatypeStringFunctionalTerm(ImmutableTerm rdfTypeTerm) {
		return getImmutableFunctionalTerm(functionSymbolFactory.getRDFDatatypeStringFunctionSymbol(), rdfTypeTerm);
    }

	@Override
	public ImmutableFunctionalTerm getDBUUID(UUID uuid) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBUUID(uuid));
	}

	@Override
	public ImmutableFunctionalTerm getDBStrBefore(ImmutableTerm arg1, ImmutableTerm arg2) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBStrBefore(), arg1, arg2);
	}

	@Override
	public ImmutableFunctionalTerm getDBStrAfter(ImmutableTerm arg1, ImmutableTerm arg2) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBStrAfter(), arg1, arg2);
	}

	@Override
	public ImmutableFunctionalTerm getDBCharLength(ImmutableTerm stringTerm) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBCharLength(), stringTerm);
	}

    @Override
    public ImmutableExpression getDBIsNull(ImmutableTerm immutableTerm) {
        return getImmutableExpression(dbFunctionSymbolFactory.getDBIsNull(), immutableTerm);
    }

	@Override
	public ImmutableExpression getDBIsNotNull(ImmutableTerm immutableTerm) {
		return getImmutableExpression(dbFunctionSymbolFactory.getDBIsNotNull(), immutableTerm);
	}

	@Override
	public Optional<ImmutableExpression> getDBIsNotNull(Stream<? extends ImmutableTerm> stream) {
		return Optional.of(stream
						.map(this::getDBIsNotNull)
						.collect(ImmutableCollectors.toList()))
				.filter(l -> !l.isEmpty())
				.map(this::getConjunction);
	}

	@Override
    public ImmutableFunctionalTerm getDBMd5(ImmutableTerm stringTerm) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBMd5(), stringTerm);
    }

	@Override
	public ImmutableFunctionalTerm getDBSha1(ImmutableTerm stringTerm) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBSha1(), stringTerm);
	}

	@Override
	public ImmutableFunctionalTerm getDBSha256(ImmutableTerm stringTerm) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBSha256(), stringTerm);
	}

	@Override
	public ImmutableFunctionalTerm getDBSha384(ImmutableTerm stringTerm) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBSha384(), stringTerm);
	}

	@Override
	public ImmutableFunctionalTerm getDBSha512(ImmutableTerm stringTerm) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBSha512(), stringTerm);
	}

	@Override
    public ImmutableFunctionalTerm getCommonPropagatedOrSubstitutedNumericType(ImmutableTerm rdfTypeTerm1, ImmutableTerm rdfTypeTerm2) {
        return getImmutableFunctionalTerm(
        		functionSymbolFactory.getCommonPropagatedOrSubstitutedNumericTypeFunctionSymbol(),
				rdfTypeTerm1, rdfTypeTerm2);
    }

	@Override
	public DBFunctionSymbolFactory getDBFunctionSymbolFactory() {
		return dbFunctionSymbolFactory;
	}

	@Override
	public <T extends ImmutableTerm> Substitution<T> getSubstitution(ImmutableMap<Variable, T> map) {
		return new SubstitutionImpl<>(map, this, true);
	}

	@Override
	public ImmutableFunctionalTerm getBinaryNumericLexicalFunctionalTerm(String dbNumericOperationName,
																		 ImmutableTerm lexicalTerm1,
																		 ImmutableTerm lexicalTerm2,
																		 ImmutableTerm rdfTypeTerm) {
		return getImmutableFunctionalTerm(
				functionSymbolFactory.getBinaryNumericLexicalFunctionSymbol(dbNumericOperationName),
				lexicalTerm1, lexicalTerm2, rdfTypeTerm);
	}

	@Override
	public ImmutableFunctionalTerm getDBBinaryNumericFunctionalTerm(String dbNumericOperationName,  DBTermType dbNumericType,
																	ImmutableTerm dbTerm1, ImmutableTerm dbTerm2) {
		return getImmutableFunctionalTerm(
				dbFunctionSymbolFactory.getDBMathBinaryOperator(dbNumericOperationName, dbNumericType),
				dbTerm1, dbTerm2);
	}

	@Override
	public ImmutableFunctionalTerm getDBBinaryNumericFunctionalTerm(String dbNumericOperationName, DBTermType argumentType1, DBTermType argumentType2,
																	ImmutableTerm dbTerm1, ImmutableTerm dbTerm2) {
		return getImmutableFunctionalTerm(
				dbFunctionSymbolFactory.getDBMathBinaryOperator(dbNumericOperationName, argumentType1, argumentType2),
				dbTerm1, dbTerm2);
	}

	@Override
	public ImmutableFunctionalTerm getUnaryLatelyTypedFunctionalTerm(ImmutableTerm lexicalTerm,
																	 ImmutableTerm inputRDFTypeTerm, DBTermType targetType,
																	 java.util.function.Function<DBTermType, Optional<DBFunctionSymbol>> dbFunctionSymbolFct) {
		return getImmutableFunctionalTerm(
				functionSymbolFactory.getUnaryLatelyTypedFunctionSymbol(dbFunctionSymbolFct, targetType),
				lexicalTerm, inputRDFTypeTerm);
	}

	@Override
	public ImmutableFunctionalTerm getUnaryLexicalFunctionalTerm(
			ImmutableTerm lexicalTerm, ImmutableTerm rdfDatatypeTerm,
			java.util.function.Function<DBTermType, Optional<DBFunctionSymbol>> dbFunctionSymbolFct) {
		return getImmutableFunctionalTerm(
				functionSymbolFactory.getUnaryLexicalFunctionSymbol(dbFunctionSymbolFct),
				lexicalTerm, rdfDatatypeTerm);
	}

	@Override
	public ImmutableFunctionalTerm getBinaryLatelyTypedFunctionalTerm(ImmutableTerm lexicalTerm0, ImmutableTerm lexicalTerm1,
																	  ImmutableTerm inputRDFTypeTerm0, ImmutableTerm inputRDFTypeTerm1,
																	  DBTermType targetType,
																	  java.util.function.Function<DBTermType, Optional<DBFunctionSymbol>> dbFunctionSymbolFct) {
		return getImmutableFunctionalTerm(
				functionSymbolFactory.getBinaryLatelyTypedFunctionSymbol(dbFunctionSymbolFct, targetType),
				lexicalTerm0, lexicalTerm1, inputRDFTypeTerm0, inputRDFTypeTerm1);
	}

	@Override
	public ImmutableFunctionalTerm getSPARQLNonStrictEquality(ImmutableTerm rdfTerm1, ImmutableTerm rdfTerm2) {
		return getImmutableFunctionalTerm(functionSymbolFactory.getRequiredSPARQLFunctionSymbol(SPARQL.EQ, 2),
				rdfTerm1, rdfTerm2);
	}

	@Override
	public ImmutableFunctionalTerm getSPARQLEffectiveBooleanValue(ImmutableTerm rdfTerm) {
		return getImmutableFunctionalTerm(functionSymbolFactory.getSPARQLEffectiveBooleanValueFunctionSymbol(), rdfTerm);
	}

	@Override
	public ImmutableExpression getLexicalEffectiveBooleanValue(ImmutableTerm lexicalTerm, ImmutableTerm rdfDatatypeTerm) {
		return getImmutableExpression(functionSymbolFactory.getLexicalEBVFunctionSymbol(), lexicalTerm, rdfDatatypeTerm);
	}

	@Override
	public ImmutableFunctionalTerm getDBRand(UUID uuid) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBRand(uuid));
	}

	@Override
    public ImmutableFunctionalTerm getDBYearFromDatetime(ImmutableTerm dbDatetimeTerm) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBYearFromDatetime(), dbDatetimeTerm);
    }

    @Override
    public ImmutableFunctionalTerm getDBMonthFromDatetime(ImmutableTerm dbDatetimeTerm) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBMonthFromDatetime(), dbDatetimeTerm);
    }

	@Override
	public ImmutableFunctionalTerm getDBDayFromDatetime(ImmutableTerm dbDatetimeTerm) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBDayFromDatetime(), dbDatetimeTerm);
	}

	@Override
	public ImmutableFunctionalTerm getDBHours(ImmutableTerm dbDatetimeTerm) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBHours(), dbDatetimeTerm);
	}

	@Override
	public ImmutableFunctionalTerm getDBMinutes(ImmutableTerm dbDatetimeTerm) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBMinutes(), dbDatetimeTerm);
	}

	@Override
	public ImmutableFunctionalTerm getDBSeconds(ImmutableTerm dbDatetimeTerm) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBSeconds(), dbDatetimeTerm);
	}

    @Override
    public ImmutableFunctionalTerm getDBTz(ImmutableTerm dbDatetimeTerm) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBTz(), dbDatetimeTerm);
    }

    @Override
    public ImmutableFunctionalTerm getDBNow() {
        return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBNow());
    }

	@Override
	public ImmutableFunctionalTerm getDBRowUniqueStr() {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBRowUniqueStr());
	}

	@Override
	public ImmutableFunctionalTerm getDBRowNumber() {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBRowNumber());
	}

    @Override
    public ImmutableFunctionalTerm getDBIriStringResolution(IRI baseIRI, ImmutableTerm argLexical) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBIriStringResolver(baseIRI), argLexical);
    }

    @Override
    public ImmutableFunctionalTerm getDBCount(boolean isDistinct) {
        return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBCount(0, isDistinct));
    }

	@Override
	public ImmutableFunctionalTerm getDBCount(ImmutableTerm term, boolean isDistinct) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBCount(1, isDistinct), term);
	}

	@Override
	public ImmutableFunctionalTerm getDBSum(ImmutableTerm subTerm, DBTermType dbType, boolean isDistinct) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getNullIgnoringDBSum(dbType, isDistinct), subTerm);
	}

	@Override
	public ImmutableFunctionalTerm getDBAvg(ImmutableTerm subTerm, DBTermType dbType, boolean isDistinct) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getNullIgnoringDBAvg(dbType, isDistinct), subTerm);
	}

	@Override
	public ImmutableFunctionalTerm getDBStdev(ImmutableTerm subTerm, DBTermType dbType, boolean isPop, boolean isDistinct) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getNullIgnoringDBStdev(dbType, isPop, isDistinct), subTerm);
	}

	@Override
	public ImmutableFunctionalTerm getDBVariance(ImmutableTerm subTerm, DBTermType dbType, boolean isPop, boolean isDistinct) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getNullIgnoringDBVariance(dbType, isPop, isDistinct), subTerm);
	}

    @Override
    public ImmutableFunctionalTerm getDBMin(ImmutableTerm subTerm, DBTermType dbType) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBMin(dbType), subTerm);
    }

	@Override
	public ImmutableFunctionalTerm getDBMax(ImmutableTerm subTerm, DBTermType dbType) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBMax(dbType), subTerm);
	}

	@Override
	public ImmutableFunctionalTerm getDBSample(ImmutableTerm subTerm, DBTermType dbType) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBSample(dbType), subTerm);
	}

    @Override
    public ImmutableFunctionalTerm getDBGroupConcat(ImmutableTerm subTerm, String separator, boolean isDistinct) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getNullIgnoringDBGroupConcat(isDistinct), subTerm,
				getDBStringConstant(separator));
    }

	// Topological functions
    @Override
    public ImmutableTerm getDBSTWithin(ImmutableTerm arg1, ImmutableTerm arg2) {
        return getImmutableExpression(dbFunctionSymbolFactory.getDBSTWithin(), arg1, arg2);
    }

    @Override
    public ImmutableTerm getDBSTOverlaps(ImmutableTerm arg1, ImmutableTerm arg2) {
        return getImmutableExpression(dbFunctionSymbolFactory.getDBSTOverlaps(), arg1, arg2);
    }

    @Override
    public ImmutableTerm getDBSTContains(ImmutableTerm arg1, ImmutableTerm arg2) {
        return getImmutableExpression(dbFunctionSymbolFactory.getDBSTContains(), arg1, arg2);
    }

    @Override
    public ImmutableTerm getDBSTCrosses(ImmutableTerm arg1, ImmutableTerm arg2) {
        return getImmutableExpression(dbFunctionSymbolFactory.getDBSTCrosses(), arg1, arg2);
    }

    @Override
    public ImmutableTerm getDBSTDisjoint(ImmutableTerm arg1, ImmutableTerm arg2) {
        return getImmutableExpression(dbFunctionSymbolFactory.getDBSTDisjoint(), arg1, arg2);
    }

    @Override
    public ImmutableTerm getDBSTEquals(ImmutableTerm arg1, ImmutableTerm arg2) {
        return getImmutableExpression(dbFunctionSymbolFactory.getDBSTEquals(), arg1, arg2);
    }

    @Override
    public ImmutableTerm getDBSTIntersects(ImmutableTerm arg1, ImmutableTerm arg2) {
        return getImmutableExpression(dbFunctionSymbolFactory.getDBSTIntersects(), arg1, arg2);
    }

    @Override
    public ImmutableTerm getDBSTTouches(ImmutableTerm arg1, ImmutableTerm arg2) {
        return getImmutableExpression(dbFunctionSymbolFactory.getDBSTTouches(), arg1, arg2);
    }

    @Override
    public ImmutableTerm getDBSTCoveredBy(ImmutableTerm arg1, ImmutableTerm arg2) {
        return getImmutableExpression(dbFunctionSymbolFactory.getDBSTCoveredBy(), arg1, arg2);
    }

    @Override
    public ImmutableTerm getDBSTCovers(ImmutableTerm arg1, ImmutableTerm arg2) {
        return getImmutableExpression(dbFunctionSymbolFactory.getDBSTCovers(), arg1, arg2);
    }

    @Override
    public ImmutableTerm getDBSTContainsProperly(ImmutableTerm arg1, ImmutableTerm arg2) {
        return getImmutableExpression(dbFunctionSymbolFactory.getDBSTContainsProperly(), arg1, arg2);
    }

	// Non-topological and common form functions
    @Override
    public ImmutableTerm getDBSTSTransform(ImmutableTerm arg1, ImmutableTerm srid) {
        return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBSTTransform(), arg1, srid);
    }

	@Override
	public ImmutableTerm getDBSTGeomFromText(ImmutableTerm arg1) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBSTGeomFromText(), ImmutableList.of(arg1));
	}

	@Override
	public ImmutableTerm getDBSTMakePoint(ImmutableTerm arg1, ImmutableTerm arg2) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBSTMakePoint(), ImmutableList.of(arg1, arg2));
	}

    @Override
    public ImmutableTerm getDBSTSetSRID(ImmutableTerm arg1, ImmutableTerm arg2) {
        return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBSTSetSRID(), ImmutableList.of(arg1, arg2));
    }

    @Override
    public ImmutableTerm getDBSTDistance(ImmutableTerm arg1, ImmutableTerm arg2) {
        return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBSTDistance(), ImmutableList.of(arg1, arg2));
    }

    @Override
    public ImmutableTerm getDBAsText(ImmutableTerm arg1) {
        return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBAsText(), ImmutableList.of(arg1));
    }

    @Override
    public ImmutableTerm getDBSTFlipCoordinates(ImmutableTerm arg1) {
        return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBSTFlipCoordinates(), ImmutableList.of(arg1));
    }

    @Override
    public ImmutableTerm getDBBuffer(ImmutableTerm arg1, ImmutableTerm arg2) {
        return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBBuffer(), ImmutableList.of(arg1, arg2));
    }

    @Override
    public ImmutableTerm getDBSTDistanceSphere(ImmutableTerm arg1, ImmutableTerm arg2) {
        return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBSTDistanceSphere(), ImmutableList.of(arg1, arg2));
    }

    @Override
    public ImmutableTerm getDBSTDistanceSpheroid(ImmutableTerm arg1, ImmutableTerm arg2, ImmutableTerm arg3) {
        return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBSTDistanceSpheroid(), ImmutableList.of(arg1, arg2, arg3));
    }

    @Override
    public ImmutableTerm getDBIntersection(ImmutableTerm arg1, ImmutableTerm arg2) {
        return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBIntersection(), ImmutableList.of(arg1, arg2));
    }

    @Override
    public ImmutableTerm getDBUnion(ImmutableTerm arg1, ImmutableTerm arg2) {
        return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBUnion(), ImmutableList.of(arg1, arg2));
    }

    @Override
    public ImmutableTerm getDBEnvelope(ImmutableTerm arg1) {
        return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBEnvelope(), ImmutableList.of(arg1));
    }

    @Override
    public ImmutableTerm getDBConvexHull(ImmutableTerm arg1) {
        return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBConvexHull(), ImmutableList.of(arg1));
    }

    @Override
    public ImmutableTerm getDBBoundary(ImmutableTerm arg1) {
        return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBBoundary(), ImmutableList.of(arg1));
    }

    @Override
    public ImmutableTerm getDBDifference(ImmutableTerm arg1, ImmutableTerm arg2) {
        return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBDifference(), ImmutableList.of(arg1, arg2));
    }

    @Override
    public ImmutableTerm getDBSymDifference(ImmutableTerm arg1, ImmutableTerm arg2) {
         return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBSymDifference(), ImmutableList.of(arg1, arg2));
    }

    @Override
    public ImmutableTerm getDBRelate(ImmutableTerm arg1, ImmutableTerm arg2, ImmutableTerm arg3) {
        return getImmutableExpression(dbFunctionSymbolFactory.getDBRelate(), arg1, arg2, arg3);
    }

    @Override
    public ImmutableTerm getDBRelateMatrix(ImmutableTerm arg1, ImmutableTerm arg2) {
        return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBRelateMatrix(), arg1, arg2);
    }

    @Override
    public ImmutableTerm getDBGetSRID(ImmutableTerm arg1) {
        return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBGetSRID(), ImmutableList.of(arg1));
    }

	/**
	 * Time extension - duration arithmetic
	 */

	@Override
	public ImmutableFunctionalTerm getDBWeeksBetweenFromDateTime(ImmutableTerm arg1, ImmutableTerm arg2) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBWeeksBetweenFromDateTime(), arg1, arg2);
	}

	@Override
	public ImmutableFunctionalTerm getDBWeeksBetweenFromDate(ImmutableTerm arg1, ImmutableTerm arg2) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBWeeksBetweenFromDate(), arg1, arg2);
	}

	@Override
	public ImmutableFunctionalTerm getDBDaysBetweenFromDateTime(ImmutableTerm arg1, ImmutableTerm arg2) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBDaysBetweenFromDateTime(), arg1, arg2);
	}

	@Override
	public ImmutableFunctionalTerm getDBDaysBetweenFromDate(ImmutableTerm arg1, ImmutableTerm arg2) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBDaysBetweenFromDate(), arg1, arg2);
	}

	@Override
	public ImmutableFunctionalTerm getDBHoursBetweenFromDateTime(ImmutableTerm arg1, ImmutableTerm arg2) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBHoursBetweenFromDateTime(), arg1, arg2);
	}

	@Override
	public ImmutableFunctionalTerm getDBMinutesBetweenFromDateTime(ImmutableTerm arg1, ImmutableTerm arg2) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBMinutesBetweenFromDateTime(), arg1, arg2);
	}

	@Override
	public ImmutableFunctionalTerm getDBSecondsBetweenFromDateTime(ImmutableTerm arg1, ImmutableTerm arg2) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBSecondsBetweenFromDateTime(), arg1, arg2);
	}

	@Override
	public ImmutableFunctionalTerm getDBMillisBetweenFromDateTime(ImmutableTerm arg1, ImmutableTerm arg2) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBMillisBetweenFromDateTime(), arg1, arg2);
	}

	@Override
    public ImmutableExpression getNotYetTypedEquality(ImmutableTerm t1, ImmutableTerm t2) {
		return getImmutableExpression(functionSymbolFactory.getNotYetTypedEquality(), t1, t2);
    }

    @Override
	public ImmutableExpression getLexicalNonStrictEquality(ImmutableTerm lexicalTerm1, ImmutableTerm typeTerm1,
														   ImmutableTerm lexicalTerm2, ImmutableTerm typeTerm2) {
		return getImmutableExpression(functionSymbolFactory.getLexicalNonStrictEqualityFunctionSymbol(),
				lexicalTerm1, typeTerm1, lexicalTerm2, typeTerm2);
	}

	@Override
	public ImmutableExpression getLexicalInequality(InequalityLabel inequalityLabel, ImmutableTerm lexicalTerm1,
													ImmutableTerm typeTerm1, ImmutableTerm lexicalTerm2,
													ImmutableTerm typeTerm2) {
		return getImmutableExpression(functionSymbolFactory.getLexicalInequalityFunctionSymbol(inequalityLabel),
				lexicalTerm1, typeTerm1, lexicalTerm2, typeTerm2);
	}

	@Override
	public ImmutableExpression getDBNonStrictNumericEquality(ImmutableTerm dbNumericTerm1, ImmutableTerm dbNumericTerm2) {
		return getImmutableExpression(dbFunctionSymbolFactory.getDBNonStrictNumericEquality(), dbNumericTerm1, dbNumericTerm2);
	}

	@Override
	public ImmutableExpression getDBNonStrictStringEquality(ImmutableTerm dbStringTerm1, ImmutableTerm dbStringTerm2) {
		return getImmutableExpression(dbFunctionSymbolFactory.getDBNonStrictStringEquality(), dbStringTerm1, dbStringTerm2);
	}

	@Override
	public ImmutableExpression getDBNonStrictDatetimeEquality(ImmutableTerm dbDatetimeTerm1, ImmutableTerm dbDatetimeTerm2) {
		return getImmutableExpression(dbFunctionSymbolFactory.getDBNonStrictDatetimeEquality(), dbDatetimeTerm1, dbDatetimeTerm2);
	}

	@Override
	public ImmutableExpression getDBNonStrictDateEquality(ImmutableTerm dbTerm1, ImmutableTerm dbTerm2) {
		return getImmutableExpression(dbFunctionSymbolFactory.getDBNonStrictDateEquality(), dbTerm1, dbTerm2);
	}

	@Override
	public ImmutableExpression getDBNonStrictDefaultEquality(ImmutableTerm term1, ImmutableTerm term2) {
		return getImmutableExpression(dbFunctionSymbolFactory.getDBNonStrictDefaultEquality(), term1, term2);
	}

	@Override
	public ImmutableExpression getDBNumericInequality(InequalityLabel inequalityLabel, ImmutableTerm dbNumericTerm1, ImmutableTerm dbNumericTerm2) {
		return getImmutableExpression(dbFunctionSymbolFactory.getDBNumericInequality(inequalityLabel),
				dbNumericTerm1, dbNumericTerm2);
	}

	@Override
	public ImmutableExpression getDBBooleanInequality(InequalityLabel inequalityLabel, ImmutableTerm dbBooleanTerm1,
													  ImmutableTerm dbBooleanTerm2) {
		return getImmutableExpression(dbFunctionSymbolFactory.getDBBooleanInequality(inequalityLabel),
				dbBooleanTerm1, dbBooleanTerm2);
	}

	@Override
	public ImmutableExpression getDBStringInequality(InequalityLabel inequalityLabel, ImmutableTerm dbStringTerm1,
													 ImmutableTerm dbStringTerm2) {
		return getImmutableExpression(dbFunctionSymbolFactory.getDBStringInequality(inequalityLabel),
				dbStringTerm1, dbStringTerm2);
	}

	@Override
	public ImmutableExpression getDBDatetimeInequality(InequalityLabel inequalityLabel, ImmutableTerm dbDatetimeTerm1,
													   ImmutableTerm dbDatetimeTerm2) {
		return getImmutableExpression(dbFunctionSymbolFactory.getDBDatetimeInequality(inequalityLabel),
				dbDatetimeTerm1, dbDatetimeTerm2);
	}

	@Override
	public ImmutableExpression getDBDateInequality(InequalityLabel inequalityLabel, ImmutableTerm dbDateTerm1,
												   ImmutableTerm dbDateTerm2) {
		return getImmutableExpression(dbFunctionSymbolFactory.getDBDateInequality(inequalityLabel),
				dbDateTerm1, dbDateTerm2);
	}

	@Override
	public ImmutableExpression getDBDefaultInequality(InequalityLabel inequalityLabel, ImmutableTerm dbTerm1,
													  ImmutableTerm dbTerm2) {
		return getImmutableExpression(dbFunctionSymbolFactory.getDBDefaultInequality(inequalityLabel),
				dbTerm1, dbTerm2);
	}

	@Override
	public BNode getConstantBNode(String name) {
		return new BNodeConstantImpl(name, typeFactory);
	}

	@Override
	public DBConstant getDBBooleanConstant(boolean value) {
		return value ? valueTrue : valueFalse;
	}

	@Override
	public DBConstant getXsdBooleanLexicalConstant(boolean value) {
		return value ? lexicalTrue : lexicalFalse;
	}

	@Override
	public Constant getNullConstant() {
		return valueNull;
	}

    @Override
    public ImmutableFunctionalTerm getTypedNull(DBTermType termType) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getTypedNullFunctionSymbol(termType));
    }

    @Override
	public DBConstant getDBIntegerConstant(int value) {
		return getDBConstant(String.format("%d", value), typeFactory.getDBTypeFactory().getDBLargeIntegerType());
	}

	@Override
	public Optional<DBConstant> getDoubleNaN() {
		return Optional.ofNullable(doubleNaN);
	}

	@Override
	public DBConstant getProvenanceSpecialConstant() {
		return provenanceConstant;
	}

	@Override
	public ImmutableFunctionalTerm getRDFFunctionalTerm(ImmutableTerm lexicalTerm, ImmutableTerm typeTerm) {
		return getImmutableFunctionalTerm(functionSymbolFactory.getRDFTermFunctionSymbol(), lexicalTerm, typeTerm);
	}

	@Override
	public ImmutableFunctionalTerm getIRIFunctionalTerm(ImmutableTerm term) {
		return getRDFFunctionalTerm(term, iriTypeConstant);
	}

	@Override
	public ImmutableFunctionalTerm getIRIFunctionalTerm(ImmutableList<Template.Component> iriTemplate,
														ImmutableList<? extends ImmutableTerm> arguments) {
		if (arguments.isEmpty())
			throw new IllegalArgumentException("At least one argument for the IRI functional term " +
					"with an IRI template is required");

		FunctionSymbol templateFunctionSymbol = dbFunctionSymbolFactory.getIRIStringTemplateFunctionSymbol(iriTemplate);
		ImmutableFunctionalTerm templateFunctionalTerm = getImmutableFunctionalTerm(templateFunctionSymbol, arguments);

		return getRDFFunctionalTerm(templateFunctionalTerm, iriTypeConstant);

	}

	@Override
	public ImmutableFunctionalTerm getIRIFunctionalTerm(IRIStringTemplateFunctionSymbol templateSymbol,
														ImmutableList<DBConstant> arguments) {
		ImmutableFunctionalTerm lexicalTerm = getImmutableFunctionalTerm(templateSymbol, arguments);
		return getRDFFunctionalTerm(lexicalTerm, iriTypeConstant);
	}

	@Override
	public ImmutableFunctionalTerm getBnodeFunctionalTerm(ImmutableTerm term) {
		return getRDFFunctionalTerm(term, bnodeTypeConstant);
	}

	@Override
	public ImmutableFunctionalTerm getBnodeFunctionalTerm(ImmutableList<Template.Component> bnodeTemplate,
														  ImmutableList<? extends ImmutableTerm> arguments) {
		ImmutableFunctionalTerm lexicalTerm = getImmutableFunctionalTerm(
				dbFunctionSymbolFactory.getBnodeStringTemplateFunctionSymbol(bnodeTemplate),
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
	public ImmutableFunctionalTerm getDBIntIndex(ImmutableTerm idTerm, ImmutableTerm... possibleValues) {
		ImmutableList.Builder<ImmutableTerm> argumentBuilder = ImmutableList.builder();
		argumentBuilder.add(idTerm);
		argumentBuilder.addAll(ImmutableList.copyOf(possibleValues));

		return getImmutableFunctionalTerm(
				dbFunctionSymbolFactory.getDBIntIndex(possibleValues.length),
				argumentBuilder.build());
	}

	@Override
	public ImmutableFunctionalTerm getDBIntIndex(ImmutableTerm idTerm, ImmutableList<ImmutableTerm> possibleValues) {
		ImmutableList.Builder<ImmutableTerm> argumentBuilder = ImmutableList.builder();
		argumentBuilder.add(idTerm);
		argumentBuilder.addAll(possibleValues);

		return getImmutableFunctionalTerm(
				dbFunctionSymbolFactory.getDBIntIndex(possibleValues.size()),
				argumentBuilder.build());
	}

	@Override
	public ImmutableFunctionalTerm getConversion2RDFLexical(DBTermType inputType, ImmutableTerm term, RDFTermType rdfTermType) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getConversion2RDFLexicalFunctionSymbol(inputType, rdfTermType), term);
	}

	@Override
	public ImmutableFunctionalTerm getConversion2RDFLexical(ImmutableTerm dbTerm, RDFTermType rdfType) {
		return getConversion2RDFLexical(
				rdfType.getClosestDBType(typeFactory.getDBTypeFactory()),
				dbTerm, rdfType);
	}

	@Override
	public ImmutableFunctionalTerm getConversionFromRDFLexical2DB(DBTermType targetDBType, ImmutableTerm dbTerm, RDFTermType rdfType) {
		return getImmutableFunctionalTerm(
				dbFunctionSymbolFactory.getConversionFromRDFLexical2DBFunctionSymbol(targetDBType, rdfType),
				dbTerm);
	}

	@Override
	public ImmutableFunctionalTerm getConversionFromRDFLexical2DB(DBTermType targetDBType, ImmutableTerm dbTerm) {
		return getImmutableFunctionalTerm(
				dbFunctionSymbolFactory.getConversionFromRDFLexical2DBFunctionSymbol(targetDBType),
				dbTerm);
	}

	@Override
	public ImmutableFunctionalTerm getConversionFromRDFLexical2DB(ImmutableTerm dbTerm, RDFTermType rdfType) {
		return getConversionFromRDFLexical2DB(
				rdfType.getClosestDBType(typeFactory.getDBTypeFactory()),
				dbTerm, rdfType);
	}

	@Override
	public ImmutableFunctionalTerm getPartiallyDefinedConversionToString(Variable variable) {
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
	public ImmutableExpression getBooleanIfElseNull(ImmutableExpression condition, ImmutableExpression thenExpression) {
		return getImmutableExpression(dbFunctionSymbolFactory.getDBBooleanIfElseNull(), condition, thenExpression);
	}

	@Override
    public ImmutableFunctionalTerm getIfThenElse(ImmutableExpression condition, ImmutableTerm thenTerm, ImmutableTerm elseTerm) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBIfThenElse(), condition, thenTerm, elseTerm);
    }

    @Override
	public ImmutableFunctionalTerm getDBCase(
			Stream<? extends Map.Entry<ImmutableExpression, ? extends ImmutableTerm>> whenPairs, ImmutableTerm defaultTerm,
			boolean doOrderingMatter) {
		ImmutableList<ImmutableTerm> terms = Stream.concat(
				whenPairs
						.flatMap(e -> Stream.of(e.getKey(), e.getValue())),
				Stream.of(defaultTerm))
				.collect(ImmutableCollectors.toList());

		int arity = terms.size();

		if (arity < 3) {
			throw new IllegalArgumentException("whenPairs must be non-empty");
		}

		if (arity == 3)
			return defaultTerm.equals(valueNull)
					? getIfElseNull((ImmutableExpression) terms.get(0), terms.get(1))
					: getIfThenElse((ImmutableExpression) terms.get(0), terms.get(1), defaultTerm);

		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBCase(arity, doOrderingMatter), terms);
	}

	@Override
	public ImmutableFunctionalTerm getDBCaseElseNull(Stream<? extends Map.Entry<ImmutableExpression, ? extends ImmutableTerm>> whenPairs,
													 boolean doOrderingMatter) {
		return getDBCase(whenPairs, valueNull, doOrderingMatter);
	}

    @Override
    public ImmutableExpression getDBBooleanCase(Stream<Map.Entry<ImmutableExpression, ImmutableExpression>> whenPairs,
												ImmutableExpression defaultExpression, boolean doOrderingMatter) {
		ImmutableList<ImmutableExpression> terms = Stream.concat(
				whenPairs
						.flatMap(e -> Stream.of(e.getKey(), e.getValue())),
				Stream.of(defaultExpression))
				.collect(ImmutableCollectors.toList());

		int arity = terms.size();

		if (arity < 3) {
			throw new IllegalArgumentException("whenPairs must be non-empty");
		}

		//if (arity == 3)
		//	return getBooleanIfThenElse(terms.get(0), terms.get(1), defaultExpression);

		return getImmutableExpression(dbFunctionSymbolFactory.getDBBooleanCase(arity, doOrderingMatter), terms);
    }

    @Override
    public ImmutableFunctionalTerm getDBCoalesce(ImmutableTerm term1, ImmutableTerm term2, ImmutableTerm... terms) {
		ImmutableList.Builder<ImmutableTerm> builder = ImmutableList.builder();
		builder.add(term1);
		builder.add(term2);
		builder.addAll(ImmutableList.copyOf(terms));
        return getDBCoalesce(builder.build());
    }

    @Override
    public ImmutableFunctionalTerm getDBCoalesce(ImmutableList<ImmutableTerm> terms) {
		if (terms.size() < 1)
			throw new IllegalArgumentException("At least one argument is expected");
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBCoalesce(terms.size()), terms);
    }

	@Override
	public ImmutableExpression getDBBooleanCoalesce(ImmutableList<ImmutableTerm> terms) {
		if (terms.size() < 1)
			throw new IllegalArgumentException("At least one argument is expected");
		return getImmutableExpression(dbFunctionSymbolFactory.getDBBooleanCoalesce(terms.size()), terms);
	}

	@Override
	public ImmutableFunctionalTerm getDBReplace(ImmutableTerm text, ImmutableTerm from, ImmutableTerm to) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBReplace(), text, from, to);
	}

	@Override
    public ImmutableFunctionalTerm getDBRegexpReplace(ImmutableTerm text, ImmutableTerm from, ImmutableTerm to) {
        return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBRegexpReplace3(), text, from, to);
    }

	@Override
	public ImmutableFunctionalTerm getDBRegexpReplace(ImmutableTerm arg, ImmutableTerm pattern, ImmutableTerm replacement, ImmutableTerm flags) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBRegexpReplace4(), arg, pattern, replacement, flags);
	}

	@Override
    public ImmutableExpression getDBStartsWith(ImmutableList<ImmutableTerm> terms) {
		return getImmutableExpression(dbFunctionSymbolFactory.getDBStartsWith(), terms);
    }

	@Override
	public ImmutableExpression getDBEndsWith(ImmutableList<? extends ImmutableTerm> terms) {
		return getImmutableExpression(dbFunctionSymbolFactory.getDBEndsWith(), terms);
	}

    @Override
    public ImmutableExpression getDBContains(ImmutableList<? extends ImmutableTerm> terms) {
		return getImmutableExpression(dbFunctionSymbolFactory.getDBContains(), terms);
    }

	@Override
	public ImmutableExpression getDBRegexpMatches(ImmutableList<ImmutableTerm> terms) {
		int arity = terms.size();
		if (arity < 2 || arity > 3)
			throw new IllegalArgumentException("Arity must be 2 or 3");

		BooleanFunctionSymbol functionSymbol = (arity == 2)
				? dbFunctionSymbolFactory.getDBRegexpMatches2()
				: dbFunctionSymbolFactory.getDBRegexpMatches3();

		return getImmutableExpression(functionSymbol, terms);
	}

	@Override
    public ImmutableFunctionalTerm getR2RMLIRISafeEncodeFunctionalTerm(ImmutableTerm term) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getR2RMLIRISafeEncode(), term);
    }

	@Override
	public ImmutableFunctionalTerm getDBEncodeForURI(ImmutableTerm term) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBEncodeForURI(), term);
	}

    @Override
	public ImmutableFunctionalTerm getNullRejectingDBConcatFunctionalTerm(ImmutableList<? extends ImmutableTerm> terms) {
		int arity = terms.size();
		if (arity < 2)
			throw new IllegalArgumentException("String concatenation needs at least two arguments");
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getNullRejectingDBConcat(arity), terms);
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
	public ImmutableExpression getStrictEquality(ImmutableList<? extends ImmutableTerm> terms) {
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
	public ImmutableExpression getDBIsStringEmpty(ImmutableTerm stringTerm) {
		return getImmutableExpression(dbFunctionSymbolFactory.getDBIsStringEmpty(), stringTerm);
	}

	@Override
	public ImmutableExpression getStrictNEquality(ImmutableList<? extends ImmutableTerm> terms) {
		if (terms.size() < 2)
			throw new IllegalArgumentException("At least two values where expected in " + terms);
		return getImmutableExpression(dbFunctionSymbolFactory.getDBStrictNEquality(terms.size()), terms);
	}

	@Override
	public ImmutableExpression getStrictNEquality(ImmutableTerm term1, ImmutableTerm term2, ImmutableTerm... otherTerms) {
		return getStrictNEquality(Stream.concat(Stream.of(term1, term2), Stream.of(otherTerms))
				.collect(ImmutableCollectors.toList()));
	}

    @Override
    public ImmutableExpression getIsTrue(NonFunctionalTerm dbBooleanTerm) {
		return getImmutableExpression(dbFunctionSymbolFactory.getIsTrue(), dbBooleanTerm);
    }

	@Override
	public ImmutableFunctionalTerm getDBSubString2(ImmutableTerm stringTerm, ImmutableTerm from) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBSubString2(), stringTerm, from);
	}

	@Override
	public ImmutableFunctionalTerm getDBSubString3(ImmutableTerm stringTerm, ImmutableTerm from, ImmutableTerm to) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBSubString3(), stringTerm, from, to);
	}

	@Override
	public ImmutableFunctionalTerm getDBRight(ImmutableTerm stringTerm, ImmutableTerm lengthTerm) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBRight(), stringTerm, lengthTerm);
	}

    @Override
    public ImmutableFunctionalTerm getDBUpper(ImmutableTerm stringTerm) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBUpper(), stringTerm);
    }

	@Override
	public ImmutableFunctionalTerm getDBLower(ImmutableTerm stringTerm) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBLower(), stringTerm);
	}

    @Override
    public ImmutableFunctionalTerm getLangTypeFunctionalTerm(ImmutableTerm rdfTypeTerm) {
		return getImmutableFunctionalTerm(functionSymbolFactory.getLangTagFunctionSymbol(), rdfTypeTerm);
    }

    @Override
    public ImmutableExpression getLexicalLangMatches(ImmutableTerm langTagTerm, ImmutableTerm langRangeTerm) {
		return getImmutableExpression(functionSymbolFactory.getLexicalLangMatches(), langTagTerm, langRangeTerm);
    }

	@Override
	public ImmutableFunctionalTerm getDBJsonElement(ImmutableTerm arg, ImmutableList<String> path) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBJsonElt(path), arg);
	}

	@Override
	public ImmutableFunctionalTerm getDBJsonElementAsText(ImmutableTerm arg, ImmutableList<String> path) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBJsonEltAsText(path), arg);
	}

	@Override
	public ImmutableExpression getDBJsonIsBoolean(DBTermType dbType, ImmutableTerm arg) {
		return getImmutableExpression(dbFunctionSymbolFactory.getDBJsonIsBoolean(dbType), arg);
	}

	@Override
	public ImmutableExpression getDBJsonIsNumber(DBTermType dbType, ImmutableTerm arg) {
		return getImmutableExpression(dbFunctionSymbolFactory.getDBJsonIsNumber(dbType), arg);
	}

	@Override
	public ImmutableExpression getDBJsonIsScalar(DBTermType dbType, ImmutableTerm arg) {
		return getImmutableExpression(dbFunctionSymbolFactory.getDBJsonIsScalar(dbType), arg);
	}

	@Override
	public ImmutableExpression getDBIsArray(DBTermType dbType, ImmutableTerm arg) {
		return getImmutableExpression(dbFunctionSymbolFactory.getDBIsArray(dbType), arg);
	}



	@Override
	public ImmutableFunctionalTerm getDBWeek(ImmutableTerm dbDatetimeTerm) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBWeek(), dbDatetimeTerm);
	}
	@Override
	public ImmutableFunctionalTerm getDBQuarter(ImmutableTerm dbDatetimeTerm) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBQuarter(), dbDatetimeTerm);
	}
	@Override
	public ImmutableFunctionalTerm getDBDecade(ImmutableTerm dbDatetimeTerm) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBDecade(), dbDatetimeTerm);
	}
	@Override
	public ImmutableFunctionalTerm getDBCentury(ImmutableTerm dbDatetimeTerm) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBCentury(), dbDatetimeTerm);
	}
	@Override
	public ImmutableFunctionalTerm getDBMillennium(ImmutableTerm dbDatetimeTerm) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBMillennium(), dbDatetimeTerm);
	}
	@Override
	public ImmutableFunctionalTerm getDBMilliseconds(ImmutableTerm dbDatetimeTerm) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBMilliseconds(), dbDatetimeTerm);
	}
	@Override
	public ImmutableFunctionalTerm getDBMicroseconds(ImmutableTerm dbDatetimeTerm) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBMicroseconds(), dbDatetimeTerm);
	}

	@Override
	public ImmutableFunctionalTerm getDBDateTrunc(ImmutableTerm dbDatetimeTerm, ImmutableTerm datePartTerm, String datePart) {
		return getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBDateTrunc(datePart), dbDatetimeTerm, datePartTerm);
	}

}
