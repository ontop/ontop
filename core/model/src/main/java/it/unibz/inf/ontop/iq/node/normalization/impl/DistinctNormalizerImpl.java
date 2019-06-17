package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.DistinctNormalizer;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.annotation.Nonnull;
import java.util.Optional;

@Singleton
public class DistinctNormalizerImpl implements DistinctNormalizer {

    private static final int MAX_ITERATIONS = 10000;
    private final IntermediateQueryFactory iqFactory;
    private final CoreSingletons coreSingletons;

    @Inject
    private DistinctNormalizerImpl(IntermediateQueryFactory iqFactory, CoreSingletons coreSingletons) {
        this.iqFactory = iqFactory;
        this.coreSingletons = coreSingletons;
    }

    @Override
    public IQTree normalizeForOptimization(DistinctNode distinctNode, IQTree child,
                                           VariableGenerator variableGenerator, IQProperties currentIQProperties) {
        IQTree newChild = child.removeDistincts();
        return liftBinding(distinctNode, newChild, variableGenerator, currentIQProperties);
    }

    private IQTree liftBinding(DistinctNode distinctNode, IQTree child, VariableGenerator variableGenerator,
                               IQProperties currentIQProperties) {
        IQTree newChild = child.normalizeForOptimization(variableGenerator);
        QueryNode newChildRoot = newChild.getRootNode();

        if (newChildRoot instanceof ConstructionNode)
            return liftBindingConstructionChild((ConstructionNode) newChildRoot, currentIQProperties,
                    (UnaryIQTree) newChild, variableGenerator);
        else if (newChildRoot instanceof EmptyNode)
            return newChild;
        else
            return iqFactory.createUnaryIQTree(distinctNode, newChild,
                    currentIQProperties.declareNormalizedForOptimization());
    }

    private IQTree liftBindingConstructionChild(ConstructionNode constructionNode,
                                                IQProperties currentIQProperties, UnaryIQTree child,
                                                VariableGenerator variableGenerator) {
        // Non-final
        InjectiveBindingLiftState state = new InjectiveBindingLiftStateForDistinct(constructionNode, child.getChild(), variableGenerator,
                coreSingletons);

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            InjectiveBindingLiftState newState = state.liftBindings();

            if (newState.equals(state))
                return newState.createNormalizedTree(currentIQProperties);
            state = newState;
        }
        throw new MinorOntopInternalBugException("DistinctNormalizerImpl.liftBindingConstructionChild() " +
                "did not converge after " + MAX_ITERATIONS);
    }

    protected static class InjectiveBindingLiftStateForDistinct extends InjectiveBindingLiftState {


        protected InjectiveBindingLiftStateForDistinct(@Nonnull ConstructionNode childConstructionNode, IQTree grandChildTree,
                                                       VariableGenerator variableGenerator, CoreSingletons coreSingletons) {
            super(childConstructionNode, grandChildTree, variableGenerator, coreSingletons);
        }

        protected InjectiveBindingLiftStateForDistinct(ImmutableList<ConstructionNode> ancestors, IQTree grandChildTree,
                                                       VariableGenerator variableGenerator, CoreSingletons coreSingletons) {
            super(ancestors, grandChildTree, variableGenerator, coreSingletons);
        }

        protected InjectiveBindingLiftStateForDistinct(ImmutableList<ConstructionNode> ancestors, IQTree grandChildTree,
                                                       VariableGenerator variableGenerator, @Nonnull ConstructionNode childConstructionNode,
                                                       CoreSingletons coreSingletons) {
            super(ancestors, grandChildTree, variableGenerator, childConstructionNode, coreSingletons);
        }

        @Override
        protected InjectiveBindingLiftState newState(ImmutableList<ConstructionNode> newAncestors, IQTree grandChildTree) {
            return new InjectiveBindingLiftStateForDistinct(ancestors, grandChildTree, variableGenerator, coreSingletons);
        }

        @Override
        protected InjectiveBindingLiftState newState(ImmutableList<ConstructionNode> newAncestors, IQTree grandChildTree,
                                                     ConstructionNode childConstructionNode) {
            return new InjectiveBindingLiftStateForDistinct(ancestors, grandChildTree, variableGenerator,
                    childConstructionNode, coreSingletons);
        }

        public IQTree createNormalizedTree(IQProperties currentIQProperties) {
            IntermediateQueryFactory iqFactory = coreSingletons.getIQFactory();

            IQTree newChildTree = Optional.ofNullable(childConstructionNode)
                    .map(c -> (IQTree) iqFactory.createUnaryIQTree(c, grandChildTree,
                            iqFactory.createIQProperties().declareNormalizedForOptimization()))
                    .orElse(grandChildTree);

            IQTree distinctTree = iqFactory.createUnaryIQTree(iqFactory.createDistinctNode(), newChildTree,
                    currentIQProperties.declareNormalizedForOptimization());

            return ancestors.reverse().stream()
                    .reduce(distinctTree,
                            (t, a) -> iqFactory.createUnaryIQTree(a, t),
                            (t1, t2) -> { throw new MinorOntopInternalBugException("No merge was expected"); })
                    // Recursive (for merging top construction nodes)
                    .normalizeForOptimization(variableGenerator);
        }
    }

}
