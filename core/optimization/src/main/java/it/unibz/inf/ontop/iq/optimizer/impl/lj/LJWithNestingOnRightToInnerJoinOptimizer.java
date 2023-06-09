package it.unibz.inf.ontop.iq.optimizer.impl.lj;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.LeftJoinNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.normalization.impl.RightProvenanceNormalizer;
import it.unibz.inf.ontop.iq.optimizer.LeftJoinIQOptimizer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Restricted to LJs on the right to limit overlap with existing techniques.
 * Typical case optimized: self-left-join with LJ nesting on the right (and possibly on the left)
 *
 */
@Singleton
public class LJWithNestingOnRightToInnerJoinOptimizer implements LeftJoinIQOptimizer {

    private final RightProvenanceNormalizer rightProvenanceNormalizer;
    private final CoreSingletons coreSingletons;
    private final IntermediateQueryFactory iqFactory;
    private final CardinalitySensitiveJoinTransferLJOptimizer otherLJOptimizer;

    @Inject
    protected LJWithNestingOnRightToInnerJoinOptimizer(RightProvenanceNormalizer rightProvenanceNormalizer,
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

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected static class Transformer extends DefaultRecursiveIQTreeVisitingTransformer {

        private final VariableGenerator variableGenerator;
        private final RightProvenanceNormalizer rightProvenanceNormalizer;
        private final TermFactory termFactory;
        private final CardinalitySensitiveJoinTransferLJOptimizer otherLJOptimizer;
        private final AtomFactory atomFactory;
        private final SubstitutionFactory substitutionFactory;

        protected Transformer(VariableGenerator variableGenerator, RightProvenanceNormalizer rightProvenanceNormalizer,
                              CoreSingletons coreSingletons, CardinalitySensitiveJoinTransferLJOptimizer otherLJOptimizer) {
            super(coreSingletons);
            this.variableGenerator = variableGenerator;
            this.rightProvenanceNormalizer = rightProvenanceNormalizer;
            this.termFactory = coreSingletons.getTermFactory();
            this.otherLJOptimizer = otherLJOptimizer;
            this.atomFactory = coreSingletons.getAtomFactory();
            this.substitutionFactory = coreSingletons.getSubstitutionFactory();
        }

        @Override
        public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            IQTree newLeftChild = transform(leftChild);
            IQTree newRightChild = transform(rightChild);

            Optional<ConstructionNode> rightConstructionNode = Optional.of(newRightChild.getRootNode())
                    .filter(n -> n instanceof ConstructionNode)
                    .map(n -> (ConstructionNode) n);

            Optional<BinaryNonCommutativeIQTree> rightLJ = rightConstructionNode
                    .map(c -> newRightChild.getChildren().get(0))
                    .or(() -> Optional.of(newRightChild))
                    .filter(t -> t.getRootNode() instanceof LeftJoinNode)
                    .map(t -> (BinaryNonCommutativeIQTree) t);

            Optional<IQTree> simplifiedTree = rightLJ
                    .flatMap(rLJ -> tryToSimplify(newLeftChild, newRightChild, rootNode.getOptionalFilterCondition(), rLJ));

            return simplifiedTree
                    .orElseGet(() -> newLeftChild.equals(leftChild) && newRightChild.equals(rightChild)
                            ? tree
                            : iqFactory.createBinaryNonCommutativeIQTree(rootNode, newLeftChild, newRightChild)
                            .normalizeForOptimization(variableGenerator));
        }

        private Optional<IQTree> tryToSimplify(IQTree leftChild, IQTree rightChild,
                                               Optional<ImmutableExpression> leftJoinCondition,
                                               BinaryNonCommutativeIQTree rightLJ) {
            Set<Variable> commonVariables = Sets.intersection(leftChild.getVariables(), rightChild.getVariables());

            // If some variables defined by the construction node are common with the left --> no optimization
            if (!rightLJ.getVariables().containsAll(commonVariables))
                return Optional.empty();

            // In the presence of a LJ condition, a unique constraint must be present on the right child
            // and be joined over
            if (leftJoinCondition.isPresent()
                    && rightChild.inferUniqueConstraints().stream()
                    .noneMatch(commonVariables::containsAll))
                return Optional.empty();

            Optional<IQTree> safeLeftOfRightDescendant = extractSafeLeftOfRightDescendantTree(
                    rightLJ.getLeftChild(), commonVariables);

            return safeLeftOfRightDescendant
                    .filter(r -> canLJBeReduced(leftChild, r))
                    // Reduces the LJ to an inner join
                    .map(r -> buildInnerJoin(leftChild, rightChild, leftJoinCondition))
                    .map(t -> t.normalizeForOptimization(variableGenerator));
        }

        private boolean canLJBeReduced(IQTree leftChild, IQTree safeLeftOfRightDescendant) {
            RightProvenanceNormalizer.RightProvenance rightProvenance = rightProvenanceNormalizer.normalizeRightProvenance(
                    safeLeftOfRightDescendant, leftChild.getVariables(), Optional.empty(), variableGenerator);

            IQTree minusTree = iqFactory.createUnaryIQTree(
                    iqFactory.createFilterNode(termFactory.getDBIsNull(rightProvenance.getProvenanceVariable())),
                    iqFactory.createBinaryNonCommutativeIQTree(
                            iqFactory.createLeftJoinNode(),
                            leftChild, rightProvenance.getRightTree()));

            // Hack
            DistinctVariableOnlyDataAtom minusFakeProjectionAtom = atomFactory.getDistinctVariableOnlyDataAtom(
                    atomFactory.getRDFAnswerPredicate(minusTree.getVariables().size()),
                    ImmutableList.copyOf(minusTree.getVariables()));

            return otherLJOptimizer.optimize(iqFactory.createIQ(minusFakeProjectionAtom, minusTree))
                    .normalizeForOptimization().getTree()
                    .isDeclaredAsEmpty();
        }

        /**
         * Finds the first non-LJ descendant on the left. If it exposes all the variables interacting with the left, returns it
         */
        private Optional<IQTree> extractSafeLeftOfRightDescendantTree(IQTree leftChild, Set<Variable> rightVariablesInteractingWithLeft) {
            // To be safe, we want all these variables to be present on the left
            // Otherwise, we don't apply this optimization
            if (!leftChild.getVariables().containsAll(rightVariablesInteractingWithLeft))
                return Optional.empty();

            QueryNode rootNode = leftChild.getRootNode();
            if (rootNode instanceof LeftJoinNode)
                // Recursive
                return extractSafeLeftOfRightDescendantTree(leftChild.getChildren().get(0), rightVariablesInteractingWithLeft);
            else
                return Optional.of(leftChild);
        }

        private IQTree buildInnerJoin(IQTree leftChild, IQTree rightChild, Optional<ImmutableExpression> leftJoinCondition) {
            IQTree joinTree = iqFactory.createNaryIQTree(iqFactory.createInnerJoinNode(),
                    ImmutableList.of(leftChild, rightChild));

            if (leftJoinCondition.isEmpty())
                return joinTree;

            InjectiveSubstitution<Variable> renaming = Sets.difference(rightChild.getVariables(), leftChild.getVariables()).stream()
                    .collect(substitutionFactory.toFreshRenamingSubstitution(variableGenerator));

            ImmutableExpression renamedCondition = renaming.apply(leftJoinCondition.get());

            return iqFactory.createUnaryIQTree(
                    iqFactory.createConstructionNode(joinTree.getVariables(),
                            renaming.builder()
                                    .transform(t -> termFactory.getIfElseNull(renamedCondition, t))
                                    .build()),
                    joinTree.applyFreshRenaming(renaming));
        }
    }
}
