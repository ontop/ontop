package it.unibz.inf.ontop.iq.node.normalization.impl;

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
        IQTree normalizedChild = child.normalizeForOptimization(variableGenerator);
        return liftBinding(distinctNode, normalizedChild, variableGenerator, currentIQProperties);
    }

    private IQTree liftBinding(DistinctNode distinctNode, IQTree newChild, VariableGenerator variableGenerator,
                               IQProperties currentIQProperties) {
        QueryNode newChildRoot = newChild.getRootNode();

        if (newChildRoot instanceof ConstructionNode)
            return liftBindingConstructionChild((ConstructionNode) newChildRoot, currentIQProperties,
                    (UnaryIQTree) newChild, variableGenerator);
        else if (newChildRoot instanceof EmptyNode)
            return newChild;
        else
            return createDistinctTree(distinctNode, newChild, currentIQProperties.declareNormalizedForOptimization());
    }

    private IQTree createDistinctTree(DistinctNode distinctNode, IQTree child, IQProperties iqProperties) {
        return child.isDistinct()
                ? child
                : iqFactory.createUnaryIQTree(distinctNode, child, iqProperties);
    }

    private IQTree liftBindingConstructionChild(ConstructionNode constructionNode,
                                                IQProperties currentIQProperties, UnaryIQTree child,
                                                VariableGenerator variableGenerator) {
        // Non-final
        InjectiveBindingLiftState state = new InjectiveBindingLiftState(constructionNode, child.getChild(), variableGenerator,
                coreSingletons);

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            InjectiveBindingLiftState newState = state.liftBindings();

            if (newState.equals(state))
                return createNormalizedTree(newState, currentIQProperties, variableGenerator);
            state = newState;
        }
        throw new MinorOntopInternalBugException("DistinctNormalizerImpl.liftBindingConstructionChild() " +
                "did not converge after " + MAX_ITERATIONS);
    }

    public IQTree createNormalizedTree(InjectiveBindingLiftState state, IQProperties currentIQProperties,
                                       VariableGenerator variableGenerator) {
        IntermediateQueryFactory iqFactory = coreSingletons.getIQFactory();

        IQTree grandChildTree = state.getGrandChildTree();
        // No need to have a DISTINCT as a grand child
        IQTree newGrandChildTree = (grandChildTree.getRootNode() instanceof DistinctNode)
                ? ((UnaryIQTree)grandChildTree).getChild()
                : grandChildTree;

        IQProperties childProperties = (newGrandChildTree == grandChildTree)
                ? iqFactory.createIQProperties().declareNormalizedForOptimization()
                : iqFactory.createIQProperties();

        IQTree newChildTree = state.getChildConstructionNode()
                .map(c -> iqFactory.createUnaryIQTree(c, newGrandChildTree, childProperties))
                // To be normalized again in case a DISTINCT was present as a grand child.
                // NB: does nothing if it is not the case
                .map(t -> t.normalizeForOptimization(variableGenerator))
                .orElse(newGrandChildTree);

        IQTree distinctTree = createDistinctTree(iqFactory.createDistinctNode(), newChildTree,
                currentIQProperties.declareNormalizedForOptimization());

        return state.getAncestors().reverse().stream()
                .reduce(distinctTree,
                        (t, a) -> iqFactory.createUnaryIQTree(a, t),
                        (t1, t2) -> { throw new MinorOntopInternalBugException("No merge was expected"); })
                // Recursive (for merging top construction nodes)
                .normalizeForOptimization(variableGenerator);
    }

}
