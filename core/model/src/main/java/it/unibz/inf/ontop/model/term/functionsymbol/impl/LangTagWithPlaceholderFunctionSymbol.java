package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.MetaRDFTermType;

/**
 * When the argument is not a literal, returns a special string
 */
public class LangTagWithPlaceholderFunctionSymbol extends AbstractLangTagLikeFunctionSymbol {

    public static String PLACEHOLDER = "not-a-literal";

    protected LangTagWithPlaceholderFunctionSymbol(MetaRDFTermType metaRDFTermType, DBTermType dbStringType) {
        super("LANG_TAG_WITH_PLACEHOLDER", metaRDFTermType, dbStringType);
    }

    @Override
    protected Constant defaultValueForNonLiteral(TermFactory termFactory) {
        return termFactory.getDBStringConstant(PLACEHOLDER);
    }

    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return false;
    }

    protected boolean enableIfElseNullLifting() {
        return true;
    }
}
