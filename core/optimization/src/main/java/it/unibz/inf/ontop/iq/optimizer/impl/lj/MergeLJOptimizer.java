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
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.*;
import java.util.stream.Stream;

/**
 * Tries to merge LJs nested on the left
 *
 */
@Singleton
public class MergeLJOptimizer implements LeftJoinIQOptimizer {

    private final RightProvenanceNormalizer rightProvenanceNormalizer;
    private final CoreSingletons coreSingletons;
    private final IntermediateQueryFactory iqFactory;
    private final CardinalitySensitiveJoinTransferLJOptimizer otherLJOptimizer;
    private final LJWithNestingOnRightToInnerJoinOptimizer ljReductionOptimizer;

    @Inject
    protected MergeLJOptimizer(RightProvenanceNormalizer rightProvenanceNormalizer,
                               CoreSingletons coreSingletons,
                               CardinalitySensitiveJoinTransferLJOptimizer joinTransferLJOptimizer,
                               LJWithNestingOnRightToInnerJoinOptimizer ljReductionOptimizer) {
        this.rightProvenanceNormalizer = rightProvenanceNormalizer;
        this.coreSingletons = coreSingletons;
        this.iqFactory = coreSingletons.getIQFactory();
        this.otherLJOptimizer = joinTransferLJOptimizer;
        this.ljReductionOptimizer = ljReductionOptimizer;
    }

    @Override
    public IQ optimize(IQ query) {
        IQTree initialTree = query.getTree();

        Transformer transformer = new Transformer(
                query.getVariableGenerator(),
                rightProvenanceNormalizer,
                coreSingletons,
                otherLJOptimizer,
                ljReductionOptimizer);

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

        protected Transformer(VariableGenerator variableGenerator, RightProvenanceNormalizer rightProvenanceNormalizer,
                              CoreSingletons coreSingletons,
                              CardinalitySensitiveJoinTransferLJOptimizer joinTransferOptimizer,
                              LJWithNestingOnRightToInnerJoinOptimizer ljReductionOptimizer) {
            super(coreSingletons);
            this.variableGenerator = variableGenerator;
            this.rightProvenanceNormalizer = rightProvenanceNormalizer;
            this.termFactory = coreSingletons.getTermFactory();
            this.joinTransferOptimizer = joinTransferOptimizer;
            this.atomFactory = coreSingletons.getAtomFactory();
            this.substitutionFactory = coreSingletons.getSubstitutionFactory();
            this.ljReductionOptimizer = ljReductionOptimizer;
        }

        @Override
        public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            IQTree newLeftChild = transform(leftChild);
            IQTree newRightChild = transform(rightChild);

            Optional<ImmutableExpression> optionalLJCondition = rootNode.getOptionalFilterCondition();

            if (!tolerateLJCondition(optionalLJCondition, newLeftChild, newRightChild))
                return buildUnoptimizedLJTree(tree, leftChild, rightChild, newLeftChild, newRightChild, rootNode);

            ImmutableSet<Variable> rightSpecificVariables = Sets.difference(newRightChild.getVariables(), newLeftChild.getVariables())
                    .immutableCopy();

            Optional<IQTree> simplifiedTree = tryToSimplify(newLeftChild, newRightChild, optionalLJCondition,
                    rightSpecificVariables, new LinkedList<>());

            return simplifiedTree
                    .orElseGet(() -> buildUnoptimizedLJTree(tree, leftChild, rightChild, newLeftChild, newRightChild, rootNode));
        }

