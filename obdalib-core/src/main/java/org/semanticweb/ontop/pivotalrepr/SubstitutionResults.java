package org.semanticweb.ontop.pivotalrepr;

import com.google.common.base.Optional;
import org.semanticweb.ontop.model.ImmutableSubstitution;
import org.semanticweb.ontop.model.ImmutableTerm;
import org.semanticweb.ontop.model.VariableOrGroundTerm;

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
    Optional<? extends ImmutableSubstitution<? extends VariableOrGroundTerm>> getSubstitutionToPropagate();

}
