package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.function.Function;

/**
 * Function symbol for Dremio's Array Access operation `ARRAY[<key>]`.
 *
 */
public class DremioArrayAccessDBFunctionSymbol extends DefaultUntypedDBFunctionSymbol {

    protected DremioArrayAccessDBFunctionSymbol(DBTermType rootDBTermType) {
        super("ARRAY_ACCESS", 2, rootDBTermType);
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format(
                "%s[%s]",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1))
        );
    }

}
