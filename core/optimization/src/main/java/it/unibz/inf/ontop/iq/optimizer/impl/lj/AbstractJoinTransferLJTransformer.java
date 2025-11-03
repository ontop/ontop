package it.unibz.inf.ontop.iq.optimizer.impl.lj;

import com.google.common.collect.*;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.impl.BinaryNonCommutativeIQTreeTools;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.impl.JoinOrFilterVariableNullabilityTools;
import it.unibz.inf.ontop.iq.node.normalization.impl.RightProvenanceNormalizer;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ArgumentSubstitution;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.impl.UnaryIQTreeTools.UnaryIQTreeDecomposition;

public abstract class AbstractJoinTransferLJTransformer extends AbstractLJTransformerWithVariableNullability {

    protected final RequiredExtensionalDataNodeExtractor requiredDataNodeExtractor;
    protected final IQTreeTools iqTreeTools;
    protected final SubstitutionFactory substitutionFactory;

    protected AbstractJoinTransferLJTransformer(IQTreeTransformer searchingFromScratchTransformer,
                                                Supplier<VariableNullability> variableNullabilitySupplier,
                                                VariableGenerator variableGenerator,
                                                RequiredExtensionalDataNodeExtractor requiredDataNodeExtractor,
                                                RightProvenanceNormalizer rightProvenanceNormalizer,
                                                JoinOrFilterVariableNullabilityTools variableNullabilityTools,
                                                CoreSingletons coreSingletons) {
        super(searchingFromScratchTransformer, variableNullabilitySupplier, variableGenerator, rightProvenanceNormalizer, variableNullabilityTools,
                coreSingletons);
        this.requiredDataNodeExtractor = requiredDataNodeExtractor;
        this.iqTreeTools = coreSingletons.getIQTreeTools();
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
    }

    /**
     * Returns empty if no optimization has been applied
     */
    @Override
    protected Optional<IQTree> furtherTransformLeftJoin(LeftJoinNode rootNode, IQTree leftChild,
                                                        IQTree rightChild) {
        ImmutableSet<ExtensionalDataNode> leftDataNodes = requiredDataNodeExtractor.extractSomeRequiredNodesFromLeft(leftChild)
                .collect(ImmutableCollectors.toSet());
        if (leftDataNodes.isEmpty())
            return Optional.empty();

        ImmutableSet<ExtensionalDataNode> rightDataNodes = extractRightUniqueDataNodes(rightChild);
        if (rightDataNodes.isEmpty())
            return Optional.empty();

        ImmutableSet<SelectedNode> selectedRightDataNodes = selectRightDataNodesToTransfer(leftDataNodes, rightDataNodes);
        if (selectedRightDataNodes.isEmpty())
            return Optional.empty();

        Optional<IQTree> rightChildWithConstructionNodeMovedAside = moveTopConstructionNodeAside(rightChild);
        return rightChildWithConstructionNodeMovedAside
                .map(newRightChild -> transfer(rootNode, leftChild, newRightChild, selectedRightDataNodes, BinaryNonCommutativeIQTreeTools.projectedVariables(leftChild, rightChild).immutableCopy())
                        .normalizeForOptimization(variableGenerator));
    }

    protected ImmutableSet<SelectedNode> selectRightDataNodesToTransfer(
            ImmutableSet<ExtensionalDataNode> leftDataNodes, ImmutableSet<ExtensionalDataNode> rightDataNodes) {

        ImmutableMultimap<RelationDefinition, ExtensionalDataNode> leftDataNodeMultimap = leftDataNodes.stream()
                .collect(ImmutableCollectors.toMultimap(
                        ExtensionalDataNode::getRelationDefinition,
                        n -> n));

        return rightDataNodes.stream()
                .map(r -> selectForTransfer(r, leftDataNodeMultimap))
                .flatMap(Optional::stream)
                .collect(ImmutableCollectors.toSet());
    }

    protected abstract Optional<SelectedNode> selectForTransfer(ExtensionalDataNode rightDataNode,
                                                                ImmutableMultimap<RelationDefinition, ExtensionalDataNode> leftMultimap);

