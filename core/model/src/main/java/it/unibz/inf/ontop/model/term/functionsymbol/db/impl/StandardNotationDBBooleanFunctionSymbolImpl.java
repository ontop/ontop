package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;

import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class StandardNotationDBBooleanFunctionSymbolImpl extends DBBooleanFunctionSymbolImpl {

    private final String nameInDialect;
    private static final String FUNCTIONAL_TEMPLATE = "%s(%s)";

    protected StandardNotationDBBooleanFunctionSymbolImpl(String nameInDialect, ImmutableList<TermType> expectedBaseTypes,
                                                          DBTermType dbBooleanTermType) {
        super(nameInDialect + expectedBaseTypes.size(), expectedBaseTypes, dbBooleanTermType);
        this.nameInDialect = nameInDialect;
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format(FUNCTIONAL_TEMPLATE, nameInDialect,
                terms.stream()
                        .map(termConverter::apply)
                        .collect(Collectors.joining(",")));
    }
}
