package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

/**
 * Cannot be simplified {@code ---> } has to be evaluated by the DB engine
 *
 * Only suitable for DB terms
 */
public class DefaultDBNonStrictDefaultEqOperator extends AbstractDBNonStrictEqOperator {

    protected DefaultDBNonStrictDefaultEqOperator(DBTermType rootDBTermType, DBTermType dbBoolean) {
        super("NON_STRICT_EQ", rootDBTermType, dbBoolean);
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }
}
