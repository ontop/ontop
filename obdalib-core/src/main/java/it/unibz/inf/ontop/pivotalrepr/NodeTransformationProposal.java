package it.unibz.inf.ontop.pivotalrepr;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.Variable;

import java.util.Optional;

/**
 * TODO: explain
 */
public interface NodeTransformationProposal {

    NodeTransformationProposedState getState();

    Optional<QueryNode> getOptionalNewNode();

    ImmutableSet<Variable> getNullVariables();
}
