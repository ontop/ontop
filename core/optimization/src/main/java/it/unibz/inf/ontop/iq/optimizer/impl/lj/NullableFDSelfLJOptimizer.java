package it.unibz.inf.ontop.iq.optimizer.impl.lj;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.Attribute;
import it.unibz.inf.ontop.dbschema.FunctionalDependency;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
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
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

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

        private IQTree updateLeftTree(Transfer transfer, IQTree leftChild, Optional<ImmutableExpression> optionalFilterCondition) {
            var newLeftNode = transfer.generateNewLeftNode(variableGenerator, iqFactory);
            var leftVariables = leftChild.getVariables();
            var condition = computeRightTermCondition(newLeftNode, leftVariables,
                    transfer.determinantVariables, transfer.argumentsToTransfer, optionalFilterCondition);
            var substitution = computeSubstitution(condition, leftVariables, transfer.argumentsToTransfer, newLeftNode);
            var newLeftTree = replaceNodeOnLeft(leftChild, transfer.leftNode, newLeftNode);
            return iqFactory.createUnaryIQTree(
                    iqFactory.createConstructionNode(
                            Sets.union(leftVariables, substitution.getDomain()).immutableCopy(),
                            substitution),
                    newLeftTree);
        }

        private ImmutableExpression computeRightTermCondition(ExtensionalDataNode newLeftNode,
                                                              ImmutableSet<Variable> leftVariables,
                                                              ImmutableSet<Variable> determinantVariables,
                                                              ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentsToTransfer,
                                                              Optional<ImmutableExpression> optionalFilterCondition) {

            var leftArgumentMap = newLeftNode.getArgumentMap();

            var groundTermsAndImplicitEqualitiesWithLeft = argumentsToTransfer.entrySet().stream()
                    .filter(e -> e.getValue().isGround() || leftVariables.contains((Variable)e.getValue()))
                    .map(e -> termFactory.getStrictEquality(leftArgumentMap.get(e.getKey()), e.getValue()));

            var inverseVariableMap = argumentsToTransfer.entrySet().stream()
                    .filter(e -> e.getValue() instanceof Variable)
                    .collect(ImmutableCollectors.toMultimap(
                            e -> (Variable)e.getValue(),
                            Map.Entry::getKey))
                    .asMap();


            var coOccurrenceEqualities = inverseVariableMap.values().stream()
                    .flatMap(indexes -> {
                        var firstTerm = leftArgumentMap.get(indexes.iterator().next());
                        return indexes.stream().skip(1)
                                .map(i -> termFactory.getStrictEquality(firstTerm, leftArgumentMap.get(i)));
                    });

            return termFactory.getConjunction(optionalFilterCondition,
                        Stream.concat(
                                Stream.concat(
                                    determinantVariables.stream()
                                        .map(termFactory::getDBIsNotNull),
                                    groundTermsAndImplicitEqualitiesWithLeft),
                                coOccurrenceEqualities))
                    .orElseThrow(() -> new MinorOntopInternalBugException("At least one determinant was expected"));
        }

        private Substitution<? extends ImmutableTerm> computeSubstitution(ImmutableExpression condition, ImmutableSet<Variable> leftVariables,
                                                                          ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentsToTransfer,
                                                                          ExtensionalDataNode newLeftNode) {
            var leftArgumentMap = newLeftNode.getArgumentMap();

            return argumentsToTransfer.asMultimap().inverse().asMap().entrySet().stream()
                    .filter(e -> e.getKey() instanceof Variable)
                    .filter(e -> !leftVariables.contains((Variable)e.getKey()))
                    .collect(substitutionFactory.toSubstitution(
                            e -> (Variable) e.getKey(),
                            e -> termFactory.getIfElseNull(
                                    condition,
                                    // Picking the first index
                                    leftArgumentMap.get(e.getValue().iterator().next())
                            )));
        }

        private IQTree replaceNodeOnLeft(IQTree leftChild, ExtensionalDataNode leftNode, ExtensionalDataNode newLeftNode) {
            if (leftChild.equals(leftNode))
                return newLeftNode;
            throw new RuntimeException("TODO: implement replacing node in non-trivial case");
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

        /**
         * Adds fresh variables for columns that will be "transferred" but are not already used by the left
         */
        public ExtensionalDataNode generateNewLeftNode(VariableGenerator variableGenerator, IntermediateQueryFactory iqFactory) {
            var leftArgumentMap = leftNode.getArgumentMap();

            var newArgumentMap = Sets.union(leftArgumentMap.keySet(), argumentsToTransfer.keySet()).stream()
                    .collect(ImmutableCollectors.toMap(
                            i -> i,
                            i -> Optional.ofNullable((VariableOrGroundTerm) leftArgumentMap.get(i))
                            .orElseGet(() -> Optional.ofNullable(argumentsToTransfer.get(i))
                                    .filter(t -> t instanceof Variable)
                                    .map(v -> variableGenerator.generateNewVariableFromVar((Variable) v))
                                    .orElseGet(variableGenerator::generateNewVariable))));

            return iqFactory.createExtensionalDataNode(leftNode.getRelationDefinition(), newArgumentMap);
        }
    }
}
