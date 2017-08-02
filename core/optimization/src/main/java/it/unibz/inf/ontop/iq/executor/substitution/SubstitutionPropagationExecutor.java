package it.unibz.inf.ontop.iq.executor.substitution;


import it.unibz.inf.ontop.iq.executor.SimpleNodeCentricExecutor;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.proposal.SubstitutionPropagationProposal;

public interface SubstitutionPropagationExecutor<N extends QueryNode> extends
        SimpleNodeCentricExecutor<N, SubstitutionPropagationProposal<N>> {
}
