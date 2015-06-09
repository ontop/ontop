package org.semanticweb.ontop.pivotalrepr;

import com.google.common.base.Optional;

/**
 * Head node an IntermediateQuery
 */
public interface ProjectionNode extends QueryNode {

    DataAtom getHeadAtom();

    Optional<ImmutableQueryModifiers> getOptionalModifiers();
}
