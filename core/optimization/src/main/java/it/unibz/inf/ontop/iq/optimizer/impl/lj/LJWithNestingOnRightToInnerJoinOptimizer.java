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
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.LeftJoinNode;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.node.impl.JoinOrFilterVariableNullabilityTools;
import it.unibz.inf.ontop.iq.node.normalization.impl.RightProvenanceNormalizer;
import it.unibz.inf.ontop.iq.optimizer.LeftJoinIQOptimizer;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;
import static it.unibz.inf.ontop.iq.impl.IQTreeTools.BinaryNonCommutativeIQTreeDecomposition;

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
    private final JoinOrFilterVariableNullabilityTools variableNullabilityTools;
    private final LeftJoinTools leftJoinTools;

    @Inject
    protected LJWithNestingOnRightToInnerJoinOptimizer(RightProvenanceNormalizer rightProvenanceNormalizer,
                                                       CoreSingletons coreSingletons,
                                                       CardinalitySensitiveJoinTransferLJOptimizer otherLJOptimizer,
                                                       JoinOrFilterVariableNullabilityTools variableNullabilityTools, LeftJoinTools leftJoinTools) {
        this.rightProvenanceNormalizer = rightProvenanceNormalizer;
        this.coreSingletons = coreSingletons;
        this.iqFactory = coreSingletons.getIQFactory();
        this.otherLJOptimizer = otherLJOptimizer;
        this.variableNullabilityTools = variableNullabilityTools;
        this.leftJoinTools = leftJoinTools;
    }

    @Override
    public IQ optimize(IQ query) {
        IQTree initialTree = query.getTree();

        Transformer transformer = new Transformer(
                initialTree::getVariableNullability,
                query.getVariableGenerator(),
                rightProvenanceNormalizer,
                coreSingletons,
                otherLJOptimizer,
                variableNullabilityTools,
                leftJoinTools);

        IQTree newTree = initialTree.acceptTransformer(transformer);

        return newTree.equals(initialTree)
                ? query
                : iqFactory.createIQ(query.getProjectionAtom(), newTree);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected static class Transformer extends AbstractLJTransformer {
        private final CardinalitySensitiveJoinTransferLJOptimizer otherLJOptimizer;
        private final AtomFactory atomFactory;
        private final IQTreeTools iqTreeTools;
        private final LeftJoinTools leftJoinTools;

        protected Transformer(Supplier<VariableNullability> variableNullabilitySupplier,
                              VariableGenerator variableGenerator, RightProvenanceNormalizer rightProvenanceNormalizer,
                              CoreSingletons coreSingletons, CardinalitySensitiveJoinTransferLJOptimizer otherLJOptimizer,
                              JoinOrFilterVariableNullabilityTools variableNullabilityTools, LeftJoinTools leftJoinTools) {
            super(variableNullabilitySupplier, variableGenerator, rightProvenanceNormalizer, variableNullabilityTools,
                    coreSingletons);
            this.otherLJOptimizer = otherLJOptimizer;
            this.atomFactory = coreSingletons.getAtomFactory();
            this.iqTreeTools = coreSingletons.getIQTreeTools();
            this.leftJoinTools = leftJoinTools;
        }

        @Override
        protected Optional<IQTree> furtherTransformLeftJoin(LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            var construction = UnaryIQTreeDecomposition.of(rightChild, ConstructionNode.class);

            return Optional.of(construction.getTail())
                    .filter(t -> t.getRootNode() instanceof LeftJoinNode)
                    .map(t -> (BinaryNonCommutativeIQTree) t)
                    .flatMap(rLJ -> tryToSimplify(LeftJoinAnalysis.of(rootNode, leftChild, rightChild), rLJ));
        }

        @Override
        protected IQTree transformBySearchingFromScratch(IQTree tree) {
            Transformer newTransformer = new Transformer(tree::getVariableNullability, variableGenerator,
                    rightProvenanceNormalizer, coreSingletons, otherLJOptimizer, variableNullabilityTools, leftJoinTools);
            return tree.acceptTransformer(newTransformer);
        }

        @Override
        protected IQTree preTransformLJRightChild(IQTree rightChild, Optional<ImmutableExpression> ljCondition, ImmutableSet<Variable> leftVariables) {
            Supplier<VariableNullability> variableNullabilitySupplier =
                    () -> computeRightChildVariableNullability(rightChild, ljCondition);

            Transformer newTransformer = new Transformer(variableNullabilitySupplier, variableGenerator,
                    rightProvenanceNormalizer, coreSingletons, otherLJOptimizer, variableNullabilityTools, leftJoinTools);
            return rightChild.acceptTransformer(newTransformer);
        }

        private Optional<IQTree> tryToSimplify(LeftJoinAnalysis leftJoin,
                                               BinaryNonCommutativeIQTree rightLJ) {

            Set<Variable> commonVariables = Sets.intersection(leftJoin.leftVariables(), leftJoin.rightVariables());

            // If some variables defined by the construction node are common with the left --> no optimization
            if (!rightLJ.getVariables().containsAll(commonVariables))
                return Optional.empty();

            // In the presence of a LJ condition, a unique constraint must be present on the right child
            // and be joined over
            if (leftJoin.joinCondition().isPresent()
                    && leftJoin.rightChild().inferUniqueConstraints().stream()
                    .noneMatch(commonVariables::containsAll))
                return Optional.empty();

            Optional<IQTree> safeLeftOfRightDescendant = extractSafeLeftOfRightDescendantTree(
                    rightLJ.getLeftChild(), commonVariables);

            return safeLeftOfRightDescendant
                    .filter(r -> canLJBeReduced(leftJoin.leftChild(), r))
                    // Reduces the LJ to an inner join
                    .map(r -> buildInnerJoin(leftJoin))
                    .map(t -> t.normalizeForOptimization(variableGenerator));
        }

        private boolean canLJBeReduced(IQTree leftChild, IQTree safeLeftOfRightDescendant) {
            VariableNullability inheritedVariableNullability = getInheritedVariableNullability();

            IQ minusIQ = leftJoinTools.constructMinusIQ(leftChild, safeLeftOfRightDescendant, inheritedVariableNullability::isPossiblyNullable, variableGenerator);

            return otherLJOptimizer.optimize(minusIQ)
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

            var leftJoin = BinaryNonCommutativeIQTreeDecomposition.of(leftChild, LeftJoinNode.class);
            if (leftJoin.isPresent())
                // Recursive
                return extractSafeLeftOfRightDescendantTree(leftJoin.getLeftChild(), rightVariablesInteractingWithLeft);
            else
                return Optional.of(leftChild);
        }

        private IQTree buildInnerJoin(LeftJoinAnalysis leftJoin) {
            IQTree joinTree = iqTreeTools.createInnerJoinTree(ImmutableList.of(leftJoin.leftChild(), leftJoin.rightChild()));

            if (leftJoin.joinCondition().isEmpty())
                return joinTree;

            InjectiveSubstitution<Variable> renaming = leftJoin.rightSpecificVariables().stream()
                    .collect(substitutionFactory.toFreshRenamingSubstitution(variableGenerator));

            ImmutableExpression renamedCondition = renaming.apply(leftJoin.joinCondition().get());

            Substitution<ImmutableFunctionalTerm> newSubstitution = renaming.builder()
                    .transform(t -> termFactory.getIfElseNull(renamedCondition, t))
                    .build();

            return iqFactory.createUnaryIQTree(
                    iqFactory.createConstructionNode(joinTree.getVariables(),
                            newSubstitution),
                    joinTree.applyFreshRenaming(renaming));
        }
    }
}
