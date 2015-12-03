package org.semanticweb.ontop.pivotalrepr.transformer;

import org.semanticweb.ontop.model.ImmutableSubstitution;
import org.semanticweb.ontop.model.VariableOrGroundTerm;
import org.semanticweb.ontop.pivotalrepr.QueryNode;
import org.semanticweb.ontop.pivotalrepr.QueryNodeTransformationException;

/**
 * TODO: explain
 */
public class NewSubstitutionException extends QueryNodeTransformationException {
    private final ImmutableSubstitution<VariableOrGroundTerm> substitution;
    private final QueryNode transformedNode;

    public NewSubstitutionException(ImmutableSubstitution<VariableOrGroundTerm> substitution,
                                    QueryNode transformedNode) {
        super("New substitution to propagate (" + substitution + ") and new node (" + transformedNode + ")");
        this.substitution = substitution;
        this.transformedNode = transformedNode;
    }

    public ImmutableSubstitution<VariableOrGroundTerm> getSubstitution() {
        return substitution;
    }

    public QueryNode getTransformedNode() {
        return transformedNode;
    }
}
