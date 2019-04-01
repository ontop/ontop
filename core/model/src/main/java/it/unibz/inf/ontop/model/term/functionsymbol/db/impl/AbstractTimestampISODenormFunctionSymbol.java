package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.Optional;
import java.util.function.Function;

public class AbstractTimestampISODenormFunctionSymbol extends AbstractDBTypeConversionFunctionSymbolImpl {

    private final static String CAST_TEMPLATE = "CAST(%s AS %s)";
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

    protected ImmutableTerm buildTermFromFunctionalTerm(ImmutableFunctionalTerm subTerm, TermFactory termFactory,
                                                        VariableNullability variableNullability) {
        FunctionSymbol subTermFunctionSymbol = subTerm.getFunctionSymbol();
        // TODO: avoid relying on a concrete class
        if (subTermFunctionSymbol instanceof AbstractTimestampISONormFunctionSymbol) {
            DBTermType targetType = getTargetType();
            ImmutableTerm subSubTerm = subTerm.getTerm(0);

            return ((AbstractTimestampISONormFunctionSymbol) subTermFunctionSymbol).getInputType()
                    // There might be several DB datetime types
                    .filter(t -> !t.equals(targetType))
                    .map(t -> (ImmutableTerm) termFactory.getDBCastFunctionalTerm(t, targetType, subSubTerm))
                    .orElse(subSubTerm);
        }
        return termFactory.getImmutableFunctionalTerm(this, ImmutableList.of(subTerm));
    }

    @Override
    protected ImmutableTerm convertDBConstant(DBConstant constant, TermFactory termFactory) {
        String newString = constant.getValue()
                .replace("T", " ")
                //.replaceFirst("([+-]\\d\\d:\\d\\d)", " $0")
                //.replace("Z", " +00:00");
                .replace("Z", "+00:00");

        return termFactory.getDBConstant(newString, getTargetType());
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        ImmutableFunctionalTerm replaceTTerm = termFactory.getDBReplace(terms.get(0),
                termFactory.getDBStringConstant("T"),
                termFactory.getDBStringConstant(" "));

        ImmutableFunctionalTerm newTerm = termFactory.getDBReplace(replaceTTerm,
                termFactory.getDBStringConstant("Z"),
                termFactory.getDBStringConstant("+00:00"));

        return String.format(CAST_TEMPLATE, termConverter.apply(newTerm), getTargetType().getCastName());
    }
}
