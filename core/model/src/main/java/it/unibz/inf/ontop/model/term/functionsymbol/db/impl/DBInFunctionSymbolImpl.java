package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBInFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DBInFunctionSymbolImpl extends DBBooleanFunctionSymbolImpl implements DBInFunctionSymbol {

    protected DBInFunctionSymbolImpl(int arity, DBTermType rootDBTermType, DBTermType dbBooleanTermType) {
        super("DB_IN_" + arity, IntStream.range(0, arity)
                .mapToObj(i -> (TermType) rootDBTermType)
                .collect(ImmutableCollectors.toList()), dbBooleanTermType);
    }

    @Override
    protected boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    protected boolean tolerateNulls() {
        return true;
    }

    @Override
    public boolean blocksNegation() {
        return true;
    }

    @Override
    public ImmutableExpression negate(ImmutableList<? extends ImmutableTerm> subTerms, TermFactory termFactory) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return termConverter.apply(
                termFactory.getImmutableExpression(
                        termFactory.getDBFunctionSymbolFactory().getDBOr(getArity() - 1),
                        terms.stream()
                                .skip(1)
                                .map(t -> termFactory.getStrictEquality(terms.get(0), t))
                                .collect(ImmutableCollectors.toList())
                )
        );
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory, VariableNullability variableNullability) {
        if(newTerms.get(0).isNull())
            return termFactory.getNullConstant();

        //Remove duplicates
        ImmutableList<ImmutableTerm> newChildren = Stream.concat(
                Stream.of(newTerms.get(0)),
                newTerms.stream()
                        .skip(1)
                        .distinct()
        ).collect(ImmutableCollectors.toList());

        if(newChildren.size() <= 1) {
            //If the search term is NULL, this will evaluate to NULL. Otherwise it is FALSE.
            return termFactory.getBooleanIfElseNull(
                    termFactory.getDBIsNotNull(newTerms.get(0)),
                    termFactory.getIsTrue(termFactory.getDBBooleanConstant(false))
            );
        }
        if(newChildren.size() == 2) {
            //The pattern `"x" IN (anything)` can be replaced with `"x" = anything`.
            return termFactory.getStrictEquality(newChildren);
        }
        return termFactory.getImmutableExpression(termFactory.getDBFunctionSymbolFactory().getDBIn(newChildren.size()), newChildren);
    }
}
