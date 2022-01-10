package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.tools.TypeConstantDictionary;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermTypeFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBIfThenFunctionSymbol;
import it.unibz.inf.ontop.model.type.MetaRDFTermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class AbstractCommonDenominatorFunctionSymbol extends FunctionSymbolImpl {

    private final MetaRDFTermType metaRDFTermType;

    public AbstractCommonDenominatorFunctionSymbol(String functionSymbolName, int arity, MetaRDFTermType metaRDFTermType) {
        super(functionSymbolName, IntStream.range(0, arity)
                .mapToObj(i -> metaRDFTermType)
                .collect(ImmutableCollectors.toList()));
        this.metaRDFTermType = metaRDFTermType;
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(metaRDFTermType));
    }

    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return true;
    }

    @Override
    protected boolean tolerateNulls() {
        return false;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return true;
    }

    /**
     * To be overridden by concrete classes
     *
     * Default case: looks for DBIfThen
     */
    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     TermFactory termFactory, VariableNullability variableNullability) {
        return newTerms.stream()
                .filter(t -> t instanceof ImmutableFunctionalTerm)
                .map(t -> (ImmutableFunctionalTerm)t)
                .filter(t -> t.getFunctionSymbol() instanceof DBIfThenFunctionSymbol)
                .findAny()
                .map(t -> ((DBIfThenFunctionSymbol) t.getFunctionSymbol())
                        .pushDownRegularFunctionalTerm(
                                termFactory.getImmutableFunctionalTerm(this, newTerms),
                                newTerms.indexOf(t),
                                termFactory))
                .map(t -> t.simplify(variableNullability))
                .orElseGet(() -> super.buildTermAfterEvaluation(newTerms, termFactory, variableNullability));
    }

    /**
     * otherTerms: all use a RDFTermTypeFunctionSymbol. Non-empty.
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected ImmutableTerm simplifyUsingMagicNumbers(ImmutableList<ImmutableFunctionalTerm> otherTerms,
                                                    Optional<RDFTermTypeConstant> optionalMergedTypeConstant,
                                                    TermFactory termFactory) {
        ImmutableList<? extends ImmutableTerm> subTerms = otherTerms.stream()
                .map(t -> t.getTerm(0))
                .collect(ImmutableCollectors.toList());

        if (!subTerms.stream().allMatch(t -> t instanceof Variable)) {
            throw new MinorOntopInternalBugException(
                    "Was expecting RDF term type functional terms to have a variable as argument\n" + otherTerms);
        }
        ImmutableList<Variable> subVariables = (ImmutableList<Variable>) subTerms;

        ImmutableSet<ImmutableList<RDFTermTypeConstant>> possibleCombinations = extractPossibleCombinations(otherTerms);

        TypeConstantDictionary dictionary = otherTerms.stream()
                .map(t -> ((RDFTermTypeFunctionSymbol) t.getFunctionSymbol()).getDictionary())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("otherTerms must be non-empty"));

        ImmutableMap<ImmutableList<RDFTermTypeConstant>, RDFTermTypeConstant> validCombinations = possibleCombinations.stream()
                .map(l -> evaluateCombination(l, optionalMergedTypeConstant, termFactory)
                        .map(r -> Maps.immutableEntry(l, r)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(ImmutableCollectors.toMap());

        if (validCombinations.isEmpty())
            return termFactory.getNullConstant();

        ImmutableFunctionalTerm caseTerm = termFactory.getDBCaseElseNull(validCombinations.entrySet().stream()
                .map(e -> Maps.immutableEntry(
                        convertIntoConjunction(e.getKey(), subVariables, dictionary, termFactory),
                        dictionary.convert(e.getValue()))),
                false);

        return termFactory.getRDFTermTypeFunctionalTerm(caseTerm, dictionary,
                ImmutableSet.copyOf(validCombinations.values()), true);
    }

    /**
     * Recursive
     */
    private ImmutableSet<ImmutableList<RDFTermTypeConstant>> extractPossibleCombinations(ImmutableList<ImmutableFunctionalTerm> terms) {
        if (terms.isEmpty())
            return ImmutableSet.of();

        ImmutableFunctionalTerm firstTerm = terms.get(0);
        RDFTermTypeFunctionSymbol functionSymbol = (RDFTermTypeFunctionSymbol) firstTerm.getFunctionSymbol();

        ImmutableList<ImmutableFunctionalTerm> followingTerms = terms.subList(1, terms.size());

        if (followingTerms.isEmpty()) {
            return functionSymbol.getConversionMap().values().stream()
                    .map(ImmutableList::of)
                    .collect(ImmutableCollectors.toSet());
        }
        else {
            // Recursive (non-tail)
            ImmutableSet<ImmutableList<RDFTermTypeConstant>> otherCombinations = extractPossibleCombinations(followingTerms);

            return functionSymbol.getConversionMap().values().stream()
                    .flatMap(v1 -> otherCombinations.stream()
                            .map(c -> Stream.concat(Stream.of(v1), c.stream())
                                    .collect(ImmutableCollectors.toList())))
                    .collect(ImmutableCollectors.toSet());
        }
    }

    private ImmutableExpression convertIntoConjunction(ImmutableList<RDFTermTypeConstant> constants,
                                                       ImmutableList<Variable> subVariables,
                                                       TypeConstantDictionary dictionary, TermFactory termFactory) {
        return termFactory.getConjunction(IntStream.range(0, constants.size())
                .mapToObj(i -> termFactory.getStrictEquality(subVariables.get(i), dictionary.convert(constants.get(i))))
                .collect(ImmutableCollectors.toList()));
    }

    protected abstract Optional<RDFTermTypeConstant> evaluateCombination(ImmutableList<RDFTermTypeConstant> constants,
                                                                         Optional<RDFTermTypeConstant> optionalMergedTypeConstant,
                                                                         TermFactory termFactory);

    @Override
    protected boolean enableIfElseNullLifting() {
        return true;
    }
}
