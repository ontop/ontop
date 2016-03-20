package it.unibz.krdb.obda.ontology.impl;

import it.unibz.krdb.obda.ontology.TemporalExpression;

public class TemporalExpressionImpl extends ClassImpl implements TemporalExpression  {
    String tempOp;
    int duration;

  public TemporalExpressionImpl(String name, String tempOp, int duration) {
        super(name);
        this.tempOp = tempOp;
        this.duration = duration;
    }

    @Override
    public String getTempOp() {

        return this.tempOp;
    }

    @Override
    public int getDuration() {

        return this.duration;
    }

}
