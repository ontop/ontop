package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermTypeFunctionSymbol;
import it.unibz.inf.ontop.model.type.MetaRDFTermType;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.Stream;

public class CommonDenominatorFunctionSymbolImpl extends AbstractCommonDenominatorFunctionSymbol {

    CommonDenominatorFunctionSymbolImpl(int arity, MetaRDFTermType metaRDFTermType) {
        super("COMMON_TYPE" + arity, arity, metaRDFTermType);
    }

    /**
     * Reduces all the RDFTermTypeConstants into one.
     */
    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     TermFactory termFactory, VariableNullability variableNullability) {
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
                    .filter(t -> t.getArity() < getArity())
                    .map(t -> t.simplify(variableNullability))
                    .orElseGet(() -> super.buildTermAfterEvaluation(newTerms, termFactory, variableNullability));
        }
    }

    protected Optional<RDFTermTypeConstant> evaluateCombination(ImmutableList<RDFTermTypeConstant> constants,
                                                                Optional<RDFTermTypeConstant> optionalMergedTypeConstant,
                                                                TermFactory termFactory) {
        return optionalMergedTypeConstant
                .map(c -> Stream.concat(Stream.of(c), constants.stream()))
                .orElseGet(constants::stream)
                .reduce((c1, c2) -> termFactory.getRDFTermTypeConstant(
                        (RDFTermType) c1.getRDFTermType().getCommonDenominator(c2.getRDFTermType())))
                .filter(c -> !c.getRDFTermType().isAbstract());
    }
}
