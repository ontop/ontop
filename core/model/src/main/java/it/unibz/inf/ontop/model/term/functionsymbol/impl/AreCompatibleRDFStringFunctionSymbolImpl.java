package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.term.functionsymbol.BooleanExpressionOperation.NOT;

/**
 * TODO: find a better name
 */
public class AreCompatibleRDFStringFunctionSymbolImpl extends BooleanFunctionSymbolImpl {
    protected AreCompatibleRDFStringFunctionSymbolImpl(MetaRDFTermType metaRDFTermType, DBTermType dbBooleanTermType) {
        super("ARE_STR_COMP", ImmutableList.of(metaRDFTermType, metaRDFTermType), dbBooleanTermType);
    }

    @Override
    public boolean blocksNegation() {
        return true;
    }

    @Override
    public ImmutableExpression negate(ImmutableList<? extends ImmutableTerm> subTerms, TermFactory termFactory) {
        return termFactory.getImmutableExpression(NOT, termFactory.getImmutableExpression(this, subTerms));
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     boolean isInConstructionNodeInOptimizationPhase,
                                                     TermFactory termFactory) {
        ImmutableList<TermType> termTypes = newTerms.stream()
                .filter(t -> t instanceof RDFTermTypeConstant)
                .map(t -> (RDFTermTypeConstant) t)
                .map(RDFTermTypeConstant::getRDFTermType)
                .collect(ImmutableCollectors.toList());

        /*
         * Must be RDF strings
         */
        if (termTypes.stream().anyMatch(t -> (!(t instanceof RDFDatatype))
                || (!((RDFDatatype)t).isA(XSD.STRING))))
            return termFactory.getDBBooleanConstant(false);

        switch (termTypes.size()) {
            case 2:
                return termFactory.getDBBooleanConstant(
                        areCompatible((RDFDatatype) termTypes.get(0), (RDFDatatype) termTypes.get(1)));
            default:
                /*
                 * TODO: simplify in the presence of magic numbers
                 */
                return termFactory.getImmutableExpression(this, newTerms);
        }
    }

    private boolean areCompatible(RDFDatatype firstType, RDFDatatype secondType) {
        return firstType.isA(secondType)
                && (
                        secondType.getIRI().equals(XSD.STRING)
                                // Lang strings must be equal
                        || secondType.equals(firstType));
    }

    @Override
    public boolean isInjective(ImmutableList<? extends ImmutableTerm> arguments, ImmutableSet<Variable> nonNullVariables) {
        return false;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return true;
    }
}
