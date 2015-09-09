package org.semanticweb.ontop.pivotalrepr;

import com.google.common.base.Optional;

/**
 * TODO: explain
 */
public interface NodeTransformationProposal {

    NodeTransformationProposedState getState();

    Optional<QueryNode> getOptionalNewNode();
}
