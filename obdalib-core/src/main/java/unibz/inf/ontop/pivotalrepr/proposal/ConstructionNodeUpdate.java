package unibz.inf.ontop.pivotalrepr.proposal;

import java.util.Optional;

import unibz.inf.ontop.model.VariableOrGroundTerm;
import unibz.inf.ontop.model.ImmutableSubstitution;
import unibz.inf.ontop.model.ImmutableTerm;
import unibz.inf.ontop.pivotalrepr.ConstructionNode;

/**
 * TODO: explain
 *
 * Immutable (depends on ConstructionNode).
 */
public interface ConstructionNodeUpdate {

    ConstructionNode getFormerNode();

    Optional<ConstructionNode> getOptionalNewNode();

    ConstructionNode getMostRecentConstructionNode();

    ConstructionNodeUpdate removeSomeBindings(ImmutableSubstitution<ImmutableTerm> bindingsToRemove);

    ConstructionNodeUpdate addBindings(ImmutableSubstitution<ImmutableTerm> substitutionToLift);

    Optional<ImmutableSubstitution<VariableOrGroundTerm>> getOptionalSubstitutionToPropagate();

    boolean hasNewBindings();

    ImmutableSubstitution<ImmutableTerm> getNewBindings();
}
