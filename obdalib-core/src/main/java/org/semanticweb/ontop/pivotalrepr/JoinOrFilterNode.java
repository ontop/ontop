package org.semanticweb.ontop.pivotalrepr;

import com.google.common.base.Optional;
import org.semanticweb.ontop.model.ImmutableBooleanExpression;

/**
 * TODO: explain
 */
public interface JoinOrFilterNode extends QueryNode {

    public Optional<ImmutableBooleanExpression> getOptionalFilterCondition();
}
