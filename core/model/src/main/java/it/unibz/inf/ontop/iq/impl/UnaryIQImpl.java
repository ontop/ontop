package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.UnaryIQ;
import it.unibz.inf.ontop.iq.node.ExplicitVariableProjectionNode;
import it.unibz.inf.ontop.iq.node.UnaryOperatorNode;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

public class UnaryIQImpl extends AbstractCompositeIQ<UnaryOperatorNode> implements UnaryIQ {

    @AssistedInject
    private UnaryIQImpl(@Assisted UnaryOperatorNode rootNode, @Assisted IQ child) {
        super(rootNode, ImmutableList.of(child));
    }

    @Override
    public IQ liftBinding(VariableGenerator variableGenerator) {
        IQ initialChild = getChild();
        IQ liftedChild = initialChild.liftBinding(variableGenerator);
        return initialChild.equals(liftedChild)
                ? this
                :getRootNode().liftBinding(liftedChild);
    }

    @Override
    public IQ applyDescendingSubstitution(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                          Optional<ImmutableExpression> constraint) {
        throw new RuntimeException("TODO: implement it");
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
    public IQ getChild() {
        return getChildren().get(0);
    }
}
