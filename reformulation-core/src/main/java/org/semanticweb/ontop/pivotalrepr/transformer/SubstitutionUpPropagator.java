package org.semanticweb.ontop.pivotalrepr.transformer;

import org.semanticweb.ontop.pivotalrepr.QueryNodeTransformationException;


/**
 * TODO: explain
 *
 * Propagates the substitution to the ancestors (one by one).
 */
public interface SubstitutionUpPropagator extends SubstitutionPropagator<StopPropagationException,
        QueryNodeTransformationException> {
}
