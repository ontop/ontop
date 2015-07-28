package org.semanticweb.ontop.pivotalrepr.transformer;

import org.semanticweb.ontop.model.ImmutableSubstitution;
import org.semanticweb.ontop.model.VariableOrGroundTerm;
import org.semanticweb.ontop.pivotalrepr.ConstructionNode;
import org.semanticweb.ontop.pivotalrepr.QueryNodeTransformationException;

/**
 * TODO: find a better name
 *
 * Does not substitute Construction nodes (does not make sense
 * during the substitution process)
 *
 */
public class SubstitutionLiftPropagator extends SubstitutionPropagator {
    public SubstitutionLiftPropagator(ImmutableSubstitution<VariableOrGroundTerm> substitution) {
        super(substitution);
    }

    @Override
    public ConstructionNode transform(ConstructionNode constructionNode)
            throws QueryNodeTransformationException {
        throw new QueryNodeTransformationException("The propagated substitution MUST NOT be applied to construction nodes");
    }
}
