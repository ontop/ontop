package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class DefaultNumberNormAsBooleanFunctionSymbol extends AbstractDBTypeConversionFunctionSymbolImpl {

    private final DBTermType numberType;

    protected DefaultNumberNormAsBooleanFunctionSymbol(DBTermType numberType, DBTermType stringType) {
        super(numberType + "2BooleanLexicalNorm", numberType, stringType);
        this.numberType = numberType;
    }

    @Override
    public Optional<DBTermType> getInputType() {
        return Optional.of(numberType);
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
     * All non-zero numbers are considered as true
     */
    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return true;
    }

    @Override
    protected ImmutableTerm convertDBConstant(DBConstant constant, TermFactory termFactory) throws DBTypeConversionException {
        return transformIntoDBCase(constant, termFactory)
                .simplify();
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

    @Override
    protected IncrementalEvaluation evaluateStrictEqWithNonNullConstant(ImmutableList<? extends ImmutableTerm> terms,
                                                                        NonNullConstant otherTerm, TermFactory termFactory,
                                                                        VariableNullability variableNullability) {
        ImmutableTerm term = terms.get(0);
        ImmutableExpression zeroEquality = termFactory.getDBNonStrictNumericEquality(term,
                termFactory.getDBIntegerConstant(0));
        String valueString = otherTerm.getValue();
        switch (valueString) {
            case "false":
                return zeroEquality.evaluate(variableNullability, true);
            case "true":
                return termFactory.getDBNot(zeroEquality)
                        .evaluate(variableNullability, true);
            default:
                return termFactory.getFalseOrNullFunctionalTerm(ImmutableList.of(termFactory.getDBIsNull(term)))
                        .evaluate(variableNullability, true);
        }
    }

    private Map.Entry<ImmutableExpression, ? extends ImmutableTerm> buildEntry(ImmutableTerm term, boolean b,
                                                                               TermFactory termFactory) {
        DBConstant zero = termFactory.getDBIntegerConstant(0);
        ImmutableExpression zeroEquality = termFactory.getDBNonStrictNumericEquality(term, zero);

        ImmutableExpression condition = b ? termFactory.getDBNot(zeroEquality) : zeroEquality;

        return Maps.immutableEntry(condition, termFactory.getXsdBooleanLexicalConstant(b));
    }

}
