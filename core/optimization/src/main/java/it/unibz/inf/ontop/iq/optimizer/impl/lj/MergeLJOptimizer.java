package it.unibz.inf.ontop.iq.optimizer.impl.lj;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.LeftJoinNode;
import it.unibz.inf.ontop.iq.node.normalization.impl.RightProvenanceNormalizer;
import it.unibz.inf.ontop.iq.optimizer.impl.AbstractExtendedIQOptimizer;
import it.unibz.inf.ontop.iq.transform.IQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.iq.visit.impl.DefaultRecursiveIQTreeVisitingTransformerWithVariableGenerator;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.*;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.impl.BinaryNonCommutativeIQTreeTools.LeftJoinDecomposition;


/**
 * Tries to merge LJs nested on the left
 *
 */
@Singleton
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class MergeLJOptimizer extends AbstractExtendedIQOptimizer {

    private final RightProvenanceNormalizer rightProvenanceNormalizer;
    private final CardinalitySensitiveJoinTransferLJOptimizer otherLJOptimizer;
    private final LJWithNestingOnRightToInnerJoinOptimizer ljReductionOptimizer;
    private final ComplexStrictEqualityLeftJoinExpliciter ljConditionExpliciter;
    private final LeftJoinTools leftJoinTools;
    private final IQTreeTools iqTreeTools;
    private final SubstitutionFactory substitutionFactory;
    private final TermFactory termFactory;

    @Inject
    protected MergeLJOptimizer(RightProvenanceNormalizer rightProvenanceNormalizer,
                               CoreSingletons coreSingletons,
                               CardinalitySensitiveJoinTransferLJOptimizer joinTransferLJOptimizer,
                               LJWithNestingOnRightToInnerJoinOptimizer ljReductionOptimizer,
                               ComplexStrictEqualityLeftJoinExpliciter ljConditionExpliciter,
                               LeftJoinTools leftJoinTools) {
        super(coreSingletons.getIQFactory(), NO_ACTION);
        this.rightProvenanceNormalizer = rightProvenanceNormalizer;
        this.otherLJOptimizer = joinTransferLJOptimizer;
        this.ljReductionOptimizer = ljReductionOptimizer;
        this.ljConditionExpliciter = ljConditionExpliciter;
        this.leftJoinTools = leftJoinTools;
        this.iqTreeTools = coreSingletons.getIQTreeTools();
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
        this.termFactory = coreSingletons.getTermFactory();
    }

    @Override
    protected IQTreeVariableGeneratorTransformer getTransformer() {
        return IQTreeVariableGeneratorTransformer.of(Transformer::new);
    }

    private class Transformer extends DefaultRecursiveIQTreeVisitingTransformerWithVariableGenerator {

        Transformer(VariableGenerator variableGenerator) {
            super(MergeLJOptimizer.this.iqFactory, variableGenerator);
        }

        @Override
        public IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            IQTree newLeftChild = transformChild(leftChild);
            IQTree newRightChild = transformChild(rightChild);

            LeftJoinDecomposition newLJ = LeftJoinDecomposition.of(rootNode, newLeftChild, newRightChild);

            if (!(newLeftChild.getRootNode() instanceof LeftJoinNode))
                return buildUnoptimizedLJTree(tree, leftChild, rightChild, newLJ);

            LeftJoinDecomposition normalization = ljConditionExpliciter.makeComplexEqualitiesImplicit(newLJ, variableGenerator);

            if (!normalization.tolerateLJConditionLifting())
                return buildUnoptimizedLJTree(tree, leftChild, rightChild, newLJ);

            Optional<IQTree> simplifiedTree = new SimplificationContext(normalization)
                    .tryToSimplify(ImmutableList.of(), normalization.leftChild());

            if (simplifiedTree.isPresent()) {
                var newTree = simplifiedTree.get();
                return iqTreeTools.unaryIQTreeBuilder(tree.getVariables())
                        .build(newTree)
                        .normalizeForOptimization(variableGenerator);
            }
            else {
                return buildUnoptimizedLJTree(tree, leftChild, rightChild, newLJ);
            }
        }

        private IQTree buildUnoptimizedLJTree(IQTree tree, IQTree leftChild, IQTree rightChild, LeftJoinDecomposition newLeftJoin) {
            return newLeftJoin.leftChild().equals(leftChild) && newLeftJoin.rightChild().equals(rightChild)
                    ? tree
                    : iqFactory.createBinaryNonCommutativeIQTree(newLeftJoin.getNode(), newLeftJoin.leftChild(), newLeftJoin.rightChild())
                    .normalizeForOptimization(variableGenerator);
        }

        private class SimplificationContext {

            private final LeftJoinDecomposition topLJ;

            SimplificationContext(LeftJoinDecomposition topLJ) {
                this.topLJ = topLJ;
            }

            private Optional<IQTree> tryToSimplify(ImmutableList<LeftJoinDecomposition> ancestors, IQTree currentLeftChild) {

                var leftLJ = LeftJoinDecomposition.of(currentLeftChild);
                if (!leftLJ.isPresent())
                    return Optional.empty();

                // No optimization if outside the "well-designed fragment" (NB: we ignore LJ conditions)
                // TODO: do we need this restriction? Isn't it always enforced?
                if (!Sets.intersection(leftLJ.rightSpecificVariables(), topLJ.rightVariables()).isEmpty())
                    return Optional.empty();

                /*
                 * If cannot be merged with this right child, continue the search on the left
                 */
                if (!leftLJ.tolerateLJConditionLifting()
                        || !canBeMerged(leftLJ.rightChild(), leftLJ.leftVariables())) {

                    return tryToSimplify(
                            Stream.concat(Stream.of(leftLJ), ancestors.stream()).collect(ImmutableCollectors.toList()),
                            leftLJ.leftChild());
                }

                IQTree mergedLocalRightBeforeRenaming = iqTreeTools.createInnerJoinTree(
                        ImmutableList.of(leftLJ.rightChild(), topLJ.rightChild()));

                var renaming = computeRenaming(leftLJ);

                IQTree newLocalRightTreeBeforeRenaming;
                if (renaming.isEmpty()) {
                    newLocalRightTreeBeforeRenaming = mergedLocalRightBeforeRenaming;
                }
                else {
                    var localRightProvenance = rightProvenanceNormalizer.normalizeRightProvenance(
                            mergedLocalRightBeforeRenaming, leftLJ.getTree().getVariables(),
                            Optional.empty(), variableGenerator);

                    newLocalRightTreeBeforeRenaming = localRightProvenance.getRightTree();
                }

                IQTree newLocalTree = iqTreeTools.createLeftJoinTree(
                        Optional.empty(), leftLJ.leftChild(), newLocalRightTreeBeforeRenaming.applyFreshRenaming(renaming));

                IQTree newLJTree = ancestors.stream()
                        .reduce(newLocalTree, (t, a) ->
                                        iqFactory.createBinaryNonCommutativeIQTree(a.getNode(), t, a.rightChild()),
                                (t1, t2) -> {
                                    throw new MinorOntopInternalBugException("Parallelization is not supported here");
                                });

                if (renaming.isEmpty())
                    return Optional.of(newLJTree);

                Substitution<ImmutableFunctionalTerm> newSubstitution = computeSubstitution(leftLJ, renaming);

                Set<Variable> newVariables =
                        Sets.difference(newLJTree.getVariables(), renaming.getRangeSet());

                IQTree newTree = iqFactory.createUnaryIQTree(
                        iqTreeTools.createExtendingConstructionNode(newVariables, newSubstitution),
                        newLJTree);

                return Optional.of(newTree);
            }

            private boolean canBeMerged(IQTree subRightChild, ImmutableSet<Variable> leftVariables) {
                return isTreeIncluded(subRightChild, topLJ.rightChild(), leftVariables)
                        && isTreeIncluded(topLJ.rightChild(), subRightChild, leftVariables);
            }

            private InjectiveSubstitution<Variable> computeRenaming(LeftJoinDecomposition leftLJ) {

                var localRightVariablesOnlySharedWithLeft = Sets.difference(Sets.intersection(leftLJ.leftVariables(), leftLJ.rightVariables()), topLJ.rightVariables());
                var topRightVariablesOnlySharedWithLeft = Sets.difference(Sets.intersection(leftLJ.leftVariables(), topLJ.rightVariables()), leftLJ.rightVariables());

                // some tests depend on the order in the steams
                return Stream.concat(
                                Stream.concat(
                                        (leftLJ.joinCondition().isPresent() || !localRightVariablesOnlySharedWithLeft.isEmpty())
                                                ? leftLJ.rightSpecificVariables().stream()
                                                : Stream.empty(),
                                        (topLJ.joinCondition().isPresent() || !topRightVariablesOnlySharedWithLeft.isEmpty())
                                                ? topLJ.rightSpecificVariables().stream()
                                                : Stream.empty()),
                                Stream.concat(
                                        localRightVariablesOnlySharedWithLeft.stream(),
                                        topRightVariablesOnlySharedWithLeft.stream()))
                        .distinct()
                        .collect(substitutionFactory.toFreshRenamingSubstitution(variableGenerator));
            }

            private Substitution<ImmutableFunctionalTerm> computeSubstitution(LeftJoinDecomposition leftLJ, InjectiveSubstitution<Variable> renaming) {

                var localRightVariablesOnlySharedWithLeft = Sets.difference(Sets.intersection(leftLJ.leftVariables(), leftLJ.rightVariables()), topLJ.rightVariables());
                var topRightVariablesOnlySharedWithLeft = Sets.difference(Sets.intersection(leftLJ.leftVariables(), topLJ.rightVariables()), leftLJ.rightVariables());

                var newLocalCondition = computeCondition(leftLJ, localRightVariablesOnlySharedWithLeft, renaming);
                var newTopCondition = computeCondition(topLJ, topRightVariablesOnlySharedWithLeft, renaming);

                var topRightSpecificVariables = topLJ.rightSpecificVariables();

                return renaming.builder()
                        .removeFromDomain(leftLJ.leftVariables())
                        .transform(
                                v -> (topRightSpecificVariables.contains(v) ? newTopCondition : newLocalCondition)
                                        .orElseThrow(() -> new MinorOntopInternalBugException("A lj condition was expected")),
                                (t, c) -> termFactory.getIfElseNull(c, t))
                        .build();
            }
        }

        private Optional<ImmutableExpression> computeCondition(LeftJoinDecomposition lj, Set<Variable> sharedWithLeftVariables, InjectiveSubstitution<Variable> renaming) {
            return termFactory.getConjunction(
                    lj.joinCondition().map(renaming::apply),
                    sharedWithLeftVariables.stream()
                            .map(v -> termFactory.getStrictEquality(v, renaming.apply(v))));
        }

        private boolean isTreeIncluded(IQTree tree, IQTree otherTree, ImmutableSet<Variable> leftVariables) {
            IQ minusIQ = leftJoinTools.constructMinusIQ(tree, otherTree, v -> !leftVariables.contains(v), variableGenerator);

            IQTree optimizedTree = ljReductionOptimizer.optimize(
                            otherLJOptimizer.optimize(minusIQ.normalizeForOptimization()))
                    .normalizeForOptimization().getTree();

            return optimizedTree.isDeclaredAsEmpty();
        }
    }
}
