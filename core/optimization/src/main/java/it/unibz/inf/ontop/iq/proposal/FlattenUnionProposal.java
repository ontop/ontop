package it.unibz.inf.ontop.iq.proposal;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.UnionNode;

public interface FlattenUnionProposal extends SimpleNodeCentricOptimizationProposal<UnionNode>{

    ImmutableSet<QueryNode> getSubqueryRoots();
}
