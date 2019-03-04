package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.NonDeterministicDBFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.UUID;
import java.util.function.Function;

public class DefaultNonDeterministicNullaryFunctionSymbol extends AbstractTypedDBFunctionSymbol implements NonDeterministicDBFunctionSymbol {

    private final String nameInDialect;
    private final UUID uuid;

    protected DefaultNonDeterministicNullaryFunctionSymbol(String nameInDialect, UUID uuid, DBTermType targetType) {
        super(nameInDialect + uuid, ImmutableList.of(), targetType);
        this.nameInDialect = nameInDialect;
        this.uuid = uuid;
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter,
                                    TermFactory termFactory) {
        return nameInDialect + "()";
    }

    /**
     * Non-deterministic so non-injective
     */
    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }
}
