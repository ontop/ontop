package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

public class DefaultDBNonStrictStringEqOperator extends AbstractDBNonStrictEqOperator {
    /**
     * TODO: type the input
     */
    protected DefaultDBNonStrictStringEqOperator(DBTermType rootDBTermType, DBTermType dbBoolean) {
        super("STR_NON_STRICT_EQ", rootDBTermType, dbBoolean);
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return true;
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory,
                                                     VariableNullability variableNullability) {
        if (newTerms.stream().allMatch(t -> t instanceof DBConstant)) {
            return termFactory.getDBBooleanConstant(
                    ((DBConstant) newTerms.get(0)).getValue().equals(
                            ((DBConstant) newTerms.get(1)).getValue()));
        }
        else
            return termFactory.getImmutableExpression(this, newTerms);
    }
}
