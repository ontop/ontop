package it.unibz.inf.ontop.pivotalrepr.proposal;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.pivotalrepr.ExplicitVariableProjectionNode;

public interface ProjectionShrinkingProposal extends SimpleNodeCentricOptimizationProposal<ExplicitVariableProjectionNode> {

    ImmutableSet<Variable> getRetainedVariables();
}
