package org.semanticweb.ontop.pivotalrepr.impl;

import org.semanticweb.ontop.model.ImmutableSubstitution;
import org.semanticweb.ontop.model.VariableOrGroundTerm;
import org.semanticweb.ontop.pivotalrepr.*;

/**
 * TODO: describe
 */
public class SubstitutionPropagator implements QueryNodeTransformer {

    /**
     * TODO: explain
     */
    public static class NewSubstitutionException extends QueryNodeTransformationException {
        private final ImmutableSubstitution<VariableOrGroundTerm> substitution;
        private final QueryNode transformedNode;

        public NewSubstitutionException(ImmutableSubstitution<VariableOrGroundTerm> substitution,
                                        QueryNode transformedNode) {
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

    private final ImmutableSubstitution<VariableOrGroundTerm> substitution;

    public SubstitutionPropagator(ImmutableSubstitution<VariableOrGroundTerm> substitution) {
        this.substitution = substitution;
    }

    @Override
    public FilterNode transform(FilterNode filterNode){
        throw new RuntimeException("TODO: implement it");
    }

    @Override
    public TableNode transform(TableNode tableNode) {
        throw new RuntimeException("TODO: implement it");
    }

    @Override
    public LeftJoinNode transform(LeftJoinNode leftJoinNode) {
        throw new RuntimeException("TODO: implement it");
    }

    @Override
    public UnionNode transform(UnionNode unionNode) {
        throw new RuntimeException("TODO: implement it");
    }

    @Override
    public OrdinaryDataNode transform(OrdinaryDataNode ordinaryDataNode) {
        throw new RuntimeException("TODO: implement it");
    }

    @Override
    public InnerJoinNode transform(InnerJoinNode innerJoinNode) {
        throw new RuntimeException("TODO: implement it");
    }

    @Override
    public ConstructionNode transform(ConstructionNode constructionNode) throws NewSubstitutionException {
        throw new RuntimeException("TODO: implement it");
    }


}
