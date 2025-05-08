package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.optimizer.splitter.PreventDistinctProjectionSplitter;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OptimizationSingletons;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.PreventDistinctOptimizer;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.inject.Inject;
import java.util.Optional;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;


public class PreventDistinctOptimizerImpl implements PreventDistinctOptimizer {

    private final IntermediateQueryFactory iqFactory;
    private final OptimizationSingletons optimizationSingletons;
    private final PreventDistinctProjectionSplitter preventDistinctProjectionSplitter;

    @Inject
    private PreventDistinctOptimizerImpl(IntermediateQueryFactory iqFactory, OptimizationSingletons optimizationSingletons, PreventDistinctProjectionSplitter preventDistinctProjectionSplitter) {
        this.iqFactory = iqFactory;
        this.optimizationSingletons = optimizationSingletons;
        this.preventDistinctProjectionSplitter = preventDistinctProjectionSplitter;
    }

    @Override
    public IQ optimize(IQ query) {
        VariableGenerator variableGenerator = query.getVariableGenerator();
        IQTreeTransformer transformer = new PreventDistinctTransformer(variableGenerator);
        IQTree newTree = transformer.transform(query.getTree());
        if (newTree.equals(query.getTree()))
            return query;
        return iqFactory.createIQ(query.getProjectionAtom(), newTree);
    }

    private class PreventDistinctTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        private final VariableGenerator variableGenerator;

        public PreventDistinctTransformer(VariableGenerator variableGenerator) {
            super(PreventDistinctOptimizerImpl.this.iqFactory);
            this.variableGenerator = variableGenerator;
        }

        @Override
        public IQTree transformConstruction(UnaryIQTree tree, ConstructionNode rootNode, IQTree child) {
            var decomposition = Decomposition.decomposeTree(child);
            if (decomposition.distinctNode.isPresent()) {
                var split = preventDistinctProjectionSplitter.split(tree, variableGenerator);
                var newTree = iqFactory.createUnaryIQTree(split.getConstructionNode(),
                        split.getSubTree());
                if (newTree.equals(tree))
                    return tree;
                if (!validatePushedVariables(
                        split.getPushedVariables(),
                        Sets.difference(
                                rootNode.getChildVariables(),
                                split.getPushedVariables()).immutableCopy(),
                        decomposition.descendantTree))
                    throw new MinorOntopInternalBugException("Unable to push down substitution terms that are not supported with DISTINCT without a functional dependency.");
                if (!validatePushedTerms(split.getPushedTerms()))
                    throw new MinorOntopInternalBugException("Unable to push down substitution terms that are not supported with DISTINCT if the functional terms are not deterministic.");
                return newTree;
            }
            return super.transformConstruction(tree, rootNode, child);
        }

        private boolean validatePushedVariables(ImmutableSet<Variable> pushedVariables, ImmutableSet<Variable> keptVariables, IQTree child) {
            var functionalDependencies = child.inferFunctionalDependencies();
            return pushedVariables.stream()
                    .allMatch(v -> functionalDependencies.getDeterminantsOf(v).stream()
                                    .anyMatch(determinants ->
                                            keptVariables.containsAll(determinants)
                                            && Sets.intersection(determinants, pushedVariables).isEmpty()
                                    ));
        }

        private boolean validatePushedTerms(ImmutableSet<ImmutableTerm> pushedTerms) {
            return pushedTerms.stream()
                    .filter(term -> term instanceof ImmutableFunctionalTerm)
                    .map(term -> (ImmutableFunctionalTerm) term)
                    .allMatch(this::isDeterministic);
        }

        private boolean isDeterministic(ImmutableTerm term) {
            if(!(term instanceof ImmutableFunctionalTerm))
                return true;
            var f = (ImmutableFunctionalTerm) term;
            if(!f.getFunctionSymbol().isDeterministic())
                return false;
            return f.getTerms().stream()
                            .allMatch(this::isDeterministic);
        }
    }

    /**
     * Below the Construction, we may find the following nodes: DistinctNode, SliceNode.
     * Some nodes may be missing but the order must be respected. There are no multiple instances of the same kind of node.
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected static class Decomposition {
        final Optional<SliceNode> sliceNode;
        final Optional<DistinctNode> distinctNode;

        final IQTree descendantTree;

        private Decomposition(Optional<SliceNode> sliceNode,
                              Optional<DistinctNode> distinctNode,
                              IQTree descendantTree) {
            this.sliceNode = sliceNode;
            this.distinctNode = distinctNode;
            this.descendantTree = descendantTree;
        }

        static Decomposition decomposeTree(IQTree tree) {
            var slice = UnaryIQTreeDecomposition.of(tree, SliceNode.class);
            var distinct = UnaryIQTreeDecomposition.of(slice.getTail(), DistinctNode.class);

            return new Decomposition(slice.getOptionalNode(), distinct.getOptionalNode(), distinct.getTail());
        }
    }
}
