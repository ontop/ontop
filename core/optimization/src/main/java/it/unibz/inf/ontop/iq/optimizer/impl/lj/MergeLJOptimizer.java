package it.unibz.inf.ontop.iq.optimizer.impl.lj;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.LeftJoinNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.normalization.impl.RightProvenanceNormalizer;
import it.unibz.inf.ontop.iq.node.normalization.impl.RightProvenanceNormalizer.RightProvenance;
import it.unibz.inf.ontop.iq.optimizer.LeftJoinIQOptimizer;
import it.unibz.inf.ontop.iq.optimizer.impl.lj.ComplexStrictEqualityLeftJoinExpliciter.LeftJoinNormalization;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.*;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.optimizer.impl.lj.LeftJoinAnalysisTools.tolerateLJConditionLifting;

/**
 * Tries to merge LJs nested on the left
 *
 */
@Singleton
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class MergeLJOptimizer implements LeftJoinIQOptimizer {

    private final RightProvenanceNormalizer rightProvenanceNormalizer;
    private final CoreSingletons coreSingletons;
    private final IntermediateQueryFactory iqFactory;
    private final CardinalitySensitiveJoinTransferLJOptimizer otherLJOptimizer;
    private final LJWithNestingOnRightToInnerJoinOptimizer ljReductionOptimizer;
    private final ComplexStrictEqualityLeftJoinExpliciter ljConditionExpliciter;

    @Inject
    protected MergeLJOptimizer(RightProvenanceNormalizer rightProvenanceNormalizer,
                               CoreSingletons coreSingletons,
                               CardinalitySensitiveJoinTransferLJOptimizer joinTransferLJOptimizer,
                               LJWithNestingOnRightToInnerJoinOptimizer ljReductionOptimizer,
                               ComplexStrictEqualityLeftJoinExpliciter ljConditionExpliciter) {
        this.rightProvenanceNormalizer = rightProvenanceNormalizer;
        this.coreSingletons = coreSingletons;
        this.iqFactory = coreSingletons.getIQFactory();
        this.otherLJOptimizer = joinTransferLJOptimizer;
        this.ljReductionOptimizer = ljReductionOptimizer;
        this.ljConditionExpliciter = ljConditionExpliciter;
    }

    @Override
    public IQ optimize(IQ query) {
        IQTree initialTree = query.getTree();

        Transformer transformer = new Transformer(
                query.getVariableGenerator(),
                rightProvenanceNormalizer,
                coreSingletons,
                otherLJOptimizer,
                ljReductionOptimizer,
                ljConditionExpliciter);

        IQTree newTree = initialTree.acceptTransformer(transformer);

        return newTree.equals(initialTree)
                ? query
                : iqFactory.createIQ(query.getProjectionAtom(), newTree);
    }

    protected static class Transformer extends DefaultRecursiveIQTreeVisitingTransformer {

        private final VariableGenerator variableGenerator;
        private final RightProvenanceNormalizer rightProvenanceNormalizer;
        private final TermFactory termFactory;
        private final CardinalitySensitiveJoinTransferLJOptimizer joinTransferOptimizer;
        private final AtomFactory atomFactory;
        private final SubstitutionFactory substitutionFactory;
        private final LJWithNestingOnRightToInnerJoinOptimizer ljReductionOptimizer;
        private final ComplexStrictEqualityLeftJoinExpliciter ljConditionExpliciter;

        protected Transformer(VariableGenerator variableGenerator, RightProvenanceNormalizer rightProvenanceNormalizer,
                              CoreSingletons coreSingletons,
                              CardinalitySensitiveJoinTransferLJOptimizer joinTransferOptimizer,
                              LJWithNestingOnRightToInnerJoinOptimizer ljReductionOptimizer,
                              ComplexStrictEqualityLeftJoinExpliciter ljConditionExpliciter) {
            super(coreSingletons);
            this.variableGenerator = variableGenerator;
            this.rightProvenanceNormalizer = rightProvenanceNormalizer;
            this.termFactory = coreSingletons.getTermFactory();
            this.joinTransferOptimizer = joinTransferOptimizer;
            this.atomFactory = coreSingletons.getAtomFactory();
            this.substitutionFactory = coreSingletons.getSubstitutionFactory();
            this.ljReductionOptimizer = ljReductionOptimizer;
            this.ljConditionExpliciter = ljConditionExpliciter;
        }

        @Override
        public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            IQTree newLeftChild = transform(leftChild);
            IQTree newRightChild = transform(rightChild);

            if (!(newLeftChild.getRootNode() instanceof LeftJoinNode))
                return buildUnoptimizedLJTree(tree, leftChild, rightChild, newLeftChild, newRightChild, rootNode);

            LeftJoinNormalization normalization = ljConditionExpliciter.makeComplexEqualitiesImplicit(
                    newLeftChild, newRightChild, rootNode.getOptionalFilterCondition(), variableGenerator);

            if (!tolerateLJConditionLifting(normalization.ljCondition, normalization.leftChild, normalization.rightChild))
                return buildUnoptimizedLJTree(tree, leftChild, rightChild, newLeftChild, newRightChild, rootNode);

            ImmutableSet<Variable> rightSpecificVariables = Sets.difference(normalization.rightChild.getVariables(),
                            normalization.leftChild.getVariables())
                    .immutableCopy();

            Optional<IQTree> simplifiedTree = tryToSimplify(normalization.leftChild, normalization.rightChild, normalization.ljCondition,
                    rightSpecificVariables, new LinkedList<>());

            return simplifiedTree
                    .map(t -> normalization.isIntroducingNewVariables
                            ? iqFactory.createUnaryIQTree(
                                    iqFactory.createConstructionNode(tree.getVariables()), t)
                            : t)
                    .map(t -> t.normalizeForOptimization(variableGenerator))
                    .orElseGet(() -> buildUnoptimizedLJTree(tree, leftChild, rightChild, newLeftChild, newRightChild, rootNode));
        }

        private IQTree buildUnoptimizedLJTree(IQTree tree, IQTree leftChild, IQTree rightChild, IQTree newLeftChild, IQTree newRightChild,
                                              LeftJoinNode rootNode) {
            return newLeftChild.equals(leftChild) && newRightChild.equals(rightChild)
                    ? tree
                    : iqFactory.createBinaryNonCommutativeIQTree(rootNode, newLeftChild, newRightChild)
                    .normalizeForOptimization(variableGenerator);
        }

        private Optional<IQTree> tryToSimplify(IQTree leftDescendent, IQTree topRightTree, Optional<ImmutableExpression> topLJCondition,
                                               ImmutableSet<Variable> topRightSpecificVariables, List<Ancestor> ancestors) {
            QueryNode leftRootNode = leftDescendent.getRootNode();
            if (!(leftRootNode instanceof LeftJoinNode))
                return Optional.empty();

            LeftJoinNode leftJoinNode = (LeftJoinNode) leftRootNode;
            BinaryNonCommutativeIQTree leftJoinTree = (BinaryNonCommutativeIQTree) leftDescendent;
            IQTree leftSubTree = leftJoinTree.getLeftChild();
            IQTree rightSubTree = leftJoinTree.getRightChild();

            ImmutableSet<Variable> leftVariables = leftSubTree.getVariables();

            // No optimization if outside the "well-designed fragment" (NB: we ignore LJ conditions)
            // TODO: do we need this restriction? Isn't it always enforced?
            if (!Sets.intersection(
                    Sets.difference(rightSubTree.getVariables(), leftVariables),
                    topRightTree.getVariables()).isEmpty())
                return Optional.empty();

            Optional<ImmutableExpression> localLJCondition = leftJoinNode.getOptionalFilterCondition();

            /*
             * If cannot be merged with this right child, continue the search on the left
             */
            if ((!tolerateLJConditionLifting(localLJCondition, leftSubTree, rightSubTree))
                    || (!canBeMerged(rightSubTree, topRightTree, leftVariables))) {
              ancestors.add(0, new Ancestor(leftJoinNode, rightSubTree));
              return tryToSimplify(leftSubTree, topRightTree, topLJCondition, topRightSpecificVariables, ancestors);
            }

            var renamingAndUpdatedConditions = computeRenaming(localLJCondition, leftSubTree.getVariables(),
                    rightSubTree.getVariables(), topRightTree.getVariables(), topLJCondition, topRightSpecificVariables);

            IQTree mergedLocalRightBeforeRenaming = iqFactory.createNaryIQTree(
                    iqFactory.createInnerJoinNode(),
                    ImmutableList.of(rightSubTree, topRightTree));

            Optional<RightProvenance> localRightProvenance =  renamingAndUpdatedConditions.renaming.isEmpty()
                    ? Optional.empty()
                    : Optional.of(rightProvenanceNormalizer.normalizeRightProvenance(
                            mergedLocalRightBeforeRenaming, leftJoinTree.getVariables(),
                    Optional.empty(), variableGenerator));


            IQTree newLocalRightTree = localRightProvenance
                    .map(RightProvenance::getRightTree)
                    .orElse(mergedLocalRightBeforeRenaming)
                    .applyFreshRenaming(renamingAndUpdatedConditions.renaming);

            IQTree newLocalTree = iqFactory.createBinaryNonCommutativeIQTree(iqFactory.createLeftJoinNode(),
                    leftSubTree, newLocalRightTree);

            IQTree newLJTree = ancestors.stream()
                    .reduce(newLocalTree, (t, a) -> iqFactory.createBinaryNonCommutativeIQTree(a.rootNode, t, a.right),
                            (t1, t2) -> {
                                throw new MinorOntopInternalBugException("Parallelization is not supported here");
                            });

            if (renamingAndUpdatedConditions.renaming.isEmpty())
                return Optional.of(newLJTree);

            Substitution<ImmutableFunctionalTerm> newSubstitution = renamingAndUpdatedConditions.renaming.builder()
                    .removeFromDomain(leftVariables)
                    .transform(
                            v -> v,
                            (t, v) -> createIfElseNull(v, t, topRightSpecificVariables,
                                    renamingAndUpdatedConditions.localCondition,
                                    renamingAndUpdatedConditions.topCondition))
                    .build();

            ImmutableSet<Variable> projectedVariables = Sets.union(
                    Sets.difference(newLJTree.getVariables(), renamingAndUpdatedConditions.renaming.getRangeSet()),
                            newSubstitution.getDomain())
                    .immutableCopy();

            IQTree newTree = iqFactory.createUnaryIQTree(
                    iqFactory.createConstructionNode(projectedVariables, newSubstitution),
                    newLJTree);

            return Optional.of(newTree);
        }

        private ImmutableFunctionalTerm createIfElseNull(Variable originalVariable, Variable renamedVariable,
                                                         ImmutableSet<Variable> topRightSpecificVariables,
                                                         Optional<ImmutableExpression> localLJCondition,
                                                         Optional<ImmutableExpression> topLJCondition) {
            Optional<ImmutableExpression> condition = topRightSpecificVariables.contains(originalVariable)
                    ? topLJCondition : localLJCondition;

            return condition
                    .map(c -> termFactory.getIfElseNull(c, renamedVariable))
                    .orElseThrow(() -> new MinorOntopInternalBugException("A lj condition was expected"));
        }

        private RenamingAndUpdatedConditions computeRenaming(
                Optional<ImmutableExpression> localLJCondition, ImmutableSet<Variable> leftVariables, ImmutableSet<Variable> localRightVariables,
                ImmutableSet<Variable> topRightVariables, Optional<ImmutableExpression> topLJCondition, ImmutableSet<Variable> topRightSpecificVariables) {

            var localRightVariablesOnlySharedWithLeft = Sets.difference(Sets.intersection(leftVariables, localRightVariables), topRightVariables);
            var topRightVariablesOnlySharedWithLeft = Sets.difference(Sets.intersection(leftVariables, topRightVariables), localRightVariables);

            InjectiveSubstitution<Variable> renaming = Stream.concat(
                    Stream.concat(
                            (localLJCondition.isPresent() || !localRightVariablesOnlySharedWithLeft.isEmpty())
                                    ? Sets.difference(localRightVariables, leftVariables).stream()
                                    : Stream.empty(),
                            (topLJCondition.isPresent() || !topRightVariablesOnlySharedWithLeft.isEmpty())
                                    ? topRightSpecificVariables.stream()
                                    : Stream.empty()),
                    Stream.concat(
                            localRightVariablesOnlySharedWithLeft.stream(),
                                    topRightVariablesOnlySharedWithLeft.stream()))
                    .distinct()
                    .collect(substitutionFactory.toFreshRenamingSubstitution(variableGenerator));

            Optional<ImmutableExpression> newLocalCondition = termFactory.getConjunction(
                    localLJCondition.map(renaming::apply),
                    extractEqualities(localRightVariablesOnlySharedWithLeft, renaming));

            Optional<ImmutableExpression> newTopCondition = termFactory.getConjunction(
                    topLJCondition.map(renaming::apply),
                    extractEqualities(topRightVariablesOnlySharedWithLeft, renaming));

            return new RenamingAndUpdatedConditions(renaming, newLocalCondition, newTopCondition);
        }

        private Stream<ImmutableExpression> extractEqualities(
                Set<Variable> sharedWithLeftVariables, InjectiveSubstitution<Variable> renaming) {
            return sharedWithLeftVariables.stream()
                    .map(v -> termFactory.getStrictEquality(v, renaming.apply(v)));
        }

        private boolean canBeMerged(IQTree subRightChild, IQTree rightChildToMerge, ImmutableSet<Variable> leftVariables) {
            return isTreeIncluded(subRightChild, rightChildToMerge, leftVariables)
                    && isTreeIncluded(rightChildToMerge, subRightChild, leftVariables);
        }

        private boolean isTreeIncluded(IQTree tree, IQTree otherTree, ImmutableSet<Variable> leftVariables) {
            RightProvenance rightProvenance = rightProvenanceNormalizer.normalizeRightProvenance(
                    otherTree, tree.getVariables(), Optional.empty(), variableGenerator);

            Optional<ImmutableExpression> nonNullabilityCondition = termFactory.getConjunction(
                    Sets.intersection(tree.getVariables(), leftVariables).stream()
                            .map(termFactory::getDBIsNotNull));

            ImmutableExpression isNullCondition = termFactory.getDBIsNull(rightProvenance.getProvenanceVariable());

            ImmutableExpression filterCondition = nonNullabilityCondition
                    .map(c -> termFactory.getConjunction(isNullCondition, c))
                    .orElse(isNullCondition);


            IQTree minusTree = iqFactory.createUnaryIQTree(
                    iqFactory.createConstructionNode(ImmutableSet.of(rightProvenance.getProvenanceVariable())),
                    iqFactory.createUnaryIQTree(
                            iqFactory.createFilterNode(filterCondition),
                            iqFactory.createBinaryNonCommutativeIQTree(
                                    iqFactory.createLeftJoinNode(),
                                    tree, rightProvenance.getRightTree())));

            // Hack
            DistinctVariableOnlyDataAtom minusFakeProjectionAtom = atomFactory.getDistinctVariableOnlyDataAtom(
                    atomFactory.getRDFAnswerPredicate(1),
                    ImmutableList.of(rightProvenance.getProvenanceVariable()));

            IQ minusIQ = iqFactory.createIQ(minusFakeProjectionAtom, minusTree);

            IQTree optimizedTree = ljReductionOptimizer.optimize(
                    joinTransferOptimizer.optimize(minusIQ.normalizeForOptimization()))
                    .normalizeForOptimization().getTree();

            return optimizedTree.isDeclaredAsEmpty();
        }
    }

    protected static class Ancestor {
        public final LeftJoinNode rootNode;
        public final IQTree right;

        protected Ancestor(LeftJoinNode rootNode, IQTree right) {
            this.rootNode = rootNode;
            this.right = right;
        }
    }

    protected static class RenamingAndUpdatedConditions {
        public final InjectiveSubstitution<Variable> renaming;
        public final Optional<ImmutableExpression> localCondition;
        public final Optional<ImmutableExpression> topCondition;

        protected RenamingAndUpdatedConditions(InjectiveSubstitution<Variable> renaming,
                                               Optional<ImmutableExpression> localCondition,
                                               Optional<ImmutableExpression> topCondition) {
            this.renaming = renaming;
            this.localCondition = localCondition;
            this.topCondition = topCondition;
        }
    }
}
