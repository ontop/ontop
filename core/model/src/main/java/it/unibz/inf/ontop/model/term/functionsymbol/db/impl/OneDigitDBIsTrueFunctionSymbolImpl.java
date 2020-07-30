package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

/**
 * The JDBC driver of DB2 may return the boolean as "0" or "1"
 */
public class OneDigitDBIsTrueFunctionSymbolImpl extends DefaultDBIsTrueFunctionSymbol {

    protected OneDigitDBIsTrueFunctionSymbolImpl(DBTermType dbBooleanTermType) {
        super(dbBooleanTermType);
    }

    @Override
    protected boolean evaluateDBConstant(DBConstant constant, TermFactory termFactory) {
        /*
         * Special cases
         */
        if (constant.getType().getCategory() == DBTermType.Category.BOOLEAN) {
            switch (constant.getValue()) {
                case "0":
                    return false;
                case "1":
                    return true;
            }
        }
        return super.evaluateDBConstant(constant, termFactory);
    }
}
