package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.Optional;
import java.util.function.Function;

public class SparkSQLTimestampDenormFunctionSymbol extends AbstractDBTypeConversionFunctionSymbolImpl{

    private final DBTermType dbStringType;
    protected static final String TEMPLATE = "TO_TIMESTAMP(%s, 'yyyy-MM-dd HH:mm:ss.SSSxxx')";

    protected SparkSQLTimestampDenormFunctionSymbol(DBTermType dbTimestampType,DBTermType dbStringType) {
        super("timestampDenorm", dbTimestampType, dbStringType);
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
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {

        ImmutableFunctionalTerm replaceTTerm = termFactory.getDBReplace(terms.get(0),
                termFactory.getDBStringConstant("T"),
                termFactory.getDBStringConstant(" "));

        ImmutableFunctionalTerm replaceZTerm = termFactory.getDBReplace(replaceTTerm,
                termFactory.getDBStringConstant("Z"),
                termFactory.getDBStringConstant("+00:00"));

        return String.format(TEMPLATE, termConverter.apply(replaceZTerm));
    }

    @Override
    protected ImmutableTerm convertDBConstant(DBConstant constant, TermFactory termFactory) throws DBTypeConversionException {
        return termFactory.getImmutableFunctionalTerm(this, constant);
    }

    @Override
    protected boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return true;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }
}
