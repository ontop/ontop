package org.semanticweb.ontop.pivotalrepr;

import com.google.common.base.Optional;
import org.semanticweb.ontop.model.*;

/**
 * Head node an IntermediateQuery
 *
 * TODO: further explain
 *
 */
public interface ConstructionNode extends QueryNode {

    /**
     * Data atom containing the projected variables
     */
    PureDataAtom getProjectionAtom();


    /**
     * Renamings between PROJECTED variables
     */
    IndempotentVar2VarSubstitution getRenamings();

    /**
     * Projected variables --> ground terms
     */
    ImmutableSubstitution<GroundTerm> getGroundTermBindings();

    /**
     * Projected variables --> transformed variable
     */
    ImmutableSubstitution<ImmutableFunctionalTerm> getFunctionalBindings();


    /**
     * TODO: explain
     */
    Optional<ImmutableQueryModifiers> getOptionalModifiers();

    ConstructionNode clone();
}
