package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.Optional;
import java.util.function.Function;

public class OracleDateDenormFunctionSymbol extends AbstractDBTypeConversionFunctionSymbolImpl {

    private final DBTermType dbStringType;
    private static final String TEMPLATE = "TO_DATE(%s,'YYYY-MM-DD')";

    protected OracleDateDenormFunctionSymbol(DBTermType dbStringType, DBTermType dbDateType) {
        super("dateDenorm", dbStringType, dbDateType);
        this.dbStringType = dbStringType;
    }

    /**
     * No optimization
     */
    @Override
    protected ImmutableTerm convertDBConstant(DBConstant constant, TermFactory termFactory) throws DBTypeConversionException {
        return termFactory.getImmutableFunctionalTerm(this, constant);
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

    /**
     * Assumption: a given Oracle instance always use the DATE value
     */
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
        return String.format(TEMPLATE, termConverter.apply(terms.get(0)));
    }
}
