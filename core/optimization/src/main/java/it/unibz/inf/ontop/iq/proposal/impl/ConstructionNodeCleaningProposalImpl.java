package it.unibz.inf.ontop.iq.proposal.impl;

import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ImmutableQueryModifiers;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.proposal.ConstructionNodeCleaningProposal;

import java.util.Optional;

public class ConstructionNodeCleaningProposalImpl implements ConstructionNodeCleaningProposal {

    private final ConstructionNode focusNode;
    private final Optional<ImmutableQueryModifiers> combinedModifiers;
    private final QueryNode childSubtreeRoot;
    private final boolean deleteConstructionNodeChain;


    public ConstructionNodeCleaningProposalImpl(ConstructionNode focusNode,
                                                Optional<ImmutableQueryModifiers> combinedModifiers,
                                                QueryNode childSubtreeRoot,
                                                boolean deleteConstructionNodeChain) {
        this.focusNode = focusNode;
        this.combinedModifiers = combinedModifiers;
        this.childSubtreeRoot = childSubtreeRoot;
        this.deleteConstructionNodeChain = deleteConstructionNodeChain;
    }

    @Override
    public Optional<ImmutableQueryModifiers> getCombinedModifiers() {
        return combinedModifiers;
    }

    @Override
    public boolean deleteConstructionNodeChain() {
        return deleteConstructionNodeChain;
    }

    @Override
    public ConstructionNode getFocusNode() {
        return focusNode;
    }

    @Override
    public QueryNode getChildSubtreeRoot() {
        return childSubtreeRoot;
    }
}
