package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm.FunctionalTermDecomposition;
import it.unibz.inf.ontop.model.term.functionsymbol.BooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermTypeFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBCoalesceFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBIfElseNullFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.NonDeterministicDBFunctionSymbol;
import it.unibz.inf.ontop.model.term.impl.FunctionalTermNullabilityImpl;
import it.unibz.inf.ontop.model.term.impl.PredicateImpl;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import it.unibz.inf.ontop.utils.impl.VariableGeneratorImpl;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class FunctionSymbolImpl extends PredicateImpl implements FunctionSymbol {

    private final ImmutableList<TermType> expectedBaseTypes;

    protected FunctionSymbolImpl(@Nonnull String name,
                                 @Nonnull ImmutableList<TermType> expectedBaseTypes) {
        super(name, expectedBaseTypes.size());
        this.expectedBaseTypes = expectedBaseTypes;
    }

    /**
     * When the function symbol is, in the absence of non-injective functional sub-terms, sometimes but not always injective,
     * please override isInjective(...)
     */
    protected abstract boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms();

    @Override
    public FunctionalTermNullability evaluateNullability(ImmutableList<? extends NonFunctionalTerm> arguments,
                                                         VariableNullability childNullability, TermFactory termFactory) {
        IncrementalEvaluation evaluation = evaluateIsNotNull(transformIntoRegularArguments(arguments, termFactory), termFactory, childNullability);
        switch (evaluation.getStatus()) {
            case SIMPLIFIED_EXPRESSION:
                return evaluation.getNewExpression()
                        .filter(e -> e.getFunctionSymbol().equals(termFactory.getDBFunctionSymbolFactory().getDBIsNotNull()))
                        .map(e -> e.getTerm(0))
                        .filter(t -> t instanceof Variable)
                        .map(t -> (Variable)t)
                        // Bound to that variable
                        .map(FunctionalTermNullabilityImpl::new)
                        // Depends on multiple variables -> is not bound to a variable
                        .orElseGet(() -> new FunctionalTermNullabilityImpl(true));
            case IS_NULL:
                throw new MinorOntopInternalBugException("An IS_NOT_NULL cannot evaluate to NULL");
            case IS_TRUE:
                return new FunctionalTermNullabilityImpl(false);
            case IS_FALSE:
            case SAME_EXPRESSION:
            default:
                return new FunctionalTermNullabilityImpl(true);
        }
    }

    /**
     * By default, reuses the same arguments
     *
     * Needed to be overridden by function symbols that require EXPRESSIONS for some of their arguments
     */
    protected ImmutableList<? extends ImmutableTerm> transformIntoRegularArguments(
            ImmutableList<? extends NonFunctionalTerm> arguments, TermFactory termFactory) {
        return arguments;
    }

    @Override
    public ImmutableTerm simplify(ImmutableList<? extends ImmutableTerm> terms,
                                  TermFactory termFactory, VariableNullability variableNullability) {

        ImmutableList<ImmutableTerm> newTerms = terms.stream()
                .map(t -> (t instanceof ImmutableFunctionalTerm)
                        ? t.simplify(variableNullability)
                        : t)
                .collect(ImmutableCollectors.toList());

        if ((!tolerateNulls()) && newTerms.stream().anyMatch(ImmutableTerm::isNull))
            return termFactory.getNullConstant();

        return simplifyIfElseNullOrCoalesce(newTerms, termFactory, variableNullability)
                .orElseGet(() -> buildTermAfterEvaluation(newTerms, termFactory, variableNullability));
    }

    private Optional<ImmutableTerm> simplifyIfElseNullOrCoalesce(ImmutableList<ImmutableTerm> terms, TermFactory termFactory,
                                                       VariableNullability variableNullability) {
        return simplifyIfElseNull(terms, termFactory, variableNullability)
                .or(() -> simplifyCoalesce(terms, termFactory, variableNullability));
    }

    /**
     * If one argument is a IF_ELSE_NULL(...) functional term, tries to lift the IF_ELSE_NULL above.
     *
     * Lifting is only possible for function symbols that do not tolerate nulls.
     *
     */
    private Optional<ImmutableTerm> simplifyIfElseNull(ImmutableList<ImmutableTerm> terms, TermFactory termFactory,
                                                       VariableNullability variableNullability) {
        if ((!enableIfElseNullLifting())
                || tolerateNulls()
                // Avoids infinite loops
                || (this instanceof DBIfElseNullFunctionSymbol))
            return Optional.empty();

        return IntStream.range(0, terms.size())
                .filter(i -> {
                    ImmutableTerm term = terms.get(i);
                    return (term instanceof ImmutableFunctionalTerm)
                            && (((ImmutableFunctionalTerm) term).getFunctionSymbol() instanceof DBIfElseNullFunctionSymbol);
                })
                .boxed()
                .findAny()
                .map(i -> liftIfElseNull(terms, i, termFactory, variableNullability));
    }

    /**
     * Lifts the IF_ELSE_NULL above the current functional term
     */
    private ImmutableTerm liftIfElseNull(ImmutableList<ImmutableTerm> terms, int index, TermFactory termFactory,
                                         VariableNullability variableNullability) {
        ImmutableFunctionalTerm ifElseNullTerm = (ImmutableFunctionalTerm) terms.get(index);
        ImmutableExpression condition = (ImmutableExpression) ifElseNullTerm.getTerm(0);
        ImmutableTerm conditionalTerm = ifElseNullTerm.getTerm(1);

        ImmutableList<ImmutableTerm> newTerms = IntStream.range(0, terms.size())
                .mapToObj(i -> i == index ? conditionalTerm : terms.get(i))
                .collect(ImmutableCollectors.toList());

        ImmutableFunctionalTerm newFunctionalTerm = (this instanceof BooleanFunctionSymbol)
                ? termFactory.getBooleanIfElseNull(condition,
                termFactory.getImmutableExpression((BooleanFunctionSymbol) this, newTerms))
                : termFactory.getIfElseNull(condition,
                termFactory.getImmutableFunctionalTerm(this, newTerms));

        return newFunctionalTerm.simplify(variableNullability);
    }

    private Optional<ImmutableTerm> simplifyCoalesce(ImmutableList<ImmutableTerm> terms, TermFactory termFactory,
                                                     VariableNullability variableNullability) {
        if (enableCoalesceLifting() && (getArity() == 1) && (!tolerateNulls()) && (!mayReturnNullWithoutNullArguments())) {
            ImmutableTerm firstTerm = terms.get(0);
            if ((firstTerm instanceof ImmutableFunctionalTerm)
                    && (((ImmutableFunctionalTerm) firstTerm).getFunctionSymbol() instanceof DBCoalesceFunctionSymbol)) {
                ImmutableFunctionalTerm initialCoalesceFunctionalTerm = (ImmutableFunctionalTerm) firstTerm;

                ImmutableList<ImmutableTerm> subTerms = initialCoalesceFunctionalTerm.getTerms().stream()
                        .map(t -> termFactory.getImmutableFunctionalTerm(this, t))
                        .collect(ImmutableCollectors.toList());

                if (this instanceof BooleanFunctionSymbol) {
                    return Optional.of(termFactory.getDBBooleanCoalesce(subTerms).simplify(variableNullability));
                }
                else
                    return Optional.of(termFactory.getDBCoalesce(subTerms).simplify(variableNullability));
            }
        }
        return Optional.empty();
    }

    /**
     * Default implementation, to be overridden to convert more cases
     *
     * Incoming terms are not simplified as they are presumed to be already simplified
     *  (so please simplify them before)
     *
     */
    @Override
    public IncrementalEvaluation evaluateStrictEq(ImmutableList<? extends ImmutableTerm> terms, ImmutableTerm otherTerm,
                                                  TermFactory termFactory, VariableNullability variableNullability) {
        boolean incompatibleTypesDetected = inferType(terms)
                .flatMap(TermTypeInference::getTermType)
                .map(t1 -> otherTerm.inferType()
                        .flatMap(TermTypeInference::getTermType)
                        .map(t2 -> areIncompatibleForStrictEq(t1,t2))
                        .orElse(false))
                .orElse(false);

        if (incompatibleTypesDetected)
            return IncrementalEvaluation.declareIsFalse();

        if ((otherTerm instanceof ImmutableFunctionalTerm))
            return evaluateStrictEqWithFunctionalTerm(terms, (ImmutableFunctionalTerm) otherTerm, termFactory,
                    variableNullability);
        else if (otherTerm instanceof NonNullConstant) {
            return evaluateStrictEqWithNonNullConstant(terms, (NonNullConstant) otherTerm, termFactory, variableNullability);
        }
        else if (otherTerm.isNull())
            return IncrementalEvaluation.declareIsNull();

        return IncrementalEvaluation.declareSameExpression();
    }

    /**
     * Different types are only tolerated for DB term types
     */
    private boolean areIncompatibleForStrictEq(TermType type1, TermType type2) {
        if (type1.equals(type2))
            return false;
        return !((type1 instanceof DBTermType) && (type2 instanceof DBTermType));
    }

    /**
     * Default implementation, can be overridden
     */
    @Override
    public IncrementalEvaluation evaluateIsNotNull(ImmutableList<? extends ImmutableTerm> terms, TermFactory termFactory,
                                                   VariableNullability variableNullability) {
        if ((!mayReturnNullWithoutNullArguments()) && (!tolerateNulls())) {
            ImmutableSet<Variable> nullableVariables = variableNullability.getNullableVariables();
            Optional<ImmutableExpression> optionalExpression = termFactory.getDBIsNotNull(terms.stream()
                    .filter(t -> (t.isNullable(nullableVariables))));

            return optionalExpression
                    .map(e -> e.evaluate(variableNullability, true))
                    .orElseGet(IncrementalEvaluation::declareIsTrue);
        }
        // By default, does not optimize (to be overridden for optimizing)
        return IncrementalEvaluation.declareSameExpression();
    }

    @Override
    public boolean isDeterministic() {
        return !(this instanceof NonDeterministicDBFunctionSymbol);
    }

    /**
     * By default, to be overridden by function symbols that supports tolerate NULL values
     */
    @Override
    public boolean isNullable(ImmutableSet<Integer> nullableIndexes) {
        return mayReturnNullWithoutNullArguments() || (!nullableIndexes.isEmpty());
    }

    /**
     * By default, assume it is not an aggregation function symbol
     *
     * To be overridden when needed
     */
    @Override
    public boolean isAggregation() {
        return false;
    }

    /**
     * Conservative by default
     *
     * Can be overridden
     */
    @Override
    public Stream<Variable> proposeProvenanceVariables(ImmutableList<? extends ImmutableTerm> terms) {
        if (!mayReturnNullWithoutNullArguments() && (!tolerateNulls()))
            return terms.stream()
                .filter(t -> t instanceof NonConstantTerm)
                .flatMap(t -> (t instanceof Variable)
                        ? Stream.of((Variable) t)
                        : ((ImmutableFunctionalTerm)t).proposeProvenanceVariables());
        // By default
        return Stream.empty();
    }

    /**
     * By default, only handles the case of function symbols that do not tolerate nulls
     *  and never return nulls in the absence of nulls as input.
     *
     */
    @Override
    public FunctionalTermSimplification simplifyAsGuaranteedToBeNonNull(ImmutableList<? extends ImmutableTerm> terms,
                                                                        TermFactory termFactory) {
        if (!mayReturnNullWithoutNullArguments() && (!tolerateNulls())) {
            ImmutableMap<Integer, FunctionalTermSimplification> subTermSimplifications = IntStream.range(0, terms.size())
                    .filter(i -> terms.get(i) instanceof ImmutableFunctionalTerm)
                    .boxed()
                    .collect(ImmutableCollectors.toMap(
                            i -> i,
                            // Recursive
                            i -> ((ImmutableFunctionalTerm) terms.get(i)).simplifyAsGuaranteedToBeNonNull()));

            ImmutableList<ImmutableTerm> newSubTerms = IntStream.range(0, terms.size())
                    .mapToObj(i -> Optional.ofNullable(subTermSimplifications.get(i))
                            .map(FunctionalTermSimplification::getSimplifiedTerm)
                            .orElseGet(() -> terms.get(i)))
                    .collect(ImmutableCollectors.toList());

            ImmutableFunctionalTerm simplifiedTerm = termFactory.getImmutableFunctionalTerm(this, newSubTerms);

            ImmutableSet<Variable> simplifiableVariables = Stream.concat(
                    subTermSimplifications.values().stream()
                            .flatMap(s -> s.getSimplifiableVariables().stream()),
                    terms.stream()
                            .filter(t -> t instanceof Variable)
                            .map(v -> (Variable) v))
                    .collect(ImmutableCollectors.toSet());

            return FunctionalTermSimplification.create(simplifiedTerm, simplifiableVariables);
        }
        else
            return FunctionalTermSimplification.create(
                    termFactory.getImmutableFunctionalTerm(this, terms),
                    ImmutableSet.of());
    }

    /**
     * By default, we assume it to be safe to be decomposed, as we experienced very little problems with it.
     *
     * Ideally, it should check the input data types, but at the moment (09/2021) they are rarely precisely specified.
     */
    @Override
    public boolean shouldBeDecomposedInUnion() {
        // return IntStream.range(0, getArity()).anyMatch(i -> getExpectedBaseType(i).isAbstract());
        return true;
    }

    /**
     * Default implementation, can be overridden
     *
     */
    protected IncrementalEvaluation evaluateStrictEqWithFunctionalTerm(ImmutableList<? extends ImmutableTerm> terms,
                                                                       ImmutableFunctionalTerm otherTerm,
                                                                       TermFactory termFactory,
                                                                       VariableNullability variableNullability) {
        /*
         * In case of injectivity
         */
        if (otherTerm.getFunctionSymbol().equals(this)) {
            ImmutableList<? extends ImmutableTerm> otherTerms = otherTerm.getTerms();
            Decomposability decomposability = testDecomposabilityIntoConjunction(terms, variableNullability, otherTerms);

            if (decomposability == Decomposability.CANNOT_BE_DECOMPOSED)
                return IncrementalEvaluation.declareSameExpression();

            if (getArity() == 0)
                return IncrementalEvaluation.declareIsTrue();

            ImmutableExpression conjunction = termFactory.getConjunction(
                    IntStream.range(0, getArity())
                            .mapToObj(i -> termFactory.getStrictEquality(terms.get(i), otherTerm.getTerm(i)))
                            .collect(ImmutableCollectors.toList()));

            ImmutableExpression newExpression = (decomposability == Decomposability.NEEDS_NON_NULL_CONDITION_WRAPPING)
                ? termFactory.getBooleanIfElseNull(
                        termFactory.getConjunction(
                                Stream.concat(terms.stream(), otherTerms.stream())
                                        .map(termFactory::getDBIsNotNull)
                                ).orElseThrow(),
                        conjunction)
                : conjunction;

            return newExpression.evaluate(variableNullability, true);
        }
        else
            return IncrementalEvaluation.declareSameExpression();
    }

    /**
     *
     * Makes sure that the conjunction would never evaluate as FALSE instead of NULL
     * (first produced equality evaluated as false, while the second evaluates as NULL)
     *
     * In such a case, wrapping into a IF-ELSE-NULL expression checking that all the arguments are not null is required
     */
    protected Decomposability testDecomposabilityIntoConjunction(ImmutableList<? extends ImmutableTerm> terms,
                                                                 VariableNullability variableNullability,
                                                                 ImmutableList<? extends ImmutableTerm> otherTerms) {
        // Can be relaxed by overriding
        if (!isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms())
            return Decomposability.CANNOT_BE_DECOMPOSED;

        return testDecomposabilityIntoConjunctionWhenInjective(terms, variableNullability, otherTerms);
    }

    /**
     * ONLY when injectivity has been proved
     *
     */
    protected Decomposability testDecomposabilityIntoConjunctionWhenInjective(ImmutableList<? extends ImmutableTerm> terms,
                                                                              VariableNullability variableNullability,
                                                                              ImmutableList<? extends ImmutableTerm> otherTerms) {
        if (mayReturnNullWithoutNullArguments())
            return Decomposability.CANNOT_BE_DECOMPOSED;
        if (getArity() < 2)
            return Decomposability.NO_WRAPPING_NEEDED;

        return (variableNullability.canPossiblyBeNullSeparately(terms)
                || variableNullability.canPossiblyBeNullSeparately(otherTerms))
                ? Decomposability.NEEDS_NON_NULL_CONDITION_WRAPPING
                : Decomposability.NO_WRAPPING_NEEDED;
    }

    /**
     * Default implementation, does nothing, can be overridden
     */
    protected IncrementalEvaluation evaluateStrictEqWithNonNullConstant(ImmutableList<? extends ImmutableTerm> terms,
                                                                        NonNullConstant otherTerm, TermFactory termFactory,
                                                                        VariableNullability variableNullability) {
        return IncrementalEvaluation.declareSameExpression();
    }

    /**
     * Returns true if is not guaranteed to return NULL when one argument is NULL.
     *
     * Can be the case for some function symbols that are rejecting certain cases
     * and therefore refuse to simplify themselves, e.g. RDF(NULL,IRI) is invalid
     * and therefore cannot be simplified.
     *
     */
    protected abstract boolean tolerateNulls();

    /**
     * Returns false when a functional term with this symbol:
     *   1. never produce NULLs
     *   2. May produce NULLs but it is always due to a NULL argument
     */
    protected abstract boolean mayReturnNullWithoutNullArguments();

    /**
     * Returns false if IfElseNullLifting must be disabled although it may have been technically possible.
     *
     * False by default
     */
    protected boolean enableIfElseNullLifting() {
        return false;
    }

    /**
     * Returns false if CoalesceLifting must be disabled although it may have been technically possible.
     *
     * False by default
     */
    protected boolean enableCoalesceLifting() {
        return false;
    }

    /**
     * To be overridden when is sometimes but not always injective in the absence of non-injective functional terms
     */
    @Override
    public Optional<FunctionalTermDecomposition> analyzeInjectivity(ImmutableList<? extends ImmutableTerm> arguments,
                                                                    ImmutableSet<Variable> nonFreeVariables,
                                                                    VariableNullability variableNullability,
                                                                    VariableGenerator variableGenerator,
                                                                    TermFactory termFactory) {
        if (!isDeterministic())
            return Optional.empty();

        if (arguments.stream()
                .allMatch(t -> ((t instanceof GroundTerm) && ((GroundTerm) t).isDeterministic())
                        || nonFreeVariables.contains(t)))
            return Optional.of(termFactory.getFunctionalTermDecomposition(
                    termFactory.getImmutableFunctionalTerm(this, arguments)));

        if (!isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms())
            return Optional.empty();

        return Optional.of(decomposeInjectiveTopFunctionalTerm(arguments, nonFreeVariables, variableNullability, variableGenerator, termFactory));
    }

    protected FunctionalTermDecomposition decomposeInjectiveTopFunctionalTerm(ImmutableList<? extends ImmutableTerm> arguments,
                                                                              ImmutableSet<Variable> nonFreeVariables,
                                                                              VariableNullability variableNullability,
                                                                              VariableGenerator variableGenerator,
                                                                              TermFactory termFactory) {

        InjectivityAnalyzer analyzer = new InjectivityAnalyzer(nonFreeVariables, variableNullability, variableGenerator, termFactory);

        return analyzer.decomposeInjectiveTopFunctionalTerm(arguments);
    }

    protected class InjectivityAnalyzer {
        private final ImmutableSet<Variable> nonFreeVariables;
        private final VariableNullability variableNullability;
        private final VariableGenerator variableGenerator;
        private final TermFactory termFactory;
        protected InjectivityAnalyzer(ImmutableSet<Variable> nonFreeVariables, VariableNullability variableNullability,
                            VariableGenerator variableGenerator, TermFactory termFactory) {
            this.nonFreeVariables = nonFreeVariables;
            this.variableNullability = variableNullability;
            this.variableGenerator = variableGenerator;
            this.termFactory = termFactory;
        }

        /**
         * Only when injectivity of the top function symbol is proved!
         */
        protected FunctionalTermDecomposition decomposeInjectiveTopFunctionalTerm(ImmutableList<? extends ImmutableTerm> arguments) {

            ImmutableMap<Integer, FunctionalTermDecomposition> subTermDecompositions = IntStream.range(0, getArity())
                    .filter(i -> arguments.get(i) instanceof ImmutableFunctionalTerm)
                    .boxed()
                    .collect(ImmutableCollectors.toMap(
                            i -> i,
                            // Recursive
                            i -> getFunctionalTermDecomposition((ImmutableFunctionalTerm) arguments.get(i))));

            ImmutableList<ImmutableTerm> newArguments = IntStream.range(0, getArity())
                    .mapToObj(i -> Optional.ofNullable(subTermDecompositions.get(i))
                            .map(FunctionalTermDecomposition::getLiftableTerm)
                            // Previous argument when non-functional
                            .orElseGet(() -> arguments.get(i)))
                    .collect(ImmutableCollectors.toList());

            Substitution<ImmutableFunctionalTerm> subTermSubstitution = termFactory.getSubstitution(subTermDecompositions.values().stream()
                    .map(FunctionalTermDecomposition::getSubstitution)
                    .flatMap(Substitution::stream)
                    .collect(ImmutableCollectors.toMap()));

            ImmutableFunctionalTerm newFunctionalTerm = termFactory.getImmutableFunctionalTerm(FunctionSymbolImpl.this, newArguments);

            return termFactory.getFunctionalTermDecomposition(newFunctionalTerm, subTermSubstitution);
        }

        private FunctionalTermDecomposition getFunctionalTermDecomposition(ImmutableFunctionalTerm arg)  {

            Optional<FunctionalTermDecomposition> optional = arg.analyzeInjectivity(nonFreeVariables, variableNullability, variableGenerator);
            // Injective functional sub-term
            if (optional.isPresent())
                return optional.get();

            // Otherwise a fresh variable
            Variable var = variableGenerator.generateNewVariable();
            return termFactory.getFunctionalTermDecomposition(var, termFactory.getSubstitution(ImmutableMap.of(var, arg)));
        }
    }


    protected final boolean isInjective(ImmutableList<? extends ImmutableTerm> arguments,
                                        VariableNullability variableNullability, TermFactory termFactory) {
        // Only for test purposes
        VariableGenerator testVariableGenerator = new VariableGeneratorImpl(
                arguments.stream()
                        .flatMap(ImmutableTerm::getVariableStream)
                        .collect(ImmutableCollectors.toSet()), termFactory);

        return analyzeInjectivity(arguments, ImmutableSet.of(), variableNullability, testVariableGenerator, termFactory)
                .filter(d -> d.getSubstitution().isEmpty())
                .isPresent();
    }

    /**
     * By default, just build a new functional term.
     *
     * NB: If the function symbol does not tolerate NULL values, no need to handle them here.
     *
     */
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     TermFactory termFactory, VariableNullability variableNullability) {
        return termFactory.getImmutableFunctionalTerm(this, newTerms);
    }

    protected ImmutableList<TermType> getExpectedBaseTypes() {
        return expectedBaseTypes;
    }

    @Override
    public TermType getExpectedBaseType(int index) {
        return expectedBaseTypes.get(index);
    }

    protected Optional<ImmutableTerm> tryToLiftMagicNumbers(ImmutableList<ImmutableTerm> newTerms,
                                                            TermFactory termFactory,
                                                            VariableNullability variableNullability, boolean isBoolean) {
        Optional<ImmutableFunctionalTerm> optionalTermTypeFunctionalTerm = newTerms.stream()
                .filter(t -> t instanceof ImmutableFunctionalTerm)
                .map(t -> (ImmutableFunctionalTerm) t)
                .filter(t -> t.getFunctionSymbol() instanceof RDFTermTypeFunctionSymbol)
                .findFirst();

        if (optionalTermTypeFunctionalTerm.isPresent()) {
            ImmutableFunctionalTerm firstTermTypeFunctionalTerm = optionalTermTypeFunctionalTerm.get();
            int index = newTerms.indexOf(firstTermTypeFunctionalTerm);

            ImmutableTerm newTerm = ((RDFTermTypeFunctionSymbol) firstTermTypeFunctionalTerm.getFunctionSymbol())
                    .lift(
                            firstTermTypeFunctionalTerm.getTerms(),
                            c -> termFactory.getImmutableFunctionalTerm(
                                    this,
                                    IntStream.range(0, newTerms.size())
                                            .mapToObj(i -> i == index ? c : newTerms.get(i))
                                            .collect(ImmutableCollectors.toList())),
                            termFactory, isBoolean)
                    // Recursive
                    .simplify(variableNullability);

            return Optional.of(newTerm);
        }
        else
            return Optional.empty();
    }

    protected enum Decomposability {
        NEEDS_NON_NULL_CONDITION_WRAPPING,
        NO_WRAPPING_NEEDED,
        CANNOT_BE_DECOMPOSED
    }

}
