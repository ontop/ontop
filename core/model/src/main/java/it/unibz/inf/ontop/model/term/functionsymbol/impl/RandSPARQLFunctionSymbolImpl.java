package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;

import java.util.Optional;
import java.util.UUID;

/**
 * Non-deterministic
 *
 * TODO: explain the use of the UUID
 *
 */
public class RandSPARQLFunctionSymbolImpl extends SPARQLFunctionSymbolImpl {

    private final UUID uuid;
    private final RDFDatatype xsdDoubleType;

    protected RandSPARQLFunctionSymbolImpl(UUID uuid, RDFDatatype xsdDoubleType) {
        super("SP_RAND" + uuid, SPARQL.RAND, ImmutableList.of());
        this.uuid = uuid;
        this.xsdDoubleType = xsdDoubleType;
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
        return Optional.of(TermTypeInference.declareTermType(xsdDoubleType));
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory,
                                                     VariableNullability variableNullability) {

        return termFactory.getRDFFunctionalTerm(
                termFactory.getConversion2RDFLexical(termFactory.getDBRand(uuid), xsdDoubleType),
                termFactory.getRDFTermTypeConstant(xsdDoubleType));
    }
}
