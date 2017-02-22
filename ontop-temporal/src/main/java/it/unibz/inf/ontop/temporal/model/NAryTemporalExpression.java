package it.unibz.inf.ontop.temporal.model;

import java.util.List;

public interface NAryTemporalExpression extends TemporalExpression {

    List<TemporalExpression> getOperands();

}
