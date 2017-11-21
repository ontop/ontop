package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.UnaryIQ;
import it.unibz.inf.ontop.iq.node.ExplicitVariableProjectionNode;
import it.unibz.inf.ontop.iq.node.UnaryOperatorNode;
import it.unibz.inf.ontop.model.term.Variable;

public class UnaryIQImpl extends AbstractCompositeIQ implements UnaryIQ {

    private final UnaryOperatorNode rootNode;

    @AssistedInject
    private UnaryIQImpl(@Assisted UnaryOperatorNode rootNode, @Assisted IQ child) {
        super(rootNode, ImmutableList.of(child));
        this.rootNode = rootNode;
    }

    @Override
    public IQ liftBinding() {
        IQ initialChild = getChild();
        IQ liftedChild = initialChild.liftBinding();
        return initialChild.equals(liftedChild)
                ? this
                :rootNode.liftBinding(liftedChild);
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
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
