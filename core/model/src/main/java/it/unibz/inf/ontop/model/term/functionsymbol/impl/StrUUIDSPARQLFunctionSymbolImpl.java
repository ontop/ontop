package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.SPARQLFunctionSymbol;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;
import org.apache.commons.rdf.api.IRI;

import java.util.Optional;
import java.util.UUID;

public class StrUUIDSPARQLFunctionSymbolImpl extends FunctionSymbolImpl implements SPARQLFunctionSymbol {

    private final UUID uuid;
    private final RDFDatatype xsdStringType;

    protected StrUUIDSPARQLFunctionSymbolImpl(UUID uuid, RDFDatatype xsdStringType) {
        super("SP_STRUUID", ImmutableList.of());
        this.uuid = uuid;
        this.xsdStringType = xsdStringType;
    }

    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return false;
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(xsdStringType));
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
        return SPARQL.STRUUID;
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     TermFactory termFactory,
                                                     VariableNullability variableNullability) {
        ImmutableFunctionalTerm lexicalTerm = termFactory.getDBUUID(uuid);
        return termFactory.getRDFFunctionalTerm(lexicalTerm, termFactory.getRDFTermTypeConstant(xsdStringType));
    }

    @Override
    protected boolean tolerateNulls() {
        return false;
    }
}