    /**
     * Does not consider nodes co-occurring multiple times on the right. This allows to guarantee
     * that the position of node in the tree can be found again.
     *   This looks fair as such co-occurrences are likely to be eliminated by other optimizations.
     */
    private ImmutableSet<ExtensionalDataNode> extractRightUniqueDataNodes(IQTree rightChild) {
        ImmutableMultiset<ExtensionalDataNode> multiset = extractRightDataNodes(rightChild)
                .collect(ImmutableCollectors.toMultiset());

        return multiset.entrySet().stream()
                .filter(e -> e.getCount() == 1)
                .map(Multiset.Entry::getElement)
                .collect(ImmutableCollectors.toSet());
    }

    private ImmutableList<Integer> getIndexes(Stream<Attribute> attributeStream) {
        return attributeStream.map(a -> a.getIndex() - 1)
                .collect(ImmutableCollectors.toList());
    }

    /**
     * Matches an unique constraint whose determinants are nullable in the tree
     */
    protected Optional<ImmutableList<Integer>> matchUniqueConstraint(UniqueConstraint uniqueConstraint,
                                         ImmutableSet<ExtensionalDataNode> sameRelationLeftNodes,
                                         ImmutableMap<Integer, ? extends VariableOrGroundTerm> rightArgumentMap) {

        ImmutableList<Integer> indexes = getIndexes(uniqueConstraint.getDeterminants().stream());
        if (!rightArgumentMap.keySet().containsAll(indexes))
            return Optional.empty();

        return matchIndexes(sameRelationLeftNodes, rightArgumentMap, indexes);
    }

    protected Optional<ImmutableList<Integer>> matchForeignKey(ForeignKeyConstraint fk,
                                                               ImmutableCollection<ExtensionalDataNode> leftNodes,
                                                               ImmutableMap<Integer,? extends VariableOrGroundTerm> rightArgumentMap) {
        // NB: order matters
        ImmutableList<Integer> leftIndexes = getIndexes(fk.getComponents().stream()
                .map(ForeignKeyConstraint.Component::getAttribute));

        ImmutableList<Integer> rightIndexes = getIndexes(fk.getComponents().stream()
                .map(ForeignKeyConstraint.Component::getReferencedAttribute));

        return leftNodes.stream()
                .map(ExtensionalDataNode::getArgumentMap)
                .filter(lMap -> IntStream.range(0, leftIndexes.size())
                        .allMatch(i -> Optional.ofNullable(lMap.get(leftIndexes.get(i)))
                                .filter(t -> !(t instanceof Variable)
                                        || !getInheritedVariableNullability().isPossiblyNullable((Variable)t))
                                .filter(l -> Optional.ofNullable(rightArgumentMap.get(rightIndexes.get(i)))
                                        .filter(l::equals)
                                        .isPresent())
                                .isPresent()))
                .findAny()
                .map(l -> rightIndexes);
    }

    protected Optional<ImmutableList<Integer>> matchFunctionalDependency(FunctionalDependency functionalDependency,
                                                                         ImmutableSet<ExtensionalDataNode> sameRelationLeftNodes,
                                                                         ImmutableMap<Integer,? extends VariableOrGroundTerm> rightArgumentMap) {

        ImmutableList<Integer> determinantIndexes = getIndexes(functionalDependency.getDeterminants().stream());
        if (!rightArgumentMap.keySet().containsAll(determinantIndexes))
            return Optional.empty();

        ImmutableSet<Integer> dependentIndexes = functionalDependency.getDependents().stream()
                .map(a -> a.getIndex() - 1)
                .collect(ImmutableCollectors.toSet());

        // Determinants + non-dependent indexes
        ImmutableList<Integer> indexes = rightArgumentMap.keySet().stream()
                .filter(i -> !dependentIndexes.contains(i))
                .collect(ImmutableCollectors.toList());

        return matchIndexes(sameRelationLeftNodes, rightArgumentMap, indexes);
    }

    protected Optional<ImmutableList<Integer>> matchIndexes(ImmutableSet<ExtensionalDataNode> sameRelationLeftNodes,
                                                            ImmutableMap<Integer, ? extends VariableOrGroundTerm> rightArgumentMap,
                                                            ImmutableList<Integer> indexes) {
        VariableNullability variableNullability = getInheritedVariableNullability();
        if (indexes.stream().anyMatch(i ->
                Optional.of(rightArgumentMap.get(i))
                        .filter(t -> (t instanceof Variable) && variableNullability.isPossiblyNullable((Variable) t))
                        .isPresent()))
            return Optional.empty();

        return sameRelationLeftNodes.stream()
                .map(ExtensionalDataNode::getArgumentMap)
                .filter(leftArgumentMap -> leftArgumentMap.keySet().containsAll(indexes)
                        && indexes.stream().allMatch(
                        i -> leftArgumentMap.get(i).equals(rightArgumentMap.get(i))))
                .findAny()
                .map(n -> indexes);
    }


