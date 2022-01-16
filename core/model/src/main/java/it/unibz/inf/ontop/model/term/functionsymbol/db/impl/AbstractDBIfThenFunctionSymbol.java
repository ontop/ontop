package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.tools.TypeConstantDictionary;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.BooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermTypeFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBIfThenFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Abstract both for IF-THEN-ELSE or more general CASE
 *
 * Arguments are an alternation of (ImmutableExpression, ImmutableTerm) plus optionally an ImmutableTerm for the default case
 */
public abstract class AbstractDBIfThenFunctionSymbol extends AbstractArgDependentTypedDBFunctionSymbol
        implements DBIfThenFunctionSymbol {

    protected final boolean doOrderingMatter;

    protected AbstractDBIfThenFunctionSymbol(@Nonnull String name, int arity, DBTermType dbBooleanType,
                                             DBTermType rootDBTermType, boolean doOrderingMatter) {
        super(name, computeBaseTypes(arity, dbBooleanType, rootDBTermType));
        this.doOrderingMatter = doOrderingMatter;
    }

    private static ImmutableList<TermType> computeBaseTypes(int arity, DBTermType dbBooleanType, DBTermType rootDBTermType) {
        Stream<DBTermType> regularConditions = IntStream.range(0, arity - (arity % 2))
                .mapToObj(i -> (i % 2 == 0) ? dbBooleanType : rootDBTermType);

        Stream<DBTermType> typeStream = (arity % 2 == 0)
                ? regularConditions
                : Stream.concat(regularConditions, Stream.of(rootDBTermType));

        return typeStream.collect(ImmutableCollectors.toList());
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    /**
     * Due to the presence of boolean expressions, seems better to avoid decomposing it (can be revisited)
     */
    @Override
    public boolean shouldBeDecomposedInUnion() {
        return false;
    }

    @Override
    public Stream<ImmutableTerm> extractPossibleValues(ImmutableList<? extends ImmutableTerm> terms) {
        return IntStream.range(1, terms.size())
                .filter(i -> i % 2 == 1)
                .mapToObj(terms::get);
    }


    @Override
    public ImmutableTerm simplify(ImmutableList<? extends ImmutableTerm> terms,
                                  TermFactory termFactory, VariableNullability variableNullability) {
        int arity = getArity();

        List<Map.Entry<ImmutableExpression, ? extends ImmutableTerm>> newWhenPairs = new ArrayList<>();

        /*
         * When conditions
         */
        for (int i=0; i < arity - (arity % 2); i+=2) {
            ImmutableTerm term = terms.get(i);
            ImmutableExpression expression =  Optional.of(term)
                    .filter(t -> t instanceof ImmutableExpression)
                    .map(t -> (ImmutableExpression)t)
                    .orElseThrow(() -> new MinorOntopInternalBugException(term + " was expected to be " +
                            "an ImmutableExpression due to its position in " + this));

            ImmutableExpression.Evaluation evaluation = expression.evaluate2VL(variableNullability);
            if (evaluation.getValue().isPresent()) {
                switch (evaluation.getValue().get()) {
                    case TRUE:
                        ImmutableTerm possibleValue = simplifyValue(terms.get(i+1),variableNullability, termFactory);
                        if (newWhenPairs.isEmpty())
                            return possibleValue;
                        else
                            return termFactory.getDBCase(newWhenPairs.stream(), possibleValue, doOrderingMatter)
                                    .simplify(variableNullability);
                    default:
                        // Discard the case entry
                }
            }
            else {
                ImmutableExpression newExpression = evaluation.getExpression()
                        .orElseThrow(() -> new MinorOntopInternalBugException("The evaluation was expected " +
                                "to return an expression because no value was returned"));
                ImmutableTerm possibleValue = simplifyValue(terms.get(i+1), variableNullability, termFactory);
                newWhenPairs.add(Maps.immutableEntry(newExpression, possibleValue));
            }
        }

        ImmutableTerm defaultValue = simplifyValue(extractDefaultValue(terms, termFactory), variableNullability, termFactory);

        Optional<ImmutableFunctionalTerm> optionalLiftedOptionalTerm = tryToLiftRDFTermTypeFunctions(newWhenPairs,
                defaultValue, termFactory);
        if (optionalLiftedOptionalTerm.isPresent())
            return optionalLiftedOptionalTerm.get()
                    .simplify(variableNullability);


        ImmutableList<Map.Entry<ImmutableExpression, ? extends ImmutableTerm>> shrunkWhenPairs =
                shrinkWhenPairs(newWhenPairs, defaultValue);

        if (shrunkWhenPairs.isEmpty())
            return defaultValue;

        ImmutableFunctionalTerm newTerm = buildCase(shrunkWhenPairs.stream(), defaultValue, termFactory);

        // Make sure the size was reduced so as to avoid an infinite loop
        // For instance, new opportunities may appear when reduced to a IF_ELSE_NULL
        return (shrunkWhenPairs.size() < terms.size() % 2)
                ? newTerm.simplify(variableNullability)
                : newTerm;
    }

    /**
     * Can be overridden
     */
    protected ImmutableTerm simplifyValue(ImmutableTerm immutableTerm, VariableNullability variableNullability, TermFactory termFactory) {
        return immutableTerm.simplify(variableNullability);
    }

    private Optional<ImmutableFunctionalTerm> tryToLiftRDFTermTypeFunctions(
            List<Map.Entry<ImmutableExpression, ? extends ImmutableTerm>> whenPairs,
            ImmutableTerm defaultValue, TermFactory termFactory) {

        ImmutableList<? extends ImmutableTerm> possibleTerms = Stream.concat(
                whenPairs.stream().map(Map.Entry::getValue),
                Stream.of(defaultValue))
                .collect(ImmutableCollectors.toList());

        if (possibleTerms.stream().allMatch(t -> (t instanceof ImmutableFunctionalTerm)
                && (((ImmutableFunctionalTerm) t).getFunctionSymbol())instanceof RDFTermTypeFunctionSymbol)) {

            Stream<Map.Entry<ImmutableExpression, ImmutableTerm>> newWhenPairs = whenPairs.stream()
                    .map(e -> Maps.immutableEntry(
                            e.getKey(),
                            // Unwraps the RDF type functional term
                            ((ImmutableFunctionalTerm) e.getValue()).getTerm(0)));
            ImmutableTerm newDefaultValue = ((ImmutableFunctionalTerm) defaultValue).getTerm(0);

            ImmutableFunctionalTerm newCase = termFactory.getDBCase(newWhenPairs, newDefaultValue, doOrderingMatter);

            ImmutableSet<RDFTermTypeFunctionSymbol> functionSymbols = possibleTerms.stream()
                    .map(t -> (ImmutableFunctionalTerm) t)
                    .map(t -> (RDFTermTypeFunctionSymbol) t.getFunctionSymbol())
                    .collect(ImmutableCollectors.toSet());

            ImmutableSet<TypeConstantDictionary> dictionaries = functionSymbols.stream()
                    .map(RDFTermTypeFunctionSymbol::getDictionary)
                    .collect(ImmutableCollectors.toSet());
            if (dictionaries.size() != 1)
                throw new MinorOntopInternalBugException(
                        "All the RDFTermTypeFunctionSymbol were expected to use the same dictionary");
            TypeConstantDictionary dictionary = dictionaries.stream().findAny().get();

            ImmutableSet<RDFTermTypeConstant> possibleConstants = functionSymbols.stream()
                    .flatMap(f -> f.getConversionMap().values().stream())
                    .collect(ImmutableCollectors.toSet());

            return Optional.of(termFactory.getRDFTermTypeFunctionalTerm(
                    newCase, dictionary, possibleConstants, false));
        }
        else
            return Optional.empty();
    }


    /**
     * Can be overridden
     */
    protected ImmutableFunctionalTerm buildCase(Stream<Map.Entry<ImmutableExpression, ? extends ImmutableTerm>> newWhenPairs,
                                                ImmutableTerm defaultValue, TermFactory termFactory) {
        return termFactory.getDBCase(newWhenPairs, defaultValue, doOrderingMatter);
    }

    private ImmutableList<Map.Entry<ImmutableExpression, ? extends ImmutableTerm>> shrinkWhenPairs(
            List<Map.Entry<ImmutableExpression, ? extends ImmutableTerm>> newWhenPairs, ImmutableTerm defaultValue) {
        return doOrderingMatter
                ? shrinkWhenPairsWithOrder(newWhenPairs, defaultValue)
                : shrinkWhenPairsWithoutOrder(newWhenPairs, defaultValue);
    }

    /**
     * Removes the last when pairs that return the same value as the default "else" case
     */
    private ImmutableList<Map.Entry<ImmutableExpression, ? extends ImmutableTerm>> shrinkWhenPairsWithOrder(
            List<Map.Entry<ImmutableExpression, ? extends ImmutableTerm>> whenPairs, ImmutableTerm defaultValue) {
        int nbPairs = whenPairs.size();

        Optional<Integer> lastIncompatibleIndex = IntStream.range(0, nbPairs)
                .map(i -> nbPairs - i - 1)
                .filter(i -> !whenPairs.get(i).getValue().equals(defaultValue))
                .boxed()
                .findFirst();

        return lastIncompatibleIndex
                .map(i -> ImmutableList.copyOf(whenPairs.subList(0, i + 1)))
                .orElseGet(ImmutableList::of);
    }

    /**
     * When order does not matter, when pairs returning the same value as the default one are filtered out
     */
    private ImmutableList<Map.Entry<ImmutableExpression, ? extends ImmutableTerm>> shrinkWhenPairsWithoutOrder(
            List<Map.Entry<ImmutableExpression, ? extends ImmutableTerm>> whenPairs, ImmutableTerm defaultValue) {
        return whenPairs.stream()
                .filter(p -> !p.getValue().equals(defaultValue))
                .collect(ImmutableCollectors.toList());
    }


    /**
     * Conservative: can only be post-processed when all sub-functional terms (at different levels of depth)
     * can be post-processed.
     *
     * TODO: consider perhaps a less conservative approach
     *
     */
    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return extractSubFunctionalTerms(arguments)
                .allMatch(ImmutableFunctionalTerm::canBePostProcessed);
    }

    /**
     * Recursive
     */
    protected Stream<ImmutableFunctionalTerm> extractSubFunctionalTerms(ImmutableList<? extends ImmutableTerm> subTerms) {
        return subTerms.stream()
                .filter(t -> t instanceof ImmutableFunctionalTerm)
                .map(t -> (ImmutableFunctionalTerm)t)
                .flatMap(f -> Stream.concat(Stream.of(f), extractSubFunctionalTerms(f.getTerms())));
    }

    protected abstract ImmutableTerm extractDefaultValue(ImmutableList<? extends ImmutableTerm> terms, TermFactory termFactory);

    /**
     * Default, can be overridden
     */
    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return true;
    }

    @Override
    protected boolean tolerateNulls() {
        return true;
    }

    @Override
    public boolean isPreferringToBePostProcessedOverBeingBlocked() {
        return false;
    }

    /**
     * Requires some of its arguments to be expressions
     */
    @Override
    protected ImmutableList<? extends ImmutableTerm> transformIntoRegularArguments(
            ImmutableList<? extends NonFunctionalTerm> arguments, TermFactory termFactory) {
        return IntStream.range(0, arguments.size())
                .mapToObj(i -> i % 2 == 0
                        ? termFactory.getIsTrue(arguments.get(i))
                        : arguments.get(i))
                .collect(ImmutableCollectors.toList());
    }

    @Override
    public ImmutableExpression pushDownExpression(ImmutableExpression expression, int indexOfDBIfThenFunctionSymbol,
                                                  TermFactory termFactory) {
        return pushDownFunctionalTerm(expression, indexOfDBIfThenFunctionSymbol, termFactory,
                (f, terms) -> termFactory.getImmutableExpression((BooleanFunctionSymbol)f, terms),
                (pairs, defaultValue) -> termFactory.getDBBooleanCase(pairs, defaultValue, doOrderingMatter));
    }

    @Override
    public ImmutableFunctionalTerm pushDownRegularFunctionalTerm(ImmutableFunctionalTerm functionalTerm,
                                                                 int indexOfDBIfThenFunctionSymbol,
                                                                 TermFactory termFactory) {
        return pushDownFunctionalTerm(functionalTerm, indexOfDBIfThenFunctionSymbol, termFactory,
                termFactory::getImmutableFunctionalTerm,
                (pairs, defaultValue) -> termFactory.getDBCase(pairs, defaultValue, doOrderingMatter));
    }

    protected <T extends ImmutableFunctionalTerm> T pushDownFunctionalTerm(
            T functionalTerm, int indexOfDBIfThenFunctionSymbol, TermFactory termFactory,
            BiFunction<FunctionSymbol, ImmutableList<? extends ImmutableTerm>, T> functionalTermCst,
            BiFunction<Stream<Map.Entry<ImmutableExpression, T>>, T, T> caseCst) {

        ImmutableList<? extends ImmutableTerm> expressionArguments = functionalTerm.getTerms();
        if (indexOfDBIfThenFunctionSymbol >= expressionArguments.size())
            throw new IllegalArgumentException("Wrong index given");

        ImmutableList<? extends ImmutableTerm> ifThenArguments = Optional.of(expressionArguments.get(indexOfDBIfThenFunctionSymbol))
                .filter(t -> t instanceof ImmutableFunctionalTerm)
                .map(t -> (ImmutableFunctionalTerm) t)
                .filter(t -> equals(t.getFunctionSymbol()))
                .map(ImmutableFunctionalTerm::getTerms)
                .orElseThrow(() -> new IllegalArgumentException("Was expected to find this function symbol at the indicated position"));

        FunctionSymbol functionSymbol = functionalTerm.getFunctionSymbol();

        Stream<Map.Entry<ImmutableExpression, T>> whenPairs = IntStream.range(0, ifThenArguments.size() / 2)
                .mapToObj(i -> Maps.immutableEntry(
                        (ImmutableExpression) ifThenArguments.get(2 * i),
                        functionalTermCst.apply(functionSymbol,
                                updateArguments(ifThenArguments.get(2 * i + 1), indexOfDBIfThenFunctionSymbol, expressionArguments))));

        T defaultValue = functionalTermCst.apply(functionSymbol,
                updateArguments(extractDefaultValue(ifThenArguments, termFactory), indexOfDBIfThenFunctionSymbol,
                        expressionArguments));

        return caseCst.apply(whenPairs, defaultValue);
    }

    private ImmutableList<? extends ImmutableTerm> updateArguments(ImmutableTerm subTerm, int index,
                                                                   ImmutableList<? extends ImmutableTerm> expressionArguments) {
        return IntStream.range(0, expressionArguments.size())
                .mapToObj(i -> i == index ? subTerm : expressionArguments.get(i))
                .collect(ImmutableCollectors.toList());
    }
}
