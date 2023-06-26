package it.unibz.inf.ontop.iq.optimizer.impl.lj;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.LeftJoinNode;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBStrictEqFunctionSymbol;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.AbstractCollection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * NOT the "normalizeForOptimization" normal form!
 */
@Singleton
public class ComplexStrictEqualityLeftJoinExpliciter {

    private final TermFactory termFactory;
    private final SubstitutionFactory substitutionFactory;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    protected ComplexStrictEqualityLeftJoinExpliciter(TermFactory termFactory, SubstitutionFactory substitutionFactory,
                                                      IntermediateQueryFactory iqFactory) {
        this.termFactory = termFactory;
        this.substitutionFactory = substitutionFactory;
        this.iqFactory = iqFactory;
    }

    public LeftJoinNormalization makeComplexEqualitiesImplicit(IQTree leftChild, IQTree rightChild,
                                                               Optional<ImmutableExpression> ljCondition,
                                                               VariableGenerator variableGenerator) {
        if (ljCondition.isPresent())
            return makeComplexEqualitiesImplicit(leftChild, rightChild, ljCondition.get(),
                    substitutionFactory.getSubstitution(), variableGenerator);

        return new LeftJoinNormalization(leftChild, rightChild, ljCondition, false);
    }

    protected LeftJoinNormalization makeComplexEqualitiesImplicit(IQTree leftChild, IQTree rightChild,
                                                                  ImmutableExpression ljCondition,
                                                                  Substitution<ImmutableTerm> downSubstitution,
                                                                  VariableGenerator variableGenerator) {

        var leftVariables = leftChild.getVariables();

        ImmutableSet<Variable> rightSpecificVariables = Sets.difference(rightChild.getVariables(), leftVariables)
                .immutableCopy();

        var conditionMap = ljCondition.flattenAND()
                .collect(ImmutableCollectors.partitioningBy(e -> isDecomposibleStrictEquality(e, leftVariables, rightSpecificVariables)));
        var strictEqualities = conditionMap.get(true);
        if (strictEqualities == null || strictEqualities.isEmpty())
            return new LeftJoinNormalization(leftChild, rightChild, Optional.of(ljCondition), false);

        var newLJCondition = Optional.ofNullable(conditionMap.get(false))
                .filter(cs -> !cs.isEmpty())
                .map(termFactory::getConjunction);

        var substitutionPair = computeSubstitutionPair(ImmutableSet.copyOf(strictEqualities),
                rightSpecificVariables, downSubstitution, variableGenerator);

        var newRight = iqFactory.createUnaryIQTree(
                iqFactory.createConstructionNode(
                        Sets.union(substitutionPair.rightSubstitution.getDomain(), rightChild.getVariables()).immutableCopy(),
                        substitutionPair.rightSubstitution),
                rightChild);

        IQTree newLeft = normalizeLeft(leftChild, substitutionPair.leftSubstitution, variableGenerator);

        return new LeftJoinNormalization(newLeft, newRight, newLJCondition,
                newLeft.getVariables().size() != leftVariables.size());
    }

    private SubstitutionPair computeSubstitutionPair(ImmutableSet<ImmutableExpression> strictEqualities,
                                                     ImmutableSet<Variable> rightSpecificVariables,
                                                     Substitution<ImmutableTerm> downSubstitution,
                                                     VariableGenerator variableGenerator) {

        Map<Variable, ImmutableTerm> leftSubstitutionMap = new HashMap<>();
        Map<Variable, ImmutableTerm> rightSubstitutionMap = new HashMap<>();

        for (ImmutableExpression strictEquality : strictEqualities) {
            ImmutableTerm rightArgument = strictEquality.getTerms().stream()
                    .filter(t -> t.getVariableStream().anyMatch(rightSpecificVariables::contains))
                    .findAny()
                    .orElseThrow(() -> new MinorOntopInternalBugException("Was expecting a right argument"));

            ImmutableTerm leftArgument = strictEquality.getTerm(0).equals(rightArgument)
                    ? strictEquality.getTerm(1)
                    : strictEquality.getTerm(0);

            if (leftArgument instanceof Variable) {
                rightSubstitutionMap.put((Variable) leftArgument, rightArgument);
            }
            else {
                var leftVariableFromDownSubstitution = downSubstitution.getPreImage(t -> t.equals(leftArgument)).stream()
                        .findAny();

                var leftVariable = leftVariableFromDownSubstitution
                        .orElseGet(variableGenerator::generateNewVariable);

                if (leftVariableFromDownSubstitution.isEmpty())
                    leftSubstitutionMap.put(leftVariable, leftArgument);
                rightSubstitutionMap.put(leftVariable, rightArgument);
            }
        }
        return new SubstitutionPair(
                downSubstitution.compose(
                    leftSubstitutionMap.entrySet().stream()
                        .collect(substitutionFactory.toSubstitution())),
                rightSubstitutionMap.entrySet().stream()
                        .collect(substitutionFactory.toSubstitution()));
    }

