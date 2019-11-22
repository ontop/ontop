package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

/**
 * The JDBC driver of Postgres may return the boolean as "t" or "f"
 */
public class OneLetterDBIsTrueFunctionSymbolImpl extends DefaultDBIsTrueFunctionSymbol {

    protected OneLetterDBIsTrueFunctionSymbolImpl(DBTermType dbBooleanTermType) {
        super(dbBooleanTermType);
    }

    @Override
    protected boolean evaluateDBConstant(DBConstant constant, TermFactory termFactory) {
        /*
         * Special cases
         */
        if (constant.getType().getCategory() == DBTermType.Category.BOOLEAN) {
            switch (constant.getValue()) {
                case "f":
                    return false;
                case "t":
                    return true;
            }
        }
        return super.evaluateDBConstant(constant, termFactory);
    }
}
