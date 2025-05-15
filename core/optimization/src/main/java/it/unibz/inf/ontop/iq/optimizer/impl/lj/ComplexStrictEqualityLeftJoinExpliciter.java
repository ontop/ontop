package it.unibz.inf.ontop.iq.optimizer.impl.lj;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
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

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.BinaryNonCommutativeIQTreeDecomposition;


/**
 * NOT the "normalizeForOptimization" normal form!
 */
@Singleton
public class ComplexStrictEqualityLeftJoinExpliciter {

    private final TermFactory termFactory;
    private final SubstitutionFactory substitutionFactory;
    private final IntermediateQueryFactory iqFactory;
    private final IQTreeTools iqTreeTools;

    @Inject
    protected ComplexStrictEqualityLeftJoinExpliciter(TermFactory termFactory, SubstitutionFactory substitutionFactory,
                                                      IntermediateQueryFactory iqFactory, IQTreeTools iqTreeTools) {
        this.termFactory = termFactory;
        this.substitutionFactory = substitutionFactory;
        this.iqFactory = iqFactory;
        this.iqTreeTools = iqTreeTools;
    }

    public LeftJoinAnalysis makeComplexEqualitiesImplicit(LeftJoinAnalysis leftJoin,
                                                               VariableGenerator variableGenerator) {

        if (leftJoin.joinCondition().isEmpty())
            return leftJoin;

        return makeComplexEqualitiesImplicit(leftJoin, substitutionFactory.getSubstitution(), variableGenerator);
    }

    protected LeftJoinAnalysis makeComplexEqualitiesImplicit(LeftJoinAnalysis leftJoin,
                                                                  Substitution<ImmutableTerm> downSubstitution,
                                                                  VariableGenerator variableGenerator) {

        ImmutableSet<Variable> rightSpecificVariables = leftJoin.rightSpecificVariables();

        var conditionMap = leftJoin.joinCondition().orElseThrow().flattenAND()
                .collect(ImmutableCollectors.partitioningBy(
                        e -> isDecomposibleStrictEquality(e, leftJoin.leftChild().getVariables(), rightSpecificVariables)));
        var strictEqualities = conditionMap.get(true);
        assert strictEqualities != null;
        if (strictEqualities.isEmpty())
            return leftJoin;

        var otherConditions = conditionMap.get(false);
        assert otherConditions != null;
        var newLeftJoin = iqFactory.createLeftJoinNode(Optional.of(otherConditions)
                .filter(cs -> !cs.isEmpty())
                .map(termFactory::getConjunction));

        var substitutionPair = computeSubstitutionPair(ImmutableSet.copyOf(strictEqualities),
                rightSpecificVariables, downSubstitution, variableGenerator);

        IQTree newLeftChild = normalizeLeft(leftJoin.leftChild(), substitutionPair.leftSubstitution, variableGenerator);

        var newRightChild = iqFactory.createUnaryIQTree(
                iqTreeTools.createExtendingConstructionNode(leftJoin.rightChild().getVariables(), substitutionPair.rightSubstitution),
                leftJoin.rightChild());

        return LeftJoinAnalysis.of(newLeftJoin, newLeftChild, newRightChild);
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

        var leftJoin = BinaryNonCommutativeIQTreeDecomposition.of(tree, LeftJoinNode.class);
        if (!leftJoin.isPresent() ||
                // Blocks the substitution if there is any right-specific variable in the substitution (unlikely)
                // Stops the normalization
                !leftJoin.getLeftChild().getVariables().containsAll(downSubstitution.getRangeVariables()))
            return downSubstitution.isEmpty()
                    ? tree
                    : iqFactory.createUnaryIQTree(
                            iqTreeTools.createExtendingConstructionNode(tree.getVariables(), downSubstitution),
                            tree);

        if (leftJoin.getNode().getOptionalFilterCondition().isEmpty()) {
            var newLeftChild = normalizeLeft(leftJoin.getLeftChild(), downSubstitution, variableGenerator);
            return iqFactory.createBinaryNonCommutativeIQTree(
                    leftJoin.getNode(),
                    newLeftChild,
                    leftJoin.getRightChild());
        }

        var localNormalization = makeComplexEqualitiesImplicit(
                LeftJoinAnalysis.of(leftJoin),
                downSubstitution, variableGenerator);

        return iqFactory.createBinaryNonCommutativeIQTree(
                iqFactory.createLeftJoinNode(localNormalization.joinCondition()),
                localNormalization.leftChild(),
                localNormalization.rightChild());
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
