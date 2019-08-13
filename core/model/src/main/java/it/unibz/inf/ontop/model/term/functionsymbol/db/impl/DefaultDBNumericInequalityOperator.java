package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.math.BigDecimal;

public class DefaultDBNumericInequalityOperator extends AbstractDBInequalityOperator {

    /**
     * TODO: type the input
     */
    protected DefaultDBNumericInequalityOperator(InequalityLabel inequalityLabel,
                                                 DBTermType rootDBTermType, DBTermType dbBoolean) {
        super(inequalityLabel, "NUM_" + inequalityLabel.name(), rootDBTermType, dbBoolean);
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return true;
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory,
                                                     VariableNullability variableNullability) {
        if (newTerms.stream()
                .allMatch(t -> t instanceof DBConstant)) {
            BigDecimal n1 = new BigDecimal(((DBConstant) newTerms.get(0)).getValue());
            BigDecimal n2 = new BigDecimal(((DBConstant) newTerms.get(1)).getValue());

            int comparison = n1.compareTo(n2);
            switch (inequalityLabel) {
                case LT:
                    return termFactory.getDBBooleanConstant(comparison < 0);
                case LTE:
                    return termFactory.getDBBooleanConstant(comparison <= 0);
                case GT:
                    return termFactory.getDBBooleanConstant(comparison > 0);
                case GTE:
                    return termFactory.getDBBooleanConstant(comparison >= 0);
                default:
                    throw new MinorOntopInternalBugException("Unexpected inequalityLabel:" + inequalityLabel);
            }
        }
        return super.buildTermAfterEvaluation(newTerms, termFactory, variableNullability);
    }

    @Override
    protected ImmutableExpression buildInequality(InequalityLabel inequalityLabel,
                                                  ImmutableList<? extends ImmutableTerm> subTerms,
                                                  TermFactory termFactory) {
        return termFactory.getDBNumericInequality(inequalityLabel, subTerms.get(0), subTerms.get(1));
    }
}
