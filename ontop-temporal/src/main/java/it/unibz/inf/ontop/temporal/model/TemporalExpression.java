package it.unibz.inf.ontop.temporal.model;

import it.unibz.inf.ontop.temporal.model.tree.TemporalTreeNode;

public interface TemporalExpression {

    String render();

    Iterable<TemporalExpression> getChildNodes();

}
