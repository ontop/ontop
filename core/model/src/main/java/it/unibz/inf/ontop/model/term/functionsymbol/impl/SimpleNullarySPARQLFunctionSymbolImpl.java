package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Function;


public class SimpleNullarySPARQLFunctionSymbolImpl extends SPARQLFunctionSymbolImpl {

    private final RDFTermType targetType;
    private final Function<TermFactory, ImmutableFunctionalTerm> dbFunctionalTermFct;

    protected SimpleNullarySPARQLFunctionSymbolImpl(@Nonnull String name, IRI functionIRI,
                                                    RDFTermType targetType,
                                                    Function<TermFactory, ImmutableFunctionalTerm> dbFunctionalTermFct) {
        super(name, functionIRI, ImmutableList.of());
        this.targetType = targetType;
        this.dbFunctionalTermFct = dbFunctionalTermFct;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(targetType));
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory,
                                                     VariableNullability variableNullability) {
        var lexicalTerm = dbFunctionalTermFct.apply(termFactory);
        return termFactory.getRDFFunctionalTerm(
                lexicalTerm,
                termFactory.getIfElseNull(
                        termFactory.getDBIsNotNull(lexicalTerm),
                        termFactory.getRDFTermTypeConstant(targetType)
                )
        );
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    protected boolean tolerateNulls() {
        return true;
    }
}
