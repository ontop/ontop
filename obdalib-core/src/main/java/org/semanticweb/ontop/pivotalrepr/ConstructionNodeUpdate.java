package org.semanticweb.ontop.pivotalrepr;

import fj.data.Option;
import org.semanticweb.ontop.model.ImmutableSubstitution;
import org.semanticweb.ontop.model.ImmutableTerm;
import org.semanticweb.ontop.model.VariableOrGroundTerm;

/**
 * TODO: explain
 *
 * Quasi-immutable (depends on ConstructionNode).
 */
public interface ConstructionNodeUpdate {

    ConstructionNode getFormerNode();

    Option<ConstructionNode> getOptionalNewNode();

    ConstructionNode getMostRecentConstructionNode();

    ConstructionNodeUpdate removeSomeBindings(ImmutableSubstitution<ImmutableTerm> bindingsToRemove);

    ConstructionNodeUpdate addBindings(ImmutableSubstitution<ImmutableTerm> substitutionToLift);

    Option<ImmutableSubstitution<VariableOrGroundTerm>> getOptionalSubstitutionToPropagate();
}