        /**
         * A LJ condition can be handled if it can safely be lifting, which requires that the LJ operates over a
         * unique constraint on the right side
         */
        private boolean tolerateLJCondition(Optional<ImmutableExpression> optionalLJCondition, IQTree leftChild, IQTree rightChild) {
            return optionalLJCondition.isEmpty() || rightChild.inferUniqueConstraints().stream()
                 .anyMatch(uc -> leftChild.getVariables().containsAll(uc));
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

            // No optimization if outside the "well-designed fragment" (NB: we ignore LJ conditions)
            // TODO: do we need this restriction? Isn't it always enforced?
            if (!Sets.intersection(
                    Sets.difference(rightSubTree.getVariables(), leftSubTree.getVariables()),
                    topRightTree.getVariables()).isEmpty())
                return Optional.empty();

            Optional<ImmutableExpression> localLJCondition = leftJoinNode.getOptionalFilterCondition();

            /*
             * If cannot be merged with this right child, continue the search on the left
             */
            if ((!tolerateLJCondition(localLJCondition, leftSubTree, rightSubTree))
                    || (!canBeMerged(rightSubTree, topRightTree))) {
              ancestors.add(0, new Ancestor(leftJoinNode, rightSubTree));
              return tryToSimplify(leftSubTree, topRightTree, topLJCondition, topRightSpecificVariables, ancestors);
            }

            InjectiveSubstitution<Variable> renaming = computeRenaming(localLJCondition, leftSubTree, rightSubTree,
                    topLJCondition, topRightSpecificVariables);

            IQTree mergedLocalRightBeforeRenaming = iqFactory.createNaryIQTree(
                    iqFactory.createInnerJoinNode(),
                    ImmutableList.of(rightSubTree, topRightTree));

            Optional<RightProvenance> localRightProvenance =  renaming.isEmpty()
                    ? Optional.empty()
                    : Optional.of(rightProvenanceNormalizer.normalizeRightProvenance(
                            mergedLocalRightBeforeRenaming, leftJoinTree.getVariables(),
                    Optional.empty(), variableGenerator));

            IQTree newLocalTreeBeforeRenaming = iqFactory.createBinaryNonCommutativeIQTree(
                    iqFactory.createLeftJoinNode(),
                    leftSubTree,
                    localRightProvenance
                            .map(RightProvenance::getRightTree)
                            .orElse(mergedLocalRightBeforeRenaming));

            IQTree newLJTreeBeforeRenaming = ancestors.stream()
                    .reduce(newLocalTreeBeforeRenaming, (t, a) -> iqFactory.createBinaryNonCommutativeIQTree(a.rootNode, t, a.right),
                            (t1, t2) -> {
                                throw new MinorOntopInternalBugException("Parallelization is not supported here");
                            });

            if (renaming.isEmpty())
                return Optional.of(newLJTreeBeforeRenaming);

            Optional<ImmutableExpression> renamedLocalCondition = localLJCondition
                    .map(renaming::apply);

            Optional<ImmutableExpression> renamedTopCondition = topLJCondition
                    .map(renaming::apply);

            IQTree newTree = renaming.isEmpty()
                    ? newLJTreeBeforeRenaming
                    : iqFactory.createUnaryIQTree(
                            iqFactory.createConstructionNode(newLJTreeBeforeRenaming.getVariables(),
                                    renaming.builder()
                                            .transform(
                                                    v -> v,
                                                    (t, v) -> createIfElseNull(v,t, topRightSpecificVariables, renamedLocalCondition, renamedTopCondition))
                                            .build()),
                            newLJTreeBeforeRenaming.applyFreshRenaming(renaming));

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

        private InjectiveSubstitution<Variable> computeRenaming(
                Optional<ImmutableExpression> localLJCondition, IQTree leftSubTree, IQTree rightSubTree,
                Optional<ImmutableExpression> topLJCondition, ImmutableSet<Variable> topRightSpecificVariables) {
            return Stream.concat(localLJCondition
                                    .map(c -> Sets.difference(rightSubTree.getVariables(), leftSubTree.getVariables()))
                                    .stream(),
                            topLJCondition
                                    .map(c -> topRightSpecificVariables).stream())
                    .flatMap(Collection::stream)
                    .collect(substitutionFactory.toFreshRenamingSubstitution(variableGenerator));
        }

        private boolean canBeMerged(IQTree subRightChild, IQTree rightChildToMerge) {
            return isTreeIncluded(subRightChild, rightChildToMerge) && isTreeIncluded(rightChildToMerge, subRightChild);
        }

        private boolean isTreeIncluded(IQTree tree, IQTree otherTree) {
            RightProvenance rightProvenance = rightProvenanceNormalizer.normalizeRightProvenance(
                    otherTree, tree.getVariables(), Optional.empty(), variableGenerator);

            IQTree minusTree = iqFactory.createUnaryIQTree(
                    iqFactory.createConstructionNode(ImmutableSet.of(rightProvenance.getProvenanceVariable())),
                    iqFactory.createUnaryIQTree(
                            iqFactory.createFilterNode(termFactory.getDBIsNull(rightProvenance.getProvenanceVariable())),
                            iqFactory.createBinaryNonCommutativeIQTree(
                                    iqFactory.createLeftJoinNode(),
                                    tree, rightProvenance.getRightTree())));

            // Hack
            DistinctVariableOnlyDataAtom minusFakeProjectionAtom = atomFactory.getDistinctVariableOnlyDataAtom(
                    atomFactory.getRDFAnswerPredicate(1),
                    ImmutableList.of(rightProvenance.getProvenanceVariable()));

            IQTree optimizedTree = ljReductionOptimizer.optimize(
                    joinTransferOptimizer.optimize(
                            iqFactory.createIQ(minusFakeProjectionAtom, minusTree)))
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
}
