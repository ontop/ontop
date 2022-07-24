package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermTypeFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.MetaRDFTermType;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;

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
        else if (subTerm instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm functionalTerm = (ImmutableFunctionalTerm) subTerm;
            FunctionSymbol functionSymbol = functionalTerm.getFunctionSymbol();

            if (functionSymbol instanceof RDFTermTypeFunctionSymbol) {
                ImmutableMap<DBConstant, RDFTermTypeConstant> conversionMap = ((RDFTermTypeFunctionSymbol)
                        functionalTerm.getFunctionSymbol()).getConversionMap();

                return simplifyIntoConjunction(conversionMap, functionalTerm.getTerm(0), termFactory, variableNullability);
            }
        }
        return super.buildTermAfterEvaluation(newTerms, termFactory, variableNullability);
    }

    private ImmutableTerm simplifyIntoConjunction(ImmutableMap<DBConstant, RDFTermTypeConstant> conversionMap,
                                                  ImmutableTerm term, TermFactory termFactory, VariableNullability variableNullability) {
        ImmutableList<ImmutableExpression> excludedMagicNumbers = conversionMap.entrySet().stream()
                .filter(e -> !e.getValue().getRDFTermType().isA(baseType))
                .map(Map.Entry::getKey)
                .map(n -> termFactory.getStrictNEquality(term, n))
                .distinct()
                .collect(ImmutableCollectors.toList());

        if (excludedMagicNumbers.size() == conversionMap.size())
            return termFactory.getFalseOrNullFunctionalTerm(
                    ImmutableList.of(termFactory.getDBIsNull(term)))
                    .simplify(variableNullability);

        if (excludedMagicNumbers.isEmpty())
            return termFactory.getTrueOrNullFunctionalTerm(
                    ImmutableList.of(termFactory.getDBIsNotNull(term)))
                    .simplify(variableNullability);

        return termFactory.getConjunction(excludedMagicNumbers)
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
