package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.node.NaryOperatorNode;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.VariableOrGroundTermSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.stream.Stream;

public class NaryIQTreeImpl extends AbstractCompositeIQTree<NaryOperatorNode> implements NaryIQTree {

    private final boolean isLifted;

    @AssistedInject
    private NaryIQTreeImpl(@Assisted NaryOperatorNode rootNode, @Assisted ImmutableList<IQTree> children,
                           @Assisted boolean isLifted) {
        super(rootNode, children);
        this.isLifted = isLifted;
        if (children.size() < 2)
            throw new IllegalArgumentException("At least two children are required for a n-ary node");
    }

    @AssistedInject
    private NaryIQTreeImpl(@Assisted NaryOperatorNode rootNode, @Assisted ImmutableList<IQTree> children) {
        this(rootNode, children, false);
    }

    @Override
    public IQTree liftBinding(VariableGenerator variableGenerator) {
        return isLifted
                ? this
                : getRootNode().liftBinding(getChildren(), variableGenerator);
    }

    @Override
    public IQTree applyDescendingSubstitution(
            VariableOrGroundTermSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
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
}
