package it.unibz.inf.ontop.temporal.model;

import java.util.List;

public interface NAryDatalogMTLExpression extends DatalogMTLExpression {

    List<? extends DatalogMTLExpression> getOperands();

}
