package it.unibz.inf.ontop.iq.proposal;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.iq.node.ExplicitVariableProjectionNode;

public interface ProjectionShrinkingProposal extends SimpleNodeCentricOptimizationProposal<ExplicitVariableProjectionNode> {

    ImmutableSet<Variable> getRetainedVariables();
}
