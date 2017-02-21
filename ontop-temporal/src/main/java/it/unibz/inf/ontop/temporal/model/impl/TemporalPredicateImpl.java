package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.model.Predicate;
import it.unibz.inf.ontop.temporal.model.TemporalModifier;
import it.unibz.inf.ontop.temporal.model.TemporalPredicate;

public class TemporalPredicateImpl implements TemporalPredicate{

    private TemporalModifier temporalModifier;

    private Predicate innerPredicate;

    public TemporalPredicateImpl(TemporalModifier tempModifier, Predicate innerPredicate){
        this.temporalModifier = tempModifier;
        this.innerPredicate = innerPredicate;
    }

    @Override
    public TemporalModifier getTemporalModifier() {
        return this.temporalModifier;
    }

    @Override
    public Predicate getInnerPredicate() {
        return this.innerPredicate;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public int getArity() {
        return 0;
    }

    @Override
    public COL_TYPE getType(int column) {
        return null;
    }

    @Override
    public boolean isClass() {
        return false;
    }

    @Override
    public boolean isObjectProperty() {
        return false;
    }

    @Override
    public boolean isAnnotationProperty() {
        return false;
    }

    @Override
    public boolean isDataProperty() {
        return false;
    }

    @Override
    public boolean isSameAsProperty() {
        return false;
    }

    @Override
    public boolean isCanonicalIRIProperty() {
        return false;
    }

    @Override
    public boolean isTriplePredicate() {
        return false;
    }
}
