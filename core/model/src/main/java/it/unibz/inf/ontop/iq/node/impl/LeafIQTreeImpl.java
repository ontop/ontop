package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.VariableGenerator;

public abstract class LeafIQTreeImpl extends QueryNodeImpl implements LeafIQTree {

    private static final ImmutableList<IQTree> EMPTY_LIST = ImmutableList.of();

    public LeafIQTreeImpl() {
        super();
    }

    @Override
    public LeafIQTree getRootNode() {
        return this;
    }

    @Override
    public ImmutableList<IQTree> getChildren() {
        return EMPTY_LIST;
    }

    @Override
    public IQTree liftBinding(VariableGenerator variableGenerator) {
        return this;
    }

    @Override
    public boolean isConstructed(Variable variable) {
        return false;
    }

    @Override
    public boolean isEquivalentTo(IQTree tree) {
        return (tree instanceof LeafIQTree)
                && isEquivalentTo((QueryNode) tree);
    }

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint) {
        return this;
    }

    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable) {
        return this;
    }
}
