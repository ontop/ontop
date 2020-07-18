package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;

import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;
import java.util.Optional;

/**
 * Non-deterministic when arity
 *
 * TODO: explain the use of the UUID
 *
 */
public abstract class AbstractBnodeSPARQLFunctionSymbol extends SPARQLFunctionSymbolImpl {

    private final RDFTermType bnodeType;

    protected AbstractBnodeSPARQLFunctionSymbol(String name, ImmutableList<TermType> expectedBaseTypes, RDFTermType bnodeType) {
        super(name, SPARQL.BNODE, expectedBaseTypes);
        this.bnodeType = bnodeType;
    }

    @Override
    protected boolean tolerateNulls() {
        return false;
    }

    /**
     * Non-deterministic therefore non-injective
     */
    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(bnodeType));
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory,
                                                     VariableNullability variableNullability) {

        // Decomposes
        if (newTerms.stream().allMatch(t -> isRDFFunctionalTerm(t) || (t instanceof Constant))) {
            return termFactory.getRDFFunctionalTerm(
                    buildLexicalTerm(newTerms, termFactory),
                    termFactory.getRDFTermTypeConstant(bnodeType));
        }

        return super.buildTermAfterEvaluation(newTerms, termFactory, variableNullability);
    }

    protected abstract ImmutableTerm buildLexicalTerm(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory);
}
