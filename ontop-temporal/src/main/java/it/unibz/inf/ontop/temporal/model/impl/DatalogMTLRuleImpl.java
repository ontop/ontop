package it.unibz.inf.ontop.temporal.model.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.temporal.model.AtomicExpression;
import it.unibz.inf.ontop.temporal.model.DatalogMTLExpression;
import it.unibz.inf.ontop.temporal.model.DatalogMTLRule;
import it.unibz.inf.ontop.temporal.model.StaticAtomicExpression;

public class DatalogMTLRuleImpl implements DatalogMTLRule {
    DatalogMTLRuleImpl(AtomicExpression head, DatalogMTLExpression body) {
        this.head = head;
        this.body = body;
    }

    private final AtomicExpression head;
    private final DatalogMTLExpression body;

    @Override
    public AtomicExpression getHead() {
        return head;
    }

    @Override
    public DatalogMTLExpression getBody() {
        return body;
    }

    @Override
    public String render() {
        if(isStatic()) {
            return "[static]\n" + toString();
        }
        return toString();
    }

    @Override
    public boolean isStatic(){
        return head instanceof StaticAtomicExpression;
    }

    @Override
    public ImmutableList<AtomicExpression> getLeaves() {
        return null;
    }

    @Override
    public String toString(){
        return String.format("%s :- \n{\n\t%s\n}", head.toString(), body.toString());
    }
}
