package it.unibz.inf.ontop.iq.optimizer.impl.lj;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.dbschema.UniqueConstraint;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.node.impl.JoinOrFilterVariableNullabilityTools;
import it.unibz.inf.ontop.iq.node.normalization.impl.RightProvenanceNormalizer;
import it.unibz.inf.ontop.iq.optimizer.LeftJoinIQOptimizer;
import it.unibz.inf.ontop.iq.optimizer.impl.AbstractIQOptimizer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.function.Supplier;

@Singleton
public class CardinalitySensitiveJoinTransferLJOptimizer extends AbstractIQOptimizer implements LeftJoinIQOptimizer {

    private final RequiredExtensionalDataNodeExtractor requiredDataNodeExtractor;
    private final RightProvenanceNormalizer rightProvenanceNormalizer;
    private final CoreSingletons coreSingletons;
    private final JoinOrFilterVariableNullabilityTools variableNullabilityTools;

    @Inject
    protected CardinalitySensitiveJoinTransferLJOptimizer(RequiredExtensionalDataNodeExtractor requiredDataNodeExtractor,
                                                          RightProvenanceNormalizer rightProvenanceNormalizer,
                                                          JoinOrFilterVariableNullabilityTools variableNullabilityTools,
                                                          CoreSingletons coreSingletons) {
        super(coreSingletons.getIQFactory());
        this.requiredDataNodeExtractor = requiredDataNodeExtractor;
        this.rightProvenanceNormalizer = rightProvenanceNormalizer;
        this.coreSingletons = coreSingletons;
        this.variableNullabilityTools = variableNullabilityTools;
    }

    @Override
    protected IQVisitor<IQTree> getTransformer(IQ query) {
        return new Transformer(
                query.getTree()::getVariableNullability,
                query.getVariableGenerator());
    }


    protected class Transformer extends AbstractJoinTransferLJTransformer {

        protected Transformer(Supplier<VariableNullability> variableNullabilitySupplier,
                              VariableGenerator variableGenerator) {
            super(variableNullabilitySupplier,
                    variableGenerator,
                    CardinalitySensitiveJoinTransferLJOptimizer.this.requiredDataNodeExtractor,
                    CardinalitySensitiveJoinTransferLJOptimizer.this.rightProvenanceNormalizer,
                    CardinalitySensitiveJoinTransferLJOptimizer.this.variableNullabilityTools,
                    CardinalitySensitiveJoinTransferLJOptimizer.this.coreSingletons);
        }


        @Override
        protected Optional<SelectedNode> selectForTransfer(ExtensionalDataNode rightDataNode,
                                                           ImmutableMultimap<RelationDefinition, ExtensionalDataNode> leftMultimap) {
            RelationDefinition rightRelation = rightDataNode.getRelationDefinition();

            ImmutableMap<Integer, ? extends VariableOrGroundTerm> rightArgumentMap = rightDataNode.getArgumentMap();

            // Unique constraints
            ImmutableList<UniqueConstraint> uniqueConstraints = rightRelation.getUniqueConstraints();
            if (!uniqueConstraints.isEmpty()) {
                ImmutableSet<ExtensionalDataNode> sameRelationLeftNodes = ImmutableSet.copyOf(leftMultimap.get(rightRelation));

                Optional<ImmutableList<Integer>> matchingIndexes = uniqueConstraints.stream()
                        .map(uc -> matchUniqueConstraint(uc, sameRelationLeftNodes, rightArgumentMap))
                        .flatMap(Optional::stream)
                        .findAny();

                if (matchingIndexes.isPresent())
                    return Optional.of(new SelectedNode(matchingIndexes.get(), rightDataNode));
            }

            // Foreign keys
            return leftMultimap.keys().stream()
                    .flatMap(leftRelation -> leftRelation.getForeignKeys().stream()
                            .filter(fk -> fk.getReferencedRelation().equals(rightRelation))
                            .map(fk -> matchForeignKey(fk, leftMultimap.get(leftRelation), rightArgumentMap))
                            .flatMap(Optional::stream))
                    .findAny()
                    .map(indexes -> new SelectedNode(indexes, rightDataNode));
        }

        /**
         * Keeps passing the non-nullability constraints down below the construction node
         */
        @Override
        public IQTree transformConstruction(UnaryIQTree tree, ConstructionNode rootNode, IQTree child) {
            var childVariableNullabilitySupplier = computeChildVariableNullabilityFromConstructionParent(tree, rootNode, child);

            return transformUnaryNode(tree, rootNode, child,
                    t -> transformBySearchingWithNewVariableNullabilitySupplier(t, childVariableNullabilitySupplier));
        }

        @Override
        protected IQTree transformBySearchingFromScratch(IQTree tree) {
            return transformBySearchingWithNewVariableNullabilitySupplier(tree, tree::getVariableNullability);
        }

        protected IQTree transformBySearchingWithNewVariableNullabilitySupplier(IQTree tree,
                                                                                Supplier<VariableNullability> variableNullabilitySupplier) {
            return tree.acceptVisitor(new Transformer(variableNullabilitySupplier, variableGenerator));
        }

        @Override
        protected IQTree preTransformLJRightChild(IQTree rightChild, Optional<ImmutableExpression> ljCondition, ImmutableSet<Variable> leftVariables) {
            Supplier<VariableNullability> variableNullabilitySupplier =
                    () -> computeRightChildVariableNullability(rightChild, ljCondition);

            return rightChild.acceptVisitor(new Transformer(variableNullabilitySupplier, variableGenerator));
        }
    }
}
