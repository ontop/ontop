package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.ExplicitVariableProjectionNode;
import it.unibz.inf.ontop.iq.node.UnaryOperatorNode;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

public class UnaryIQTreeImpl extends AbstractCompositeIQTree<UnaryOperatorNode> implements UnaryIQTree {

    private final boolean isLifted;

    @AssistedInject
    private UnaryIQTreeImpl(@Assisted UnaryOperatorNode rootNode, @Assisted IQTree child,
                            @Assisted boolean isLifted) {
        super(rootNode, ImmutableList.of(child));
        this.isLifted = isLifted;
    }

    @AssistedInject
    private UnaryIQTreeImpl(@Assisted UnaryOperatorNode rootNode, @Assisted IQTree child) {
        this(rootNode, child, false);
    }

    @Override
    public IQTree liftBinding(VariableGenerator variableGenerator) {
        if (isLifted)
            return this;
        else {
            IQTree initialChild = getChild();
            IQTree liftedChild = initialChild.liftBinding(variableGenerator);
            return initialChild.equals(liftedChild)
                    ? this
                    : getRootNode().liftBinding(liftedChild);
        }
    }

    @Override
    public IQTree applyDescendingSubstitution(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                              Optional<ImmutableExpression> constraint) {
        return getRootNode().applyDescendingSubstitution(descendingSubstitution, constraint, getChild());
    }

    @Override
    public boolean isDeclaredAsEmpty() {
        return false;
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        UnaryOperatorNode rootNode = getRootNode();
        if (rootNode instanceof ExplicitVariableProjectionNode)
            return ((ExplicitVariableProjectionNode) rootNode).getVariables();
        else
            return getChild().getVariables();
    }

    @Override
    public IQTree getChild() {
        return getChildren().get(0);
    }
}
