package it.unibz.inf.ontop.temporal.model;



public interface DatalogMTLRule {

    DatalogMTLExpression getHead();

    DatalogMTLExpression getBody();

    String render();
}
