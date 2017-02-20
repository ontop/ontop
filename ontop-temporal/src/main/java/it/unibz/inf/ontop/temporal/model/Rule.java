package it.unibz.inf.ontop.temporal.model;

import java.util.List;

/**
 * Created by elem on 20/02/2017.
 */
public interface Rule {

    TemporalPredicate getHead();

    List<TemporalPredicate> getBody();

}
