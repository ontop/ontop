package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBNotFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.TrueOrNullFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Function;

public class DefaultDBNotFunctionSymbol extends DBBooleanFunctionSymbolImpl implements DBNotFunctionSymbol {

    private final String prefix;

    protected DefaultDBNotFunctionSymbol(@Nonnull String nameInDialect, DBTermType dbBooleanTermType) {
        super(nameInDialect, ImmutableList.of(dbBooleanTermType), dbBooleanTermType);
        prefix = String.format("%s ", nameInDialect);
    }

    @Override
    public boolean blocksNegation() {
        return false;
    }

    @Override
    public ImmutableExpression negate(ImmutableList<? extends ImmutableTerm> subTerms, TermFactory termFactory) {
        return (ImmutableExpression) subTerms.get(0);
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter,
                                    TermFactory termFactory) {
        return inBrackets(prefix + termConverter.apply(terms.get(0)));
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return true;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return true;
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     TermFactory termFactory, VariableNullability variableNullability) {

        ImmutableTerm newTerm = newTerms.get(0);
        if (newTerm instanceof Constant)
            return newTerm.isNull()
                    ? newTerm
                    : termFactory.getDBBooleanConstant(newTerm.equals(termFactory.getDBBooleanConstant(false)));

        if (newTerm instanceof ImmutableExpression)
            return ((ImmutableExpression) newTerm).negate(termFactory);
        else if (newTerm instanceof Variable)
            // TODO: shall we use NOT(IS_TRUE(...) instead)?
            return termFactory.getStrictEquality(newTerm, termFactory.getDBBooleanConstant(false));
        else
            throw new MinorOntopInternalBugException("NOT was expecting an expression as parameter");
    }

    @Override
    protected boolean tolerateNulls() {
        return false;
    }

    /**
     * Requires its arguments to be expressions
     */
    @Override
    protected ImmutableList<? extends ImmutableTerm> transformIntoRegularArguments(
            ImmutableList<? extends NonFunctionalTerm> arguments, TermFactory termFactory) {
        return arguments.stream()
                .map(termFactory::getIsTrue)
                .collect(ImmutableCollectors.toList());
    }

    @Override
    public ImmutableTerm simplify2VL(ImmutableList<? extends ImmutableTerm> terms, TermFactory termFactory,
                                     VariableNullability variableNullability) {
        ImmutableTerm simplifedTerm = simplify(terms, termFactory, variableNullability);

        if (simplifedTerm.isNull())
            return termFactory.getDBBooleanConstant(false);

        Optional<ImmutableExpression> optionalNewNotExpression = Optional.of(simplifedTerm)
                .filter(t -> t instanceof ImmutableExpression)
                .map(e -> (ImmutableExpression) e)
                .filter(e -> e.getFunctionSymbol().equals(this));

        return optionalNewNotExpression
                .map(n -> Optional.of(n)
                        .map(e -> e.getTerm(0))
                        .filter(t -> t instanceof ImmutableExpression)
                        /*
                         * NOT(TRUE_OR_NULL(...)) is false under 2VL.
                         */
                        .filter(t -> ((ImmutableExpression) t).getFunctionSymbol() instanceof TrueOrNullFunctionSymbol)
                        .map(t -> (ImmutableTerm) termFactory.getDBBooleanConstant(false))
                        .orElse(simplifedTerm))
                .orElseGet(() -> Optional.of(simplifedTerm)
                        .filter(t -> t instanceof ImmutableExpression)
                        .map(t -> (ImmutableExpression) t)
                        // Makes sure the 2VL is evaluated for the simplified expression
                        .map(t -> t.evaluate2VL(variableNullability).getTerm())
                        .orElse(simplifedTerm));
    }
}
