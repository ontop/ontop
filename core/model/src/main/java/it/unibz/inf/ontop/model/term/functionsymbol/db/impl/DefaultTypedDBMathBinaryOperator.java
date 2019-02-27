package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBMathBinaryOperator;
import it.unibz.inf.ontop.model.term.functionsymbol.impl.FunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;

import java.util.Optional;
import java.util.function.Function;

public class DefaultTypedDBMathBinaryOperator extends FunctionSymbolImpl implements DBMathBinaryOperator {

    private final String template;
    private final DBTermType dbNumericType;

    protected DefaultTypedDBMathBinaryOperator(String operatorString, DBTermType dbNumericType) {
        super(dbNumericType.toString() + operatorString, ImmutableList.of(dbNumericType, dbNumericType));
        this.template = "(%s " + operatorString + " %s)";
        this.dbNumericType = dbNumericType;
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
        return Optional.of(TermTypeInference.declareTermType(dbNumericType));
    }

    /**
     * TODO: could be allowed in the future
     */
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
