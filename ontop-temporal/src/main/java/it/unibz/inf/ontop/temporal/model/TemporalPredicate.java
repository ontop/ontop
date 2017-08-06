package it.unibz.inf.ontop.temporal.model;


import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;

public interface TemporalPredicate extends Predicate {

    TemporalModifier getTemporalModifier();

    Predicate getInnerPredicate();
}
