package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class DefaultBooleanNormFunctionSymbol extends AbstractDBTypeConversionFunctionSymbolImpl {

    private final DBTermType booleanType;

    protected DefaultBooleanNormFunctionSymbol(DBTermType booleanType, DBTermType stringType) {
        super("booleanLexicalNorm", booleanType, stringType);
        this.booleanType = booleanType;
    }

    @Override
    public Optional<DBTermType> getInputType() {
        return Optional.of(booleanType);
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
     * Here we assume that the DB has only one way to represent the boolean value as a string
     */
    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return true;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return true;
    }

    @Override
    protected DBConstant convertDBConstant(DBConstant constant, TermFactory termFactory) throws DBTypeConversionException {
        ImmutableTerm newTerm = transformIntoDBCase(constant, termFactory)
                .simplify();
        if (newTerm instanceof DBConstant)
            return (DBConstant) newTerm;

        throw new DBTypeConversionException("Problem while normalizing " + constant + "(value: " + newTerm + ")");
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        ImmutableFunctionalTerm newFunctionalTerm = transformIntoDBCase(terms.get(0), termFactory);
        return termConverter.apply(newFunctionalTerm);
    }

    protected ImmutableFunctionalTerm transformIntoDBCase(ImmutableTerm subTerm, TermFactory termFactory) {
        return termFactory.getDBCase(Stream.of(
                buildEntry(subTerm, true, termFactory),
                buildEntry(subTerm, false, termFactory)),
                termFactory.getNullConstant(), false);
    }


    private Map.Entry<ImmutableExpression, ? extends ImmutableTerm> buildEntry(ImmutableTerm term, boolean b,
                                                                               TermFactory termFactory) {
        return Maps.immutableEntry(termFactory.getStrictEquality(term, termFactory.getDBBooleanConstant(b)),
                termFactory.getXsdBooleanLexicalConstant(b));
    }

}
