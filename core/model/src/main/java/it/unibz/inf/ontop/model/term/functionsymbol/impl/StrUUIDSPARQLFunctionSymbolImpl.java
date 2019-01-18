package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.SPARQLFunctionSymbol;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;
import java.util.Optional;

public class StrUUIDSPARQLFunctionSymbolImpl extends FunctionSymbolImpl implements SPARQLFunctionSymbol {

    private final RDFDatatype xsdStringType;

    protected StrUUIDSPARQLFunctionSymbolImpl(RDFDatatype xsdStringType) {
        super("SP_STRUUID", ImmutableList.of());
        this.xsdStringType = xsdStringType;
    }

    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return false;
    }

    @Override
    protected boolean isAlwaysInjective() {
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
                                                     boolean isInConstructionNodeInOptimizationPhase, TermFactory termFactory,
                                                     VariableNullability variableNullability) {
        ImmutableFunctionalTerm lexicalTerm = termFactory.getDBUUID();
        return termFactory.getRDFFunctionalTerm(lexicalTerm, termFactory.getRDFTermTypeConstant(xsdStringType));
    }

    @Override
    protected boolean tolerateNulls() {
        return false;
    }
}
