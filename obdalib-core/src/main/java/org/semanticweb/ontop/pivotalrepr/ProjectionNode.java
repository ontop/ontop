package org.semanticweb.ontop.pivotalrepr;

import com.google.common.base.Optional;
import org.semanticweb.ontop.model.ImmutableSubstitution;
import org.semanticweb.ontop.model.impl.VariableImpl;

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
    ImmutableSubstitution getAliasDefinition();

    /**
     * TODO: explain
     */
    Optional<ImmutableQueryModifiers> getOptionalModifiers();

    ProjectionNode clone();

    /**
     * Returns true if the variable is defined by the substitution.
     */
    boolean isAlias(VariableImpl variable);
}
