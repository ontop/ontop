package it.unibz.inf.ontop.pivotalrepr.proposal;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.pivotalrepr.ConstructionNode;

/**
 * TODO: explain
 */
public interface BindingTransfer {

    ImmutableSubstitution<ImmutableTerm> getTransferredBindings();

    ImmutableList<ConstructionNode> getSourceNodes();
    ConstructionNode getTargetNode();
}
