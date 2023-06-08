package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.function.Function;

public class CDataDynamoDBSQLDBIsNullOrNotFunctionSymbol extends AbstractDBIsNullOrNotFunctionSymbol {

    private final String template;

    protected CDataDynamoDBSQLDBIsNullOrNotFunctionSymbol(boolean isNull, DBTermType dbBooleanTermType, DBTermType rootDBTermType) {
        super(isNull, dbBooleanTermType, rootDBTermType);

        this.template = isNull
                ?  "(ISNULL(CAST(%s as VARCHAR), 'TEMPTEMP') = 'TEMPTEMP')"
                :  "(ISNULL(CAST(%s as VARCHAR), 'TEMPTEMP') != 'TEMPTEMP')";

        /*this.template = isNull
                ?  "%s IS NULL"
                :  "%S IS NOT NULL";*/
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format(template, termConverter.apply(terms.get(0)));
    }
}
