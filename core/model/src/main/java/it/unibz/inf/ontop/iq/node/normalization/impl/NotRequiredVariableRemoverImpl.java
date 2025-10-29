package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.ConstructionSubstitutionNormalizer;
import it.unibz.inf.ontop.iq.node.normalization.NotRequiredVariableRemover;
import it.unibz.inf.ontop.iq.visit.impl.DefaultRecursiveIQTreeVisitingTransformerWithVariableGenerator;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

@Singleton
public class NotRequiredVariableRemoverImpl implements NotRequiredVariableRemover {

    protected final IntermediateQueryFactory iqFactory;
    protected final IQTreeTools iqTreeTools;
    protected final ConstructionSubstitutionNormalizer substitutionNormalizer;

    @Inject
    protected NotRequiredVariableRemoverImpl(CoreSingletons coreSingletons) {
        this.iqFactory = coreSingletons.getIQFactory();
        this.iqTreeTools = coreSingletons.getIQTreeTools();
        this.substitutionNormalizer = coreSingletons.getConstructionSubstitutionNormalizer();
    }

    @Override
    public IQTree optimize(IQTree tree, ImmutableSet<Variable> requiredVariables, VariableGenerator variableGenerator) {
        ImmutableSet<Variable> variables = tree.getVariables();
        if (variables.equals(requiredVariables))
            return tree;

        ImmutableSet<Variable> variablesToRemove = tree.getVariableNonRequirement()
                .computeVariablesToRemove(variables, requiredVariables);

        return getTransformer(variablesToRemove, variableGenerator).transform(tree);
    }

    private Transformer getTransformer(ImmutableSet<Variable> variablesToRemove, VariableGenerator variableGenerator) {
        return new Transformer(variablesToRemove, variableGenerator);
    }

    /**
     * Is expected to always affect the tree it receives as its argument.
     *
     * {@code ---> } Not called for trees not having any variable to remove.
     *
     */
    private class Transformer extends DefaultRecursiveIQTreeVisitingTransformerWithVariableGenerator {
        private final ImmutableSet<Variable> variablesToRemove;

        Transformer(ImmutableSet<Variable> variablesToRemove, VariableGenerator variableGenerator) {
            super(NotRequiredVariableRemoverImpl.this.iqFactory, variableGenerator);
            this.variablesToRemove = variablesToRemove;
        }

        private ImmutableSet<Variable> getVariablesToKeep(IQTree tree) {
            return Sets.difference(tree.getVariables(), variablesToRemove).immutableCopy();
        }

        @Override
        public IQTree transformExtensionalData(ExtensionalDataNode rootNode) {
            ImmutableMap<Integer, ? extends VariableOrGroundTerm> newArgumentMap = rootNode.getArgumentMap().entrySet().stream()
                    .filter(e -> !variablesToRemove.contains(e.getValue()))
                    .collect(ImmutableCollectors.toMap());

            return iqFactory.createExtensionalDataNode(rootNode.getRelationDefinition(), newArgumentMap);
        }

        @Override
        public IQTree transformEmpty(EmptyNode rootNode) {
            return iqFactory.createEmptyNode(getVariablesToKeep(rootNode));
        }

        @Override
        public IQTree transformValues(ValuesNode valuesNode) {
            var variablesToKeep = getVariablesToKeep(valuesNode);
            return iqFactory.createValuesNode(
                    variablesToKeep,
                    valuesNode.getValueMaps().stream()
                            .map(m -> m.entrySet().stream()
                                    .filter(e -> variablesToKeep.contains(e.getKey()))
                                    .collect(ImmutableCollectors.toMap()))
                            .collect(ImmutableCollectors.toList()));
        }

        @Override
        public IQTree transformConstruction(UnaryIQTree tree, ConstructionNode rootNode, IQTree child) {
            // New removal opportunities may appear in the subtree ("RECURSIVE")
            return substitutionNormalizer.createNormalizedConstructionTree(
                            rootNode.getSubstitution(),
                            getVariablesToKeep(tree),
                            child)
                    .normalizeForOptimization(variableGenerator);
        }

        @Override
        public IQTree transformAggregation(UnaryIQTree tree, AggregationNode aggregationNode, IQTree child) {
            AggregationNode newAggregationNode = iqFactory.createAggregationNode(aggregationNode.getGroupingVariables(),
                    // Can only concern variables from the substitutions, the grouping ones being required
                    aggregationNode.getSubstitution().removeFromDomain(variablesToRemove));

            // New removal opportunities may appear in the subtree ("RECURSIVE")
            return iqFactory.createUnaryIQTree(newAggregationNode, child)
                    .normalizeForOptimization(variableGenerator);
        }

        /**
         * If the filter condition involves a variable to remove,
         * then we are in the special case where the right child can be removed
         *
         *  @see it.unibz.inf.ontop.iq.node.impl.LeftJoinNodeImpl#computeVariableNonRequirement(IQTree, IQTree)
         */
        @Override
        public IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            if (rootNode.getOptionalFilterCondition()
                    .filter(c -> c.getVariableStream().anyMatch(variablesToRemove::contains))
                    .isPresent())
                return transformJoinChild(leftChild);

            return iqFactory.createBinaryNonCommutativeIQTree(
                    rootNode,
                    transformJoinChild(leftChild),
                    transformJoinChild(rightChild));
        }

        @Override
        public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            return iqFactory.createNaryIQTree(
                    rootNode,
                    NaryIQTreeTools.transformChildren(children, this::transformJoinChild));
        }

        /**
         * Transforms a given child only if necessary
         */
        private IQTree transformJoinChild(IQTree child) {
            var childVariablesToRemove = Sets.intersection(child.getVariables(), variablesToRemove).immutableCopy();
            return childVariablesToRemove.isEmpty()
                    ? child
                    : getTransformer(childVariablesToRemove, variableGenerator).transform(child);
        }

        @Override
        public IQTree transformUnion(NaryIQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
            var variablesToKeep = getVariablesToKeep(tree);
            IQTree unionTree = variablesToKeep.equals(tree.getVariables())
                    ? tree
                    : iqTreeTools.createUnionTree(variablesToKeep,
                            NaryIQTreeTools.transformChildren(children,
                            // TODO: inserts a possibly removable CONSTRUCT - can it be eliminated by using a child transformer?
                            c -> iqTreeTools.unaryIQTreeBuilder(variablesToKeep).build(c)));

            // New removal opportunities may appear in the subtree ("RECURSIVE")
            return unionTree.normalizeForOptimization(variableGenerator);
        }
    }
}
