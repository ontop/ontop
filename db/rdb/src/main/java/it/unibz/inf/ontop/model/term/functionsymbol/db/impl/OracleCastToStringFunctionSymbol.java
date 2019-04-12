package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class OracleCastToStringFunctionSymbol extends AbstractSimpleDBCastFunctionSymbol {

    private static final String TEMPLATE = "TO_CHAR(%s)";

    protected OracleCastToStringFunctionSymbol(@Nonnull DBTermType inputBaseType, DBTermType targetType) {
        super(inputBaseType, targetType);
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format(TEMPLATE, termConverter.apply(terms.get(0)));
    }
}
