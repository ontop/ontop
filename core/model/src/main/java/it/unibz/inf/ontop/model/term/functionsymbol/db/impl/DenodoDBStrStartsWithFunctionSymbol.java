package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.function.Function;

public class DenodoDBStrStartsWithFunctionSymbol extends DefaultDBStrStartsWithFunctionSymbol {

    protected DenodoDBStrStartsWithFunctionSymbol(DBTermType metaDBTermType, DBTermType dbBooleanTermType) {
        super(metaDBTermType, dbBooleanTermType);
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        ImmutableTerm secondTerm = terms.get(1);

        // TODO: use a non-strict equality
        return termConverter.apply(
                termFactory.getStrictEquality(
                        termFactory.getDBSubString3(
                                terms.get(0),
                                termFactory.getDBIntegerConstant(1),
                                termFactory.getDBCharLength(secondTerm)),
                        secondTerm));
    }
}
