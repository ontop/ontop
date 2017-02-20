package it.unibz.inf.ontop.temporal.model;

import it.unibz.inf.ontop.model.Predicate;

/**
 * Created by xiao on 20/02/2017.
 */
public interface TemporalPredicate extends Predicate {

    TemporalModifer getTemporalOperator();

    Predicate getInnerPredicate();
}
