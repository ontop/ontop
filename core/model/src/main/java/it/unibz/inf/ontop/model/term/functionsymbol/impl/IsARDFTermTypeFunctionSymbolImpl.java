package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermTypeFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.MetaRDFTermType;
import it.unibz.inf.ontop.model.type.RDFTermType;

import java.util.Map;
import java.util.stream.Stream;

/**
 * TODO: find a better name!
 */
public class IsARDFTermTypeFunctionSymbolImpl extends BooleanFunctionSymbolImpl {

    private final RDFTermType baseType;

    protected IsARDFTermTypeFunctionSymbolImpl(MetaRDFTermType metaRDFTermType, DBTermType dbBooleanTermType,
                                               RDFTermType baseType) {
        super("IS_A_" + baseType.toString().toUpperCase(), ImmutableList.of(metaRDFTermType), dbBooleanTermType);
        this.baseType = baseType;
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return true;
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     TermFactory termFactory, VariableNullability variableNullability) {
        ImmutableTerm subTerm = newTerms.get(0);

        if (subTerm instanceof RDFTermTypeConstant) {
            RDFTermType firstType = ((RDFTermTypeConstant) subTerm).getRDFTermType();
            return termFactory.getDBBooleanConstant(firstType.isA(baseType));
        }
        else if ((subTerm instanceof ImmutableFunctionalTerm)
                && ((ImmutableFunctionalTerm) subTerm).getFunctionSymbol() instanceof RDFTermTypeFunctionSymbol) {
            ImmutableFunctionalTerm functionalTerm = ((ImmutableFunctionalTerm) subTerm);

            ImmutableMap<DBConstant, RDFTermTypeConstant> conversionMap = ((RDFTermTypeFunctionSymbol)
                    functionalTerm.getFunctionSymbol()).getConversionMap();

            return simplifyIntoConjunction(conversionMap, functionalTerm.getTerm(0), termFactory, variableNullability);

        }
        return super.buildTermAfterEvaluation(newTerms, termFactory, variableNullability);
    }

    private ImmutableTerm simplifyIntoConjunction(ImmutableMap<DBConstant, RDFTermTypeConstant> conversionMap,
                                                  ImmutableTerm term, TermFactory termFactory, VariableNullability variableNullability) {
        Stream<ImmutableExpression> excludedMagicNumbers = conversionMap.entrySet().stream()
                .filter(e -> !e.getValue().getRDFTermType().isA(baseType))
                .map(Map.Entry::getKey)
                .map(n -> termFactory.getStrictNEquality(term, n));

        return termFactory.getConjunction(Stream.concat(
                    Stream.of(termFactory.getDBIsNotNull(term)),
                    excludedMagicNumbers))
                .get()
                .simplify(variableNullability);
    }

    @Override
    public boolean blocksNegation() {
        return true;
    }

    @Override
    public ImmutableExpression negate(ImmutableList<? extends ImmutableTerm> subTerms, TermFactory termFactory) {
        return termFactory.getDBNot(termFactory.getImmutableExpression(this, subTerms));
    }

    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return false;
    }

    @Override
    protected boolean tolerateNulls() {
        return false;
    }
}
