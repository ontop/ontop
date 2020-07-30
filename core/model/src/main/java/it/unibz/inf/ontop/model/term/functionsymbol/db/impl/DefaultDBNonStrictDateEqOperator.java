package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.type.DBTermType;

public class DefaultDBNonStrictDateEqOperator extends AbstractDBNonStrictEqOperator {

    /**
     * TODO: type the input
     */
    protected DefaultDBNonStrictDateEqOperator(DBTermType rootDBTermType, DBTermType dbBoolean) {
        super("DATE_NON_STRICT_EQ", rootDBTermType, dbBoolean);
    }

    /**
     * TODO: allow it
     */
    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }
}
