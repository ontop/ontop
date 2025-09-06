package it.unibz.inf.ontop.iq.optimizer.impl.lj;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.FunctionalDependency;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.node.impl.JoinOrFilterVariableNullabilityTools;
import it.unibz.inf.ontop.iq.node.normalization.impl.RightProvenanceNormalizer;
import it.unibz.inf.ontop.iq.optimizer.impl.CaseInsensitiveIQTreeTransformerAdapter;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DelegatingIQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Singleton
public class CardinalityInsensitiveJoinTransferLJOptimizer extends DelegatingIQTreeVariableGeneratorTransformer implements IQTreeVariableGeneratorTransformer {

    private final RequiredExtensionalDataNodeExtractor requiredDataNodeExtractor;
    private final RightProvenanceNormalizer rightProvenanceNormalizer;
    private final JoinOrFilterVariableNullabilityTools variableNullabilityTools;
    private final CoreSingletons coreSingletons;

    private final IQTreeVariableGeneratorTransformer transformer;

    @Inject
    protected CardinalityInsensitiveJoinTransferLJOptimizer(RequiredExtensionalDataNodeExtractor requiredDataNodeExtractor,
                                                            RightProvenanceNormalizer rightProvenanceNormalizer,
                                                            JoinOrFilterVariableNullabilityTools variableNullabilityTools,
                                                            CoreSingletons coreSingletons) {
        this.requiredDataNodeExtractor = requiredDataNodeExtractor;
        this.rightProvenanceNormalizer = rightProvenanceNormalizer;
        this.variableNullabilityTools = variableNullabilityTools;
        this.coreSingletons = coreSingletons;

        this.transformer = IQTreeVariableGeneratorTransformer.of(vg ->
                new CaseInsensitiveIQTreeTransformerAdapter(coreSingletons.getIQFactory()) {
                    @Override
                    protected IQTree transformCardinalityInsensitiveTree(IQTree tree) {
                        IQVisitor<IQTree> transformer = new CardinalityInsensitiveTransformer(
                                IQTreeTransformer.of(this),
                                tree::getVariableNullability,
                                vg);
                        return tree.acceptVisitor(transformer);
                    }
                });
    }

    @Override
    protected IQTreeVariableGeneratorTransformer getTransformer() {
        return transformer;
    }

    private class CardinalityInsensitiveTransformer extends AbstractJoinTransferLJTransformer {

        private final IQTreeTransformer lookForDistinctTransformer;

        CardinalityInsensitiveTransformer(IQTreeTransformer lookForDistinctTransformer,
                                          Supplier<VariableNullability> variableNullabilitySupplier,
                                          VariableGenerator variableGenerator) {
            super(variableNullabilitySupplier,
                    variableGenerator,
                    CardinalityInsensitiveJoinTransferLJOptimizer.this.requiredDataNodeExtractor,
                    CardinalityInsensitiveJoinTransferLJOptimizer.this.rightProvenanceNormalizer,
                    CardinalityInsensitiveJoinTransferLJOptimizer.this.variableNullabilityTools,
                    CardinalityInsensitiveJoinTransferLJOptimizer.this.coreSingletons);
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

        private IQTree transformBySearchingFromScratchFromDistinctTree(IQTree tree, Supplier<VariableNullability> variableNullabilitySupplier) {
            CardinalityInsensitiveTransformer newTransformer = new CardinalityInsensitiveTransformer(lookForDistinctTransformer,
                    variableNullabilitySupplier, variableGenerator);
            return tree.acceptVisitor(newTransformer);
        }

        @Override
        protected IQTree preTransformLJRightChild(IQTree rightChild, Optional<ImmutableExpression> ljCondition,
                                                  ImmutableSet<Variable> leftVariables) {

            var isNotNullFromImplicitEqualities = Sets.intersection(leftVariables, rightChild.getVariables()).stream()
                    .map(termFactory::getDBIsNotNull);

            var condition = termFactory.getConjunction(Stream.concat(ljCondition.stream(), isNotNullFromImplicitEqualities));

            return transformBySearchingFromScratchFromDistinctTree(rightChild,
                    () -> condition
                            .map(c -> variableNullabilityTools.updateWithFilter(
                                            c,
                                            rightChild.getVariableNullability().getNullableGroups(),
                                            rightChild.getVariables()))
                            .orElseGet(rightChild::getVariableNullability));
        }

        @Override
        public IQTree transformConstruction(UnaryIQTree tree, ConstructionNode rootNode, IQTree child) {
            var childVariableNullabilitySupplier = computeChildVariableNullabilityFromConstructionParent(tree, rootNode, child);

            return transformUnaryNode(tree, rootNode, child,
                    t -> transformBySearchingFromScratchFromDistinctTree(t, childVariableNullabilitySupplier));
        }

        @Override
        public IQTree transformUnion(NaryIQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
            return transformNaryCommutativeNode(tree, rootNode, children,
                    t -> transformBySearchingFromScratchFromDistinctTree(t, t::getVariableNullability));
        }
    }
}
