package it.unibz.inf.ontop.iq.optimizer.impl.lj;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.ForeignKeyConstraint;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.dbschema.UniqueConstraint;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OptimizationSingletons;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.node.normalization.impl.RightProvenanceNormalizer;
import it.unibz.inf.ontop.iq.optimizer.LeftJoinIQOptimizer;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Singleton
public class CardinalitySensitiveJoinTransferLJOptimizer implements LeftJoinIQOptimizer {

    private final RequiredExtensionalDataNodeExtractor requiredDataNodeExtractor;
    private final RightProvenanceNormalizer rightProvenanceNormalizer;
    private final OptimizationSingletons optimizationSingletons;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    protected CardinalitySensitiveJoinTransferLJOptimizer(RequiredExtensionalDataNodeExtractor requiredDataNodeExtractor,
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

        Transformer transformer = new Transformer(initialTree::getVariableNullability,
                query.getVariableGenerator(),
                requiredDataNodeExtractor,
                rightProvenanceNormalizer,
                optimizationSingletons);

        IQTree newTree = initialTree.acceptTransformer(transformer);

        return newTree.equals(initialTree)
                ? query
                : iqFactory.createIQ(query.getProjectionAtom(), newTree);
    }

    protected static class Transformer extends AbstractJoinTransferLJTransformer {

        protected Transformer(Supplier<VariableNullability> variableNullabilitySupplier,
                              VariableGenerator variableGenerator, RequiredExtensionalDataNodeExtractor requiredDataNodeExtractor,
                              RightProvenanceNormalizer rightProvenanceNormalizer,
                              OptimizationSingletons optimizationSingletons) {
            super(variableNullabilitySupplier, variableGenerator, requiredDataNodeExtractor, rightProvenanceNormalizer, optimizationSingletons);
        }


        @Override
        protected Optional<SelectedNode> selectForTransfer(ExtensionalDataNode rightDataNode,
                                                           ImmutableMultimap<RelationDefinition, ExtensionalDataNode> leftMultimap) {
            RelationDefinition rightRelation = rightDataNode.getRelationDefinition();

            ImmutableMap<Integer, ? extends VariableOrGroundTerm> rightArgumentMap = rightDataNode.getArgumentMap();

            // Unique constraints
            ImmutableList<UniqueConstraint> uniqueConstraints = rightRelation.getUniqueConstraints();
            if (!uniqueConstraints.isEmpty()) {
                ImmutableSet<ExtensionalDataNode> sameRelationLeftNodes = Optional.ofNullable(leftMultimap.get(rightRelation))
                        .map(Collection::stream)
                        .orElseGet(Stream::empty)
                        .collect(ImmutableCollectors.toSet());

                Optional<ImmutableList<Integer>> matchingIndexes = uniqueConstraints.stream()
                        .map(uc -> matchUniqueConstraint(uc, sameRelationLeftNodes, rightArgumentMap))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .findAny();

                if (matchingIndexes.isPresent())
                    return Optional.of(new SelectedNode(matchingIndexes.get(), rightDataNode));
            }

            // Foreign keys
            return leftMultimap.keys().stream()
                    .flatMap(leftRelation -> leftRelation.getForeignKeys().stream()
                            .filter(fk -> fk.getReferencedRelation().equals(rightRelation))
                            .map(fk -> matchForeignKey(fk, leftMultimap.get(leftRelation), rightArgumentMap))
                            .filter(Optional::isPresent)
                            .map(Optional::get))
                    .findAny()
                    .map(indexes -> new SelectedNode(indexes, rightDataNode));
        }

        /**
         * Temporary restriction to avoid overlap with the existing LJ optimization techniques
         *
         * Ignores the case where a single extensional data node appears on the right.
         *
         * TODO: remove this restriction (after releasing 4.1)
         */
        @Override
        protected Stream<ExtensionalDataNode> extractRightDataNodes(IQTree rightChild) {
            IQTree rightChildAfterConstructionNode = Optional.of(rightChild)
                    .filter(c -> c.getRootNode() instanceof ConstructionNode)
                    .map(c -> ((UnaryIQTree) c).getChild())
                    .orElse(rightChild);

            if (rightChildAfterConstructionNode instanceof ExtensionalDataNode)
                return Stream.empty();

            return super.extractRightDataNodes(rightChild);
        }


        @Override
        protected IQTree transformBySearchingFromScratch(IQTree tree) {
            Transformer newTransformer = new Transformer(tree::getVariableNullability, variableGenerator, requiredDataNodeExtractor,
                    rightProvenanceNormalizer, optimizationSingletons);
            return tree.acceptTransformer(newTransformer);
        }
    }


}
