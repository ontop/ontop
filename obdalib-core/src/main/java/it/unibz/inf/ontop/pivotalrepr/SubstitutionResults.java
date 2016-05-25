package it.unibz.inf.ontop.pivotalrepr;

import java.util.Optional;

import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.VariableOrGroundTerm;
import it.unibz.inf.ontop.model.ImmutableSubstitution;

/**
 * Object returned after applying a substitution to a QueryNode
 */
public interface SubstitutionResults<T extends QueryNode> {

    /**
     * When is absent, it means that node is not needed anymore.
     */
    Optional<T> getOptionalNewNode();

    /**
     * If absent, stop propagating to the parent/children (depending on the propagation direction).
     */
    Optional<? extends ImmutableSubstitution<? extends ImmutableTerm>> getSubstitutionToPropagate();

    boolean isNodeEmpty();
}
