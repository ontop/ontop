package it.unibz.inf.ontop.temporal.model;


import com.google.common.collect.ImmutableList;

public interface DatalogMTLRule {

    AtomicExpression getHead();

    <T extends DatalogMTLExpression> T getBody();

    String render();

    ImmutableList<AtomicExpression> getLeaves();
}