    /**
     * Can be overridden to put restrictions
     */
    protected Stream<ExtensionalDataNode> extractRightDataNodes(IQTree rightChild) {
        return requiredDataNodeExtractor.extractSomeRequiredNodesFromRight(rightChild);
    }

    /**
     * Moves a top construction with ground term definitions (typically provenance constants) aside if present.
     *
     * If the top construction is having non-ground definitions, returns empty.
     *
     */
    private Optional<IQTree> moveTopConstructionNodeAside(IQTree rightTree) {
        var construction = UnaryIQTreeDecomposition.of(rightTree, ConstructionNode.class);
        if (construction.isPresent()) {
            Substitution<ImmutableTerm> substitution = construction.getNode().getSubstitution();
            if (substitution.rangeAllMatch(ImmutableTerm::isGround)) {
                NaryIQTree newTree = iqTreeTools.createInnerJoinTree(
                        ImmutableList.of(
                                construction.getChild(),
                                iqFactory.createUnaryIQTree(
                                        iqTreeTools.createExtendingConstructionNode(ImmutableSet.of(), substitution),
                                        iqFactory.createTrueNode())));

                return Optional.of(newTree);
            }
            return Optional.empty();
        }
        return Optional.of(rightTree);
    }

    private IQTree transfer(LeftJoinNode rootNode, IQTree leftChild, IQTree transformedRightChild,
                            ImmutableSet<SelectedNode> selectedNodes, ImmutableSet<Variable> projectedVariables) {

        if (selectedNodes.isEmpty())
            throw new IllegalArgumentException("selectedNodes must not be empty");

        // TODO: sets here are useless as DataNodeAndReplacement does not override equals, etc. Do we need them?
        ImmutableList<DataNodeAndReplacement> nodesToTransferAndReplacements = selectedNodes.stream()
                .map(n -> n.transformForTransfer(variableGenerator))
                .collect(ImmutableCollectors.toList());

        IQTree newLeftChild = iqTreeTools.createInnerJoinTree(
                Stream.concat(
                        Stream.of(leftChild),
                        nodesToTransferAndReplacements.stream()
                                .map(n -> n.getExtensionalDataNode(iqFactory)))
                        .collect(ImmutableCollectors.toList()));

        Substitution<VariableOrGroundTerm> replacementSubstitution = nodesToTransferAndReplacements.stream()
                .map(n -> n.getSubstitution(substitutionFactory))
                .reduce(substitutionFactory.getSubstitution(), substitutionFactory::union);

        ImmutableList<Map.Entry<Variable, VariableOrGroundTerm>> list = replacementSubstitution.stream()
                .collect(ImmutableCollectors.toList());

        InjectiveSubstitution<Variable> renamingSubstitution = substitutionFactory.extractInverseSubstitution(list.stream(), leftChild.getVariables())
                .injective();

        ImmutableSet<ImmutableExpression> equalities = iqTreeTools.getRemainingEqualitiesInverse(list, renamingSubstitution)
                .collect(ImmutableCollectors.toSet());

        Optional<ImmutableExpression> newLeftJoinCondition = termFactory.getConjunction(
                rootNode.getOptionalFilterCondition()
                        .map(renamingSubstitution::apply),
                equalities.stream());

        IQTree simplifiedRightChild = replaceSelectedNodesAndRename(selectedNodes, transformedRightChild,
                renamingSubstitution);

        RightProvenanceNormalizer.RightProvenance rightProvenance = rightProvenanceNormalizer.normalizeRightProvenance(
                simplifiedRightChild, newLeftChild.getVariables(), variableGenerator, getRightNullability(simplifiedRightChild, newLeftJoinCondition));

        BinaryNonCommutativeIQTree newLeftJoinTree = iqTreeTools.createLeftJoinTree(
                newLeftJoinCondition,
                newLeftChild, rightProvenance.getTree());

        var condition = termFactory.getDBIsNotNull(rightProvenance.getProvenanceVariable());
        Substitution<ImmutableTerm> substitution = renamingSubstitution.builder()
                .restrictDomainTo(projectedVariables)
                .<ImmutableTerm>transform(t -> termFactory.getIfElseNull(condition, t))
                .build();

        ConstructionNode constructionNode = iqFactory.createConstructionNode(projectedVariables, substitution);

        return iqFactory.createUnaryIQTree(constructionNode, newLeftJoinTree);
    }

