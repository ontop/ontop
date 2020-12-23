package it.unibz.inf.ontop.iq.node.impl;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import it.unibz.inf.ontop.evaluator.TermNullabilityEvaluator;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.iq.node.JoinOrFilterNode;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableSubstitutionTools;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;


public abstract class JoinOrFilterNodeImpl extends CompositeQueryNodeImpl implements JoinOrFilterNode {

    private Optional<ImmutableExpression> optionalFilterCondition;
    private final TermNullabilityEvaluator nullabilityEvaluator;
    protected final TermFactory termFactory;
    protected final TypeFactory typeFactory;
    protected final SubstitutionFactory substitutionFactory;
    protected final ImmutableUnificationTools unificationTools;
    protected final ImmutableSubstitutionTools substitutionTools;


    protected JoinOrFilterNodeImpl(Optional<ImmutableExpression> optionalFilterCondition,
                                   TermNullabilityEvaluator nullabilityEvaluator, TermFactory termFactory,
                                   IntermediateQueryFactory iqFactory, TypeFactory typeFactory,
                                   SubstitutionFactory substitutionFactory,
                                   ImmutableUnificationTools unificationTools, ImmutableSubstitutionTools substitutionTools) {
        super(substitutionFactory, iqFactory);
        this.optionalFilterCondition = optionalFilterCondition;
        this.nullabilityEvaluator = nullabilityEvaluator;
        this.termFactory = termFactory;
        this.typeFactory = typeFactory;
        this.substitutionFactory = substitutionFactory;
        this.unificationTools = unificationTools;
        this.substitutionTools = substitutionTools;
    }

    @Override
    public Optional<ImmutableExpression> getOptionalFilterCondition() {
        return optionalFilterCondition;
    }

    protected String getOptionalFilterString() {
        if (optionalFilterCondition.isPresent()) {
            return " " + optionalFilterCondition.get().toString();
        }

        return "";
    }

    @Override
    public ImmutableSet<Variable> getLocalVariables() {
        if (optionalFilterCondition.isPresent()) {
            return optionalFilterCondition.get().getVariables();
        }
        else {
            return ImmutableSet.of();
        }
    }

    protected boolean isFilteringNullValue(Variable variable) {
        return getOptionalFilterCondition()
                .filter(e -> nullabilityEvaluator.isFilteringNullValue(e, variable))
                .isPresent();
    }

    protected TermNullabilityEvaluator getNullabilityEvaluator() {
        return nullabilityEvaluator;
    }

    @Override
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        return getLocalVariables();
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        return ImmutableSet.of();
    }

    protected void checkExpression(ImmutableExpression expression, ImmutableList<IQTree> children)
            throws InvalidIntermediateQueryException {

        ImmutableSet<Variable> childrenVariables = children.stream()
                .flatMap(c -> c.getVariables().stream())
                .collect(ImmutableCollectors.toSet());

        ImmutableSet<Variable> unboundVariables = expression.getVariableStream()
                .filter(v -> !childrenVariables.contains(v))
                .collect(ImmutableCollectors.toSet());
        if (!unboundVariables.isEmpty()) {
            throw new InvalidIntermediateQueryException("Expression " + expression + " of "
                    + expression + " uses unbound variables (" + unboundVariables +  ").\n" + this);
        }
    }

    protected ImmutableSet<Variable> computeNotInternallyRequiredVariables(ImmutableList<IQTree> children) {
        ImmutableSet<Variable> conditionVariables = getLocallyRequiredVariables();

        ImmutableSet<Variable> notInternallyRequiredByAtLeastAChild = children.stream()
                .flatMap(c -> c.getNotInternallyRequiredVariables().stream())
                .collect(ImmutableCollectors.toSet());

        // All variables are required
        if (notInternallyRequiredByAtLeastAChild.isEmpty())
            return notInternallyRequiredByAtLeastAChild;

        ImmutableMultiset<Variable> childVariableMultiset = children.stream()
                .flatMap(c -> c.getVariables().stream())
                .collect(ImmutableCollectors.toMultiset());

        return childVariableMultiset.entrySet().stream()
                // Only coming from one child
                .filter(e -> e.getCount() == 1)
                .map(Multiset.Entry::getElement)
                .filter(notInternallyRequiredByAtLeastAChild::contains)
                .filter(v -> !conditionVariables.contains(v))
                .collect(ImmutableCollectors.toSet());
    }

    protected boolean isDistinct(IQTree tree, ImmutableList<IQTree> children) {
        if (children.stream().noneMatch(child -> mayChildCauseParentBeingNonDistinct(tree, child)))
            return true;


        ImmutableSet<ImmutableSet<Variable>> constraints = tree.inferUniqueConstraints();
        if (constraints.isEmpty())
            return false;

        VariableNullability variableNullability = tree.getVariableNullability();
        return constraints.stream()
                .anyMatch(c -> c.stream().noneMatch(variableNullability::isPossiblyNullable));
    }

    /**
     * Must not return false negative
     */
    private static boolean mayChildCauseParentBeingNonDistinct(IQTree parentTree, IQTree child) {
        if (child.isDistinct())
            return false;

        // Useful when the unique constraint from the child is "nullable" at the child level
        // but not at the parent level (thanks to implicit and explicit conditions)
        VariableNullability parentVariableNullability = parentTree.getVariableNullability();
        return child.inferUniqueConstraints().stream()
                .noneMatch(c -> c.stream().noneMatch(parentVariableNullability::isPossiblyNullable));
    }
}
