package org.semanticweb.ontop.pivotalrepr.proposal.impl;

import com.google.common.base.Optional;
import org.semanticweb.ontop.model.ImmutableSubstitution;
import org.semanticweb.ontop.model.ImmutableTerm;
import org.semanticweb.ontop.model.VariableOrGroundTerm;
import org.semanticweb.ontop.pivotalrepr.ConstructionNode;
import org.semanticweb.ontop.pivotalrepr.impl.ConstructionNodeTools;
import org.semanticweb.ontop.pivotalrepr.proposal.ConstructionNodeUpdate;

import static org.semanticweb.ontop.pivotalrepr.impl.ConstructionNodeTools.newNodeWithAdditionalBindings;
import static org.semanticweb.ontop.pivotalrepr.impl.ConstructionNodeTools.newNodeWithLessBindings;

/**
 * Quasi-immutable (depends on ConstructionNode)
 */
public class ConstructionNodeUpdateImpl implements ConstructionNodeUpdate {

    private final ConstructionNode formerNode;
    private final Optional<ConstructionNode> optionalNewNode;
    private final Optional<ImmutableSubstitution<VariableOrGroundTerm>> optionalSubstitutionToPropagate;

    public ConstructionNodeUpdateImpl(ConstructionNode formerConstructionNode) {
        this.formerNode = formerConstructionNode;
        this.optionalNewNode = Optional.absent();
        this.optionalSubstitutionToPropagate = Optional.absent();
    }

    public ConstructionNodeUpdateImpl(ConstructionNode formerConstructionNode,
                                    ConstructionNode newConstructionNode) {
        this.formerNode = formerConstructionNode;
        this.optionalNewNode = Optional.of(newConstructionNode);
        this.optionalSubstitutionToPropagate = Optional.absent();
    }

    public ConstructionNodeUpdateImpl(ConstructionNode formerConstructionNode,
                                    ConstructionNode newConstructionNode,
                                    ImmutableSubstitution<VariableOrGroundTerm> substitutionToPropagate) {
        this.formerNode = formerConstructionNode;
        this.optionalNewNode = Optional.of(newConstructionNode);
        this.optionalSubstitutionToPropagate = Optional.of(substitutionToPropagate);
    }


    @Override
    public ConstructionNode getFormerNode() {
        return formerNode;
    }

    @Override
    public Optional<ConstructionNode> getOptionalNewNode() {
        return optionalNewNode;
    }

    @Override
    public ConstructionNode getMostRecentConstructionNode() {
        if (optionalNewNode.isPresent())
            return optionalNewNode.get();
        return formerNode;
    }

    @Override
    public ConstructionNodeUpdate removeSomeBindings(ImmutableSubstitution<ImmutableTerm> bindingsToRemove) {
        if (optionalSubstitutionToPropagate.isPresent()) {
            throw new RuntimeException("Removing bindings multiple times for the same node is not supported");
        }

        ConstructionNodeTools.BindingRemoval bindingRemoval = newNodeWithLessBindings(getMostRecentConstructionNode(), bindingsToRemove);
        ConstructionNode newConstructionNode = bindingRemoval.getNewConstructionNode();

        Optional<ImmutableSubstitution<VariableOrGroundTerm>> newOptionalSubstitutionToPropagate =
                bindingRemoval.getOptionalSubstitutionToPropagateToAncestors();

        if (newOptionalSubstitutionToPropagate.isPresent()) {
            return new ConstructionNodeUpdateImpl(formerNode, newConstructionNode,
                    newOptionalSubstitutionToPropagate.get());
        }
        else {
            return new ConstructionNodeUpdateImpl(formerNode, newConstructionNode);
        }
    }

    @Override
    public ConstructionNodeUpdate addBindings(ImmutableSubstitution<ImmutableTerm> substitutionToLift) {
        if (optionalSubstitutionToPropagate.isPresent()) {
            throw new RuntimeException("Cannot add bindings after removing some.");
        }

        ConstructionNode newNode = newNodeWithAdditionalBindings(getMostRecentConstructionNode(), substitutionToLift);
        return new ConstructionNodeUpdateImpl(formerNode, newNode);
    }

    @Override
    public Optional<ImmutableSubstitution<VariableOrGroundTerm>> getOptionalSubstitutionToPropagate() {
        return optionalSubstitutionToPropagate;
    }
}
