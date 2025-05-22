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
import it.unibz.inf.ontop.iq.visit.impl.IQStateOptionalTransformer;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

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

        if (child instanceof ValuesNode)
            return simplifyValuesNode((ValuesNode) child);

        var construction = UnaryIQTreeDecomposition.of(child, ConstructionNode.class);
        if (construction.isPresent()) {
            if (isConstructionNodeWithoutChildVariablesAndDeterministic(construction.getNode()))
                // Replaces the DISTINCT by a LIMIT 1
                return iqFactory.createUnaryIQTree(iqFactory.createSliceNode(0, 1), child)
                        .normalizeForOptimization(variableGenerator);

            return new Context(variableGenerator, coreSingletons, treeCache)
                    .liftBindingConstructionChild(construction.getNode(), construction.getChild());
        }

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
                return iqTreeTools.unaryIQTreeBuilder()
                        .append(distinctNode)
                        .append(orderBy.getOptionalNode())
                        .append(filter.getOptionalNode())
                        .build(iqFactory.createNaryIQTree(union.getNode(), newUnionChildren))
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

    private class Context extends InjectiveBindingLiftContext {
        private final IQTreeCache treeCache;

        Context(VariableGenerator variableGenerator, CoreSingletons coreSingletons, IQTreeCache treeCache) {
            super(variableGenerator, coreSingletons);
            this.treeCache = treeCache;
        }

        IQTree liftBindingConstructionChild(ConstructionNode constructionNode, IQTree grandChild) {

            InjectiveBindingLiftState finalState = IQStateOptionalTransformer.reachFinalState(
                    new InjectiveBindingLiftState(constructionNode, grandChild),
                    InjectiveBindingLiftState::liftBindings,
                    MAX_ITERATIONS);

            return createNormalizedTree(finalState);
        }

        IQTree createNormalizedTree(InjectiveBindingLiftState state) {

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

            return iqTreeTools.createAncestorsUnaryIQTree(state.getAncestors(), distinctTree)
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
