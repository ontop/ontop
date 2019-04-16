package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolSerializer;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.function.Function;

public class DBContainsFunctionSymbolImpl extends DBBooleanFunctionSymbolImpl {

    private final DBFunctionSymbolSerializer serializer;

    protected DBContainsFunctionSymbolImpl(DBTermType abstractRootTermType, DBTermType dbBooleanTermType,
                                           DBFunctionSymbolSerializer serializer) {
        super("DB_CONTAINS",
                ImmutableList.of(abstractRootTermType, abstractRootTermType),
                dbBooleanTermType);
        this.serializer = serializer;
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
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return true;
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter,
                                    TermFactory termFactory) {
        return serializer.getNativeDBString(terms, termConverter, termFactory);
    }

    @Override
    protected boolean tolerateNulls() {
        return false;
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory,
                                                     VariableNullability variableNullability) {
        if (newTerms.stream().allMatch(t -> t instanceof DBConstant)) {
            String mainString = ((DBConstant) newTerms.get(0)).getValue();
            String searchString = ((DBConstant) newTerms.get(1)).getValue();

            return termFactory.getDBBooleanConstant(mainString.contains(searchString));
        }

        return super.buildTermAfterEvaluation(newTerms, termFactory, variableNullability);
    }
}
