package it.unibz.inf.ontop.iq.optimizer.impl.lj;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.Attribute;
import it.unibz.inf.ontop.dbschema.FunctionalDependency;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.impl.JoinOrFilterVariableNullabilityTools;
import it.unibz.inf.ontop.iq.node.normalization.impl.RightProvenanceNormalizer;
import it.unibz.inf.ontop.iq.optimizer.LeftJoinIQOptimizer;
import it.unibz.inf.ontop.iq.optimizer.impl.LookForDistinctOrLimit1TransformerImpl;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Is cardinality-insensitive.
 * For self-left-joins on nullable determinants of FDs
 *
 */
@Singleton
public class NullableFDSelfLJOptimizer implements LeftJoinIQOptimizer {

    private final RequiredExtensionalDataNodeExtractor requiredDataNodeExtractor;
    private final RightProvenanceNormalizer rightProvenanceNormalizer;
    private final JoinOrFilterVariableNullabilityTools variableNullabilityTools;
    private final CoreSingletons coreSingletons;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    protected NullableFDSelfLJOptimizer(RequiredExtensionalDataNodeExtractor requiredDataNodeExtractor,
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

    protected static class CardinalityInsensitiveTransformer extends AbstractLJTransformer {

        private final IQTreeTransformer lookForDistinctTransformer;
        private final RequiredExtensionalDataNodeExtractor requiredDataNodeExtractor;

        protected CardinalityInsensitiveTransformer(IQTreeTransformer lookForDistinctTransformer,
                                                    Supplier<VariableNullability> variableNullabilitySupplier,
                                                    VariableGenerator variableGenerator, RequiredExtensionalDataNodeExtractor requiredDataNodeExtractor,
                                                    RightProvenanceNormalizer rightProvenanceNormalizer,
                                                    JoinOrFilterVariableNullabilityTools variableNullabilityTools,
                                                    CoreSingletons coreSingletons) {
            super(variableNullabilitySupplier, variableGenerator, rightProvenanceNormalizer,
                    variableNullabilityTools, coreSingletons);
            this.lookForDistinctTransformer = lookForDistinctTransformer;
            this.requiredDataNodeExtractor = requiredDataNodeExtractor;
        }

        @Override
        protected Optional<IQTree> furtherTransformLeftJoin(LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            if (!(rightChild instanceof ExtensionalDataNode))
                return Optional.empty();

            var rightNode = (ExtensionalDataNode) rightChild;
            var rightArgumentMap = rightNode.getArgumentMap();

            var nullableCoveringFDs = rightNode.getRelationDefinition().getOtherFunctionalDependencies().stream()
                    .filter(fd -> fd.getDeterminants().stream().anyMatch(Attribute::isNullable))
                    // Make sure all the terms are involved in the functional dependency
                    .filter(fd -> Sets.union(fd.getDeterminants(), fd.getDependents()).stream()
                            .map(a -> a.getIndex() - 1)
                            .collect(ImmutableCollectors.toSet())
                            .containsAll(rightArgumentMap.keySet()))
                    .collect(ImmutableCollectors.toList());

            if (nullableCoveringFDs.isEmpty())
                return Optional.empty();

            var transfer = requiredDataNodeExtractor.extractSomeRequiredNodes(leftChild, true)
                    .flatMap(left -> tryToTransfer(left, rightNode, nullableCoveringFDs).stream())
                    .findAny();

            return transfer
                    .map(t -> updateLeftTree(t, leftChild, rootNode.getOptionalFilterCondition()));
        }

        private Optional<Transfer> tryToTransfer(ExtensionalDataNode leftNode, ExtensionalDataNode rightNode,
                                       ImmutableList<FunctionalDependency> coveringFDs) {
            if (!leftNode.getRelationDefinition().equals(rightNode.getRelationDefinition()))
                return Optional.empty();

            var leftArgumentMap = leftNode.getArgumentMap();
            var rightArgumentMap = rightNode.getArgumentMap();

            var commonArgumentMap = Sets.intersection(leftArgumentMap.entrySet(),
                            rightArgumentMap.entrySet()).stream()
                            .collect(ImmutableCollectors.toMap());

            if (commonArgumentMap.isEmpty())
                return Optional.empty();

            var selectedFD = coveringFDs.stream()
                    .filter(fd -> fd.getDeterminants().stream().allMatch(a -> commonArgumentMap.containsKey(a.getIndex() - 1)))
                    .findAny();

            return selectedFD.map(fd -> {
                var dependentIndexes = fd.getDependents().stream()
                        .map(a -> a.getIndex() - 1)
                        .collect(ImmutableCollectors.toSet());

                var determinantVariables = fd.getDeterminants().stream()
                        .map(a -> commonArgumentMap.get(a.getIndex() -1))
                        .filter(t -> t instanceof Variable)
                        .map(v -> (Variable) v)
                        .collect(ImmutableCollectors.toSet());

                return new Transfer(leftNode, determinantVariables, rightArgumentMap.entrySet().stream()
                        .filter(e -> dependentIndexes.contains(e.getKey()))
                        .collect(ImmutableCollectors.toMap()));
            });
        }

        private IQTree updateLeftTree(Transfer t, IQTree leftChild, Optional<ImmutableExpression> optionalFilterCondition) {
            throw new RuntimeException("TODO: implement transfer");
        }

        @Override
        protected IQTree transformBySearchingFromScratch(IQTree tree) {
            return lookForDistinctTransformer.transform(tree);
        }

        /**
         * Recursive call on the right
         */
        @Override
        protected IQTree preTransformLJRightChild(IQTree rightChild, Optional<ImmutableExpression> ljCondition,
                                                  ImmutableSet<Variable> leftVariables) {
            var newTransformer = new CardinalityInsensitiveTransformer(lookForDistinctTransformer,
                    rightChild::getVariableNullability, variableGenerator, requiredDataNodeExtractor,
                    rightProvenanceNormalizer, variableNullabilityTools, coreSingletons);
            return rightChild.acceptTransformer(newTransformer);
        }
    }


    private static class Transfer {
        private final ExtensionalDataNode leftNode;
        private final ImmutableSet<Variable> determinantVariables;
        private final ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentsToTransfer;

        protected Transfer(ExtensionalDataNode leftNode, ImmutableSet<Variable> determinantVariables,
                           ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentsToTransfer) {
            this.leftNode = leftNode;
            this.determinantVariables = determinantVariables;
            this.argumentsToTransfer = argumentsToTransfer;
        }
    }
}
