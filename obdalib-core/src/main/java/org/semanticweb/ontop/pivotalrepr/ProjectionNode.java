package org.semanticweb.ontop.pivotalrepr;

import com.google.common.base.Optional;
import org.semanticweb.ontop.model.ImmutableSubstitution;

/**
 * Head node an IntermediateQuery
 *
 * TODO: further explain
 *
 */
public interface ProjectionNode extends QueryNode {

    /**
     * TODO: explain
     */
    PureDataAtom getHeadAtom();

    /**
     * TODO: explain
     */
    ImmutableSubstitution getSubstitution();

    /**
     * TODO: explain
     */
    Optional<ImmutableQueryModifiers> getOptionalModifiers();

    ProjectionNode clone();
}
