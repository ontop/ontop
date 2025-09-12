package it.unibz.inf.ontop.iq.optimizer.impl.lj;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.LeftJoinNode;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.node.impl.JoinOrFilterVariableNullabilityTools;
import it.unibz.inf.ontop.iq.node.normalization.impl.RightProvenanceNormalizer;
import it.unibz.inf.ontop.iq.transform.IQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;
import static it.unibz.inf.ontop.iq.impl.BinaryNonCommutativeIQTreeTools.LeftJoinDecomposition;

/**
 * Restricted to LJs on the right to limit overlap with existing techniques.
 * Typical case optimized: self-left-join with LJ nesting on the right (and possibly on the left)
 *
 */
@Singleton
public class LJWithNestingOnRightToInnerJoinOptimizer implements IQTreeVariableGeneratorTransformer {

    private final RightProvenanceNormalizer rightProvenanceNormalizer;
    private final CardinalitySensitiveJoinTransferLJOptimizer otherLJOptimizer;
    private final JoinOrFilterVariableNullabilityTools variableNullabilityTools;
    private final LeftJoinTools leftJoinTools;

    private final CoreSingletons coreSingletons;
    private final IQTreeTools iqTreeTools;
    private final SubstitutionFactory substitutionFactory;

    @Inject
    protected LJWithNestingOnRightToInnerJoinOptimizer(RightProvenanceNormalizer rightProvenanceNormalizer,
                                                       CoreSingletons coreSingletons,
                                                       CardinalitySensitiveJoinTransferLJOptimizer otherLJOptimizer,
                                                       JoinOrFilterVariableNullabilityTools variableNullabilityTools,
                                                       LeftJoinTools leftJoinTools) {
        this.rightProvenanceNormalizer = rightProvenanceNormalizer;
        this.otherLJOptimizer = otherLJOptimizer;
        this.variableNullabilityTools = variableNullabilityTools;
        this.leftJoinTools = leftJoinTools;

        this.coreSingletons = coreSingletons;
        this.iqTreeTools = coreSingletons.getIQTreeTools();
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return transform(tree, tree::getVariableNullability, variableGenerator);
    }

    private IQTree transform(IQTree tree, Supplier<VariableNullability> variableNullabilitySupplier, VariableGenerator variableGenerator) {
        return tree.acceptVisitor(new Transformer(variableNullabilitySupplier, variableGenerator));
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private class Transformer extends AbstractLJTransformerWithVariableNullability {

        Transformer(Supplier<VariableNullability> variableNullabilitySupplier,
                              VariableGenerator variableGenerator) {
            super(t -> transform(t, t::getVariableNullability, variableGenerator),
                    variableNullabilitySupplier,
                    variableGenerator,
                    LJWithNestingOnRightToInnerJoinOptimizer.this.rightProvenanceNormalizer,
                    LJWithNestingOnRightToInnerJoinOptimizer.this.variableNullabilityTools,
                    LJWithNestingOnRightToInnerJoinOptimizer.this.coreSingletons);
        }

        @Override
        protected Optional<IQTree> furtherTransformLeftJoin(LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            var construction = UnaryIQTreeDecomposition.of(rightChild, ConstructionNode.class);
            var leftJoinOnTheRight = LeftJoinDecomposition.of(construction.getTail());
            if (!leftJoinOnTheRight.isPresent())
                return Optional.empty();

            LeftJoinDecomposition leftJoin = LeftJoinDecomposition.of(rootNode, leftChild, rightChild);
            Set<Variable> commonVariables = leftJoin.commonVariables();

            // If some variables defined by the construction node are common with the left --> no optimization
            if (!leftJoinOnTheRight.projectedVariables().containsAll(commonVariables))
                return Optional.empty();

            // In the presence of a LJ condition, a unique constraint must be present on the right child
            // and be joined over
            if (leftJoin.joinCondition().isPresent()
                    && leftJoin.rightChild().inferUniqueConstraints().stream().noneMatch(commonVariables::containsAll))
                return Optional.empty();

            Optional<IQTree> safeLeftOfRightDescendant = extractSafeLeftOfRightDescendantTree(
                    leftJoinOnTheRight.leftChild(), commonVariables);

            if (safeLeftOfRightDescendant.isEmpty()
                    || !canLJBeReduced(leftJoin.leftChild(), safeLeftOfRightDescendant.get()))
                return Optional.empty();

            // Reduces the LJ to an inner join
            IQTree joinTree = iqTreeTools.createInnerJoinTree(ImmutableList.of(leftJoin.leftChild(), leftJoin.rightChild()));

            if (leftJoin.joinCondition().isEmpty()) {
                return Optional.of(joinTree.normalizeForOptimization(variableGenerator));
            }

            InjectiveSubstitution<Variable> renaming = leftJoin.rightSpecificVariables().stream()
                    .collect(substitutionFactory.toFreshRenamingSubstitution(variableGenerator));

            ImmutableExpression renamedCondition = renaming.apply(leftJoin.joinCondition().get());
            Substitution<ImmutableFunctionalTerm> newSubstitution = renaming.builder()
                    .transform(t -> termFactory.getIfElseNull(renamedCondition, t))
                    .build();

            IQTree result = iqFactory.createUnaryIQTree(
                    iqFactory.createConstructionNode(joinTree.getVariables(), newSubstitution),
                    joinTree.applyFreshRenaming(renaming));

            return Optional.of(result.normalizeForOptimization(variableGenerator));
        }

        @Override
        protected IQTree preTransformLJRightChild(IQTree rightChild, Optional<ImmutableExpression> ljCondition, ImmutableSet<Variable> leftVariables) {
            Supplier<VariableNullability> variableNullabilitySupplier =
                    () -> computeRightChildVariableNullability(rightChild, ljCondition);

            return transform(rightChild, variableNullabilitySupplier, variableGenerator);
        }

        private boolean canLJBeReduced(IQTree leftChild, IQTree safeLeftOfRightDescendant) {
            VariableNullability inheritedVariableNullability = getInheritedVariableNullability();

            IQ minusIQ = leftJoinTools.constructMinusIQ(leftChild, safeLeftOfRightDescendant, inheritedVariableNullability::isPossiblyNullable);

            return IQTreeVariableGeneratorTransformer.of(
                            otherLJOptimizer,
                            IQTree::normalizeForOptimization)
                    .transform(minusIQ.getTree(), minusIQ.getVariableGenerator())
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

            var leftJoin = LeftJoinDecomposition.of(leftChild);
            if (leftJoin.isPresent())
                // Recursive
                return extractSafeLeftOfRightDescendantTree(leftJoin.leftChild(), rightVariablesInteractingWithLeft);
            else
                return Optional.of(leftChild);
        }
    }
}
