package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

/**
 * The JDBC driver of Postgres may return the boolean as "t" or "f"
 */
public class OneLetterBooleanNormFunctionSymbolImpl extends DefaultBooleanNormFunctionSymbol {

    protected OneLetterBooleanNormFunctionSymbolImpl(DBTermType booleanType, DBTermType stringType) {
        super(booleanType, stringType);
    }

    @Override
    protected DBConstant convertDBConstant(DBConstant constant, TermFactory termFactory) throws DBTypeConversionException {
        /*
         * Special cases
         */
        if (constant.getType().getCategory() == DBTermType.Category.BOOLEAN) {
            switch (constant.getValue()) {
                case "f":
                    return termFactory.getXsdBooleanLexicalConstant(false);
                case "t":
                    return termFactory.getXsdBooleanLexicalConstant(true);
            }
        }
        return super.convertDBConstant(constant, termFactory);
    }
}
