package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.Optional;
import java.util.function.Function;

public class AbstractTimestampISODenormFunctionSymbol extends AbstractDBTypeConversionFunctionSymbolImpl {

    private final DBTermType dbStringType;

    protected AbstractTimestampISODenormFunctionSymbol(DBTermType timestampType, DBTermType dbStringType) {
        super("isoTimestampDenorm", dbStringType, timestampType);
        this.dbStringType = dbStringType;
    }

    @Override
    public Optional<DBTermType> getInputType() {
        return Optional.of(dbStringType);
    }

    @Override
    public boolean isTemporary() {
        return false;
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return true;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    protected ImmutableTerm buildTermFromFunctionalTerm(ImmutableFunctionalTerm subTerm, TermFactory termFactory, VariableNullability variableNullability) {
        if (subTerm.getFunctionSymbol() instanceof AbstractTimestampISONormFunctionSymbol) {
            return subTerm.getTerm(0);
        }
        return termFactory.getImmutableFunctionalTerm(this, ImmutableList.of(subTerm));
    }

    @Override
    protected DBConstant convertDBConstant(DBConstant constant, TermFactory termFactory) {
        throw new RuntimeException("TODO: implement timestamp denormalization");
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new RuntimeException("TODO: implement getNativeDBString for " + getClass());
    }
}
