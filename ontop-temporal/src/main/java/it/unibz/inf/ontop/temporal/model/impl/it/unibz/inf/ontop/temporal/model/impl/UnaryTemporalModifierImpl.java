package it.unibz.inf.ontop.temporal.model.impl.it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.temporal.model.Range;
import it.unibz.inf.ontop.temporal.model.UnaryTemporalModifier;
import it.unibz.inf.ontop.temporal.model.UnaryTemporalOperator;

public class UnaryTemporalModifierImpl implements UnaryTemporalModifier {

    UnaryTemporalOperator operator;
    Range range;

    public UnaryTemporalModifierImpl(UnaryTemporalOperator operator, Range range){
        this.operator = operator;
        this.range = range;
    }

    @Override
    public UnaryTemporalOperator getOperator() {
        return this.operator;
    }

    @Override
    public Range getRange() {
        return this.range;
    }
}
