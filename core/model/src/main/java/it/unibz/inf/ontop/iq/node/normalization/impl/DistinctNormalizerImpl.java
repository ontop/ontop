package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.DistinctNormalizer;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;


@Singleton
public class DistinctNormalizerImpl implements DistinctNormalizer {

    private static final int MAX_ITERATIONS = 10000;

    private final CoreSingletons coreSingletons;

    @Inject
    private DistinctNormalizerImpl(CoreSingletons coreSingletons) {
        this.coreSingletons = coreSingletons;
    }

    @Override
    public IQTree normalizeForOptimization(DistinctNode distinctNode, IQTree initialChild,
                                           VariableGenerator variableGenerator, IQTreeCache treeCache) {
        Context context = new Context(distinctNode, initialChild, variableGenerator, treeCache);
        return context.normalize();
    }

    private class Context extends InjectiveBindingLiftContext {

        private final DistinctNode distinctNode;
        private final IQTree initialChild;

        Context(DistinctNode distinctNode, IQTree initialChild, VariableGenerator variableGenerator, IQTreeCache treeCache) {
            super(variableGenerator, coreSingletons, treeCache);
            this.distinctNode = distinctNode;
            this.initialChild = initialChild;
        }

        IQTree normalize() {
            IQTree child = initialChild.normalizeForOptimization(variableGenerator);
            if (child.isDistinct())
                return child;

            if (child.getVariables().isEmpty())
                return createLimit1(child);

            if (child instanceof ValuesNode)
                return simplifyValuesNode((ValuesNode) child);

            var construction = UnaryIQTreeDecomposition.of(child, ConstructionNode.class);
            if (construction.isPresent()) {
                if (isConstructionNodeWithoutChildVariablesAndDeterministic(construction.getNode()))
                    return createLimit1(child);

                return liftBindingConstructionChild(construction.getNode(), construction.getChild());
            }

            // DISTINCT [ORDER BY] [FILTER] UNION
            var orderBy = UnaryIQTreeDecomposition.of(child, OrderByNode.class);
            var filter = UnaryIQTreeDecomposition.of(orderBy, FilterNode.class);
            var union = NaryIQTreeTools.UnionDecomposition.of(filter);
            if (union.isPresent()) {
                ImmutableList<IQTree> newUnionChildren = union.transformChildren(this::simplifyUnionChild);

                if (!union.getChildren().equals(newUnionChildren))
                    return iqTreeTools.unaryIQTreeBuilder()
                            .append(distinctNode)
                            .append(orderBy.getOptionalNode())
                            .append(filter.getOptionalNode())
                            .build(iqFactory.createNaryIQTree(union.getNode(), newUnionChildren))
                            .normalizeForOptimization(variableGenerator);
            }

            return iqTreeTools.unaryIQTreeBuilder()
                    .append(distinctNode,
                            child.equals(initialChild)
                                    ? treeCache.declareAsNormalizedForOptimizationWithoutEffect()
                                    : treeCache.declareAsNormalizedForOptimizationWithEffect())
                    .build(child);
        }

        IQTree createLimit1(IQTree child) {
            // Replaces the DISTINCT by a LIMIT 1
            return iqFactory.createUnaryIQTree(iqFactory.createSliceNode(0, 1), child)
                    .normalizeForOptimization(variableGenerator);
        }

        IQTree liftBindingConstructionChild(ConstructionNode constructionNode, IQTree grandChild) {
            var initial = State.<ConstructionNode, UnarySubTree<ConstructionNode>>initial(
                    UnarySubTree.of(Optional.of(constructionNode), grandChild));

            var state = initial.reachFinal(MAX_ITERATIONS, this::liftBindings);

            return asIQTree(state);
        }

        protected IQTree asIQTree(State<ConstructionNode, UnarySubTree<ConstructionNode>> state) {

            IQTree grandChildTree = state.getSubTree().getChild();
            // No need to have a DISTINCT as a grand child
            IQTree newGrandChildTree = IQTreeTools.UnaryIQTreeDecomposition.of(grandChildTree, DistinctNode.class)
                    .getTail();

            IQTreeCache childTreeCache = iqFactory.createIQTreeCache(newGrandChildTree == grandChildTree);

            IQTree newChildTree = state.getSubTree().getOptionalNode()
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

        IQTree simplifyValuesNode(ValuesNode valuesNode) {
            return iqFactory.createValuesNode(valuesNode.getVariables(),
                    valuesNode.getValueMaps().stream()
                            .distinct()
                            .collect(ImmutableCollectors.toList()));
        }

        IQTree simplifyUnionChild(IQTree unionChild) {
            if (unionChild.isDistinct())
                return unionChild;

            if (unionChild instanceof ValuesNode)
                return simplifyValuesNode((ValuesNode) unionChild);

            var construction = UnaryIQTreeDecomposition.of(unionChild, ConstructionNode.class);
            if (construction.isPresent()) {
                if (isConstructionNodeWithoutChildVariablesAndDeterministic(construction.getNode()))
                    return createLimit1(unionChild);
            }
            return unionChild;
        }

    }

    private boolean isConstructionNodeWithoutChildVariablesAndDeterministic(ConstructionNode constructionNode) {
        return constructionNode.getChildVariables().isEmpty()
                && constructionNode.getSubstitution()
                        .rangeAllMatch(this::isConstantOrDeterministic);
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
