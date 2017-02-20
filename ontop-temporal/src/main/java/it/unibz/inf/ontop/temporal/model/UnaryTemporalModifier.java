package it.unibz.inf.ontop.temporal.model;

public interface UnaryTemporalModifier extends TemporalModifier {

    UnaryTemporalOperator getOperator();

    Range getRange();

}
