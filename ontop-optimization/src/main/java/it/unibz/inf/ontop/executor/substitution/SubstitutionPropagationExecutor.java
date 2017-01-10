package it.unibz.inf.ontop.executor.substitution;


import it.unibz.inf.ontop.executor.SimpleNodeCentricExecutor;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.SubstitutionPropagationProposal;

public interface SubstitutionPropagationExecutor<N extends QueryNode> extends
        SimpleNodeCentricExecutor<N, SubstitutionPropagationProposal<N>> {
}
