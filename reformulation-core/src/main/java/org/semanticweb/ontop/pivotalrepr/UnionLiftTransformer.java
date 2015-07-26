package org.semanticweb.ontop.pivotalrepr;


public interface UnionLiftTransformer {

    UnionNode getUnionNode();

    QueryNode getTargetQueryNode();

    IntermediateQuery apply(IntermediateQuery inputQuery);
}
