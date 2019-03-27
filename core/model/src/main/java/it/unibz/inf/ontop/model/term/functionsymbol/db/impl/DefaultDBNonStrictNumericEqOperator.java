package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.math.BigDecimal;


public class DefaultDBNonStrictNumericEqOperator extends AbstractDBNonStrictEqOperator {

    /**
     * TODO: type the input
     */
    protected DefaultDBNonStrictNumericEqOperator(DBTermType rootDBTermType,
                                                  DBTermType dbBoolean) {
        super("NUM_NON_STRICT_EQ", rootDBTermType, dbBoolean);
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return true;
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory,
                                                     VariableNullability variableNullability) {
        if (newTerms.stream().allMatch(t -> t instanceof DBConstant)) {
            BigDecimal n1 = new BigDecimal(((DBConstant) newTerms.get(0)).getValue());
            BigDecimal n2 = new BigDecimal(((DBConstant) newTerms.get(1)).getValue());

            return termFactory.getDBBooleanConstant(n1.compareTo(n2) == 0);
        }

        return super.buildTermAfterEvaluation(newTerms, termFactory, variableNullability);
    }
}
