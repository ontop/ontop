package it.unibz.inf.ontop.iq.optimizer.impl.lj;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.FunctionalDependency;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.dbschema.UniqueConstraint;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OptimizationSingletons;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.node.normalization.impl.RightProvenanceNormalizer;
import it.unibz.inf.ontop.iq.optimizer.LeftJoinIQOptimizer;
import it.unibz.inf.ontop.iq.optimizer.impl.LookForDistinctTransformerImpl;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Singleton
public class CardinalityInsensitiveJoinTransferLJOptimizer implements LeftJoinIQOptimizer {

    private final RequiredExtensionalDataNodeExtractor requiredDataNodeExtractor;
    private final RightProvenanceNormalizer rightProvenanceNormalizer;
    private final OptimizationSingletons optimizationSingletons;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    protected CardinalityInsensitiveJoinTransferLJOptimizer(RequiredExtensionalDataNodeExtractor requiredDataNodeExtractor,
                                                            RightProvenanceNormalizer rightProvenanceNormalizer,
                                                            OptimizationSingletons optimizationSingletons) {
        this.requiredDataNodeExtractor = requiredDataNodeExtractor;
        this.rightProvenanceNormalizer = rightProvenanceNormalizer;
        this.optimizationSingletons = optimizationSingletons;
        this.iqFactory = optimizationSingletons.getCoreSingletons().getIQFactory();
    }

    @Override
    public IQ optimize(IQ query) {
        IQTree initialTree = query.getTree();

        IQTreeVisitingTransformer transformer = new LookForDistinctTransformerImpl(
                (childTree, parentTransformer, optSingletons) -> new CardinalityInsensitiveTransformer(
                        parentTransformer,
                        childTree::getVariableNullability,
                        query.getVariableGenerator(),
                        requiredDataNodeExtractor,
                        rightProvenanceNormalizer,
                        optSingletons), optimizationSingletons);

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
                                                    OptimizationSingletons optimizationSingletons) {
            super(variableNullabilitySupplier, variableGenerator, requiredDataNodeExtractor, rightProvenanceNormalizer, optimizationSingletons);
            this.lookForDistinctTransformer = lookForDistinctTransformer;
        }


        @Override
        protected Optional<SelectedNode> selectForTransfer(ExtensionalDataNode rightDataNode,
                                                           ImmutableMultimap<RelationDefinition, ExtensionalDataNode> leftMultimap) {
            RelationDefinition rightRelation = rightDataNode.getRelationDefinition();

            ImmutableMap<Integer, ? extends VariableOrGroundTerm> rightArgumentMap = rightDataNode.getArgumentMap();

            ImmutableSet<ExtensionalDataNode> sameRelationLeftNodes = Optional.ofNullable(leftMultimap.get(rightRelation))
                    .map(Collection::stream)
                    .orElseGet(Stream::empty)
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

        @Override
        protected IQTree transformBySearchingFromScratch(IQTree tree) {
            return lookForDistinctTransformer.transform(tree);
        }

        protected IQTree transformBySearchingFromScratchFromDistinctTree(IQTree tree) {
            CardinalityInsensitiveTransformer newTransformer = new CardinalityInsensitiveTransformer(lookForDistinctTransformer,
                    tree::getVariableNullability, variableGenerator, requiredDataNodeExtractor,
                    rightProvenanceNormalizer, optimizationSingletons);
            return tree.acceptTransformer(newTransformer);
        }

        @Override
        protected IQTree preTransformLJRightChild(IQTree rightChild) {
            return transformBySearchingFromScratchFromDistinctTree(rightChild);
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
