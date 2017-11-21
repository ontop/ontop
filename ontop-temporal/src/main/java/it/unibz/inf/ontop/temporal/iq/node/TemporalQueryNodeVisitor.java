package it.unibz.inf.ontop.temporal.iq.node;

import it.unibz.inf.ontop.iq.node.QueryNodeVisitor;

public interface TemporalQueryNodeVisitor extends QueryNodeVisitor{

    void visit(TemporalJoinNode temporalJoinNode);

    void visit(BoxMinusNode boxMinusNode);

    void visit(BoxPlusNode boxPlusNode);

    void visit(DiamondMinusNode diamondMinusNode);

    void visit(DiamondPlusNode diamondPlusNode);

    void visit(SinceNode sinceNode);

    void visit(UntilNode untilNode);

    void visit(TemporalCoalesceNode temporalCoalesceNode);
}
