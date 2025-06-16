package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;

import java.util.Optional;

public class DurationSumFunctionSymbolImpl extends ReduciblePositiveAritySPARQLFunctionSymbolImpl {
    private final RDFTermType targetType;

    public DurationSumFunctionSymbolImpl(RDFTermType firstArgumentType, RDFTermType secondArgumentType) {
        super("SP_DURATION_SUM", "DURATION_SUM", ImmutableList.of(firstArgumentType, secondArgumentType));
        this.targetType = firstArgumentType;
    }
    @Override
    protected ImmutableTerm computeLexicalTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory, ImmutableTerm returnedTypeTerm) {
        if (subLexicalTerms.stream().noneMatch(t -> t instanceof Constant)) {
            throw new RuntimeException("At least one of the terms must be a constant");
        }

        // TODO: for now, we assume that the first term is a date and the second term is a duration
        return termFactory.getConversion2RDFLexical(
                termFactory.getDBDurationSum(
                        termFactory.getConversionFromRDFLexical2DB(targetType.getClosestDBType(termFactory.getTypeFactory().getDBTypeFactory()), subLexicalTerms.get(0), targetType),
                        subLexicalTerms.get(1)
                ), targetType);
    }

    @Override
    protected ImmutableTerm computeTypeTerm(ImmutableList<? extends ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory, VariableNullability variableNullability) {
        return termFactory.getRDFTermTypeConstant(targetType);
    }

    @Override
    protected boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(targetType));
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }
}
