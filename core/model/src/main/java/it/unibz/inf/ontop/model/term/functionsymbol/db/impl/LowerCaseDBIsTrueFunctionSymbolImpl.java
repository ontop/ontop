package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;


import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

/**
 * The JDBC driver of Denodo may return the boolean as "true" or "false" (lower case)
 */
public class LowerCaseDBIsTrueFunctionSymbolImpl extends DefaultDBIsTrueFunctionSymbol {

    protected LowerCaseDBIsTrueFunctionSymbolImpl(DBTermType dbBooleanTermType) {
        super(dbBooleanTermType);
    }

    @Override
    protected boolean evaluateDBConstant(DBConstant constant, TermFactory termFactory) {
        /*
         * Special cases
         */
        if (constant.getType().getCategory() == DBTermType.Category.BOOLEAN) {
            switch (constant.getValue()) {
                case "false":
                    return false;
                case "true":
                    return true;
            }
        }
        return super.evaluateDBConstant(constant, termFactory);
    }
}

