package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.model.term.Variable;

public abstract class QueryNodeImpl implements QueryNode {

    protected final IntermediateQueryFactory iqFactory;

    QueryNodeImpl(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    /**
     * Default value, to be overridden
     */
    @Override
    public boolean wouldKeepDescendingGroundTermInFilterAbove(Variable variable, boolean isConstant) {
        return false;
    }

    @Override
    public FunctionalDependencies inferFunctionalDependencies(ImmutableList<IQTree> children, ImmutableSet<ImmutableSet<Variable>> uniqueConstraints, ImmutableSet<Variable> variables) {
        var fromUcs = FunctionalDependencies.fromUniqueConstraints(uniqueConstraints, variables);
        if(this instanceof UnaryOperatorNode) {
            return fromUcs.concat(((UnaryOperatorNode)this).inferFunctionalDependencies(children.get(0)));
        } else if(this instanceof BinaryOrderedOperatorNode) {
            return fromUcs.concat(((BinaryOrderedOperatorNode)this).inferFunctionalDependencies(children.get(0), children.get(1)));
        } else if(this instanceof NaryOperatorNode) {
            return fromUcs.concat(((NaryOperatorNode)this).inferFunctionalDependencies(children));
        }
        return fromUcs;
    }
}
