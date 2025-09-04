package it.unibz.inf.ontop.iq.optimizer.impl.lj;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.Attribute;
import it.unibz.inf.ontop.dbschema.FunctionalDependency;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.impl.AbstractExtendedIQOptimizer;
import it.unibz.inf.ontop.iq.optimizer.impl.CaseInsensitiveIQTreeTransformerAdapter;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultNonRecursiveIQTreeTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;

/**
 * Is cardinality-insensitive.
 * For self-left-joins on nullable determinants of FDs.
 * Because some determinants may be null on the left, we cannot perform a join transfer on the left
 * (this would filter null values of determinants). Hence, the need for a new optimization.
 *
 */
@Singleton
public class NullableFDSelfLJOptimizer extends AbstractExtendedIQOptimizer {

    private final RequiredExtensionalDataNodeExtractor requiredDataNodeExtractor;
    private final DBConstant provenanceConstant;

    private final IQTreeTools iqTreeTools;
    private final SubstitutionFactory substitutionFactory;
    private final TermFactory termFactory;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    protected NullableFDSelfLJOptimizer(RequiredExtensionalDataNodeExtractor requiredDataNodeExtractor,
                                        CoreSingletons coreSingletons) {
        super(coreSingletons.getIQFactory(), NO_ACTION);
        this.requiredDataNodeExtractor = requiredDataNodeExtractor;
        this.provenanceConstant = coreSingletons.getTermFactory().getProvenanceSpecialConstant();
        this.iqTreeTools = coreSingletons.getIQTreeTools();
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
        this.termFactory = coreSingletons.getTermFactory();
        this.iqFactory = coreSingletons.getIQFactory();
    }

    @Override
    protected IQTreeVariableGeneratorTransformer getTransformer() {
        return IQTreeVariableGeneratorTransformer.of(vg ->
                new CaseInsensitiveIQTreeTransformerAdapter(iqFactory) {
                    @Override
                    protected IQTree transformCardinalityInsensitiveTree(IQTree tree) {
                        IQVisitor<IQTree> transformer = new CardinalityInsensitiveTransformer(
                                IQTreeTransformer.of(this),
                                vg);
                        return tree.acceptVisitor(transformer);
                    }
                });
    }


    private class CardinalityInsensitiveTransformer extends AbstractLJTransformer {

        private final IQTreeTransformer lookForDistinctTransformer;

        CardinalityInsensitiveTransformer(IQTreeTransformer lookForDistinctTransformer,
                                                    VariableGenerator variableGenerator) {
            super(NullableFDSelfLJOptimizer.this.iqFactory, variableGenerator);
            this.lookForDistinctTransformer = lookForDistinctTransformer;
        }

        @Override
        protected Optional<IQTree> furtherTransformLeftJoin(LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            var dataNodeAndProvenanceVariables = extractDataNodeAndProvenance(rightChild);

            if (dataNodeAndProvenanceVariables.isEmpty())
                return Optional.empty();

            var rightNode = dataNodeAndProvenanceVariables.get().dataNode;
            var provenanceVariables = dataNodeAndProvenanceVariables.get().provenanceVariables;

            var rightArgumentMap = rightNode.getArgumentMap();

            var nullableCoveringFDs = rightNode.getRelationDefinition().getOtherFunctionalDependencies().stream()
                    .filter(fd -> fd.getDeterminants().stream().anyMatch(Attribute::isNullable))
                    // Make sure all the terms are involved in the functional dependency
                    .filter(fd -> Sets.union(fd.getDeterminants(), fd.getDependents()).stream()
                            .map(a -> a.getIndex() - 1)
                            .collect(ImmutableCollectors.toSet())
                            .containsAll(rightArgumentMap.keySet()))
                    .collect(ImmutableCollectors.toList());

            var transfer = requiredDataNodeExtractor.extractSomeRequiredNodes(leftChild, true)
                    .flatMap(left -> tryToTransfer(left, rightNode, nullableCoveringFDs).stream())
                    .findAny();

            return transfer
                    .map(t -> updateLeftTree(t, leftChild, rootNode.getOptionalFilterCondition(), provenanceVariables));
        }

