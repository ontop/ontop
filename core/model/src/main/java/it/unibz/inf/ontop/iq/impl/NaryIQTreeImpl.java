package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.node.NaryOperatorNode;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.stream.Stream;

public class NaryIQTreeImpl extends AbstractCompositeIQTree<NaryOperatorNode> implements NaryIQTree {

    // Lazy
    @Nullable
    private ImmutableSet<Variable> nullableVariables;

    @AssistedInject
    private NaryIQTreeImpl(@Assisted NaryOperatorNode rootNode, @Assisted ImmutableList<IQTree> children,
                           @Assisted IQProperties iqProperties) {
        super(rootNode, children, iqProperties);
        if (children.size() < 2)
            throw new IllegalArgumentException("At least two children are required for a n-ary node");
    }

    @AssistedInject
    private NaryIQTreeImpl(@Assisted NaryOperatorNode rootNode, @Assisted ImmutableList<IQTree> children) {
        this(rootNode, children, new IQPropertiesImpl());
    }

    @Override
    public IQTree liftBinding(VariableGenerator variableGenerator) {
        return getProperties().isLifted()
                ? this
                : getRootNode().liftBinding(getChildren(), variableGenerator, getProperties());
    }

    @Override
    public IQTree applyDescendingSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
            Optional<ImmutableExpression> constraint) {

        ImmutableSet<Variable> projectedVariables = getVariables();
        if (descendingSubstitution.getDomain().stream()
                .anyMatch(projectedVariables::contains)) {
            return getRootNode().applyDescendingSubstitution(descendingSubstitution, constraint, getChildren());
        }
        else
            return this;
    }

    @Override
    public boolean isDeclaredAsEmpty() {
        return false;
    }

    @Override
    public ImmutableSet<Variable> getNullableVariables() {
        if (nullableVariables == null)
            nullableVariables = getRootNode().getNullableVariables(getChildren());
        return nullableVariables;
    }

    @Override
    public boolean containsNullableVariable(Variable variable) {
        return getNullableVariables().contains(variable);
    }
}
