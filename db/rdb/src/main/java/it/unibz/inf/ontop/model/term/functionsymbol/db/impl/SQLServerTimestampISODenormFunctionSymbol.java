package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.Optional;
import java.util.function.Function;

/**
 * Only simplifies itself when receiving a normalization function as input
 */
public class SQLServerTimestampISODenormFunctionSymbol extends AbstractTypedDBFunctionSymbol implements DBTypeConversionFunctionSymbol {

    private static final String TEMPLATE = "CONVERT(%s,%s,127)";
    private final DBTermType timestampType;
    private final DBTermType dbStringType;

    protected SQLServerTimestampISODenormFunctionSymbol(DBTermType timestampType, DBTermType dbStringType) {
        super("sqlServerTimestampDenorm", ImmutableList.of(dbStringType), timestampType);
        this.timestampType = timestampType;
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
        return false;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format(TEMPLATE,
                timestampType.getName(),
                termConverter.apply(terms.get(0)));
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory,
                                                     VariableNullability variableNullability) {
        ImmutableTerm subTerm = newTerms.get(0);

        // TODO: avoid relying on a concrete class
        if ((subTerm instanceof ImmutableFunctionalTerm)
                && (((ImmutableFunctionalTerm) subTerm).getFunctionSymbol() instanceof AbstractTimestampISONormFunctionSymbol)) {
            ImmutableFunctionalTerm functionalSubTerm = (ImmutableFunctionalTerm) subTerm;
            DBTermType targetType = getTargetType();
            ImmutableTerm subSubTerm = functionalSubTerm.getTerm(0);

            return ((AbstractTimestampISONormFunctionSymbol) functionalSubTerm.getFunctionSymbol()).getInputType()
                    // There might be several DB datetime types
                    .filter(t -> !t.equals(targetType))
                    .map(t -> (ImmutableTerm) termFactory.getDBCastFunctionalTerm(t, targetType, subSubTerm))
                    .orElse(subSubTerm);
        }

        return super.buildTermAfterEvaluation(newTerms, termFactory, variableNullability);
    }
}
