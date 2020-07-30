package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.ConstructionSubstitutionNormalizer;
import it.unibz.inf.ontop.iq.node.normalization.NotRequiredVariableRemover;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

@Singleton
public class NotRequiredVariableRemoverImpl implements NotRequiredVariableRemover {

    private final CoreSingletons coreSingletons;

    @Inject
    protected NotRequiredVariableRemoverImpl(CoreSingletons coreSingletons) {
        this.coreSingletons = coreSingletons;
    }

    @Override
    public IQTree optimize(IQTree tree, ImmutableSet<Variable> requiredVariables, VariableGenerator variableGenerator) {
        ImmutableSet<Variable> variables = tree.getVariables();
        if (variables.equals(requiredVariables))
            return tree;

        ImmutableSet<Variable> notInternallyRequiredVariables = tree.getNotInternallyRequiredVariables();
        if (notInternallyRequiredVariables.isEmpty())
            return tree;

        Sets.SetView<Variable> variablesToRemove = Sets.intersection(
                Sets.difference(variables, requiredVariables), notInternallyRequiredVariables);

        if (variablesToRemove.isEmpty())
            return tree;

        return removeNonRequiredVariables(tree, variablesToRemove.immutableCopy(), variableGenerator);
    }

    protected IQTree removeNonRequiredVariables(IQTree tree, ImmutableSet<Variable> variablesToRemove,
                                                VariableGenerator variableGenerator) {
        return new VariableRemoverTransformer(variablesToRemove, variableGenerator, coreSingletons).transform(tree);
    }

    /**
     * Is expected to always have an effect on the tree it receives as argument.
     *
     * ---> Not called for trees not having any variable to remove.
     *
     */
    protected static class VariableRemoverTransformer implements IQTreeVisitingTransformer {
        protected final ImmutableSet<Variable> variablesToRemove;
        protected final IntermediateQueryFactory iqFactory;
        protected final SubstitutionFactory substitutionFactory;
        protected final CoreSingletons coreSingletons;
        protected final VariableGenerator variableGenerator;
        protected final ConstructionSubstitutionNormalizer substitutionNormalizer;

        public VariableRemoverTransformer(ImmutableSet<Variable> variablesToRemove,
                                          VariableGenerator variableGenerator, CoreSingletons coreSingletons) {
            this.variablesToRemove = variablesToRemove;
            this.variableGenerator = variableGenerator;
            this.iqFactory = coreSingletons.getIQFactory();
            this.substitutionFactory = coreSingletons.getSubstitutionFactory();
            this.coreSingletons = coreSingletons;
            this.substitutionNormalizer = coreSingletons.getConstructionSubstitutionNormalizer();
        }

        /**
         * To be overridden by sub-classes
         */
        protected IQTreeTransformer createNewTransformer(ImmutableSet<Variable> variablesToRemove) {
            return new VariableRemoverTransformer(variablesToRemove, variableGenerator, coreSingletons);
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
            return iqFactory.createEmptyNode(
                    Sets.difference(rootNode.getVariables(), variablesToRemove)
                            .immutableCopy());
        }

        @Override
        public IQTree transformTrue(TrueNode rootNode) {
            return rootNode;
        }

        @Override
        public IQTree transformNonStandardLeafNode(LeafIQTree rootNode) {
            throw new RuntimeException("No generic method for non-standard nodes. Please extend this class");
        }

        @Override
        public IQTree transformConstruction(IQTree tree, ConstructionNode rootNode, IQTree child) {
            ImmutableSet<Variable> variablesToKeep = Sets.difference(tree.getVariables(), variablesToRemove)
                    .immutableCopy();

            ConstructionSubstitutionNormalizer.ConstructionSubstitutionNormalization normalization = substitutionNormalizer.normalizeSubstitution(
                    rootNode.getSubstitution(), variablesToKeep);

            ConstructionNode newConstructionNode = iqFactory.createConstructionNode(variablesToKeep,
                    normalization.getNormalizedSubstitution());
            IQTree newChild = normalization.updateChild(child);

            // New removal opportunities may appear in the subtree ("RECURSIVE")
            return iqFactory.createUnaryIQTree(newConstructionNode, newChild)
                    .normalizeForOptimization(variableGenerator);
        }

