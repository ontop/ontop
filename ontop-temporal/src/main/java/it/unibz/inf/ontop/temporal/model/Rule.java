package it.unibz.inf.ontop.temporal.model;

import it.unibz.inf.ontop.model.Predicate;

import java.util.List;

public interface Rule {

    Predicate getHead();

    List<Predicate> getBody();

}
