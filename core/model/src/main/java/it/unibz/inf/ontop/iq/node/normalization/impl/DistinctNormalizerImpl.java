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
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public IQTree normalizeForOptimization(DistinctNode distinctNode, IQTree child,
                                           VariableGenerator variableGenerator, IQTreeCache treeCache) {
        IQTree normalizedChild = child.normalizeForOptimization(variableGenerator);
        return liftBinding(distinctNode, normalizedChild, variableGenerator, treeCache);
    }

    private IQTree liftBinding(DistinctNode distinctNode, IQTree newChild, VariableGenerator variableGenerator, IQTreeCache treeCache) {
        QueryNode newChildRoot = newChild.getRootNode();

        if (newChildRoot instanceof ConstructionNode)
            return liftBindingConstructionChild((ConstructionNode) newChildRoot, treeCache,
                    (UnaryIQTree) newChild, variableGenerator);
        else if (newChildRoot instanceof EmptyNode)
            return newChild;
        else if (newChildRoot instanceof ValuesNode) {
            return iqFactory.createValuesNode(((ValuesNode) newChildRoot).getOrderedVariables(),
                    ((ValuesNode) newChildRoot).getValues().stream().distinct().collect(ImmutableCollectors.toList()));
        }
        // DISTINCT UNION [VALUES T2 T3 ...] -> DISTINCT UNION [[DISTINCT VALUE] T2 T3 ...] scenario
        else if ((newChildRoot instanceof UnionNode) &&
                // Check for Values Nodes present otherwise no optimization
                newChild.getChildren().stream().anyMatch(c -> c instanceof ValuesNode) &&
                // Ensure tree not already optimized
                !treeCache.isNormalizedForOptimization()) {

            // For code readability we separate the values and non-values nodes
            ImmutableList<IQTree> vNodelist = newChild.getChildren().stream()
                    .filter(c -> c instanceof ValuesNode)
                    .collect(ImmutableCollectors.toList());
            ImmutableList<IQTree> nonVnodeList = newChild.getChildren().stream()
                    .filter(c -> !(c instanceof ValuesNode))
                    .collect(ImmutableCollectors.toList());

            /**
             * Check if values node is already distinct, in that scenario, we do not push the DISTINCT further down
             * Since for this optimization under UnionNodeImpl, we merge all Values Nodes into 1, checking for the first
             * Values Node should prove sufficient
             * @see it.unibz.inf.ontop.iq.node.impl.UnionNodeImpl#liftBindingFromLiftedChildrenAndFlatten(ImmutableList, VariableGenerator, IQTreeCache)
              */
            return vNodelist.get(0).isDistinct()
                    // CASE 1: Values Node already distinct, no further optimization
                    ? createDistinctTree(distinctNode, newChild, treeCache.declareAsNormalizedForOptimizationWithEffect())
                    // CASE 2: Push DISTINCT further down as per case scenario
                    : iqFactory.createUnaryIQTree(distinctNode,
                    iqFactory.createNaryIQTree((UnionNode) newChildRoot,
                            Stream.concat(
                                        Stream.of(iqFactory.createValuesNode(((ValuesNode) vNodelist.get(0)).getOrderedVariables(),
                                    ((ValuesNode) vNodelist.get(0)).getValues().stream().distinct().collect(ImmutableCollectors.toList()))),
                                        nonVnodeList.stream())
                                    .collect(ImmutableCollectors.toList())));
        }
        else
            return createDistinctTree(distinctNode, newChild, treeCache.declareAsNormalizedForOptimizationWithEffect());
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
