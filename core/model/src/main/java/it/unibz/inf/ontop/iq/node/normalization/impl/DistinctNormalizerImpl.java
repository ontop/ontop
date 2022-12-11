package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.DistinctNormalizer;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

@Singleton
public class DistinctNormalizerImpl implements DistinctNormalizer {

    private static final int MAX_ITERATIONS = 10000;
    private final IntermediateQueryFactory iqFactory;
    private final CoreSingletons coreSingletons;

    @Inject
    private DistinctNormalizerImpl(CoreSingletons coreSingletons) {
        this.iqFactory = coreSingletons.getIQFactory();
        this.coreSingletons = coreSingletons;
    }

    @Override
    public IQTree normalizeForOptimization(DistinctNode distinctNode, IQTree initialChild,
                                           VariableGenerator variableGenerator, IQTreeCache treeCache) {
        IQTree child = initialChild.normalizeForOptimization(variableGenerator);

        if (child.isDistinct())
            return child;

        if (child.getVariables().isEmpty()) {
            // No child variable -> replace by a LIMIT 1
            IQTree limitTree = iqFactory.createUnaryIQTree(
                    iqFactory.createSliceNode(0, 1),
                    child);

            return limitTree.normalizeForOptimization(variableGenerator);
        }

        QueryNode childRoot = child.getRootNode();

        if (childRoot instanceof ConstructionNode)
            return liftBindingConstructionChild((ConstructionNode) childRoot, treeCache,
                    (UnaryIQTree) child, variableGenerator);
        else if (childRoot instanceof ValuesNode) {
            return iqFactory.createValuesNode(((ValuesNode) childRoot).getOrderedVariables(),
                    ((ValuesNode) childRoot).getValues().stream().distinct().collect(ImmutableCollectors.toList()));
        }
        else if (childRoot instanceof UnionNode) {
            Optional<IQTree> newTree = simplifyUnion(child, distinctNode, variableGenerator);
            if (newTree.isPresent())
                return newTree.get();
        }

        return child.equals(initialChild)
                ? createDistinctTree(distinctNode, child, treeCache.declareAsNormalizedForOptimizationWithoutEffect())
                : createDistinctTree(distinctNode, child, treeCache.declareAsNormalizedForOptimizationWithEffect());
    }

    private IQTree createDistinctTree(DistinctNode distinctNode, IQTree child, IQTreeCache treeCache) {
        return child.isDistinct()
                ? child
                : iqFactory.createUnaryIQTree(distinctNode, child, treeCache);
    }

    private IQTree liftBindingConstructionChild(ConstructionNode constructionNode,
                                                IQTreeCache treeCache, UnaryIQTree child,
                                                VariableGenerator variableGenerator) {
        // Non-final
        InjectiveBindingLiftState state = new InjectiveBindingLiftState(constructionNode, child.getChild(), variableGenerator,
                coreSingletons);

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            InjectiveBindingLiftState newState = state.liftBindings();

            if (newState.equals(state))
                return createNormalizedTree(newState, treeCache, variableGenerator);
            state = newState;
        }
        throw new MinorOntopInternalBugException("DistinctNormalizerImpl.liftBindingConstructionChild() " +
                "did not converge after " + MAX_ITERATIONS);
    }

    private Optional<IQTree> simplifyUnion(IQTree child, DistinctNode distinctNode, VariableGenerator variableGenerator) {
        ImmutableList<IQTree> unionChildren = child.getChildren();

        ImmutableList<IQTree> newUnionChildren = unionChildren.stream()
                .map(c -> simplifyUnionChild(c, variableGenerator))
                .collect(ImmutableCollectors.toList());

        if (unionChildren.equals(newUnionChildren))
            return Optional.empty();

        UnaryIQTree newTree = iqFactory.createUnaryIQTree(
                distinctNode,
                iqFactory.createNaryIQTree((UnionNode) child.getRootNode(), newUnionChildren));

        return Optional.of(newTree.normalizeForOptimization(variableGenerator));
    }

    private IQTree simplifyUnionChild(IQTree unionChild, VariableGenerator variableGenerator) {
        if (unionChild.isDistinct())
            return unionChild;

        if (unionChild instanceof ValuesNode) {
            ValuesNode valuesNode = (ValuesNode) unionChild;
            return iqFactory.createValuesNode(valuesNode.getOrderedVariables(),
                    valuesNode.getValues().stream()
                            .distinct()
                            .collect(ImmutableCollectors.toList()));
        }

        QueryNode unionRoot = unionChild.getRootNode();

        if (unionRoot instanceof ConstructionNode) {
            // Child without projected variable -> inserts a LIMIT 1
            if (unionChild.getChildren().get(0).getVariables().isEmpty())
                return iqFactory.createUnaryIQTree(
                        iqFactory.createSliceNode(0, 1),
                        unionChild)
                        .normalizeForOptimization(variableGenerator);
        }
        return unionChild;
    }

    private IQTree createNormalizedTree(InjectiveBindingLiftState state, IQTreeCache treeCache, VariableGenerator variableGenerator) {

        IQTree grandChildTree = state.getGrandChildTree();
        // No need to have a DISTINCT as a grand child
        IQTree newGrandChildTree = (grandChildTree.getRootNode() instanceof DistinctNode)
                ? ((UnaryIQTree)grandChildTree).getChild()
                : grandChildTree;

        IQTreeCache childTreeCache = iqFactory.createIQTreeCache(newGrandChildTree == grandChildTree);

        IQTree newChildTree = state.getChildConstructionNode()
                .map(c -> iqFactory.createUnaryIQTree(c, newGrandChildTree, childTreeCache))
                // To be normalized again in case a DISTINCT was present as a grand child.
                // NB: does nothing if it is not the case
                .map(t -> t.normalizeForOptimization(variableGenerator))
                .orElse(newGrandChildTree);

        IQTree distinctTree = createDistinctTree(iqFactory.createDistinctNode(), newChildTree,
                treeCache.declareAsNormalizedForOptimizationWithEffect());

        return state.getAncestors().reverse().stream()
                .reduce(distinctTree,
                        (t, a) -> iqFactory.createUnaryIQTree(a, t),
                        (t1, t2) -> { throw new MinorOntopInternalBugException("No merge was expected"); })
                // Recursive (for merging top construction nodes)
                .normalizeForOptimization(variableGenerator);
    }

}
