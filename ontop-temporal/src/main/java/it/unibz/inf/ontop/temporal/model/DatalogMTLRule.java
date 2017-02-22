package it.unibz.inf.ontop.temporal.model;

import it.unibz.inf.ontop.model.Predicate;

import java.util.List;

public interface DatalogMTLRule {

    TemporalExpression getHead();

    TemporalExpression getBody();

    String render();
}
