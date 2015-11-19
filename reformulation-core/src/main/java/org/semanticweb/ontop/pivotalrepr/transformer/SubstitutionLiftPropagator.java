package org.semanticweb.ontop.pivotalrepr.transformer;

import org.semanticweb.ontop.model.ImmutableSubstitution;
import org.semanticweb.ontop.model.VariableOrGroundTerm;
import org.semanticweb.ontop.pivotalrepr.ConstructionNode;
import org.semanticweb.ontop.pivotalrepr.LeftJoinNode;
import org.semanticweb.ontop.pivotalrepr.impl.LeftJoinNodeImpl;

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
    public LeftJoinNode transform(LeftJoinNode leftJoinNode) {
        return new LeftJoinNodeImpl(
                transformOptionalBooleanExpression(leftJoinNode.getOptionalFilterCondition()));
    }

    @Override
    public ConstructionNode transform(ConstructionNode constructionNode) {
        throw new IllegalArgumentException("The propagated substitution MUST NOT be applied to construction nodes");
    }
}
