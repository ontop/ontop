package it.unibz.inf.ontop.temporal.model;



public interface DatalogMTLRule {

    AtomicExpression getHead();

    DatalogMTLExpression getBody();

    String render();
}