        private Optional<DataNodeAndProvenanceVariables> extractDataNodeAndProvenance(IQTree rightChild) {
            var construction = UnaryIQTreeDecomposition.of(rightChild, ConstructionNode.class);
            if (construction.getTail() instanceof ExtensionalDataNode) {
                var extensionalDataNode = (ExtensionalDataNode) construction.getTail();
                if (!construction.isPresent()) {
                    return Optional.of(new DataNodeAndProvenanceVariables(extensionalDataNode, ImmutableSet.of()));
                }
                else {
                    Substitution<ImmutableTerm> substitution = construction.getNode().getSubstitution();
                    if (substitution.rangeAllMatch(t -> t.equals(provenanceConstant))) {
                        return Optional.of(new DataNodeAndProvenanceVariables(extensionalDataNode, substitution.getDomain()));
                    }
                }
            }
            return Optional.empty();
        }

        private Optional<Transfer> tryToTransfer(ExtensionalDataNode leftNode, ExtensionalDataNode rightNode,
                                       ImmutableList<FunctionalDependency> coveringFDs) {
            if (!leftNode.getRelationDefinition().equals(rightNode.getRelationDefinition()))
                return Optional.empty();

            var leftArgumentMap = leftNode.getArgumentMap();
            var rightArgumentMap = rightNode.getArgumentMap();

            var commonArgumentMap = Sets.intersection(leftArgumentMap.entrySet(), rightArgumentMap.entrySet()).stream()
                            .collect(ImmutableCollectors.toMap());

            if (commonArgumentMap.isEmpty())
                return Optional.empty();

            var selectedFD = coveringFDs.stream()
                    .filter(fd -> fd.getDeterminants().stream().allMatch(a -> commonArgumentMap.containsKey(a.getIndex() - 1)))
                    .findAny();

            var fdTransfer = selectedFD.map(fd -> {
                var dependentIndexes = fd.getDependents().stream()
                        .map(a -> a.getIndex() - 1)
                        .collect(ImmutableCollectors.toSet());

                var determinantVariables = fd.getDeterminants().stream()
                        .map(a -> commonArgumentMap.get(a.getIndex() - 1))
                        .filter(t -> t instanceof Variable)
                        .map(v -> (Variable) v)
                        .collect(ImmutableCollectors.toSet());

                return new Transfer(leftNode, determinantVariables, rightArgumentMap.entrySet().stream()
                        .filter(e -> dependentIndexes.contains(e.getKey()))
                        .collect(ImmutableCollectors.toMap()));
            });

            if (fdTransfer.isPresent())
                return fdTransfer;

            // Trivial FD case (same dependents as determinants)
            if (rightArgumentMap.equals(commonArgumentMap)) {
                var determinantVariables = commonArgumentMap.values().stream()
                        .filter(t -> t instanceof Variable)
                        .map(t -> (Variable) t)
                        .collect(ImmutableCollectors.toSet());

                return Optional.of(new Transfer(leftNode, determinantVariables, ImmutableMap.of()));
            }
            return Optional.empty();
        }

        private IQTree updateLeftTree(Transfer transfer, IQTree leftChild, Optional<ImmutableExpression> optionalFilterCondition,
                                      ImmutableSet<Variable> provenanceVariables) {
            var newLeftNode = transfer.generateNewLeftNode(variableGenerator, iqFactory);
            var leftVariables = leftChild.getVariables();
            var condition = computeRightTermCondition(newLeftNode, leftVariables,
                    transfer.determinantVariables, transfer.argumentsToTransfer, optionalFilterCondition);
            var substitution = computeSubstitution(condition, leftVariables, transfer.argumentsToTransfer, newLeftNode, provenanceVariables);
            var newLeftTree = replaceNodeOnLeft(leftChild, transfer.leftNode, newLeftNode);
            return iqFactory.createUnaryIQTree(
                    iqTreeTools.createExtendingConstructionNode(leftVariables, substitution),
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
                                                                          ExtensionalDataNode newLeftNode, ImmutableSet<Variable> provenanceVariables) {
            var leftArgumentMap = newLeftNode.getArgumentMap();

            var argSubstitution = argumentsToTransfer.asMultimap().inverse().asMap().entrySet().stream()
                    .filter(e -> e.getKey() instanceof Variable)
                    .filter(e -> !leftVariables.contains((Variable)e.getKey()))
                    .collect(substitutionFactory.toSubstitution(
                            e -> (Variable) e.getKey(),
                            e -> termFactory.getIfElseNull(
                                    condition,
                                    // Picking the first index
                                    leftArgumentMap.get(e.getValue().iterator().next())
                            )));

            if (provenanceVariables.isEmpty())
                return argSubstitution;

            var provSubstitution = provenanceVariables.stream()
                    .collect(substitutionFactory.toSubstitution(t -> termFactory.getIfElseNull(condition, provenanceConstant)));

            return argSubstitution.compose(provSubstitution);
        }

        private IQTree replaceNodeOnLeft(IQTree leftChild, ExtensionalDataNode leftNode, ExtensionalDataNode newLeftNode) {
            if (leftChild.equals(leftNode))
                return newLeftNode;

            if (leftNode.equals(newLeftNode))
                return leftChild;

            var replacer = new DataNodeOnLeftReplacer(leftNode, newLeftNode);
            var newLeft = leftChild.acceptVisitor(replacer);

            if (!replacer.hasBeenReplaced())
                throw new MinorOntopInternalBugException(String.format("Could not replace %s on the left", leftNode));

            return newLeft;
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
            var newTransformer = new CardinalityInsensitiveTransformer(lookForDistinctTransformer, variableGenerator);
            return rightChild.acceptVisitor(newTransformer);
        }
    }


