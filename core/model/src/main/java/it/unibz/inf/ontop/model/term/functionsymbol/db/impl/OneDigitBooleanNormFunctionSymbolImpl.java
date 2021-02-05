package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

/**
 * The JDBC driver of DB2 may return the boolean as "1" or "0"
 */
public class OneDigitBooleanNormFunctionSymbolImpl extends DefaultBooleanNormFunctionSymbol {

    protected OneDigitBooleanNormFunctionSymbolImpl(DBTermType booleanType, DBTermType stringType) {
        super(booleanType, stringType);
    }

    @Override
    protected DBConstant convertDBConstant(DBConstant constant, TermFactory termFactory) throws DBTypeConversionException {
        /*
         * Special cases
         */
        if (constant.getType().getCategory() == DBTermType.Category.BOOLEAN) {
            switch (constant.getValue()) {
                case "0":
                    return termFactory.getXsdBooleanLexicalConstant(false);
                case "1":
                    return termFactory.getXsdBooleanLexicalConstant(true);
            }
        }
        return super.convertDBConstant(constant, termFactory);
    }
}
