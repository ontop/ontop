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
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.inject.Inject;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;


public class PreventDistinctOptimizerImpl implements PreventDistinctOptimizer {

    private final IntermediateQueryFactory iqFactory;
    private final PreventDistinctProjectionSplitter preventDistinctProjectionSplitter;

    @Inject
    private PreventDistinctOptimizerImpl(OptimizationSingletons optimizationSingletons, PreventDistinctProjectionSplitter preventDistinctProjectionSplitter) {
        this.iqFactory = optimizationSingletons.getCoreSingletons().getIQFactory();
        this.preventDistinctProjectionSplitter = preventDistinctProjectionSplitter;
    }

    @Override
    public IQ optimize(IQ query) {
        VariableGenerator variableGenerator = query.getVariableGenerator();
        IQTree newTree = query.getTree().acceptVisitor(new PreventDistinctTransformer(variableGenerator));
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

            // Below the Construction, we may find the following nodes: DistinctNode, SliceNode.
            // Some nodes may be missing but the order must be respected.
            // There are no multiple instances of the same kind of node.
            var slice = UnaryIQTreeDecomposition.of(child, SliceNode.class);
            var distinct = UnaryIQTreeDecomposition.of(slice, DistinctNode.class);
            if (distinct.isPresent()) {
                var split = preventDistinctProjectionSplitter.split(tree, variableGenerator);
                var newTree = iqFactory.createUnaryIQTree(split.getConstructionNode(),
                        split.getSubTree());
                if (newTree.equals(tree))
                    return tree;
                var descendantTree = distinct.getTail();
                if (!validatePushedVariables(
                        split.getPushedVariables(),
                        Sets.difference(
                                rootNode.getChildVariables(),
                                split.getPushedVariables()).immutableCopy(),
                        descendantTree))
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
}
