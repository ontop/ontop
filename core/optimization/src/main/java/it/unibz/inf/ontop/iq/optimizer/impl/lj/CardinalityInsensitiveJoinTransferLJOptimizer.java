package it.unibz.inf.ontop.iq.optimizer.impl.lj;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.FunctionalDependency;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.node.impl.JoinOrFilterVariableNullabilityTools;
import it.unibz.inf.ontop.iq.node.normalization.impl.RightProvenanceNormalizer;
import it.unibz.inf.ontop.iq.optimizer.LeftJoinIQOptimizer;
import it.unibz.inf.ontop.iq.optimizer.impl.LookForDistinctOrLimit1TransformerImpl;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

@Singleton
public class CardinalityInsensitiveJoinTransferLJOptimizer implements LeftJoinIQOptimizer {

    private final RequiredExtensionalDataNodeExtractor requiredDataNodeExtractor;
    private final RightProvenanceNormalizer rightProvenanceNormalizer;
    private final JoinOrFilterVariableNullabilityTools variableNullabilityTools;
    private final CoreSingletons coreSingletons;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    protected CardinalityInsensitiveJoinTransferLJOptimizer(RequiredExtensionalDataNodeExtractor requiredDataNodeExtractor,
                                                            RightProvenanceNormalizer rightProvenanceNormalizer,
                                                            JoinOrFilterVariableNullabilityTools variableNullabilityTools,
                                                            CoreSingletons coreSingletons) {
        this.requiredDataNodeExtractor = requiredDataNodeExtractor;
        this.rightProvenanceNormalizer = rightProvenanceNormalizer;
        this.variableNullabilityTools = variableNullabilityTools;
        this.coreSingletons = coreSingletons;
        this.iqFactory = coreSingletons.getIQFactory();
    }

    @Override
    public IQ optimize(IQ query) {
        IQTree initialTree = query.getTree();

        IQTreeVisitingTransformer transformer = new LookForDistinctOrLimit1TransformerImpl(
                (childTree, parentTransformer) -> new CardinalityInsensitiveTransformer(
                        parentTransformer,
                        childTree::getVariableNullability,
                        query.getVariableGenerator(),
                        requiredDataNodeExtractor,
                        rightProvenanceNormalizer,
                        variableNullabilityTools,
                        coreSingletons),
                coreSingletons);

        IQTree newTree = initialTree.acceptTransformer(transformer);

        return newTree.equals(initialTree)
                ? query
                : iqFactory.createIQ(query.getProjectionAtom(), newTree);
    }

    protected static class CardinalityInsensitiveTransformer extends AbstractJoinTransferLJTransformer {

        private final IQTreeTransformer lookForDistinctTransformer;

        protected CardinalityInsensitiveTransformer(IQTreeTransformer lookForDistinctTransformer,
                                                    Supplier<VariableNullability> variableNullabilitySupplier,
                                                    VariableGenerator variableGenerator, RequiredExtensionalDataNodeExtractor requiredDataNodeExtractor,
                                                    RightProvenanceNormalizer rightProvenanceNormalizer,
                                                    JoinOrFilterVariableNullabilityTools variableNullabilityTools,
                                                    CoreSingletons coreSingletons) {
            super(variableNullabilitySupplier, variableGenerator, requiredDataNodeExtractor, rightProvenanceNormalizer,
                    variableNullabilityTools, coreSingletons);
            this.lookForDistinctTransformer = lookForDistinctTransformer;
        }


        @Override
        protected Optional<SelectedNode> selectForTransfer(ExtensionalDataNode rightDataNode,
                                                           ImmutableMultimap<RelationDefinition, ExtensionalDataNode> leftMultimap) {
            RelationDefinition rightRelation = rightDataNode.getRelationDefinition();

            ImmutableMap<Integer, ? extends VariableOrGroundTerm> rightArgumentMap = rightDataNode.getArgumentMap();

            ImmutableSet<ExtensionalDataNode> sameRelationLeftNodes = Optional.ofNullable(leftMultimap.get(rightRelation))
                    .stream()
                    .flatMap(Collection::stream)
                    .collect(ImmutableCollectors.toSet());

            if (sameRelationLeftNodes.isEmpty())
                return Optional.empty();

            ImmutableList<FunctionalDependency> functionalDependencies = rightRelation.getOtherFunctionalDependencies();
            if (!functionalDependencies.isEmpty()) {
                Optional<ImmutableList<Integer>> matchingIndexes = functionalDependencies.stream()
                        .map(fd -> matchFunctionalDependency(fd, sameRelationLeftNodes, rightArgumentMap))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .findAny();

                if (matchingIndexes.isPresent())
                    return Optional.of(new SelectedNode(matchingIndexes.get(), rightDataNode));
            }

            // Last chance: looks for a data node having the same non-nullable terms at the same position
            return matchIndexes(sameRelationLeftNodes, rightArgumentMap, ImmutableList.copyOf(rightArgumentMap.keySet()))
                    .map(idx -> new SelectedNode(idx, rightDataNode));
        }

        /**
         * Enables applying self-join elimination after the self-left-join has been reduced
         * to an inner join on the right child.
         */
        @Override
        protected boolean preventRecursiveOptimizationOnRightChild() {
            return true;
        }

        @Override
        protected IQTree transformBySearchingFromScratch(IQTree tree) {
            return lookForDistinctTransformer.transform(tree);
        }

        protected IQTree transformBySearchingFromScratchFromDistinctTree(IQTree tree) {
            return transformBySearchingFromScratchFromDistinctTree(tree, tree::getVariableNullability);
        }

        protected IQTree transformBySearchingFromScratchFromDistinctTree(IQTree tree, Supplier<VariableNullability> variableNullabilitySupplier) {
            CardinalityInsensitiveTransformer newTransformer = new CardinalityInsensitiveTransformer(lookForDistinctTransformer,
                    variableNullabilitySupplier, variableGenerator, requiredDataNodeExtractor,
                    rightProvenanceNormalizer, variableNullabilityTools, coreSingletons);
            return tree.acceptTransformer(newTransformer);
        }

        @Override
        protected IQTree preTransformLJRightChild(IQTree rightChild, Optional<ImmutableExpression> ljCondition) {

            return transformBySearchingFromScratchFromDistinctTree(rightChild,
                    () -> ljCondition
                            .map(c -> variableNullabilityTools.updateWithFilter(
                                            c,
                                            rightChild.getVariableNullability().getNullableGroups(),
                                            rightChild.getVariables()))
                            .orElseGet(rightChild::getVariableNullability));
        }

        @Override
        public IQTree transformConstruction(IQTree tree, ConstructionNode rootNode, IQTree child) {
            return transformUnaryNode(tree, rootNode, child, this::transformBySearchingFromScratchFromDistinctTree);
        }

        @Override
        public IQTree transformUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
            return transformNaryCommutativeNode(tree, rootNode, children, this::transformBySearchingFromScratchFromDistinctTree);
        }
    }


}
