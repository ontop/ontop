package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermTypeFunctionSymbol;
import it.unibz.inf.ontop.model.type.ConcreteNumericRDFDatatype;
import it.unibz.inf.ontop.model.type.MetaRDFTermType;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.Stream;


public class CommonPropagatedOrSubstitutedNumericTypeFunctionSymbolImpl extends AbstractCommonDenominatorFunctionSymbol {

    CommonPropagatedOrSubstitutedNumericTypeFunctionSymbolImpl(MetaRDFTermType metaRDFTermType) {
        super("COMMON_NUM_TYPE", 2, metaRDFTermType);
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     TermFactory termFactory, VariableNullability variableNullability) {

        if (newTerms.stream().allMatch(t -> t instanceof RDFTermTypeConstant)) {
            return evaluateCombination(
                    newTerms.stream()
                            .filter(t -> t instanceof RDFTermTypeConstant)
                            .map(c -> (RDFTermTypeConstant) c), termFactory)
                    .map(t -> (Constant) t)
                    .orElseGet(termFactory::getNullConstant);
        }

        /*
         * NB: no risk of having more than one constant at this point
         */
        Optional<RDFTermTypeConstant> optionalTypeConstant = newTerms.stream()
                .filter(t -> t instanceof RDFTermTypeConstant)
                .map(t -> (RDFTermTypeConstant) t)
                .findAny();

        ImmutableList<ImmutableTerm> otherTerms = newTerms.stream()
                .filter(t -> !(t instanceof RDFTermTypeConstant))
                .distinct()
                .collect(ImmutableCollectors.toList());

        /*
         * Presence of RDFTermTypeFunctionSymbols
         */
        if ((otherTerms.stream()
                    .anyMatch(a -> (a instanceof ImmutableFunctionalTerm) &&
                            ((ImmutableFunctionalTerm) a).getFunctionSymbol() instanceof RDFTermTypeFunctionSymbol))) {
                return simplifyUsingMagicNumbers((ImmutableList<ImmutableFunctionalTerm>)(ImmutableList<?>)otherTerms,
                        optionalTypeConstant, termFactory);
        }
        else
            return super.buildTermAfterEvaluation(newTerms, termFactory, variableNullability);
    }

    @Override
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

    private Optional<RDFTermTypeConstant> evaluateCombination(Stream<RDFTermTypeConstant> typeConstantStream, TermFactory termFactory) {
        Optional<ConcreteNumericRDFDatatype> optionalNumericType = typeConstantStream
                .map(RDFTermTypeConstant::getRDFTermType)
                .filter(t -> t instanceof ConcreteNumericRDFDatatype)
                .map(t -> (ConcreteNumericRDFDatatype) t)
                .reduce(ConcreteNumericRDFDatatype::getCommonPropagatedOrSubstitutedType)
                .filter(t -> !t.isAbstract());

        return optionalNumericType
                .map(termFactory::getRDFTermTypeConstant);
    }
}
