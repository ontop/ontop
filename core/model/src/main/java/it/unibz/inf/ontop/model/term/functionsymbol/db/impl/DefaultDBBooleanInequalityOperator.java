package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel;
import it.unibz.inf.ontop.model.type.DBTermType;

public class DefaultDBBooleanInequalityOperator extends AbstractDBInequalityOperator {

    /**
     * TODO: type the input
     */
    protected DefaultDBBooleanInequalityOperator(InequalityLabel inequalityLabel,
                                                 DBTermType rootDBTermType, DBTermType dbBoolean) {
        super(inequalityLabel, "BOOL_" + inequalityLabel.name(), rootDBTermType, dbBoolean);
    }

    /**
     * TODO: allow it
     */
    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    @Override
    protected ImmutableExpression buildInequality(InequalityLabel inequalityLabel,
                                                  ImmutableList<? extends ImmutableTerm> subTerms,
                                                  TermFactory termFactory) {
        return termFactory.getDBBooleanInequality(inequalityLabel, subTerms.get(0), subTerms.get(1));
    }
}
