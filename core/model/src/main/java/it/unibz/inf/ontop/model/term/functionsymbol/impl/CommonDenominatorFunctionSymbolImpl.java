package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.exception.FatalTypingException;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.tools.TypeConstantDictionary;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermTypeFunctionSymbol;
import it.unibz.inf.ontop.model.term.impl.FunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.MetaRDFTermType;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CommonDenominatorFunctionSymbolImpl extends FunctionSymbolImpl {

    private final MetaRDFTermType metaRDFTermType;

    CommonDenominatorFunctionSymbolImpl(int arity, MetaRDFTermType metaRDFTermType) {
        super("COMMON_TYPE" + arity,
                IntStream.range(0, arity)
                        .boxed()
                        .map(i -> metaRDFTermType)
                        .collect(ImmutableCollectors.toList()));
        this.metaRDFTermType = metaRDFTermType;
    }

    @Override
    public boolean isInjective(ImmutableList<? extends ImmutableTerm> arguments, ImmutableSet<Variable> nonNullVariables) {
        return false;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(metaRDFTermType));
    }

    @Override
    public Optional<TermTypeInference> inferAndValidateType(ImmutableList<? extends ImmutableTerm> terms) throws FatalTypingException {
        validateSubTermTypes(terms);
        return inferType(terms);
    }

    @Override
    public boolean canBePostProcessed() {
        return false;
    }

    /**
     * Reduces all the RDFTermTypeConstants into one.
     */
    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     boolean isInConstructionNodeInOptimizationPhase,
                                                     TermFactory termFactory) {
        /*
         * Constant terms are first reduced
         */
        Optional<RDFTermTypeConstant> optionalMergedTypeConstant = newTerms.stream()
                .filter(t -> t instanceof RDFTermTypeConstant)
                .map(c -> (RDFTermTypeConstant) c)
                .reduce((c1, c2) -> termFactory.getRDFTermTypeConstant(
                        (RDFTermType) c1.getRDFTermType().getCommonDenominator(c2.getRDFTermType())));

        /*
         * If it evaluates to an abstract type --> returns NULL
         */
        if (optionalMergedTypeConstant
                .filter(t -> t.getRDFTermType().isAbstract())
                .isPresent())
            return termFactory.getNullConstant();

        ImmutableList<ImmutableTerm> otherTerms = newTerms.stream()
                .filter(t -> !(t instanceof RDFTermTypeConstant))
                .distinct()
                .collect(ImmutableCollectors.toList());

        if (otherTerms.isEmpty())
            return optionalMergedTypeConstant.orElseThrow(() ->
                    new MinorOntopInternalBugException("At least one term must remain"));

        /*
         * Presence of RDFTermTypeFunctionSymbols (for all the non-constant terms)
         */
        if ((otherTerms.stream()
                .allMatch(a -> (a instanceof ImmutableFunctionalTerm) &&
                        ((ImmutableFunctionalTerm) a).getFunctionSymbol() instanceof RDFTermTypeFunctionSymbol))) {
            return simplifyUsingMagicNumbers((ImmutableList<ImmutableFunctionalTerm>)(ImmutableList<?>)otherTerms,
                        optionalMergedTypeConstant, termFactory);
        }
        else {
            return optionalMergedTypeConstant
                    .map(c -> termFactory.getCommonDenominatorFunctionalTerm(ImmutableList.<ImmutableTerm>builder()
                            .add(c)
                            .addAll(otherTerms)
                            .build()))
                    .orElseGet(() -> termFactory.getCommonDenominatorFunctionalTerm(otherTerms));
        }
    }

    /**
     * otherTerms: all use a RDFTermTypeFunctionSymbol . Non-empty.
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private ImmutableTerm simplifyUsingMagicNumbers(ImmutableList<ImmutableFunctionalTerm> otherTerms,
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
                .map(l -> evaluateCombination(l, termFactory)
                        .map(r -> Maps.immutableEntry(l, r)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(ImmutableCollectors.toMap());

        throw new RuntimeException("TODO: continue simplifyUsingMagicNumbers()");

    }


    /**
     * Recursive
     */
    private ImmutableSet<ImmutableList<RDFTermTypeConstant>> extractPossibleCombinations(ImmutableList<ImmutableFunctionalTerm> terms) {
        if (terms.isEmpty())
            return ImmutableSet.of();

        ImmutableFunctionalTerm firstTerm = terms.get(0);
        RDFTermTypeFunctionSymbol functionSymbol = (RDFTermTypeFunctionSymbol) firstTerm.getFunctionSymbol();

        // Recursive (non-tail)
        ImmutableSet<ImmutableList<RDFTermTypeConstant>> otherCombinations = extractPossibleCombinations(terms.subList(1, terms.size()));

        return functionSymbol.getConversionMap().values().stream()
                .flatMap(v1 -> otherCombinations.stream()
                        .map(c -> Stream.concat(Stream.of(v1), c.stream())
                                .collect(ImmutableCollectors.toList())))
                .collect(ImmutableCollectors.toSet());
    }

    protected Optional<RDFTermTypeConstant> evaluateCombination(ImmutableList<RDFTermTypeConstant> constants,
                                                                TermFactory termFactory) {
        return constants.stream()
                .reduce((c1, c2) -> termFactory.getRDFTermTypeConstant(
                        (RDFTermType) c1.getRDFTermType().getCommonDenominator(c2.getRDFTermType())))
                .filter(c -> !c.getRDFTermType().isAbstract());
    }
}