    private static class Transfer {
        private final ExtensionalDataNode leftNode;
        private final ImmutableSet<Variable> determinantVariables;
        private final ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentsToTransfer;

        Transfer(ExtensionalDataNode leftNode, ImmutableSet<Variable> determinantVariables,
                           ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentsToTransfer) {
            this.leftNode = leftNode;
            this.determinantVariables = determinantVariables;
            this.argumentsToTransfer = argumentsToTransfer;
        }

        /**
         * Adds fresh variables for columns that will be "transferred" but are not already used by the left
         */
        ExtensionalDataNode generateNewLeftNode(VariableGenerator variableGenerator, IntermediateQueryFactory iqFactory) {
            var leftArgumentMap = leftNode.getArgumentMap();

            var newArgumentMap = Sets.union(leftArgumentMap.keySet(), argumentsToTransfer.keySet()).stream()
                    .collect(ImmutableCollectors.toMap(
                            i -> i,
                            i -> Optional.<VariableOrGroundTerm>ofNullable(leftArgumentMap.get(i))
                            .orElseGet(() -> Optional.ofNullable(argumentsToTransfer.get(i))
                                    .filter(t -> t instanceof Variable)
                                    .map(v -> variableGenerator.generateNewVariableFromVar((Variable) v))
                                    .orElseGet(variableGenerator::generateNewVariable))));

            return iqFactory.createExtensionalDataNode(leftNode.getRelationDefinition(), newArgumentMap);
        }
    }

    /**
     * To be kept in sync with RequiredExtensionalDataNodeExtractor.
     * Not safe to run in parallel
     */
    private class DataNodeOnLeftReplacer extends DefaultNonRecursiveIQTreeTransformer {

        private final ExtensionalDataNode nodeToBeReplaced;
        private final ExtensionalDataNode replacingNode;
        // mutable
        private boolean found;

        DataNodeOnLeftReplacer(ExtensionalDataNode nodeToBeReplaced,
                               ExtensionalDataNode replacingNode) {
            this.nodeToBeReplaced = nodeToBeReplaced;
            this.replacingNode = replacingNode;
            this.found = false;
        }

        public boolean hasBeenReplaced() {
            return found;
        }

        @Override
        public IQTree transformExtensionalData(ExtensionalDataNode dataNode) {
            if (!found && dataNode.equals(nodeToBeReplaced)) {
                found = true;
                return replacingNode;
            }
            return dataNode;
        }

        @Override
        public IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            if (found)
                return tree;
            var newLeft = transformChild(leftChild);
            return newLeft.equals(leftChild)
                    ? tree
                    : iqFactory.createBinaryNonCommutativeIQTree(rootNode, newLeft, rightChild);
        }

        @Override
        public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            if (found)
                return tree;
            var newChildren = NaryIQTreeTools.transformChildren(children, this::transformChild);
            return newChildren.equals(children)
                    ? tree
                    : iqFactory.createNaryIQTree(rootNode, newChildren);
        }
    }

    protected static class DataNodeAndProvenanceVariables {
        public final ExtensionalDataNode dataNode;
        public final ImmutableSet<Variable> provenanceVariables;

        protected DataNodeAndProvenanceVariables(ExtensionalDataNode dataNode, ImmutableSet<Variable> provenanceVariables) {
            this.dataNode = dataNode;
            this.provenanceVariables = provenanceVariables;
        }
    }
}