    /**
     * NB: excludes equalities to constants
     */
    private boolean isDecomposibleStrictEquality(ImmutableExpression expression, ImmutableSet<Variable> leftVariables,
                                                 ImmutableSet<Variable> rightSpecificVariables) {
        if  (!(expression.getFunctionSymbol() instanceof DBStrictEqFunctionSymbol))
            return false;

        // TODO: support other arities
        if (expression.getArity() != 2)
            return false;

        var subTermVariables = expression.getTerms().stream()
                .map(t -> t.getVariableStream().collect(ImmutableCollectors.toSet()))
                .collect(ImmutableCollectors.toList());

        return subTermVariables.stream()
                .noneMatch(AbstractCollection::isEmpty)
                && subTermVariables.stream()
                .anyMatch(leftVariables::containsAll)
                && subTermVariables.stream()
                .anyMatch(rightSpecificVariables::containsAll);
    }

    private IQTree normalizeLeft(IQTree tree, Substitution<ImmutableTerm> downSubstitution, VariableGenerator variableGenerator) {

        if (!((tree.getRootNode() instanceof LeftJoinNode) &&
                tree.getChildren().get(0).getVariables()
                        // Blocks the substitution if there is any right-specific variable in the substitution (unlikely)
                        // Stops the normalization
                        .containsAll(downSubstitution.getRangeVariables())))
            return downSubstitution.isEmpty()
                    ? tree
                    : iqFactory.createUnaryIQTree(
                            iqFactory.createConstructionNode(
                                    Sets.union(downSubstitution.getDomain(), tree.getVariables()).immutableCopy(),
                                    downSubstitution),
                            tree);

        var leftChild = tree.getChildren().get(0);
        var rightChild = tree.getChildren().get(1);

        var leftJoinNode = (LeftJoinNode) tree.getRootNode();

        var leftJoinCondition = leftJoinNode.getOptionalFilterCondition();
        if (leftJoinCondition.isEmpty()) {
            var newLeft = normalizeLeft(leftChild, downSubstitution, variableGenerator);
            return iqFactory.createBinaryNonCommutativeIQTree(
                    leftJoinNode,
                    newLeft, rightChild);
        }

        var localNormalization = makeComplexEqualitiesImplicit(leftChild, rightChild,
                leftJoinCondition.get(), downSubstitution, variableGenerator);

        return iqFactory.createBinaryNonCommutativeIQTree(
                iqFactory.createLeftJoinNode(localNormalization.ljCondition),
                localNormalization.leftChild,
                localNormalization.rightChild);
    }


    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static class LeftJoinNormalization {
        public final IQTree leftChild;
        public final IQTree rightChild;
        public final Optional<ImmutableExpression> ljCondition;

        public boolean isIntroducingNewVariables;

        public LeftJoinNormalization(IQTree leftChild, IQTree rightChild, Optional<ImmutableExpression> ljCondition,
                                     boolean isIntroducingNewVariables) {
            this.leftChild = leftChild;
            this.rightChild = rightChild;
            this.ljCondition = ljCondition;
            this.isIntroducingNewVariables = isIntroducingNewVariables;
        }
    }

    public static class SubstitutionPair {
        public final Substitution<ImmutableTerm> leftSubstitution;
        public final Substitution<ImmutableTerm> rightSubstitution;

        public SubstitutionPair(Substitution<ImmutableTerm> leftSubstitution, Substitution<ImmutableTerm> rightSubstitution) {
            this.leftSubstitution = leftSubstitution;
            this.rightSubstitution = rightSubstitution;
        }
    }

}
