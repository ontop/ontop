package it.unibz.inf.ontop.temporal.model;

public interface DatalogMTLExpression {

    public String render();

    Iterable<? extends DatalogMTLExpression> getChildNodes();

}
