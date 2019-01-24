package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel;
import it.unibz.inf.ontop.model.type.DBTermType;

public class DefaultDBStringInequalityOperator extends AbstractDBInequalityOperator {

    /**
     * TODO: type the input
     */
    protected DefaultDBStringInequalityOperator(InequalityLabel inequalityLabel,
                                                DBTermType rootDBTermType, DBTermType dbBoolean) {
        super(inequalityLabel, "STR_" + inequalityLabel.name(), rootDBTermType, dbBoolean);
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
        return termFactory.getDBStringInequality(inequalityLabel, subTerms.get(0), subTerms.get(1));
    }
}
