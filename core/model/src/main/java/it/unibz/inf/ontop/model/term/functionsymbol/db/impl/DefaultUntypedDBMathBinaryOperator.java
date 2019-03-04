package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBMathBinaryOperator;
import it.unibz.inf.ontop.model.term.functionsymbol.impl.FunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;

import java.util.Optional;
import java.util.function.Function;

/**
 * Mostly for the native query parser (for source queries in the mapping),
 * which is not able to infer the type of its input.
 *
 * Ideally, these operators should be replaced by typed ones later on in the mapping process, once the input type
 * can be inferred.
 *
 */
public class DefaultUntypedDBMathBinaryOperator extends FunctionSymbolImpl implements DBMathBinaryOperator {

    private final String template;

    protected DefaultUntypedDBMathBinaryOperator(String operatorString, DBTermType rootDBTermType) {
        super("UNTYPED" + operatorString, ImmutableList.of(rootDBTermType, rootDBTermType));
        this.template = "(%s " + operatorString + " %s)";
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format(template, termConverter.apply(terms.get(0)), termConverter.apply(terms.get(1)));
    }

    @Override
    protected boolean tolerateNulls() {
        return false;
    }

    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return false;
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.empty();
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    /**
     * By default, to be overridden when necessary
     */
    @Override
    public boolean isPreferringToBePostProcessedOverBeingBlocked() {
        return false;
    }
}
