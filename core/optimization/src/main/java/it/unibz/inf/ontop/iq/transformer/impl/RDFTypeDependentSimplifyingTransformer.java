package it.unibz.inf.ontop.iq.transformer.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.OptimizationSingletons;
import it.unibz.inf.ontop.injection.OptimizerFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.request.DefinitionPushDownRequest;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.RDFTermTypeConstant;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBIfElseNullFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBIfThenFunctionSymbol;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * TODO: find a better name
 */
public abstract class RDFTypeDependentSimplifyingTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

    private final OptimizerFactory optimizerFactory;

    protected RDFTypeDependentSimplifyingTransformer(OptimizationSingletons optimizationSingletons) {
        super(optimizationSingletons.getCoreSingletons());
        this.optimizerFactory = optimizationSingletons.getOptimizerFactory();
    }

    protected ImmutableTerm unwrapIfElseNull(ImmutableTerm term) {
        return Optional.of(term)
                .filter(t -> t instanceof ImmutableFunctionalTerm)
                .map(t -> (ImmutableFunctionalTerm) t)
                .filter(t -> t.getFunctionSymbol() instanceof DBIfElseNullFunctionSymbol)
                .map(t -> t.getTerm(1))
                .orElse(term);
    }

    protected Optional<ImmutableSet<RDFTermType>> extractPossibleTypes(ImmutableTerm rdfTypeTerm, IQTree childTree) {
        if (rdfTypeTerm.isNull())
            return Optional.empty();
        else if (rdfTypeTerm instanceof RDFTermTypeConstant) {
            return Optional.of(ImmutableSet.of(((RDFTermTypeConstant) rdfTypeTerm).getRDFTermType()));
        }

        ImmutableSet<ImmutableTerm> possibleValues = childTree.getPossibleVariableDefinitions().stream()
                .map(s -> s.apply(rdfTypeTerm))
                .map(t -> t.simplify(childTree.getVariableNullability()))
                .flatMap(this::extractPossibleFromCase)
                .filter(t -> !t.isNull())
                .collect(ImmutableCollectors.toSet());

        return Optional.of(possibleValues)
                .filter(vs -> vs.stream().allMatch(t -> t instanceof RDFTermTypeConstant))
                .map(vs -> vs.stream()
                        .map(t -> (RDFTermTypeConstant) t)
                        .map(RDFTermTypeConstant::getRDFTermType)
                        .collect(ImmutableCollectors.toSet()));
    }

    private Stream<ImmutableTerm> extractPossibleFromCase(ImmutableTerm immutableTerm) {
        if ((immutableTerm instanceof ImmutableFunctionalTerm) &&
                (((ImmutableFunctionalTerm) immutableTerm).getFunctionSymbol() instanceof DBIfThenFunctionSymbol)) {
            ImmutableFunctionalTerm functionalTerm = (ImmutableFunctionalTerm) immutableTerm;
            return ((DBIfThenFunctionSymbol)functionalTerm.getFunctionSymbol()).extractPossibleValues(functionalTerm.getTerms());
        }
        else
            return Stream.of(immutableTerm);
    }

    /**
     * Pushes down definitions emerging from the simplification of the order comparators
     */
    protected IQTree pushDownDefinitions(IQTree initialChild, Stream<DefinitionPushDownRequest> definitionsToPushDown) {
        return definitionsToPushDown
                .reduce(initialChild,
                        (c, r) -> optimizerFactory.createDefinitionPushDownTransformer(r).transform(c),
                        (c1, c2) -> { throw new MinorOntopInternalBugException("Merging must not happen") ; });
    }

}
