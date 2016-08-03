package it.unibz.inf.ontop.executor.substitution;


import it.unibz.inf.ontop.executor.SimpleNodeCentricInternalExecutor;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.SubstitutionPropagationProposal;

public interface SubstitutionPropagationExecutor<N extends QueryNode> extends
        SimpleNodeCentricInternalExecutor<N, SubstitutionPropagationProposal<N>> {
}
