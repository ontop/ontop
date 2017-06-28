package it.unibz.inf.ontop.temporal.model;

import java.util.List;

public interface NAryStaticExpression extends StaticExpression{

    List<StaticExpression> getOperands();
}
