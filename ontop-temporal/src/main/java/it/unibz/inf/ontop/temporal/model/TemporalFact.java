package it.unibz.inf.ontop.temporal.model;

public interface TemporalFact {

    GroundTemporalAtomicExpression getAtom();

    TemporalInterval getInterval();


}
