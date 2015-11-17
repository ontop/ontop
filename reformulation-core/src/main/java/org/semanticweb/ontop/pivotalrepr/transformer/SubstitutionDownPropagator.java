package org.semanticweb.ontop.pivotalrepr.transformer;

import org.semanticweb.ontop.model.DataAtom;
import org.semanticweb.ontop.model.ImmutableSubstitution;
import org.semanticweb.ontop.model.VariableOrGroundTerm;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.impl.LeftJoinNodeImpl;
import org.semanticweb.ontop.pivotalrepr.impl.SubQueryUnificationTools;

/**
 * Propagates the substitution down even to construction nodes.
 *
 * However, the latter may throw a NewSubstitutionException.
 *
 */
public class SubstitutionDownPropagator extends SubstitutionPropagator<SubstitutionDownPropagator.UnificationException,
        SubstitutionDownPropagator.NewSubstitutionException> {

    /**
     * TODO: explain
     */
    public static class UnificationException extends QueryNodeTransformationException {
        public UnificationException(String message) {
            super(message);
        }
    }

    /**
     * TODO: explain
     */
    public static class NewSubstitutionException extends QueryNodeTransformationException {
        private final ImmutableSubstitution<VariableOrGroundTerm> substitution;
        private final QueryNode transformedNode;

        protected NewSubstitutionException(ImmutableSubstitution<VariableOrGroundTerm> substitution,
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

    public SubstitutionDownPropagator(ImmutableSubstitution<VariableOrGroundTerm> substitution) {
        super(substitution);
    }

    /**
     * TODO: implement
     */
    @Override
    public ConstructionNode transform(ConstructionNode constructionNode)
            throws NewSubstitutionException, UnificationException {
        DataAtom newProjectionAtom = transformDataAtom(constructionNode.getProjectionAtom());

        try {
            /**
             * TODO: explain why it makes sense (interface)
             */
            SubQueryUnificationTools.ConstructionNodeUnification constructionNodeUnification =
                    SubQueryUnificationTools.unifyConstructionNode(constructionNode, newProjectionAtom);

            ConstructionNode newConstructionNode = constructionNodeUnification.getUnifiedNode();
            ImmutableSubstitution<VariableOrGroundTerm> newSubstitutionToPropagate =
                    constructionNodeUnification.getSubstitutionPropagator().getSubstitution();

            /**
             * If the substitution has changed, throws the new substitution
             * and the new construction node so that the "client" can continue
             * with the new substitution (for the children nodes).
             */
            if (!getSubstitution().equals(newSubstitutionToPropagate)) {
                throw new NewSubstitutionException(newSubstitutionToPropagate,
                        newConstructionNode);
            }

            /**
             * Otherwise, continues with the current substitution
             */
            return newConstructionNode;

        } catch (SubQueryUnificationTools.SubQueryUnificationException e) {
            throw new UnificationException(e.getMessage());
        }
    }

    @Override
    public LeftJoinNode transform(LeftJoinNode leftJoinNode) {
        return new LeftJoinNodeImpl(
                transformOptionalBooleanExpression(leftJoinNode.getOptionalFilterCondition()));
    }
}
