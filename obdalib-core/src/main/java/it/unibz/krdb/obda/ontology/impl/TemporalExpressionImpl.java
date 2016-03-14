package it.unibz.krdb.obda.ontology.impl;

import it.unibz.krdb.obda.ontology.TemporalExpression;

public class TemporalExpressionImpl extends ClassImpl implements TemporalExpression  {
    String tempOp;
    String duration;

  public TemporalExpressionImpl(String name, String tempOp, String duration) {
        super(name);
        this.tempOp = tempOp;
        this.duration = duration;
    }

    @Override
    public String getTempOp() {
        return null;
    }

    @Override
    public String getDuration() {
        return null;
    }

}