    private VariableNullability getRightNullability(IQTree rightTree, Optional<ImmutableExpression> leftJoinExpression) {
        ImmutableSet<Variable> rightVariables = rightTree.getVariables();

        var optionalFilter = iqTreeTools.createOptionalFilterNode(leftJoinExpression.flatMap(e -> termFactory.getConjunction(
                e.flattenAND().filter(e1 -> rightVariables.containsAll(e1.getVariables())))));

        return iqTreeTools.unaryIQTreeBuilder()
                .append(optionalFilter)
                .build(rightTree)
                .getVariableNullability();
    }


    /**
     * NB: nodes to be replaced by TrueNodes should be safe to do so (should have been already checked before)
     * In this context, renaming is safe to apply after.
     */
    private IQTree replaceSelectedNodesAndRename(ImmutableSet<SelectedNode> selectedNodes, IQTree rightChild,
                                                 InjectiveSubstitution<Variable> renamingSubstitution) {

        var dataNodesToReplace = selectedNodes.stream()
                .map(n -> n.extensionalDataNode)
                .collect(ImmutableCollectors.toSet());

        var transformer = new DefaultRecursiveIQTreeVisitingTransformer(AbstractJoinTransferLJTransformer.this.iqFactory) {
            @Override
            public IQTree transformExtensionalData(ExtensionalDataNode dataNode) {
                return dataNodesToReplace.contains(dataNode)
                        ? iqFactory.createTrueNode()
                        : dataNode;
            }
        };

        IQTree transformedTree = transformer.transform(rightChild);
        return iqTreeTools.applyDownPropagation(renamingSubstitution, transformedTree);
    }

    protected static class SelectedNode {

        public final ImmutableList<Integer> determinantIndexes;
        public final ExtensionalDataNode extensionalDataNode;

        public SelectedNode(ImmutableList<Integer> determinantIndexes, ExtensionalDataNode extensionalDataNode) {
            this.determinantIndexes = determinantIndexes;
            this.extensionalDataNode = extensionalDataNode;
        }

        /**
         * The determinants are preserved, while the other arguments are replaced by a fresh variable
         */

        public DataNodeAndReplacement transformForTransfer(VariableGenerator variableGenerator) {
            ImmutableMap<Integer, Variable> replacement = extensionalDataNode.getArgumentMap().entrySet().stream()
                    .filter(e -> !determinantIndexes.contains(e.getKey()))
                    .collect(ImmutableCollectors.toMap(
                            Map.Entry::getKey,
                            e -> generateFreshVariable(e.getValue(), variableGenerator)));

            return new DataNodeAndReplacement(extensionalDataNode, new ArgumentSubstitution<>(replacement, Optional::ofNullable));
        }

        // TODO: compare with ExplicitEqualityTransformerImpl - why no "accumulator" sets of variables here?
        Variable generateFreshVariable(VariableOrGroundTerm term, VariableGenerator variableGenerator) {
            return Optional.of(term)
                    .filter(t -> t instanceof Variable)
                    .map(v -> (Variable) v)
                    .map(Variable::getName)
                    .map(variableGenerator::generateNewVariable)
                    .orElseGet(variableGenerator::generateNewVariable);
        }
    }

    protected static class DataNodeAndReplacement {
        private final ExtensionalDataNode extensionalDataNode;
        private final ArgumentSubstitution<VariableOrGroundTerm> replacement;

        public DataNodeAndReplacement(ExtensionalDataNode extensionalDataNode, ArgumentSubstitution<VariableOrGroundTerm> replacement) {
            this.extensionalDataNode = extensionalDataNode;
            this.replacement = replacement;
        }

        public Substitution<VariableOrGroundTerm> getSubstitution(SubstitutionFactory substitutionFactory) {
            return replacement.getSubstitution(substitutionFactory, extensionalDataNode.getArgumentMap()::get);
        }

        public ExtensionalDataNode getExtensionalDataNode(IntermediateQueryFactory iqFactory) {
            return iqFactory.createExtensionalDataNode(
                    extensionalDataNode.getRelationDefinition(),
                    replacement.replaceTerms(extensionalDataNode.getArgumentMap()));
        }
    }


}