        @Override
        public IQTree transformAggregation(IQTree tree, AggregationNode aggregationNode, IQTree child) {
            AggregationNode newAggregationNode = iqFactory.createAggregationNode(aggregationNode.getGroupingVariables(),
                    // Can only concern variables from the substitutions, the grouping ones being required
                    substitutionFactory.getSubstitution(
                            aggregationNode.getSubstitution().getImmutableMap().entrySet().stream()
                                    .filter(e -> !variablesToRemove.contains(e.getKey()))
                                    .collect(ImmutableCollectors.toMap())
                    ));

            // New removal opportunities may appear in the subtree ("RECURSIVE")
            return iqFactory.createUnaryIQTree(newAggregationNode, child)
                    .normalizeForOptimization(variableGenerator);
        }

        @Override
        public IQTree transformFilter(IQTree tree, FilterNode rootNode, IQTree child) {
            return iqFactory.createUnaryIQTree(
                    rootNode,
                    transform(child));
        }

        @Override
        public IQTree transformDistinct(IQTree tree, DistinctNode rootNode, IQTree child) {
            return iqFactory.createUnaryIQTree(
                    rootNode,
                    transform(child));
        }

        @Override
        public IQTree transformSlice(IQTree tree, SliceNode rootNode, IQTree child) {
            return iqFactory.createUnaryIQTree(
                    rootNode,
                    transform(child));
        }

        @Override
        public IQTree transformOrderBy(IQTree tree, OrderByNode rootNode, IQTree child) {
            return iqFactory.createUnaryIQTree(
                    rootNode,
                    transform(child));
        }

        @Override
        public IQTree transformNonStandardUnaryNode(IQTree tree, UnaryOperatorNode rootNode, IQTree child) {
            throw new RuntimeException("No generic method for non-standard nodes. Please extend this class");
        }

        @Override
        public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            return iqFactory.createBinaryNonCommutativeIQTree(
                    rootNode,
                    transformNonUniqueChild(leftChild),
                    transformNonUniqueChild(rightChild));
        }

        /**
         * Transforms the non unique child only if needed
         */
        private IQTree transformNonUniqueChild(IQTree child) {
            Sets.SetView<Variable> childVariablesToRemove = Sets.intersection(child.getVariables(), variablesToRemove);

            return childVariablesToRemove.isEmpty()
                    ? child
                    : childVariablesToRemove.equals(variablesToRemove)
                        ? transform(child)
                        : createNewTransformer(childVariablesToRemove.immutableCopy()).transform(child);
        }

        @Override
        public IQTree transformNonStandardBinaryNonCommutativeNode(IQTree tree, BinaryNonCommutativeOperatorNode rootNode,
                                                                   IQTree leftChild, IQTree rightChild) {
            throw new RuntimeException("No generic method for non-standard nodes. Please extend this class");
        }

        @Override
        public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            return iqFactory.createNaryIQTree(
                    rootNode,
                    children.stream()
                            .map(this::transformNonUniqueChild)
                            .collect(ImmutableCollectors.toList()));
        }

        @Override
        public IQTree transformUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
            UnionNode newUnionNode = iqFactory.createUnionNode(Sets.difference(rootNode.getVariables(), variablesToRemove)
                    .immutableCopy());

            // New removal opportunities may appear in the subtree ("RECURSIVE")
            return iqFactory.createNaryIQTree(newUnionNode, children)
                    .normalizeForOptimization(variableGenerator);
        }

        @Override
        public IQTree transformNonStandardNaryNode(IQTree tree, NaryOperatorNode rootNode,
                                                   ImmutableList<IQTree> children) {
            throw new RuntimeException("No generic method for non-standard nodes. Please extend this class");
        }
    }


}
