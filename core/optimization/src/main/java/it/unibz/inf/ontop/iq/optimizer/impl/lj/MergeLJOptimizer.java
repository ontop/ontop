package it.unibz.inf.ontop.iq.optimizer.impl.lj;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.LeftJoinNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.normalization.impl.RightProvenanceNormalizer;
import it.unibz.inf.ontop.iq.optimizer.LeftJoinIQOptimizer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Tries to merge LJs nested on the left
 *
 */
public class MergeLJOptimizer implements LeftJoinIQOptimizer {

    private final RightProvenanceNormalizer rightProvenanceNormalizer;
    private final CoreSingletons coreSingletons;
    private final IntermediateQueryFactory iqFactory;
    private final CardinalitySensitiveJoinTransferLJOptimizer otherLJOptimizer;

    @Inject
    protected MergeLJOptimizer(RightProvenanceNormalizer rightProvenanceNormalizer,
                               CoreSingletons coreSingletons,
                               CardinalitySensitiveJoinTransferLJOptimizer otherLJOptimizer) {
        this.rightProvenanceNormalizer = rightProvenanceNormalizer;
        this.coreSingletons = coreSingletons;
        this.iqFactory = coreSingletons.getIQFactory();
        this.otherLJOptimizer = otherLJOptimizer;
    }

    @Override
    public IQ optimize(IQ query) {
        IQTree initialTree = query.getTree();

        Transformer transformer = new Transformer(
                query.getVariableGenerator(),
                rightProvenanceNormalizer,
                coreSingletons,
                otherLJOptimizer);

        IQTree newTree = initialTree.acceptTransformer(transformer);

        return newTree.equals(initialTree)
                ? query
                : iqFactory.createIQ(query.getProjectionAtom(), newTree);
    }

    protected static class Transformer extends DefaultRecursiveIQTreeVisitingTransformer {

        private final VariableGenerator variableGenerator;
        private final RightProvenanceNormalizer rightProvenanceNormalizer;
        private final TermFactory termFactory;
        private final CardinalitySensitiveJoinTransferLJOptimizer otherLJOptimizer;
        private final AtomFactory atomFactory;

        protected Transformer(VariableGenerator variableGenerator, RightProvenanceNormalizer rightProvenanceNormalizer,
                              CoreSingletons coreSingletons, CardinalitySensitiveJoinTransferLJOptimizer otherLJOptimizer) {
            super(coreSingletons);
            this.variableGenerator = variableGenerator;
            this.rightProvenanceNormalizer = rightProvenanceNormalizer;
            this.termFactory = coreSingletons.getTermFactory();
            this.otherLJOptimizer = otherLJOptimizer;
            this.atomFactory = coreSingletons.getAtomFactory();
        }

        @Override
        public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            if (rootNode.getOptionalFilterCondition().isPresent())
                return super.transformLeftJoin(tree, rootNode, leftChild, rightChild)
                        .normalizeForOptimization(variableGenerator);

            IQTree newLeftChild = transform(leftChild);
            IQTree newRightChild = transform(rightChild);

            Optional<IQTree> simplifiedTree = tryToSimplify(newLeftChild, newRightChild, new LinkedList<>());

            return simplifiedTree
                    .orElseGet(() -> newLeftChild.equals(leftChild) && newRightChild.equals(rightChild)
                            ? tree
                            : iqFactory.createBinaryNonCommutativeIQTree(rootNode, newLeftChild, newRightChild)
                            .normalizeForOptimization(variableGenerator));
        }

        private Optional<IQTree> tryToSimplify(IQTree leftDescendent, IQTree rightChildToMerge, List<Ancestor> ancestors) {
            QueryNode leftRootNode = leftDescendent.getRootNode();
            if (!(leftRootNode instanceof LeftJoinNode))
                return Optional.empty();

            LeftJoinNode leftJoinNode = (LeftJoinNode) leftRootNode;
            BinaryNonCommutativeIQTree leftJoinTree = (BinaryNonCommutativeIQTree) leftDescendent;
            IQTree leftSubTree = leftJoinTree.getLeftChild();
            IQTree rightSubTree = leftJoinTree.getRightChild();

            // No optimization if outside the "well-designed fragment"
            if (!Sets.intersection(
                    Sets.difference(rightSubTree.getVariables(), leftSubTree.getVariables()),
                    rightChildToMerge.getVariables()).isEmpty())
                return Optional.empty();

            /*
             * If cannot be merged with this right child, continue the search on the left.
             */
            if (leftJoinNode.getOptionalFilterCondition().isPresent()
                    || (!canBeMerged(rightSubTree, rightChildToMerge))) {
              ancestors.add(0, new Ancestor(leftJoinNode, rightSubTree));
              return tryToSimplify(leftSubTree, rightChildToMerge, ancestors);
            }

            IQTree newSubTree = iqFactory.createBinaryNonCommutativeIQTree(
                    leftJoinNode,
                    leftSubTree,
                    iqFactory.createNaryIQTree(
                            iqFactory.createInnerJoinNode(),
                            ImmutableList.of(rightSubTree, rightChildToMerge)));

            IQTree newTree = ancestors.stream()
                    .reduce(newSubTree, (t, a) -> iqFactory.createBinaryNonCommutativeIQTree(a.rootNode, t, a.right),
                            (t1, t2) -> {
                                throw new MinorOntopInternalBugException("Parallelization is not supported here");
                            });

            return Optional.of(newTree);
        }

        private boolean canBeMerged(IQTree subRightChild, IQTree rightChildToMerge) {
            return isTreeIncluded(subRightChild, rightChildToMerge) && isTreeIncluded(rightChildToMerge, subRightChild);
        }

        private boolean isTreeIncluded(IQTree tree, IQTree otherTree) {
            RightProvenanceNormalizer.RightProvenance rightProvenance = rightProvenanceNormalizer.normalizeRightProvenance(
                    otherTree, tree.getVariables(), Optional.empty(), variableGenerator);

            IQTree minusTree = iqFactory.createUnaryIQTree(
                    iqFactory.createFilterNode(termFactory.getDBIsNull(rightProvenance.getProvenanceVariable())),
                    iqFactory.createBinaryNonCommutativeIQTree(
                            iqFactory.createLeftJoinNode(),
                            tree, rightProvenance.getRightTree()));

            // Hack
            DistinctVariableOnlyDataAtom minusFakeProjectionAtom = atomFactory.getDistinctVariableOnlyDataAtom(
                    atomFactory.getRDFAnswerPredicate(minusTree.getVariables().size()),
                    ImmutableList.copyOf(minusTree.getVariables()));

            return otherLJOptimizer.optimize(iqFactory.createIQ(minusFakeProjectionAtom, minusTree))
                    .normalizeForOptimization().getTree()
                    .isDeclaredAsEmpty();
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
