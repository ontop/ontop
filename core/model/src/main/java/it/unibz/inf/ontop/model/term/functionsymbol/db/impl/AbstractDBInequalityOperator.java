package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.function.Function;

import static it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel.*;


public abstract class AbstractDBInequalityOperator extends DBBooleanFunctionSymbolImpl {

    private final String template;
    protected final InequalityLabel inequalityLabel;

    protected AbstractDBInequalityOperator(InequalityLabel inequalityLabel, String functionSymbolName,
                                           DBTermType rootDBTermType,
                                           DBTermType dbBoolean) {
        super(functionSymbolName, ImmutableList.of(rootDBTermType, rootDBTermType), dbBoolean);
        this.inequalityLabel = inequalityLabel;
        this.template = "(%s " + inequalityLabel.getMathString() + " %s)";
    }

    /**
     * Can be overridden
     */
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
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public boolean blocksNegation() {
        return false;
    }

    @Override
    public ImmutableExpression negate(ImmutableList<? extends ImmutableTerm> subTerms, TermFactory termFactory) {
        switch (inequalityLabel) {
            case LT:
                return buildInequality(GTE, subTerms, termFactory);
            case LTE:
                return buildInequality(GT, subTerms, termFactory);
            case GT:
                return buildInequality(LTE, subTerms, termFactory);
            case GTE:
                return buildInequality(LT, subTerms, termFactory);
            default:
                throw new MinorOntopInternalBugException("Unexpected inequality label: " + inequalityLabel);
        }
    }

    protected abstract ImmutableExpression buildInequality(InequalityLabel inequalityLabel,
                                                           ImmutableList<? extends ImmutableTerm> subTerms,
                                                           TermFactory termFactory);


}
