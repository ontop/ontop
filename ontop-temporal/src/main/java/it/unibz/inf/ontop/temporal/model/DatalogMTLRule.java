package it.unibz.inf.ontop.temporal.model;



public interface DatalogMTLRule {

    AtomicExpression getHead();

    <T extends DatalogMTLExpression> T getBody();

    String render();
}
