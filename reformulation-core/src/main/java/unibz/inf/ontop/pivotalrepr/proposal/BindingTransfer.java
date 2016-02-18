package unibz.inf.ontop.pivotalrepr.proposal;

import com.google.common.collect.ImmutableList;
import unibz.inf.ontop.model.ImmutableSubstitution;
import unibz.inf.ontop.model.ImmutableTerm;
import unibz.inf.ontop.pivotalrepr.ConstructionNode;

/**
 * TODO: explain
 */
public interface BindingTransfer {

    ImmutableSubstitution<ImmutableTerm> getTransferredBindings();

    ImmutableList<ConstructionNode> getSourceNodes();
    ConstructionNode getTargetNode();
}
