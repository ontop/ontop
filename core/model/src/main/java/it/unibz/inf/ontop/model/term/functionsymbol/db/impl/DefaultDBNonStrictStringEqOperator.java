package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;

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
        // Same term type --> reduce it to a strict equality
        else if (newTerms.get(0).inferType()
                .flatMap(TermTypeInference::getTermType)
                .filter(t -> newTerms.get(1).inferType()
                        .flatMap(TermTypeInference::getTermType)
                        .filter(t::equals)
                        .isPresent())
                .isPresent())
            return termFactory.getStrictEquality(newTerms)
                    .simplify(variableNullability);
        else
            return termFactory.getImmutableExpression(this, newTerms);
    }
}
