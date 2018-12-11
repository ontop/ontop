package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermTypeFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.MetaRDFTermType;
import it.unibz.inf.ontop.model.type.RDFTermType;

import java.util.Map;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.term.functionsymbol.BooleanExpressionOperation.IS_NOT_NULL;
import static it.unibz.inf.ontop.model.term.functionsymbol.BooleanExpressionOperation.NEQ;
import static it.unibz.inf.ontop.model.term.functionsymbol.BooleanExpressionOperation.NOT;

/**
 * TODO: find a better name!
 */
public class IsARDFTermTypeFunctionSymbolImpl extends BooleanFunctionSymbolImpl {

    private final RDFTermType baseType;

    protected IsARDFTermTypeFunctionSymbolImpl(MetaRDFTermType metaRDFTermType, DBTermType dbBooleanTermType,
                                               RDFTermType baseType) {
        super("IS_A" + baseType.toString().toUpperCase(), ImmutableList.of(metaRDFTermType), dbBooleanTermType);
        this.baseType = baseType;
    }

    @Override
    public boolean isInjective(ImmutableList<? extends ImmutableTerm> arguments, ImmutableSet<Variable> nonNullVariables) {
        return false;
    }

    @Override
    public boolean canBePostProcessed() {
        return true;
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     boolean isInConstructionNodeInOptimizationPhase,
                                                     TermFactory termFactory) {
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

            return simplifyIntoConjunction(conversionMap, functionalTerm.getTerm(0), termFactory);

        }
        return termFactory.getImmutableFunctionalTerm(this, newTerms);
    }

    private ImmutableTerm simplifyIntoConjunction(ImmutableMap<DBConstant, RDFTermTypeConstant> conversionMap,
                                                        ImmutableTerm term, TermFactory termFactory) {
        Stream<ImmutableExpression> excludedMagicNumbers = conversionMap.entrySet().stream()
                .filter(e -> !e.getValue().getRDFTermType().isA(baseType))
                .map(Map.Entry::getKey)
                .map(n -> termFactory.getImmutableExpression(NEQ, term, n));

        return termFactory.getConjunction(Stream.concat(
                    Stream.of(termFactory.getImmutableExpression(IS_NOT_NULL, term)),
                    excludedMagicNumbers))
                .get()
                .simplify(false);
    }

    @Override
    public boolean blocksNegation() {
        return false;
    }

    @Override
    public ImmutableExpression negate(ImmutableList<? extends ImmutableTerm> subTerms, TermFactory termFactory) {
        return termFactory.getImmutableExpression(NOT, termFactory.getImmutableExpression(this, subTerms));
    }
}
