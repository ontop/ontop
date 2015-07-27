package org.semanticweb.ontop.pivotalrepr.proposal;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.ImmutableSubstitution;
import org.semanticweb.ontop.model.ImmutableTerm;
import org.semanticweb.ontop.pivotalrepr.ConstructionNode;

/**
 * TODO: explain
 */
public interface BindingTransfer {

    ImmutableSubstitution<ImmutableTerm> getTransferredBindings();

    ImmutableList<ConstructionNode> getSourceNodes();
    ConstructionNode getTargetNode();
}
