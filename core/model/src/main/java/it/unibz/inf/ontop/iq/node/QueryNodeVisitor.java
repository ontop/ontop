package it.unibz.inf.ontop.iq.node;

/**
 * Visits QueryNodes without having effect on them and the intermediate query.
 *
 * If you want to make optimization proposals to the nodes/query, use an Optimizer instead.
 *
 */
public interface QueryNodeVisitor {

    void visit(ConstructionNode constructionNode);

    void visit(AggregationNode aggregationNode);

    void visit(UnionNode unionNode);

    void visit(InnerJoinNode innerJoinNode);

    void visit(LeftJoinNode leftJoinNode);

    void visit(FilterNode filterNode);

    void visit(IntensionalDataNode intensionalDataNode);

    void visit(ExtensionalDataNode extensionalDataNode);

    void visit(EmptyNode emptyNode);

    void visit(TrueNode trueNode);

    void visit(ValuesNode valuesNode);

    void visit(DistinctNode distinctNode);

    void visit(SliceNode sliceNode);

    void visit(OrderByNode orderByNode);
}
