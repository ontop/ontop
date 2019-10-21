package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermTypeFunctionSymbol;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;

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
        return termFactory.getDBNot(termFactory.getImmutableExpression(this, subTerms));
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     TermFactory termFactory, VariableNullability variableNullability) {
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
                return tryToLiftMagicNumbers(newTerms, termFactory, variableNullability)
                        .orElseGet(() -> termFactory.getImmutableExpression(this, newTerms));
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
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return true;
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
