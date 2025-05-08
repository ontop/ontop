package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.DistinctNormalizer;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;
import static it.unibz.inf.ontop.iq.impl.IQTreeTools.NaryIQTreeDecomposition;


@Singleton
public class DistinctNormalizerImpl implements DistinctNormalizer {

    private static final int MAX_ITERATIONS = 10000;
    private final IntermediateQueryFactory iqFactory;
    private final CoreSingletons coreSingletons;
    private final IQTreeTools iqTreeTools;

    @Inject
    private DistinctNormalizerImpl(CoreSingletons coreSingletons) {
        this.iqFactory = coreSingletons.getIQFactory();
        this.coreSingletons = coreSingletons;
        this.iqTreeTools = coreSingletons.getIQTreeTools();
    }

    @Override
    public IQTree normalizeForOptimization(DistinctNode distinctNode, IQTree initialChild,
                                           VariableGenerator variableGenerator, IQTreeCache treeCache) {

        IQTree child = initialChild.normalizeForOptimization(variableGenerator);
        if (child.isDistinct())
            return child;

        if (child.getVariables().isEmpty()) {
            // No child variable -> replace by a LIMIT 1
            return iqFactory.createUnaryIQTree(iqFactory.createSliceNode(0, 1), child)
                    .normalizeForOptimization(variableGenerator);
        }

        var construction = UnaryIQTreeDecomposition.of(child, ConstructionNode.class);
        if (construction.isPresent()) {
            if (isConstructionNodeWithoutChildVariablesAndDeterministic(construction.getNode()))
                // Replaces the DISTINCT by a LIMIT 1
                return iqFactory.createUnaryIQTree(iqFactory.createSliceNode(0, 1), child)
                        .normalizeForOptimization(variableGenerator);

            return liftBindingConstructionChild(construction.getNode(), construction.getChild(), treeCache, variableGenerator);
        }

        if (child instanceof ValuesNode)
            return simplifyValuesNode((ValuesNode) child);

        // DISTINCT [ORDER BY] [FILTER] UNION
        var orderBy = UnaryIQTreeDecomposition.of(child, OrderByNode.class);
        var filter = UnaryIQTreeDecomposition.of(orderBy.getTail(), FilterNode.class);
        var union = NaryIQTreeDecomposition.of(filter.getTail(), UnionNode.class);
        if (union.isPresent()) {
            ImmutableList<IQTree> unionChildren = union.getChildren();
            ImmutableList<IQTree> newUnionChildren = unionChildren.stream()
                    .map(c -> simplifyUnionChild(c, variableGenerator))
                    .collect(ImmutableCollectors.toList());

            if (!unionChildren.equals(newUnionChildren))
                return iqTreeTools.createOptionalAncestorsUnaryIQTree(
                                ImmutableList.of(
                                        Optional.of(distinctNode),
                                        orderBy.getOptionalNode(),
                                        filter.getOptionalNode()),
                                iqFactory.createNaryIQTree(union.getNode(), newUnionChildren))
                        .normalizeForOptimization(variableGenerator);
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
                                                IQTree grandChild,IQTreeCache treeCache,
                                                VariableGenerator variableGenerator) {
        // Non-final
        InjectiveBindingLiftState state = new InjectiveBindingLiftState(constructionNode, grandChild, variableGenerator,
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

    private IQTree simplifyValuesNode(ValuesNode valuesNode) {
        return iqFactory.createValuesNode(valuesNode.getOrderedVariables(),
                valuesNode.getValues().stream()
                        .distinct()
                        .collect(ImmutableCollectors.toList()));
    }

    private IQTree simplifyUnionChild(IQTree unionChild, VariableGenerator variableGenerator) {
        if (unionChild.isDistinct())
            return unionChild;

        if (unionChild instanceof ValuesNode)
            return simplifyValuesNode((ValuesNode) unionChild);

        var construction = UnaryIQTreeDecomposition.of(unionChild, ConstructionNode.class);
        if (construction.isPresent()) {
            // No child variable and no non-deterministic function used -> inserts a LIMIT 1
            if (isConstructionNodeWithoutChildVariablesAndDeterministic(construction.getNode()))
                return iqFactory.createUnaryIQTree(iqFactory.createSliceNode(0, 1),
                        unionChild)
                        .normalizeForOptimization(variableGenerator);
        }
        return unionChild;
    }

    private boolean isConstructionNodeWithoutChildVariablesAndDeterministic(ConstructionNode constructionNode) {
        return constructionNode.getChildVariables().isEmpty()
                && (constructionNode.getSubstitution().rangeAllMatch(this::isConstantOrDeterministic));
    }


    private boolean isConstantOrDeterministic(ImmutableTerm term) {
        if (term instanceof Constant)
            return true;
        if (term instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm functionalTerm = (ImmutableFunctionalTerm) term;
            if (!functionalTerm.getFunctionSymbol().isDeterministic())
                return false;
            return functionalTerm.getTerms().stream().allMatch(this::isConstantOrDeterministic);
        }
        throw new MinorOntopInternalBugException("The term was expected to be grounded");
    }

    private IQTree createNormalizedTree(InjectiveBindingLiftState state, IQTreeCache treeCache, VariableGenerator variableGenerator) {

        IQTree grandChildTree = state.getGrandChildTree();
        // No need to have a DISTINCT as a grand child
        IQTree newGrandChildTree = UnaryIQTreeDecomposition.of(grandChildTree, DistinctNode.class)
                .getTail();

        IQTreeCache childTreeCache = iqFactory.createIQTreeCache(newGrandChildTree == grandChildTree);

        IQTree newChildTree = state.getChildConstructionNode()
                .map(c -> iqFactory.createUnaryIQTree(c, newGrandChildTree, childTreeCache))
                // To be normalized again in case a DISTINCT was present as a grand child.
                // NB: does nothing if it is not the case
                .map(t -> t.normalizeForOptimization(variableGenerator))
                .orElse(newGrandChildTree);

        IQTree distinctTree = createDistinctTree(iqFactory.createDistinctNode(), newChildTree,
                treeCache.declareAsNormalizedForOptimizationWithEffect());

        return iqTreeTools.createAncestorsUnaryIQTree(state.getAncestors().reverse(), distinctTree)
                // Recursive (for merging top construction nodes)
                .normalizeForOptimization(variableGenerator);
    }
}
