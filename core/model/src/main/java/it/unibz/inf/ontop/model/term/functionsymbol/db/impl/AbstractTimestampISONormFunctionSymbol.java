package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolSerializer;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.Optional;
import java.util.function.Function;

public abstract class AbstractTimestampISONormFunctionSymbol extends AbstractDBTypeConversionFunctionSymbolImpl {

    private final DBTermType timestampType;
    private final DBFunctionSymbolSerializer serializer;

    protected AbstractTimestampISONormFunctionSymbol(DBTermType timestampType, DBTermType dbStringType,
                                                     DBFunctionSymbolSerializer serializer) {
        super("iso" + timestampType.getName(), timestampType, dbStringType);
        this.timestampType = timestampType;
        this.serializer = serializer;
    }

    @Override
    public Optional<DBTermType> getInputType() {
        return Optional.of(timestampType);
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

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return serializer.getNativeDBString(terms, termConverter, termFactory);
    }
}
