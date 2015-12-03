package org.semanticweb.ontop.pivotalrepr.transformer;

import org.semanticweb.ontop.model.ImmutableSubstitution;
import org.semanticweb.ontop.model.VariableOrGroundTerm;
import org.semanticweb.ontop.pivotalrepr.HomogeneousQueryNodeTransformer;
import org.semanticweb.ontop.pivotalrepr.QueryNodeTransformationException;

/**
 * TODO: explain
 */
public interface SubstitutionPropagator<T1 extends QueryNodeTransformationException, T2 extends QueryNodeTransformationException>
        extends HomogeneousQueryNodeTransformer<T1, T2> {
    ImmutableSubstitution<? extends VariableOrGroundTerm> getSubstitution();
}
