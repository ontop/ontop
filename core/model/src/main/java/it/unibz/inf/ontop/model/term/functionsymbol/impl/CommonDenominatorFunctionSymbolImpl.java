package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.FatalTypingException;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.RDFTermTypeConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
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
        super("TYPE_COMMON" + arity,
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
        Optional<RDFTermTypeConstant> optionalMergedTypeConstant = newTerms.stream()
                .filter(t -> t instanceof RDFTermTypeConstant)
                .map(c -> (RDFTermTypeConstant) c)
                .reduce((c1, c2) -> termFactory.getRDFTermTypeConstant(
                        (RDFTermType) c1.getRDFTermType().getCommonDenominator(c2.getRDFTermType())));
        return optionalMergedTypeConstant
                .map(c -> {
                    ImmutableList<ImmutableTerm> newArgs = Stream.concat(Stream.of(c),
                            newTerms.stream().filter(t -> !(t instanceof RDFTermTypeConstant)))
                            .collect(ImmutableCollectors.toList());

                    return (newArgs.size() == 1)
                            ? c
                            : termFactory.getCommonDenominatorFunctionalTerm(newArgs);
                })
                .orElseGet(() -> termFactory.getImmutableFunctionalTerm(this, newTerms));
    }
}
