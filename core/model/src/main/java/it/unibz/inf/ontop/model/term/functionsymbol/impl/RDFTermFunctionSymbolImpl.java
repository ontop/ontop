package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.FatalTypingException;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermFunctionSymbol;
import it.unibz.inf.ontop.model.term.impl.FunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.MetaRDFTermType;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;

import java.util.Optional;

public class RDFTermFunctionSymbolImpl extends FunctionSymbolImpl implements RDFTermFunctionSymbol {

    protected RDFTermFunctionSymbolImpl(TermType lexicalType, MetaRDFTermType typeTermType) {
        super("RDF", ImmutableList.of(lexicalType, typeTermType));
    }

    /**
     * TODO: implement seriously
     */
    @Override
    public FunctionalTermNullability evaluateNullability(ImmutableList<? extends NonFunctionalTerm> arguments,
                                                         VariableNullability childNullability) {
        return super.evaluateNullability(arguments, childNullability);
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        // TODO: complain if one is null and the one is not nullable
        if (terms.stream()
                .filter(t -> t instanceof Constant)
                .anyMatch(c -> ((Constant) c).isNull()))
            return Optional.of(TermTypeInference.declareNonFatalError());

        if (terms.size() != 2)
            throw new IllegalArgumentException("Wrong arity");

        return Optional.of(terms.get(1))
                .filter(t -> t instanceof RDFTermTypeConstant)
                .map(t -> (RDFTermTypeConstant) t)
                .map(RDFTermTypeConstant::getRDFTermType)
                .map(TermTypeInference::declareTermType);
    }

    @Override
    public Optional<TermTypeInference> inferAndValidateType(ImmutableList<? extends ImmutableTerm> terms)
            throws FatalTypingException {
        validateSubTermTypes(terms);
        return inferType(terms);
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     boolean isInConstructionNodeInOptimizationPhase,
                                                     TermFactory termFactory) {
        if ((!isInConstructionNodeInOptimizationPhase) && newTerms.stream()
                .allMatch(t -> t instanceof Constant)) {

            DBConstant lexicalConstant = Optional.of(newTerms.get(0))
                    .filter(c -> c instanceof DBConstant)
                    .map(c -> (DBConstant) c)
                    .orElseThrow(() -> new MinorOntopInternalBugException(
                            "The constant for the lexical part was expected to be a DBConstant"));

            RDFTermType rdfTermType = Optional.of(newTerms.get(1))
                    .filter(c -> c instanceof RDFTermTypeConstant)
                    .map(c -> (RDFTermTypeConstant) c)
                    .map(RDFTermTypeConstant::getRDFTermType)
                    .orElseThrow(() -> new MinorOntopInternalBugException(
                            "The second constant argument was expected to be a RDFTermTypeConstant"));

            return termFactory.getRDFConstant(lexicalConstant.getValue(), rdfTermType);
        }
        else
            return termFactory.getImmutableFunctionalTerm(this, newTerms);
    }

    @Override
    public boolean canBePostProcessed() {
        return true;
    }
}
