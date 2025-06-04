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
import it.unibz.inf.ontop.iq.transform.AbstractIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.stream.IntStream;

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

        return tree.acceptVisitor(new VariableRemoverTransformer(variablesToRemove, variableGenerator));
    }

    /**
     * Is expected to always affect the tree it receives as its argument.
     *
     * {@code ---> } Not called for trees not having any variable to remove.
     *
     */
    private class VariableRemoverTransformer extends AbstractIQTreeVisitingTransformer {
        protected final ImmutableSet<Variable> variablesToRemove;
        protected final VariableGenerator variableGenerator;

        private VariableRemoverTransformer(ImmutableSet<Variable> variablesToRemove,
                                          VariableGenerator variableGenerator) {
            this.variablesToRemove = variablesToRemove;
            this.variableGenerator = variableGenerator;
        }

        private ImmutableSet<Variable> getVariablesToKeep(IQTree tree) {
            return Sets.difference(tree.getVariables(), variablesToRemove).immutableCopy();
        }

        @Override
        public IQTree transformIntensionalData(IntensionalDataNode rootNode) {
            return rootNode;
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
        public IQTree transformTrue(TrueNode rootNode) {
            return rootNode;
        }

        @Override
        public IQTree transformValues(ValuesNode valuesNode) {

            ImmutableList<Variable> orderedVariables = valuesNode.getOrderedVariables();
            int arity = orderedVariables.size();

            ImmutableList<Integer> indexesToRemove = IntStream.range(0, arity)
                    .filter(i -> variablesToRemove.contains(orderedVariables.get(i)))
                    .boxed()
                    .collect(ImmutableCollectors.toList());

            ImmutableList<Variable> newOrderedVariables = IntStream.range(0, arity)
                    .filter(i -> !indexesToRemove.contains(i))
                    .mapToObj(orderedVariables::get)
                    .collect(ImmutableCollectors.toList());

            ImmutableList<ImmutableList<Constant>> newValues = valuesNode.getValues().stream()
                    .map(t -> IntStream.range(0, arity)
                            .filter(i -> !indexesToRemove.contains(i))
                            .mapToObj(t::get)
                            .collect(ImmutableCollectors.toList()))
                    .collect(ImmutableCollectors.toList());

            return iqFactory.createValuesNode(newOrderedVariables, newValues);
        }

        @Override
        public IQTree transformConstruction(UnaryIQTree tree, ConstructionNode rootNode, IQTree child) {
            ImmutableSet<Variable> variablesToKeep = getVariablesToKeep(tree);

            ConstructionSubstitutionNormalizer.ConstructionSubstitutionNormalization normalization = substitutionNormalizer.normalizeSubstitution(
                    rootNode.getSubstitution(), variablesToKeep);

            ConstructionNode newConstructionNode = iqFactory.createConstructionNode(variablesToKeep,
                    normalization.getNormalizedSubstitution());
            IQTree newChild = normalization.updateChild(child, variableGenerator);

            // New removal opportunities may appear in the subtree ("RECURSIVE")
            return iqFactory.createUnaryIQTree(newConstructionNode, newChild)
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

        @Override
        public IQTree transformFilter(UnaryIQTree tree, FilterNode rootNode, IQTree child) {
            return iqFactory.createUnaryIQTree(rootNode, transformChild(child));
        }

        @Override
        public IQTree transformFlatten(UnaryIQTree tree, FlattenNode rootNode, IQTree child) {
            return iqFactory.createUnaryIQTree(rootNode, transformChild(child));
        }

        @Override
        public IQTree transformDistinct(UnaryIQTree tree, DistinctNode rootNode, IQTree child) {
            return iqFactory.createUnaryIQTree(rootNode, transformChild(child));
        }

        @Override
        public IQTree transformSlice(UnaryIQTree tree, SliceNode rootNode, IQTree child) {
            return iqFactory.createUnaryIQTree(rootNode, transformChild(child));
        }

        @Override
        public IQTree transformOrderBy(UnaryIQTree tree, OrderByNode rootNode, IQTree child) {
            return iqFactory.createUnaryIQTree(rootNode, transformChild(child));
        }

        @Override
        public IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            /*
             * If filter condition involves a variable to remove, we are in the special case
             *  where the right child can be removed (see LeftJoinNodeImpl.applyFilterToVariableNonRequirement)
             */
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
            ImmutableSet<Variable> childVariablesToRemove = Sets.intersection(child.getVariables(), variablesToRemove).immutableCopy();
            return childVariablesToRemove.isEmpty()
                    ? child
                    : childVariablesToRemove.equals(variablesToRemove)
                        ? transformChild(child)
                        : child.acceptVisitor(new VariableRemoverTransformer(childVariablesToRemove, variableGenerator));
        }

        @Override
        public IQTree transformUnion(NaryIQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
            ImmutableSet<Variable> variablesToKeep = getVariablesToKeep(tree);

            if (variablesToKeep.equals(tree.getVariables()))
                return tree.normalizeForOptimization(variableGenerator);

            IQTree unionTree = iqTreeTools.createUnionTree(variablesToKeep,
                    NaryIQTreeTools.transformChildren(children,
                            c -> iqTreeTools.unaryIQTreeBuilder(variablesToKeep).build(c)));

            // New removal opportunities may appear in the subtree ("RECURSIVE")
            return unionTree.normalizeForOptimization(variableGenerator);
        }
    }
}
