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
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.DistinctNormalizer;
import it.unibz.inf.ontop.iq.visit.impl.IQStateOptionalTransformer;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;


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

        if (child instanceof ValuesNode)
            return simplifyValuesNode((ValuesNode) child);

        var construction = UnaryIQTreeDecomposition.of(child, ConstructionNode.class);
        if (construction.isPresent()) {
            if (isConstructionNodeWithoutChildVariablesAndDeterministic(construction.getNode()))
                // Replaces the DISTINCT by a LIMIT 1
                return iqFactory.createUnaryIQTree(iqFactory.createSliceNode(0, 1), child)
                        .normalizeForOptimization(variableGenerator);

            return new Context(variableGenerator, treeCache)
                    .liftBindingConstructionChild(construction.getNode(), construction.getChild());
        }

        // DISTINCT [ORDER BY] [FILTER] UNION
        var orderBy = UnaryIQTreeDecomposition.of(child, OrderByNode.class);
        var filter = UnaryIQTreeDecomposition.of(orderBy, FilterNode.class);
        var union = NaryIQTreeTools.UnionDecomposition.of(filter);
        if (union.isPresent()) {
            ImmutableList<IQTree> newUnionChildren = union.transformChildren(
                    c -> simplifyUnionChild(c, variableGenerator));

            if (!union.getChildren().equals(newUnionChildren))
                return iqTreeTools.unaryIQTreeBuilder()
                        .append(distinctNode)
                        .append(orderBy.getOptionalNode())
                        .append(filter.getOptionalNode())
                        .build(iqFactory.createNaryIQTree(union.getNode(), newUnionChildren))
                        .normalizeForOptimization(variableGenerator);
        }

        return iqTreeTools.unaryIQTreeBuilder()
                .append(iqTreeTools.createOptionalDistinctNode(!child.isDistinct()),
                        child.equals(initialChild)
                            ? treeCache::declareAsNormalizedForOptimizationWithoutEffect
                            : treeCache::declareAsNormalizedForOptimizationWithEffect)
                .build(child);
    }

    private class Context extends InjectiveBindingLiftContext {

        Context(VariableGenerator variableGenerator, IQTreeCache treeCache) {
            super(variableGenerator, coreSingletons, treeCache);
        }

        IQTree liftBindingConstructionChild(ConstructionNode constructionNode, IQTree grandChild) {

            NormalizationContext.NormalizationState2<ConstructionNode, InjectiveBindingLiftContext.ConstructionSubTree> finalState = IQStateOptionalTransformer.reachFinalState(
                    new NormalizationContext.NormalizationState2<>(new ConstructionSubTree(constructionNode, grandChild)),
                    this::liftBindings,
                    MAX_ITERATIONS);

            return asIQTree(finalState);
        }

        protected IQTree asIQTree(NormalizationContext.NormalizationState2<ConstructionNode, InjectiveBindingLiftContext.ConstructionSubTree> state) {

            IQTree grandChildTree = state.getSubTree().getChild();
            // No need to have a DISTINCT as a grand child
            IQTree newGrandChildTree = IQTreeTools.UnaryIQTreeDecomposition.of(grandChildTree, DistinctNode.class)
                    .getTail();

            IQTreeCache childTreeCache = iqFactory.createIQTreeCache(newGrandChildTree == grandChildTree);

            IQTree newChildTree = state.getSubTree().getOptionalConstructionNode()
                    .map(c -> iqFactory.createUnaryIQTree(c, newGrandChildTree, childTreeCache))
                    // To be normalized again in case a DISTINCT was present as a grand child.
                    // NB: does nothing if it is not the case
                    .map(t -> t.normalizeForOptimization(variableGenerator))
                    .orElse(newGrandChildTree);

            return iqTreeTools.unaryIQTreeBuilder()
                    .append(state.getAncestors())
                    .append(
                            iqTreeTools.createOptionalDistinctNode(!newChildTree.isDistinct()),
                            treeCache::declareAsNormalizedForOptimizationWithEffect)
                    .build(newChildTree)
                    // Recursive (for merging top construction nodes)
                    .normalizeForOptimization(variableGenerator);
        }
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
}
