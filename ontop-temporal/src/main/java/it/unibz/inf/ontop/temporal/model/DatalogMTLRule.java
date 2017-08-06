package it.unibz.inf.ontop.temporal.model;



public interface DatalogMTLRule {

    TemporalExpression getHead();

    TemporalExpression getBody();

    String render();
}
