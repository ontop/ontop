package it.unibz.inf.ontop.iq.optimizer.impl.lj;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.dbschema.UniqueConstraint;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.node.impl.JoinOrFilterVariableNullabilityTools;
import it.unibz.inf.ontop.iq.node.normalization.impl.RightProvenanceNormalizer;
import it.unibz.inf.ontop.iq.optimizer.LeftJoinIQOptimizer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

@Singleton
public class CardinalitySensitiveJoinTransferLJOptimizer implements LeftJoinIQOptimizer {

    private final RequiredExtensionalDataNodeExtractor requiredDataNodeExtractor;
    private final RightProvenanceNormalizer rightProvenanceNormalizer;
    private final CoreSingletons coreSingletons;
    private final IntermediateQueryFactory iqFactory;
    private final JoinOrFilterVariableNullabilityTools variableNullabilityTools;

    @Inject
    protected CardinalitySensitiveJoinTransferLJOptimizer(RequiredExtensionalDataNodeExtractor requiredDataNodeExtractor,
                                                          RightProvenanceNormalizer rightProvenanceNormalizer,
                                                          JoinOrFilterVariableNullabilityTools variableNullabilityTools,
                                                          CoreSingletons coreSingletons) {
        this.requiredDataNodeExtractor = requiredDataNodeExtractor;
        this.rightProvenanceNormalizer = rightProvenanceNormalizer;
        this.coreSingletons = coreSingletons;
        this.iqFactory = coreSingletons.getIQFactory();
        this.variableNullabilityTools = variableNullabilityTools;
    }

    @Override
    public IQ optimize(IQ query) {
        IQTree initialTree = query.getTree();

        Transformer transformer = new Transformer(initialTree::getVariableNullability,
                query.getVariableGenerator(),
                requiredDataNodeExtractor,
                rightProvenanceNormalizer,
                coreSingletons, variableNullabilityTools);

        IQTree newTree = initialTree.acceptTransformer(transformer);

        return newTree.equals(initialTree)
                ? query
                : iqFactory.createIQ(query.getProjectionAtom(), newTree);
    }

    protected static class Transformer extends AbstractJoinTransferLJTransformer {
        private final TermFactory termFactory;

        protected Transformer(Supplier<VariableNullability> variableNullabilitySupplier,
                              VariableGenerator variableGenerator, RequiredExtensionalDataNodeExtractor requiredDataNodeExtractor,
                              RightProvenanceNormalizer rightProvenanceNormalizer,
                              CoreSingletons coreSingletons, JoinOrFilterVariableNullabilityTools variableNullabilityTools) {
            super(variableNullabilitySupplier, variableGenerator, requiredDataNodeExtractor, rightProvenanceNormalizer, variableNullabilityTools, coreSingletons);
            this.termFactory = coreSingletons.getTermFactory();
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
                        .stream()
                        .flatMap(Collection::stream)
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

        @Override
        protected IQTree transformBySearchingFromScratch(IQTree tree) {
            Transformer newTransformer = new Transformer(tree::getVariableNullability, variableGenerator, requiredDataNodeExtractor,
                    rightProvenanceNormalizer, coreSingletons, variableNullabilityTools);
            return tree.acceptTransformer(newTransformer);
        }

        @Override
        protected IQTree preTransformLJRightChild(IQTree rightChild, Optional<ImmutableExpression> ljCondition) {
            Supplier<VariableNullability> variableNullabilitySupplier =
                    () -> computeRightChildVariableNullability(rightChild, ljCondition);

            Transformer newTransformer = new Transformer(variableNullabilitySupplier, variableGenerator, requiredDataNodeExtractor,
                    rightProvenanceNormalizer, coreSingletons, variableNullabilityTools);
            return rightChild.acceptTransformer(newTransformer);
        }
    }


}
