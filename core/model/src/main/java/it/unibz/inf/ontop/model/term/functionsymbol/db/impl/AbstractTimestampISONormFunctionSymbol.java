package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolSerializer;
import it.unibz.inf.ontop.model.type.DBTermType;
import org.eclipse.rdf4j.model.datatypes.XMLDatatypeUtil;

import java.util.Optional;
import java.util.function.Function;

public abstract class AbstractTimestampISONormFunctionSymbol extends AbstractDBTypeConversionFunctionSymbolImpl {

    protected final DBTermType timestampType;
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

    @Override
    protected IncrementalEvaluation evaluateStrictEqWithNonNullConstant(ImmutableList<? extends ImmutableTerm> terms,
                                                                        NonNullConstant otherTerm, TermFactory termFactory,
                                                                        VariableNullability variableNullability) {
        if (!XMLDatatypeUtil.isValidDateTime(otherTerm.getValue()))
            return termFactory.getBooleanIfElseNull(termFactory.getDBIsNotNull(terms.get(0)),
                    termFactory.getIsTrue(termFactory.getDBBooleanConstant(false)))
                    .evaluate(variableNullability, true);

        return evaluateStringEqWithValidDatetimeConstantString(terms.get(0), otherTerm, termFactory, variableNullability);
    }

    protected IncrementalEvaluation evaluateStringEqWithValidDatetimeConstantString(ImmutableTerm subTerm,
                                                                                  NonNullConstant otherTerm,
                                                                                  TermFactory termFactory,
                                                                                  VariableNullability variableNullability) {
        return IncrementalEvaluation.declareSameExpression();
    }
}
