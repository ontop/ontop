package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.function.Function;

/**
 * Only simplifies itself when receiving a normalization function as input
 */
public class SQLServerTimestampISODenormFunctionSymbol extends AbstractTimestampISODenormFunctionSymbol {

    private static final String TEMPLATE = "CONVERT(%s,%s,127)";
    private final DBTermType timestampType;

    protected SQLServerTimestampISODenormFunctionSymbol(DBTermType timestampType, DBTermType dbStringType) {
        super(timestampType, dbStringType);
        this.timestampType = timestampType;
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format(TEMPLATE,
                timestampType.getCastName(),
                termConverter.apply(terms.get(0)));
    }

    /**
     * Does not simplify
     */
    @Override
    protected ImmutableTerm convertDBConstant(DBConstant constant, TermFactory termFactory) {
        return termFactory.getImmutableFunctionalTerm(this, constant);
    }
}