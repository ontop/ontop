package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.MetaRDFTermType;
import it.unibz.inf.ontop.model.type.RDFTermType;

import static it.unibz.inf.ontop.model.term.functionsymbol.BooleanExpressionOperation.NOT;

/**
 * TODO: find a better name!
 */
public class IsARDFTermTypeFunctionSymbolImpl extends BooleanFunctionSymbolImpl {

    private final RDFTermType baseType;

    protected IsARDFTermTypeFunctionSymbolImpl(MetaRDFTermType metaRDFTermType, DBTermType dbBooleanTermType,
                                               RDFTermType baseType) {
        super("IS_A", ImmutableList.of(metaRDFTermType), dbBooleanTermType);
        this.baseType = baseType;
    }

    @Override
    public boolean isInjective(ImmutableList<? extends ImmutableTerm> arguments, ImmutableSet<Variable> nonNullVariables) {
        return false;
    }

    @Override
    public boolean canBePostProcessed() {
        return true;
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     boolean isInConstructionNodeInOptimizationPhase,
                                                     TermFactory termFactory) {
        ImmutableTerm subTerm = newTerms.get(0);

        if (subTerm instanceof RDFTermTypeConstant) {
            RDFTermType firstType = ((RDFTermTypeConstant) subTerm).getRDFTermType();
            return termFactory.getDBBooleanConstant(firstType.isA(baseType));
        }
        return termFactory.getImmutableFunctionalTerm(this, newTerms);
    }

    @Override
    public boolean blocksNegation() {
        return false;
    }

    @Override
    public ImmutableExpression negate(ImmutableList<? extends ImmutableTerm> subTerms, TermFactory termFactory) {
        return termFactory.getImmutableExpression(NOT, termFactory.getImmutableExpression(this, subTerms));
    }
}
