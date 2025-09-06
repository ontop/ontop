package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.optimizer.splitter.PreventDistinctProjectionSplitter;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.PreventDistinctOptimizer;
import it.unibz.inf.ontop.iq.transform.IQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.iq.visit.impl.DefaultRecursiveIQTreeVisitingTransformerWithVariableGenerator;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.inject.Inject;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;


public class PreventDistinctOptimizerImpl extends AbstractExtendedIQOptimizer implements PreventDistinctOptimizer {

    private final PreventDistinctProjectionSplitter preventDistinctProjectionSplitter;

    private final IQTreeVariableGeneratorTransformer transformer;

    @Inject
    private PreventDistinctOptimizerImpl(CoreSingletons coreSingletons, PreventDistinctProjectionSplitter preventDistinctProjectionSplitter) {
        super(coreSingletons.getIQFactory());
        this.preventDistinctProjectionSplitter = preventDistinctProjectionSplitter;

        this.transformer = IQTreeVariableGeneratorTransformer.of(PreventDistinctTransformer::new);
    }

    @Override
    protected IQTreeVariableGeneratorTransformer getTransformer() {
        return transformer;
    }

    private class PreventDistinctTransformer extends DefaultRecursiveIQTreeVisitingTransformerWithVariableGenerator {

        PreventDistinctTransformer(VariableGenerator variableGenerator) {
            super(PreventDistinctOptimizerImpl.this.iqFactory, variableGenerator);
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

                if (!split.getPushedTerms().stream()
                        .allMatch(this::isDeterministic))
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
                                            && Sets.intersection(determinants, pushedVariables).isEmpty()));
        }

        private boolean isDeterministic(ImmutableTerm term) {
            if (term instanceof ImmutableFunctionalTerm) {
                var f = (ImmutableFunctionalTerm) term;
                if (!f.getFunctionSymbol().isDeterministic())
                    return false;
                return f.getTerms().stream()
                        .allMatch(this::isDeterministic);
            }
            return true;
        }
    }
}
