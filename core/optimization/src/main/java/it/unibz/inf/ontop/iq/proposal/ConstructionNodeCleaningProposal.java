package it.unibz.inf.ontop.iq.proposal;

import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ImmutableQueryModifiers;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

import java.util.Optional;

public interface ConstructionNodeCleaningProposal extends SimpleNodeCentricOptimizationProposal<ConstructionNode>{

    Optional<ImmutableQueryModifiers> getCombinedModifiers();

    boolean deleteConstructionNodeChain();

    QueryNode getChildSubtreeRoot();
}
