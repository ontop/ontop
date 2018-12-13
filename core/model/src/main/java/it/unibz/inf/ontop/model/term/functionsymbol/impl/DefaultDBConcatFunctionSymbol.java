package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.functionsymbol.DBConcatFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.stream.IntStream;

public class DefaultDBConcatFunctionSymbol extends AbstractTypedDBFunctionSymbol implements DBConcatFunctionSymbol {

    private static final String FUNCTIONAL_TEMPLATE = "%s(%s)";
    private final String nameInDialect;

    protected DefaultDBConcatFunctionSymbol(String nameInDialect, int arity, DBTermType dbStringType, DBTermType rootDBTermType) {
        super(nameInDialect + arity,
                // No restriction on the input types TODO: check if OK
                IntStream.range(0, arity)
                .boxed()
                .map(i -> (TermType) rootDBTermType)
                .collect(ImmutableCollectors.toList()), dbStringType);
        this.nameInDialect = nameInDialect;
    }

    @Override
    public boolean isInjective(ImmutableList<? extends ImmutableTerm> arguments, ImmutableSet<Variable> nonNullVariables) {
        return arguments.stream()
                .filter(t -> !(t instanceof Constant))
                .count() <= 1;
    }

    /**
     * TODO: allow post-processing
     * @param arguments
     */
    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    @Override
    public String getNativeDBString(ImmutableList<String> termStrings) {
        String parameterString = String.join(",", termStrings);
        return String.format(FUNCTIONAL_TEMPLATE, nameInDialect, parameterString);
    }
}
