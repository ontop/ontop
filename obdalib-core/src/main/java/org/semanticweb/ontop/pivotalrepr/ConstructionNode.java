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
     * TODO: find a better name and a better explanation.
     *
     * Equivalent to the regular substitution.
     * All projected variables that are defined from other variables
     * defined in the ancestor nodes have a explicit binding to them
     * in this substitution.
     *
     * In the regular substitution, they could just be bound to another
     * projected variable (INDIRECT).
     *
     */
    ImmutableSubstitution<ImmutableTerm> getDirectBindingSubstitution();
}
