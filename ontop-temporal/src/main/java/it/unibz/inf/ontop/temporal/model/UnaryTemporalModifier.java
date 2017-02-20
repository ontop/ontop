package it.unibz.inf.ontop.temporal.model;

public interface UnaryTemporalModifier extends TemporalModifer {

    UnaryTemporalOperator getOperator();

    Range getRange();

}
