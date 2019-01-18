package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.SPARQLFunctionSymbol;
import it.unibz.inf.ontop.model.type.ObjectRDFType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;
import org.apache.commons.rdf.api.IRI;

import java.util.Optional;

public class UUIDSPARQLFunctionSymbolImpl extends FunctionSymbolImpl implements SPARQLFunctionSymbol {

    private static final String URN_UUID_PREFIX = "urn:uuid:";
    private final ObjectRDFType iriStringType;

    protected UUIDSPARQLFunctionSymbolImpl(ObjectRDFType iriStringType) {
        super("SP_UUID", ImmutableList.of());
        this.iriStringType = iriStringType;
    }

    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return false;
    }

    @Override
    protected boolean tolerateNulls() {
        return false;
    }

    @Override
    protected boolean isAlwaysInjective() {
        return false;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(iriStringType));
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    @Override
    public Optional<IRI> getIRI() {
        return Optional.empty();
    }

    @Override
    public String getOfficialName() {
        return SPARQL.UUID;
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     boolean isInConstructionNodeInOptimizationPhase, TermFactory termFactory,
                                                     VariableNullability variableNullability) {
        ImmutableFunctionalTerm lexicalTerm =
                termFactory.getDBConcatFunctionalTerm(
                        ImmutableList.of(
                                termFactory.getDBStringConstant(URN_UUID_PREFIX),
                                termFactory.getDBUUID()));
        return termFactory.getRDFFunctionalTerm(lexicalTerm, termFactory.getRDFTermTypeConstant(iriStringType));
    }
}
