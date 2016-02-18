package it.unibz.inf.ontop.pivotalrepr.proposal.impl;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.pivotalrepr.proposal.BindingTransfer;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.pivotalrepr.ConstructionNode;

/**
 * Immutable
 */
public class BindingTransferImpl implements BindingTransfer {

    private final ImmutableSubstitution<ImmutableTerm> bindings;
    private final ImmutableList<ConstructionNode> sourceNodes;
    private final ConstructionNode targetNode;

    public BindingTransferImpl(ImmutableSubstitution<ImmutableTerm> bindings,
                               ImmutableList<ConstructionNode> sourceNodes,
                               ConstructionNode targetNode) {
        this.bindings = bindings;
        this.sourceNodes = sourceNodes;
        this.targetNode = targetNode;
    }

    @Override
    public ImmutableSubstitution<ImmutableTerm> getTransferredBindings() {
        return bindings;
    }

    @Override
    public ImmutableList<ConstructionNode> getSourceNodes() {
        return sourceNodes;
    }

    @Override
    public ConstructionNode getTargetNode() {
        return targetNode;
    }
}
