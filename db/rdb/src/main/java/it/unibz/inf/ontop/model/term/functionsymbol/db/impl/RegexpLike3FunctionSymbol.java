package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBBooleanFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RegexpLike3FunctionSymbol extends SimpleTypedDBFunctionSymbolImpl
        implements DBBooleanFunctionSymbol {

    private static final String REGEXP_LIKE_STR = "REGEXP_LIKE";
    private static final String FUNCTIONAL_TEMPLATE = "%s(%s)";


    protected RegexpLike3FunctionSymbol(DBTermType dbBooleanType, DBTermType rootDBTermType) {
        super(REGEXP_LIKE_STR, 3, dbBooleanType, false, rootDBTermType,
                RegexpLike3FunctionSymbol::serialize);
    }

    @Override
    public boolean blocksNegation() {
        return true;
    }

    @Override
    public ImmutableExpression negate(ImmutableList<? extends ImmutableTerm> subTerms, TermFactory termFactory) {
        throw new UnsupportedOperationException();
    }

    public static String serialize(ImmutableList<? extends ImmutableTerm> terms,
                                                       Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        /*
         * Normalizes the flag
         *   - DOT_ALL: s -> n
         */
        ImmutableTerm flagTerm = termFactory.getDBReplace(terms.get(2),
                termFactory.getDBStringConstant("s"),
                termFactory.getDBStringConstant("n"))
                .simplify();

        String parameterString = Stream.of(terms.get(0), terms.get(1), flagTerm)
                .map(termConverter)
                .collect(Collectors.joining(","));
        return String.format(FUNCTIONAL_TEMPLATE, REGEXP_LIKE_STR, parameterString);
    }
}
