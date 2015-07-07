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
    DataAtom getProjectionAtom();

    /**
     * Projected variables --> transformed variable
     */
    ImmutableSubstitution<ImmutableTerm> getSubstitution();

    /**
     * TODO: explain
     */
    Optional<ImmutableQueryModifiers> getOptionalModifiers();

    @Override
    ConstructionNode clone();

    @Override
    ConstructionNode acceptNodeTransformer(QueryNodeTransformer transformer)
            throws QueryNodeTransformationException;

    /**
     * TODO: explain
     */
    ConstructionNode newNodeWithAdditionalBindings(ImmutableSubstitution<ImmutableTerm> additionalBindings);

    /**
     * TODO: explain
     */
    ConstructionNode newNodeWithLessBindings(ImmutableSubstitution<ImmutableTerm> bindingsToRemove);
}
